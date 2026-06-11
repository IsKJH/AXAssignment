# Demo-data seeder for the presentation build.
# Operates on a local copy of budget_db pulled from the device (WAL files
# alongside), wipes test transactions / stale categories, inserts six months
# (2026-01 .. 2026-06) of realistic data, then checkpoints so only the main
# db file needs to be pushed back.
import sqlite3
from datetime import datetime, timezone, timedelta

KST = timezone(timedelta(hours=9))
DB = "_workspace/seed/budget_db"

# Category ids in the device DB (kept rows)
FOOD, TRANSPORT, SHOPPING, MEDICAL, LEISURE, HOUSING, EDU = 1, 2, 3, 4, 5, 6, 7
CAFE, SUBSCRIPTION = 18, 19

def ms(y, m, d, hh=12, mm=0):
    return int(datetime(y, m, d, hh, mm, tzinfo=KST).timestamp() * 1000)

con = sqlite3.connect(DB)
cur = con.cursor()

# --- wipe test data / stale categories -------------------------------------
cur.execute("DELETE FROM transactions")
cur.execute("DELETE FROM categories WHERE id NOT IN (1,2,3,4,5,6,7,18,19)")
cur.execute("UPDATE categories SET sortOrder=7 WHERE id=18")   # 카페
cur.execute("UPDATE categories SET sortOrder=8 WHERE id=19")   # 구독
cur.execute("DELETE FROM sqlite_sequence WHERE name='transactions'")

# --- recurring series: anchor + 11 monthly instances (RECURRING_MONTHS=12) --
def insert_series(amount, tx_type, category_id, memo, anchor, day, hh):
    """anchor=(y,m); instances anchor..anchor+11, same day-of-month."""
    y, m = anchor
    cur.execute(
        "INSERT INTO transactions(amount,type,categoryId,memo,date,isRecurring,seriesId) "
        "VALUES (?,?,?,?,?,1,NULL)",
        (amount, tx_type, category_id, memo, ms(y, m, day, hh)))
    anchor_id = cur.lastrowid
    cur.execute("UPDATE transactions SET seriesId=? WHERE id=?", (anchor_id, anchor_id))
    for off in range(1, 12):
        yy, mm0 = y + (m - 1 + off) // 12, (m - 1 + off) % 12 + 1
        cur.execute(
            "INSERT INTO transactions(amount,type,categoryId,memo,date,isRecurring,seriesId) "
            "VALUES (?,?,?,?,?,1,?)",
            (amount, tx_type, category_id, memo, ms(yy, mm0, day, hh), anchor_id))

insert_series(3_500_000, "INCOME", None, "월급", (2026, 1), 25, 9)
insert_series(500_000, "EXPENSE", HOUSING, "월세", (2026, 1), 2, 9)
insert_series(14_900, "EXPENSE", SUBSCRIPTION, "유튜브 프리미엄", (2026, 2), 15, 12)

