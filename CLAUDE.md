# AXAssignment — 가계부 앱

## 프로젝트 개요
- **목적**: AX 신입 과제 — Kotlin + Jetpack Compose 가계부 Android 앱
- **발표일**: 2026-06-12 (금)
- **패키지**: `com.ax.assignment`
- **GitHub**: https://github.com/IsKJH/AXAssignment
- **Figma**: https://www.figma.com/design/CVoVoOVq55dAEjcz4cHGZK/AX-신규-과제
- **테스트 기기**: Galaxy Z Flip3 (R3CT50PWVHA, 1080×2640)

---

## Figma 읽는 법 (중요)
- **SCR-XX 프레임 = 요구사항 명세서**: 📌목적/🔘UI요소/📐정책 텍스트만 사용. 안의 와이어프레임 UI는 무시
- **실제 UI 기준 = 각 섹션 오른쪽의 디자인 프레임들**
- 노드 매핑: `.claude/skills/implement-screen/references/screen-catalog.md`
- 최신 추출 스펙: `_workspace/final-spec-*.md`

---

## 아키텍처: MVVM + Repository (단방향 데이터 흐름)

```
feature/ (Compose UI + ViewModel + UiState)
domain/  (모델, enum)
data/    (Room Entity·DAO, Repository 인터페이스+구현, Mapper)
```

**핵심 원칙**
- 단방향: `Data → ViewModel(StateFlow) → Composable`
- UI는 Entity 직접 참조 금지 — Domain Model만
- Repository 인터페이스/구현 분리, DI는 BudgetApplication 수동 주입 (Hilt 없음)
- enum 사용 (`TransactionType.INCOME/EXPENSE`), String 비교 금지

---

## 현재 구현 상태 (2026-06-11, 발표 D-1)
**전 화면 구현 + 최신 Figma 전수 대조 + 풀 E2E 9/9 PASS + 픽셀 디테일 검수 완료.**
main 24+커밋. 남은 것: git push(미실행), 시연 데이터 입력, _trash/ 정리.

### 패키지 구조 (실제)
```
com.ax.assignment/
├── core/
│   ├── navigation/  Screen.kt, NavGraph.kt
│   ├── theme/       Color.kt, Theme.kt, Type.kt(Pretendard 번들)
│   ├── component/   AppTopBar(56dp), BottomNavBar, DonutChart(스윕+프레스스크럽),
│   │                TransactionItem, SummaryCard, MonthSelector, EmptyStateView,
│   │                DeleteConfirmDialog(햅틱)
│   └── util/        CurrencyFormatter, DateFormatter, PeriodCalculator, PeriodSwipe
├── domain/model/    Transaction(+seriesId), Category, CategorySummary, TransactionType,
│                    MonthlyExpense, RecurringScope
├── data/
│   ├── local/       AppDatabase(v5), Converters(LocalDateTime↔Long), dao/, entity/
│   ├── mapper/      TransactionMapper, CategoryMapper
│   └── repository/  TransactionRepository(+Impl), CategoryRepository(+Impl),
│                    SettingsRepository(SharedPreferences 시작일)
├── feature/
│   ├── splash/      SplashScreen (Figma 368:1519)
│   ├── home/        HomeScreen, HomeViewModel, HomeUiState
│   ├── transaction/ TransactionAddScreen, TransactionDetailScreen(+편집/정기),
│   │                TransactionViewModel, TransactionDetailViewModel
│   ├── category/    CategoryManageScreen(선택/관리/추가·수정시트), CategoryViewModel
│   ├── settings/    SettingsScreen, StartDaySettingScreen, HelpScreen
│   └── statistics/  StatisticsScreen, SixMonthHistoryScreen, StatisticsViewModel
├── BudgetApplication.kt
└── MainActivity.kt
```

### 핵심 도메인 규칙
- **DB v5** (`budget_db`), fallbackToDestructiveMigration — 버전 올리면 데이터 초기화됨
- `Transaction.date: LocalDateTime` (TypeConverter epochMilli)
- **정기 거래(seriesId)**: 등록 시 앵커+11개월 인스턴스 생성(`RECURRING_MONTHS=12`).
  **매월 자동 연장**: 앱 시작 시 topUpRecurringSeries()가 살아있는 시리즈를 현재 월+11까지 보충
  (최신 인스턴스가 과거인 시리즈 = 중단된 것 — 연장 안 함).
  범위 수정/삭제(이달만/이후/전체) = DAO updateSeriesFrom/All, deleteSeriesFrom/All.
  이후 삭제 시 남은 과거 인스턴스는 clearSeriesFlags로 일반화(정기 표기 해제).
  해제(502:1277) = 이후 삭제+전체 일반화 / 편집에서 신규 체크 = registerRecurring(12개월 생성) — 양방향
- **% 표시**: 소수점 반올림, 최소 1% 보정 없음 (Figma 코멘트 정책)
- **통계 지난달 대비**: 지난달 데이터 없으면(prevAmount=0) "0원" 회색 — 증가 빨강 아님 (488:1046)
- **날짜 입력 제한 없음**(16:172): 드럼롤 오늘 ±1년, 저장 시 속한 주기에 자동 분류.
  진입 시 1회만 조회 주기로 기본값 클램프(rememberSaveable 가드 — 카테고리 왕복 시 재클램프 금지)
- **카테고리 선택 모드**: 선택 항목 재탭 = 해제(결과 0L → 미분류), 뒤로가기로 복귀 (17:178).
  홈 거래 정렬은 date DESC, id DESC(동시각 타이브레이커)
