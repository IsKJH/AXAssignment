# 화면 카탈로그 (최신 Figma 기준, 2026-06-10)

Figma 파일 키: `CVoVoOVq55dAEjcz4cHGZK`

## 원칙
- **SCR-XX 프레임 = 요구사항 명세서** (📌목적/🔘UI요소/📐정책 텍스트만 사용, 안의 와이어프레임 UI는 무시)
- **실제 UI 기준 = 각 섹션 오른쪽의 디자인 프레임들** (아래 "실제 UI 노드" 열)

| ID | 화면명 | 명세 Node | 실제 UI 노드 | feature 패키지 | 상태 |
|----|--------|-----------|-------------|---------------|------|
| SCR-01 | 홈 (빈 상태) | 27:220 | 368:676 | feature/home/ | 완료 |
| SCR-02 | 홈 (거래 있음/초과) | 27:112 | 368:839, 446:731(초과) | feature/home/ | 완료 |
| SCR-03 | 거래 추가 | 16:172 | 368:940, 446:1226(금액on), 368:1414(드럼롤) | feature/transaction/ | 완료 |
| SCR-04 | 거래 상세/편집 | 7:149 | 368:1001(읽기), 446:1302(정기), 368:1211(편집), 446:814/897(범위시트), 368:1273(삭제), 502:1277(정기해제) | feature/transaction/ | 완료 |
| SCR-05 | 카테고리 | 17:178 | 368:1031(리스트), 368:1836(삭제 다이얼로그) | feature/category/ | 완료 |
| SCR-06 | 카테고리 추가/수정 | 69:251 | 368:1082(추가시트), 368:1848(수정시트) | feature/category/ | 완료 |
| SCR-07 | 설정/시작일 | 9:201 | 368:531(설정), 368:916(시작일) | feature/settings/ | 완료 |
| SCR-08 | 통계 | 488:1046(변경명세) | 488:548(리스트), 488:898(빈), 488:810/854(6개월 내역) | feature/statistics/ | 완료 |
| - | 스플래시 | - | 368:1519 | feature/splash/ | 완료 |

## 최신 스펙 문서
`_workspace/final-spec-{home,transaction,category,settings}.md` — 요구사항 전문 + 실제 UI 스펙
최종 QA: `_workspace/final-qa-diff-report.md`, `_workspace/full-app-qa-report.md`

## 공유 컴포넌트 (core/component/)
TransactionItem, CategoryChip, SummaryCard, MonthSelector, EmptyStateView,
DonutChart(스윕 애니메이션), AppTopBar, BottomNavBar, DeleteConfirmDialog(햅틱 포함)
