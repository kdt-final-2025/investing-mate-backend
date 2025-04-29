package redlightBack;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import redlightBack.member.memberEntity.Member;
import redlightBack.member.memberEntity.Role;
import redlightBack.member.MemberRepository;
import redlightBack.reporterapplication.domain.RequestStatus;
import redlightBack.reporterapplication.web.dto.ApplicationResponseDto;
import redlightBack.reporterapplication.web.dto.ProcessRequestDto;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ReporterApplicationApiTest extends AcceptanceTest {

    @LocalServerPort
    int port;

    @Autowired
    private DatabaseCleanup databaseCleanup;

    @Autowired
    private MemberRepository memberRepository;


    // 아래의 토큰 3개는 만료기한이 없습니다.
    private static final String ADMIN_TOKEN =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwiZW1haWwiOiJhZG1pbkBleGFtcGxlLmNvbSJ9.LPHUzJyH03G6HsWFkWl6XIfiITDJ7UoL8GJCKuH1Sck";

    private static final String USER_TOKEN =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIyIiwiZW1haWwiOiJ1c2VyQGV4YW1wbGUuY29tIn0.ukq8HAkCNiMuABuZAnmiXX-Z2Lw8AR13aC93GJ6p_yo";

    private static final String USER2_TOKEN =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIzIiwiZW1haWwiOiJ1c2VyQGV4YW1wbGUuY29tIn0.CfzDClpRqjgbwAyQJ43mG4UoZB-ZtpzXSTWqeNjwKkE";

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        databaseCleanup.execute();

        // 세팅: ADMINISTRATOR (id=1) 및 GENERAL (id=2) 멤버 생성
        Member admin = Member.builder()
                .userId("1")
                .email("admin@example.com")
                .fullname("Admin User")
                .role(Role.ADMINISTRATOR)
                .build();
        Member general = Member.builder()
                .userId("2")
                .email("user@example.com")
                .fullname("General User")
                .role(Role.GENERAL)
                .build();
        memberRepository.save(admin);
        memberRepository.save(general);
    }

    @DisplayName("관리자 - 모든 대기 및 반려 상태 조회")
    @Test
    public void listAllPendingAndRejected_asAdmin() {
        // --- (1) 두 번째 일반 사용자 추가 ---
        Member another = Member.builder()
                .userId("3")
                .email("user2@example.com")
                .fullname("General User 2")
                .role(Role.GENERAL)
                .build();
        memberRepository.save(another);

        // --- (2) 첫 번째 유저 신청 → 반려 (REJECTED) ---
        ApplicationResponseDto app1 = RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + USER_TOKEN)  // sub=“2”
                .when().post("reporter-applications")
                .then().extract().as(ApplicationResponseDto.class);

        ProcessRequestDto rejectDto = new ProcessRequestDto(
                List.of(app1.applicationId()), RequestStatus.REJECTED
        );
        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + ADMIN_TOKEN)
                .body(rejectDto)
                .when().patch("reporter-applications/admin");

        // --- (3) 두 번째 유저 신청 (PENDING) ---
        ApplicationResponseDto app2 = RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + USER2_TOKEN)
                .when().post("reporter-applications")
                .then().extract().as(ApplicationResponseDto.class);

        // --- (4) 전체 조회 (PENDING,REJECTED) 검증 ---
        ApplicationResponseDto[] all = RestAssured.given()
                .header("Authorization", "Bearer " + ADMIN_TOKEN)
                .when().get("reporter-applications/admin")
                .then().statusCode(200)
                .extract().as(ApplicationResponseDto[].class);

        List<RequestStatus> statuses = Arrays.stream(all)
                .map(ApplicationResponseDto::status)
                .toList();

        // 두 건이 리턴되고, 각각 한 건씩 PENDING·REJECTED 상태여야 함
        assertThat(all).hasSize(2);
        assertThat(statuses).containsExactlyInAnyOrder(
                RequestStatus.PENDING, RequestStatus.REJECTED
        );
    }

    @DisplayName("관리자 - 대기 상태만 조회 및 반려 상태만 조회")
    @Test
    public void listByIndividualStatus_asAdmin() {
        // 기존처럼 신청 및 반려 준비
        ApplicationResponseDto pending = RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + USER_TOKEN)
                .when()
                .post("reporter-applications")
                .then().extract().as(ApplicationResponseDto.class);

        ProcessRequestDto rejectDto = new ProcessRequestDto(
                List.of(pending.applicationId()), RequestStatus.REJECTED
        );
        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + ADMIN_TOKEN)
                .body(rejectDto)
                .when()
                .patch("reporter-applications/admin");

        // PENDING만 조회
        ApplicationResponseDto[] pendings = RestAssured.given()
                .header("Authorization", "Bearer " + ADMIN_TOKEN)
                .queryParam("statuses", "PENDING")
                .when()
                .get("reporter-applications/admin")
                .then().statusCode(200)
                .extract().as(ApplicationResponseDto[].class);

        assertThat(pendings).hasSize(0); // 이미 반려되어 PENDING 없음

        // REJECTED만 조회
        ApplicationResponseDto[] rejected = RestAssured.given()
                .header("Authorization", "Bearer " + ADMIN_TOKEN)
                .queryParam("statuses", "REJECTED")
                .when()
                .get("reporter-applications/admin")
                .then().statusCode(200)
                .extract().as(ApplicationResponseDto[].class);

        assertThat(rejected).hasSize(1);
        assertThat(rejected[0].status()).isEqualTo(RequestStatus.REJECTED);
    }

    @DisplayName("반려된 신청 재신청 테스트")
    @Test
    public void resubmit_afterRejection_success() {
        ApplicationResponseDto created = RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + USER_TOKEN)
                .when()
                .post("reporter-applications")
                .then().extract().as(ApplicationResponseDto.class);

        ProcessRequestDto reject = new ProcessRequestDto(
                List.of(created.applicationId()), RequestStatus.REJECTED
        );
        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + ADMIN_TOKEN)
                .body(reject)
                .when()
                .patch("reporter-applications/admin");

        ApplicationResponseDto resubmitted = RestAssured.given()
                .header("Authorization", "Bearer " + USER_TOKEN)
                .contentType(ContentType.JSON)
                .when()
                .put("reporter-applications/me")
                .then().statusCode(200)
                .extract().as(ApplicationResponseDto.class);

        assertThat(resubmitted.status()).isEqualTo(RequestStatus.PENDING);
        assertThat(resubmitted.appliedAt()).isAfter(created.appliedAt());
    }

    @DisplayName("관리자 - 기자 승인 시 역할 변경 검증")
    @Test
    public void approveReporter_changesUserRole() {
        ApplicationResponseDto created = RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + USER_TOKEN)
                .when()
                .post("reporter-applications")
                .then().extract().as(ApplicationResponseDto.class);

        ProcessRequestDto approve = new ProcessRequestDto(
                List.of(created.applicationId()), RequestStatus.APPROVED
        );
        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + ADMIN_TOKEN)
                .body(approve)
                .when()
                .patch("reporter-applications/admin");

        Member updated = memberRepository.findByUserId("2").orElseThrow();
        assertThat(updated.getRole()).isEqualTo(Role.REPORTER);
    }

    @DisplayName("관리자 권한 없는 사용자의 접근 차단")
    @Test
    public void listByStatuses_forbiddenForGeneral() {
        RestAssured.given()
                .header("Authorization", "Bearer " + USER_TOKEN)
                .queryParam("statuses", "PENDING")
                .when()
                .get("reporter-applications/admin")
                .then().statusCode(403);
    }
}
