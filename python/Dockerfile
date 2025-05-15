FROM python:3.10-slim

# 작업 디렉토리
WORKDIR /app

# requirements 설치
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# dotenv 사용 시 필요한 파일 복사
COPY .env .

# 크론 설치 및 로그 디렉토리 생성
RUN apt-get update && apt-get install -y cron procps
RUN mkdir -p /var/log/cron

# 크롤링 파이썬 코드 & 크론 등록 파일 복사
COPY update_sp500.py .
COPY crontab /etc/cron.d/sp500-cron
RUN chmod 0644 /etc/cron.d/sp500-cron && crontab /etc/cron.d/sp500-cron

# 크론 실행
CMD ["cron", "-f"]
