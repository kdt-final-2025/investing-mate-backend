# 📈 자동 주식 추천 크롤링 모듈

이 폴더는 S&P 500 종목 정보를 매일 자동으로 수집하여 PostgreSQL에 저장하는 Python 기반 크롤링 모듈입니다.

## 🛠 사용 방법

1. `.env` 파일 생성 (DB 설정)
2. Docker 이미지 빌드 및 실행

## 자기 DB연결 환경에 맞게 .env 수정(수정 후 push ❌ 설정은 본인 로컬에서만 사용)
DB_HOST=postgres-db
DB_PORT=5432
DB_NAME=redlight
DB_USER=user
DB_PASSWORD=password

## docker-compose.update.yml 설정 로컬이랑 맞추기(수정 후 push ❌ 설정은 본인 로컬에서만 사용)
networks:
- java_default
docker-compose.yml 있는 경로에서 터미널 열기
```bash
docker network ls
```
에서 DRIVER가 bridge인 것

environment:
      DB_HOST: postgres-db <- .env파일의 DB_HOST 이름과 같게 설정

## 설정 후 build
```bash
docker-compose -f docker-compose.updater.yml build
docker-compose -f docker-compose.updater.yml up -d
```

## 자동 크롤링 안 될 경우 수동 명령어
docker-compose.updater.yml 이 있는 경로에서 터미널 명령어 입력
```bash
docker exec -it sp500-updater python /app/update_sp500.py
```