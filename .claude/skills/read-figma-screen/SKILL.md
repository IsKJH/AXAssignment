---
name: read-figma-screen
description: AXAssignment 가계부 앱의 Figma 화면 스펙을 읽고 구조화된 문서로 추출한다. figma-reader 에이전트가 사용한다.
---

# Figma 화면 스펙 추출

## 전제 조건
- Figma 파일 키: `CVoVoOVq55dAEjcz4cHGZK`
- 화면별 Node ID: `references/screen-catalog.md` 참조 (오케스트레이터 스킬에 위치)
- MCP 도구: `mcp__figma__get_design_context`, `mcp__figma__get_screenshot`

## 실행 절차

### Step 1. 디자인 컨텍스트 조회
```
mcp__figma__get_design_context(
  fileKey="CVoVoOVq55dAEjcz4cHGZK",
  nodeId="{해당 화면 nodeId}"
)
```

### Step 2. 스크린샷 저장 (선택)
```
mcp__figma__get_screenshot(
  fileKey="CVoVoOVq55dAEjcz4cHGZK",
  nodeId="{해당 화면 nodeId}"
)
```
이미지를 `_workspace/figma-{screenId}-screenshot.png`에 저장한다.

### Step 3. 색상 매핑
조회 결과에서 16진수 색상을 `core/theme/Color.kt` 변수로 매핑한다:

| 용도 | 색상 값 | Color.kt 변수 |
|------|---------|---------------|
| 주요 버튼/강조 | #3949AB | `Primary` |
| 수입 금액 | #2AC769 | `IncomeGreen` |
| 지출 금액 | #F45A5A | `ExpenseRed` |
| 배경 | #F5F6FA | `Background` |
| 카드 배경 | #FFFFFF | `Surface` |
| 보조 배경 | #F0F1F8 | `SurfaceVariant` |
| 본문 텍스트 | #1A1C2E | `OnSurface` |
| 보조 텍스트 | #6B7280 | `OnSurfaceVariant` |
| 구분선 | #EEEFF5 | `DividerColor` |

## 출력 파일 구조
`_workspace/figma-{screenId}-spec.md`에 다음을 포함한다:

```markdown
# {화면명} 스펙 (SCR-XX)

## 1. 레이아웃 구조
- Scaffold 여부: (있음/없음)
- TopAppBar: (있음/없음, 제목, 버튼 목록)
- 주요 컨텐츠 레이아웃: (LazyColumn / Column / Box 등)
- FAB: (있음/없음, 아이콘, 위치)
- BottomNavigationBar: (있음/없음, 탭 목록)
- 컴포넌트 계층:
  Scaffold
  ├── TopAppBar(title=..., actions=[...])
  ├── FloatingActionButton(...)
  └── Content
      └── LazyColumn
          ├── SummaryCard(...)
          └── TransactionItem(...)×n

## 2. 색상 사용
- 배경: Background
- 카드: Surface
- 강조: Primary
(Figma에서 발견된 항목 모두 포함)

## 3. 타이포그래피
- 제목: MaterialTheme.typography.titleLarge / headlineMedium 등
- 금액: MaterialTheme.typography.headlineMedium + FontWeight.Bold
- 본문: MaterialTheme.typography.bodyMedium
(실제 Figma 값 기준으로 Material3 타이포에 매핑)

## 4. 컴포넌트 목록
각 컴포넌트에 재사용 여부를 표시한다.
- TransactionItem (재사용 가능 → core/component/)
- SummaryCard (이 화면 전용)
- MonthSelector (재사용 가능 → core/component/)

## 5. 인터랙션 & 이벤트
- FAB 클릭 → 거래 추가 화면 이동
- 항목 클릭 → 거래 상세 화면 이동
- 이전/다음 월 버튼 → HomeEvent.PrevMonth / HomeEvent.NextMonth

## 6. 상태 변형
- 로딩 상태: CircularProgressIndicator 표시
- 빈 상태: 일러스트 + 안내 텍스트
- 데이터 있음: 요약 카드 + 거래 목록

## 7. 여백 & 크기
(Figma에서 읽은 dp 값 기록)
- 화면 수평 패딩: 16.dp
- 항목 간격: 8.dp
- 카드 모서리: 12.dp
```