# --- normal transactions: (month, day, hour, amount, category, memo[, income]) ---
ROWS = [
    (1, 1, 11, 48_200, FOOD, "떡국 재료 장보기"),
    (1, 3, 15, 6_100, CAFE, "스타벅스"),
    (1, 5, 8, 61_600, TRANSPORT, "지하철 정기권"),
    (1, 8, 20, 189_000, SHOPPING, "무신사 패딩"),
    (1, 11, 19, 30_000, LEISURE, "CGV 영화"),
    (1, 14, 12, 9_500, FOOD, "김치찌개 점심"),
    (1, 17, 18, 12_400, MEDICAL, "감기약"),
    (1, 20, 19, 26_900, FOOD, "배달 치킨"),
    (1, 24, 23, 14_300, TRANSPORT, "심야 택시"),
    (1, 28, 16, 45_000, SHOPPING, "친구 생일선물"),
    (1, 30, 14, 5_800, CAFE, "카페모카"),
    (2, 3, 17, 87_400, FOOD, "마트 장보기"),
    (2, 6, 10, 11_200, CAFE, "스타벅스"),
    (2, 9, 13, 120_000, SHOPPING, "설 선물세트"),
    (2, 14, 7, 119_600, TRANSPORT, "KTX 부산 왕복"),
    (2, 14, 15, 145_000, LEISURE, "호텔 숙박"),
    (2, 15, 19, 68_000, FOOD, "횟집 저녁"),
    (2, 18, 18, 33_500, SHOPPING, "올리브영"),
    (2, 20, 12, 7_000, FOOD, "구내식당 점심"),
    (2, 22, 21, 99_000, EDU, "인강 결제"),
    (2, 25, 9, 30_000, TRANSPORT, "버스카드 충전"),
    (3, 2, 14, 54_000, EDU, "전공 서적"),
    (3, 4, 18, 92_300, FOOD, "마트 장보기"),
    (3, 7, 11, 6_100, CAFE, "스타벅스"),
    (3, 9, 8, 61_600, TRANSPORT, "지하철 정기권"),
    (3, 13, 19, 89_000, SHOPPING, "봄 자켓"),
    (3, 16, 10, 80_000, MEDICAL, "치과 스케일링"),
    (3, 19, 19, 52_000, FOOD, "삼겹살 회식"),
    (3, 21, 20, 24_000, LEISURE, "볼링"),
    (3, 25, 19, 18_500, FOOD, "배달 떡볶이"),
    (3, 29, 15, 28_000, CAFE, "투썸 케이크"),
    (4, 4, 10, 15_000, FOOD, "벚꽃 나들이 김밥"),
    (4, 4, 9, 88_000, TRANSPORT, "렌터카"),
    (4, 7, 18, 95_600, FOOD, "마트 장보기"),
    (4, 10, 13, 79_000, SHOPPING, "에어팟 수리"),
    (4, 12, 11, 6_700, CAFE, "스타벅스"),
    (4, 15, 18, 45_000, LEISURE, "야구 직관"),
    (4, 18, 10, 21_500, MEDICAL, "내과 진료"),
    (4, 21, 19, 38_000, FOOD, "초밥 저녁"),
    (4, 24, 22, 9_800, TRANSPORT, "택시"),
    (4, 26, 15, 129_000, SHOPPING, "운동화"),
    (4, 29, 17, 150_000, SHOPPING, "어버이날 선물"),
    (5, 2, 17, 101_200, FOOD, "마트 장보기"),
    (5, 4, 14, 50_000, SHOPPING, "조카 어린이날 선물"),
    (5, 8, 18, 124_000, FOOD, "가족 외식"),
    (5, 10, 11, 12_400, CAFE, "스타벅스"),
    (5, 12, 8, 61_600, TRANSPORT, "지하철 정기권"),
    (5, 16, 19, 32_000, LEISURE, "영화 데이트"),
    (5, 17, 13, 80_000, None, "중고 거래 판매", True),
    (5, 19, 16, 8_900, MEDICAL, "약국"),
    (5, 22, 19, 29_900, FOOD, "배달 피자"),
    (5, 24, 9, 48_000, EDU, "토익 응시료"),
    (5, 27, 18, 12_500, SHOPPING, "다이소"),
    (5, 30, 21, 18_000, FOOD, "치맥"),
    (6, 1, 17, 76_800, FOOD, "마트 장보기"),
    (6, 2, 10, 6_100, CAFE, "스타벅스"),
    (6, 3, 8, 61_600, TRANSPORT, "지하철 정기권"),
    (6, 5, 12, 10_500, FOOD, "비빔밥 점심"),
    (6, 7, 12, 100_000, None, "친구 결혼 축의금"),
    (6, 8, 20, 39_900, SHOPPING, "선풍기"),
    (6, 9, 10, 15_200, MEDICAL, "내과 진료"),
    (6, 10, 19, 41_000, FOOD, "배달 족발"),
    (6, 11, 9, 4_800, CAFE, "아메리카노"),
    (6, 11, 13, 15_000, LEISURE, "영화 예매"),
]
for row in ROWS:
    m, d, hh, amount, cat, memo = row[:6]
    income = len(row) > 6 and row[6]
    cur.execute(
        "INSERT INTO transactions(amount,type,categoryId,memo,date,isRecurring,seriesId) "
        "VALUES (?,?,?,?,?,0,NULL)",
        (amount, "INCOME" if income else "EXPENSE", cat, memo, ms(2026, m, d, hh)))

con.commit()

# --- report ------------------------------------------------------------------
for y, m in [(2026, i) for i in range(1, 7)]:
    nxt = (y + m // 12, m % 12 + 1)
    s, e = ms(y, m, 1, 0), ms(*nxt, 1, 0)
    exp, inc = [cur.execute(
        "SELECT COALESCE(SUM(amount),0) FROM transactions WHERE type=? AND date>=? AND date<?",
        (t, s, e)).fetchone()[0] for t in ("EXPENSE", "INCOME")]
    print(f"2026-{m:02d}: expense {exp:>9,}  income {inc:>9,}")
print("total tx:", cur.execute("SELECT COUNT(*) FROM transactions").fetchone()[0])
print("series:", cur.execute(
    "SELECT seriesId, COUNT(*), MIN(date), MAX(date) FROM transactions "
    "WHERE seriesId IS NOT NULL GROUP BY seriesId").fetchall())

cur.execute("PRAGMA wal_checkpoint(TRUNCATE)")
con.close()
print("done")
