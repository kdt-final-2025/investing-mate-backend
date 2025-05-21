#!/usr/bin/env python
# coding: utf-8

# In[1]:


import os
import pandas as pd
import yfinance as yf
import psycopg2
import pytz
from datetime import datetime, timedelta
from dotenv import load_dotenv  # 추가 (선택)

load_dotenv()
print(os.getenv("DB_NAME"))

# ⬇️ .env 로드 (.env 사용 시 필수)
load_dotenv()
print("💡 DB_HOST =", os.getenv("DB_HOST"))

def float_or_none(x):
    return float(x) if x is not None else None

# DB 연결 정보
DB_CONFIG = {
    'host': os.getenv("DB_HOST", "localhost"),
    'dbname': os.getenv("DB_NAME", "redlight"),
    'user': os.getenv("DB_USER", "user"),
    'password': os.getenv("DB_PASSWORD", "password"),
    'port': int(os.getenv("DB_PORT", 5432))
}

# DB 연결 함수
def get_db_connection():
    return psycopg2.connect(**DB_CONFIG)

# S&P 500 종목 리스트 가져오기
def get_sp500_list():
    url = "https://en.wikipedia.org/wiki/List_of_S%26P_500_companies"
    tables = pd.read_html(url)
    df = tables[0][["Symbol", "Security"]]
    df.columns = ["ticker", "name"]
    return df

# 종목별 주가 데이터 수집
def get_stock_data(ticker):
    try:
        stock = yf.Ticker(ticker)
        info = stock.info
        history = stock.history(period="5y")

        eastern = pytz.timezone("America/New_York")
        now = datetime.now(eastern)

        def get_high(days):
            return history[history.index > now - timedelta(days=days)]["High"].max()

        raw_yield = info.get("dividendYield")

        if raw_yield is None:
            dividend_yield = 0.0
        elif raw_yield < 1.0:
            dividend_yield = raw_yield * 100
        else:
            dividend_yield = raw_yield  # 이미 퍼센트인 경우

        return {
            "current_price": info.get("currentPrice"),
            "high_price_6m": get_high(180),
            "high_price_1y": get_high(365),
            "high_price_2y": get_high(730),
            "high_price_5y": history["High"].max(),
            "dividend_yield": dividend_yield
        }
    except Exception as e:
        print(f"❌ {ticker} 데이터 수집 실패:", e)
        return None
    
def generate_current_to_high_ratio(current_price, high_price_1y):
    if current_price is None or high_price_1y in (None, 0):
        return None
    return current_price / high_price_1y

def generate_risk_level(dividend_yield, current_to_high_ratio):
    if dividend_yield is None or current_to_high_ratio is None:
        return None
    if dividend_yield > 4.0 and current_to_high_ratio >= 0.9:
        return "LOW"
    elif dividend_yield > 2.0 and current_to_high_ratio >= 0.85:
        return "MEDIUM"
    else:
        return "HIGH"


# DB에 데이터 저장
def save_to_db(conn, ticker, name, data):
    current_to_high_ratio = generate_current_to_high_ratio(data["current_price"], data["high_price_1y"])
    risk_level = generate_risk_level(data["dividend_yield"], current_to_high_ratio)
    logging.debug("🔍 저장할 current_to_high_ratio:", current_to_high_ratio)
    logging.debug("🔍 저장할 risk_level:", risk_level)
    
    with conn.cursor() as cursor:
        cursor.execute("""
            INSERT INTO stock_recommendation (
                ticker, name, current_price, high_price_6m,
                high_price_1y, high_price_2y, high_price_5y, dividend_yield,
                current_to_high_ratio, risk_level
            ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
            ON CONFLICT (ticker) DO UPDATE SET
                current_price = EXCLUDED.current_price,
                high_price_6m = EXCLUDED.high_price_6m,
                high_price_1y = EXCLUDED.high_price_1y,
                high_price_2y = EXCLUDED.high_price_2y,
                high_price_5y = EXCLUDED.high_price_5y,
                dividend_yield = EXCLUDED.dividend_yield,
                current_to_high_ratio = EXCLUDED.current_to_high_ratio,
                risk_level = EXCLUDED.risk_level,
                updated_at = CURRENT_TIMESTAMP
        """, (
            ticker, name,
            data["current_price"], data["high_price_6m"],
            data["high_price_1y"], data["high_price_2y"],
            data["high_price_5y"], data["dividend_yield"],
            current_to_high_ratio, risk_level
        ))
    conn.commit()

# 전체 실행
def main():
    sp500_df = get_sp500_list()
    conn = get_db_connection()

    for _, row in sp500_df.iterrows():
        ticker, name = row["ticker"], row["name"]
        print(f"📊 {ticker} 수집 중...")
        data = get_stock_data(ticker)
        if data:
            current_to_high_ratio = float_or_none(generate_current_to_high_ratio(data["current_price"], data["high_price_1y"]))
            risk_level = generate_risk_level(data["dividend_yield"], current_to_high_ratio)
            clean_data = {
                "current_price": float_or_none(data["current_price"]),
                "high_price_6m": float_or_none(data["high_price_6m"]),
                "high_price_1y": float_or_none(data["high_price_1y"]),
                "high_price_2y": float_or_none(data["high_price_2y"]),
                "high_price_5y": float_or_none(data["high_price_5y"]),
                "dividend_yield": float_or_none(data["dividend_yield"]),
                "current_to_high_ratio": current_to_high_ratio,
                "risk_level": risk_level
            }
            save_to_db(conn, ticker, name, clean_data)
            print(f"✅ {ticker} 저장 완료")
        else:
            print(f"⚠️ {ticker} 저장 실패")

    conn.close()
    print("🎉 전체 완료!")

if __name__ == "__main__":
    main()
