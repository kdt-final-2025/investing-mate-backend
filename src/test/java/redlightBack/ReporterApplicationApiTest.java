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

import java.util.List;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class ReporterApplicationApiTest extends AcceptanceTest {

    @LocalServerPort
    int port;

    @Autowired
    private DatabaseCleanup databaseCleanup;

    @Autowired
    private MemberRepository memberRepository;

    // 만료 없음
    private static final String ADMIN_TOKEN =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9."
                    + "eyJzdWIiOiIxIiwiZW1haWwiOiJhZG1pbkBleGFtcGxlLmNvbSJ9."
                    + "LPHUzJyH03G6HsWFkWl6XIfiITDJ7UoL8GJCKuH1Sck";
    private static final String USER_TOKEN =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9."
                    + "eyJzdWIiOiIyIiwiZW1haWwiOiJ1c2VyQGV4YW1wbGUuY29tIn0."
                    + "ukq8HAkCNiMuABuZAnmiXX-Z2Lw8AR13aC93GJ6p_yo";
    private static final String USER2_TOKEN =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9."
                    + "eyJzdWIiOiIzIiwiZW1haWwiOiJ1c2VyMkBleGFtcGxlLmNvbSJ9."
                    + "ApaAmIFDU4QOEf6WcXlQKhMxcJo8BMCK399QDGlu96o";

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        databaseCleanup.execute();

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
        Member general2 = Member.builder()
                .userId("3")
                .email("user2@example.com")
                .fullname("General User 2")
                .role(Role.GENERAL)
                .build();
        memberRepository.save(admin);
        memberRepository.save(general);
        memberRepository.save(general2);
    }

    @DisplayName("Reporter 신청 - 성공 테스트")
    @Test
    public void applyReporter_success() {
        ApplicationResponseDto response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + USER_TOKEN)
                .when()
                .post("reporter-applications")
                .then().log().all()
                .statusCode(200)
                .extract().as(ApplicationResponseDto.class);

        assertThat(response.userId()).isEqualTo("2");
        assertThat(response.status()).isEqualTo(RequestStatus.PENDING);
        assertThat(response.applicationId()).isNotNull();
        assertThat(response.appliedAt()).isNotNull();
    }

    @DisplayName("Reporter 신청 - 중복 신청 시 잘못된 요청(400)")
    @Test
    public void applyReporter_conflict() {
        // 첫 신청
        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + USER_TOKEN)
                .when().post("reporter-applications");
        // 중복 신청
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + USER_TOKEN)
                .when().post("reporter-applications")
                .then().statusCode(400);
    }

    @DisplayName("Reporter 신청 재신청 - 반려 후 새 신청 허용(200)")
    @Test
    public void reapplyAfterRejection_success() {
        // 첫 신청
        ApplicationResponseDto first = RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + USER_TOKEN)
                .when().post("reporter-applications")
                .then().extract().as(ApplicationResponseDto.class);

        // 반려 처리
        ProcessRequestDto rejectDto = new ProcessRequestDto(
                List.of(first.applicationId()), RequestStatus.REJECTED
        );
        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + ADMIN_TOKEN)
                .body(rejectDto)
                .when().patch("/admin/console/reporter-applications");

        // 재신청
        ApplicationResponseDto second = RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + USER_TOKEN)
                .when().post("reporter-applications")
                .then().statusCode(200)
                .extract().as(ApplicationResponseDto.class);

        assertThat(second.status()).isEqualTo(RequestStatus.PENDING);
        assertThat(second.applicationId()).isNotEqualTo(first.applicationId());
        assertThat(second.appliedAt()).isAfter(first.appliedAt());
    }

    @DisplayName("최신 신청 조회 - 최신 건 반환(200)")
    @Test
    public void getMyApplication_returnsLatest() {
        // 첫 신청
        ApplicationResponseDto first = RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + USER2_TOKEN)
                .when().post("reporter-applications")
                .then().extract().as(ApplicationResponseDto.class);

        // 반려 처리
        ProcessRequestDto rejectDto = new ProcessRequestDto(
                List.of(first.applicationId()), RequestStatus.REJECTED
        );
        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + ADMIN_TOKEN)
                .body(rejectDto)
                .when().patch("/admin/console/reporter-applications");

        // 두 번째 신청
        ApplicationResponseDto second = RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + USER2_TOKEN)
                .when().post("reporter-applications")
                .then().extract().as(ApplicationResponseDto.class);

        // 최신 신청 조회
        ApplicationResponseDto latest = RestAssured.given().log().all()
                .header("Authorization", "Bearer " + USER2_TOKEN)
                .when().get("reporter-applications/me")
                .then().statusCode(200)
                .extract().as(ApplicationResponseDto.class);

        assertThat(latest.applicationId()).isEqualTo(second.applicationId());
    }

    @DisplayName("관리자 - 모든 대기 및 반려 상태 조회(200)")
    @Test
    public void listAllPendingAndRejected_asAdmin() {
        // user2 신청 (PENDING)
        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + USER2_TOKEN)
                .when().post("reporter-applications");

        // user 신청 후 반려( REJECTED )
        ApplicationResponseDto app1 = RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + USER_TOKEN)
                .when().post("reporter-applications")
                .then().extract().as(ApplicationResponseDto.class);
        ProcessRequestDto rejectDto2 = new ProcessRequestDto(
                List.of(app1.applicationId()), RequestStatus.REJECTED
        );
        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + ADMIN_TOKEN)
                .body(rejectDto2)
                .when().patch("/admin/console/reporter-applications");

        // 전체 조회
        ApplicationResponseDto[] all = RestAssured.given()
                .header("Authorization", "Bearer " + ADMIN_TOKEN)
                .when().get("/admin/console/reporter-applications")
                .then().statusCode(200)
                .extract().as(ApplicationResponseDto[].class);

        assertThat(all).hasSize(2);
        List<RequestStatus> stats = Arrays.stream(all)
                .map(ApplicationResponseDto::status).toList();
        assertThat(stats).containsExactlyInAnyOrder(
                RequestStatus.PENDING, RequestStatus.REJECTED
        );
    }

    @DisplayName("관리자 - 상태별 개별 조회(200)")
    @Test
    public void listByIndividualStatus_asAdmin() {
        // 신청 → 반려
        ApplicationResponseDto app = RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + USER_TOKEN)
                .when().post("reporter-applications")
                .then().extract().as(ApplicationResponseDto.class);
        ProcessRequestDto rejectDto = new ProcessRequestDto(
                List.of(app.applicationId()), RequestStatus.REJECTED
        );
        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + ADMIN_TOKEN)
                .body(rejectDto)
                .when().patch("/admin/console/reporter-applications");

        // PENDING only
        RestAssured.given()
                .header("Authorization", "Bearer " + ADMIN_TOKEN)
                .queryParam("statuses", "PENDING")
                .when().get("/admin/console/reporter-applications")
                .then().statusCode(200)
                .body("size()", org.hamcrest.Matchers.is(0));

        // REJECTED only
        RestAssured.given()
                .header("Authorization", "Bearer " + ADMIN_TOKEN)
                .queryParam("statuses", "REJECTED")
                .when().get("/admin/console/reporter-applications")
                .then().statusCode(200)
                .body("[0].status", org.hamcrest.Matchers.equalTo("REJECTED"));
    }

    @DisplayName("관리자 - 승인 처리 및 사용자 역할 변경 검증(200)")
    @Test
    public void approveReporter_changesUserRole() {
        ApplicationResponseDto app = RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + USER_TOKEN)
                .when().post("reporter-applications")
                .then().extract().as(ApplicationResponseDto.class);
        ProcessRequestDto approveDto = new ProcessRequestDto(
                List.of(app.applicationId()), RequestStatus.APPROVED
        );
        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + ADMIN_TOKEN)
                .body(approveDto)
                .when().patch("/admin/console/reporter-applications")
                .then().statusCode(200);

        Member updated = memberRepository.findByUserId("2").orElseThrow();
        assertThat(updated.getRole()).isEqualTo(Role.REPORTER);
    }

    @DisplayName("관리자 권한 없는 사용자의 접근 차단(403)")
    @Test
    public void adminEndpoints_forbiddenForGeneral() {
        // GET
        RestAssured.given()
                .header("Authorization", "Bearer " + USER_TOKEN)
                .when().get("/admin/console/reporter-applications")
                .then().statusCode(403);
        // PATCH
        ProcessRequestDto dto = new ProcessRequestDto(List.of(1L), RequestStatus.APPROVED);
        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + USER_TOKEN)
                .body(dto)
                .when().patch("/admin/console/reporter-applications")
                .then().statusCode(403);
    }
}