- 커스텀 카테고리 색: CategoryViewModel.CUSTOM_CATEGORY_COLORS 8색에서 미사용 색 자동 배정
- **주기**: SettingsRepository.startDay(1~28) 기반 `periodRange()`. 통계 trend는
  실제 현재 주기 anchoring (선택 주기와 무관 — 6개월 내역 버튼/페이지 일관성)
- 카테고리: 기본 7개 보호, 사용자 최대 7개, 삭제 시 거래 FK SET_NULL(미분류)
- **수입은 카테고리 미사용** (명세 공백의 의도적 결정 — SCR-05 기본 7개가 전부 지출용):
  홈 타이틀 "수입"+초록 도트 #60D551(368:885 실측), 상세 카드 값 "수입" 고정,
  추가/편집 수입 탭에서 카테고리 행 숨김, 저장 시 category=null 강제.
  탭 전환 시 기입력 카테고리는 보존 → 지출 복귀 시 복원(16:172). INCOME 기본 시드 제거됨

### UX 폴리시 (구현됨)
- 애니메이션: 도넛 스윕(800ms), 카운트업·프로그레스(700ms), 바 성장(700ms),
  통계 리스트 스태거(50ms/행), 말풍선 팝, 스플래시 등장+로딩점 3개, 프로그레스 색 전환
- **도넛 프레스&스크럽**: 누르면 중앙에 카테고리명+%, 문지르면 실시간 전환, 떼면 복귀 (햅틱)
- 좌우 스와이프 주기 이동: 홈 + 통계 (`Modifier.periodSwipe`)
- 토스트: 저장/수정/삭제. 햅틱: 삭제류 확정, 바 탭, 도넛 스크럽
- 앱 아이콘: adaptive(#559AFF+로고) + 레거시 webp 5밀도. 앱명 "AX 가계부"
- [사용자 선호] 카테고리 시트 키보드 자동 포커스 없음 (직접 탭 시에만)

### 인셋 규칙 (중요 — 회귀 주의)
- NavGraph Scaffold는 contentWindowInsets(0) — 인셋은 화면이 직접 처리
- 상단: AppTopBar/자체 톱바의 statusBarsPadding. 하단 고정 버튼: navigationBarsPadding 필수
- 전역 레이아웃 변경 시 상단 톱바 + 하단 버튼 + 바텀시트 3종 모두 검증할 것

---

## 색상 토큰 (Color.kt 주요)
| 변수 | 값 | 용도 |
|------|-----|------|
| NavigationOn/CardPrimary/FabFill | #559AFF | 브랜드 파랑 |
| TextDefault | #272727 | 기본 텍스트 |
| TextDescription | #AFAFAF | 보조 텍스트 |
| BrandLight | #EEF5FF | 연파랑 배경 |
| HomeExpenseAmount | #EE3D3D | 지출 빨강 |
| HomeIncomeAmount | #60D551 | 수입 초록 |
| DangerRed | #DA2E0B | 삭제/위험 |
| ConfirmButtonBg | #272727 | 확인 버튼 |

폰트: **Pretendard** (res/font, Regular/Medium/Bold) — Typography 전역 적용

---

## 빌드 & 실행

```bash
# JAVA_HOME은 전역 env(~/.claude/settings.json)에 설정됨 — export 접두사 금지
# (export를 붙이면 권한 allowlist에 매칭되지 않아 매번 승인 프롬프트 발생)
./gradlew installDebug
adb shell am start -n com.ax.assignment/.MainActivity
```

### 기기 좌표 & mobile-mcp 규칙 (1080×2640)
BottomNav 홈(156,2358)/통계(504,2358)/설정(852,2358), FAB(948,2181)
- 탭/스와이프 좌표 = 항상 실픽셀. 스크린샷(축소본) 좌표는 ×(1080/이미지폭) 환산
- 클릭은 `mobile_list_elements_on_screen` bounds 1순위 — 스크린샷 추정은 최후 수단
- 스와이프 direction = 손가락 방향(down=이전 콘텐츠). x/y/distance로 영역 한정 가능
- 키보드가 하단 버튼 가림 → `press_button("BACK")` 후 탭. 한글은 devicekit APK 설치됨(직접 입력 OK)
- 스크린샷 400 에러 시 `save_screenshot`+Read 우회. 상세 팁: verify-screen-on-device 스킬

---

## MCP 도구
```
Figma:  mcp__figma__get_design_context / get_screenshot(enableBase64Response=true)
기기:   mcp__mobile-mcp__* (device="R3CT50PWVHA")
문서:   mcp__context7__*
```

---

## 기술 스택
Kotlin 2.2.10 / AGP 9.2.1 / Compose BOM 2026.02.01 / Navigation 2.9.0 /
Room 2.7.1 / KSP 2.2.10-2.0.2 / Coroutines 1.10.2 / minSdk 24 / targetSdk 37

## 커밋 컨벤션
feat / fix / refactor / chore / docs / style — 한 커밋 한 논리 변경

## 금지 사항
- Entity를 UI 레이어에 노출 금지
- Repository 구현체 직접 인스턴스화 금지 (BudgetApplication 경유)
- String으로 INCOME/EXPENSE 비교 금지
- Composable 안에 비즈니스 로직 금지
- GlobalScope 금지
- DB 버전 변경 시 기기 데이터 초기화됨을 사용자에게 고지

---

## 하네스: 화면 구현
**트리거:** 화면 구현/수정/재구현 요청 시 `implement-screen` 스킬 사용.
**에이전트:** `.claude/agents/` — figma-reader, compose-builder, device-qa
**카탈로그:** `references/screen-catalog.md` (최신 노드 매핑)

| 날짜 | 변경 내용 |
|------|----------|
| 2026-06-08 | 초기 구성 |
| 2026-06-10 | 최신 Figma(0:1) 기준 카탈로그 갱신, 구버전 figma-specs 제거 |
