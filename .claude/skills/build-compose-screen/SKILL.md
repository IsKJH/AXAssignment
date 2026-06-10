---
name: build-compose-screen
description: Figma 스펙 문서를 읽고 Kotlin Jetpack Compose 화면을 구현한다. compose-builder 에이전트가 사용한다.
---

# Compose 화면 구현

## 전제 조건
- `_workspace/figma-{screenId}-spec.md`가 존재해야 한다.
- CLAUDE.md의 코딩 패턴을 반드시 준수한다.
- 프로젝트 기본 경로: `app/src/main/java/com/ax/assignment/`
- 빌드 명령:
  ```bash
  export JAVA_HOME="/c/Users/mojis/AppData/Local/Programs/Android Studio/jbr"
  export PATH="$JAVA_HOME/bin:$PATH"
  ./gradlew compileDebugKotlin
  ```

## 실행 절차

### Step 1. 스펙 파일 읽기
`_workspace/figma-{screenId}-spec.md`를 읽어 구현 계획을 수립한다.

### Step 2. 기존 컴포넌트 확인
```
Glob("core/component/**/*.kt")
```
이미 있는 컴포넌트는 새로 만들지 않고 임포트해서 사용한다.

### Step 3. 공유 컴포넌트 구현 (재사용 가능한 것 먼저)
스펙의 "컴포넌트 목록"에서 재사용 가능으로 표시된 항목을 `core/component/`에 먼저 구현한다.
각 컴포넌트 파일에는 `@Preview`를 포함한다.

### Step 4. 화면 구현

**파일 구조 (feature/{feature}/):**
- `{Feature}UiState.kt` — 이미 존재, 내용 보완 필요 시 수정
- `{Feature}ViewModel.kt` — 이미 존재, 내용 보완 필요 시 수정
- `{Feature}Screen.kt` — 메인 구현 파일

**Screen.kt 필수 구조:**
```kotlin
// Screen composable: ViewModel 연결 (NavController 파라미터)
@Composable
fun HomeScreen(navController: NavController) {
    val app = LocalContext.current.applicationContext as BudgetApplication
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModel.factory(app))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateToAdd = { navController.navigate(Screen.TransactionAdd.route) },
    )
}

// Content composable: 순수 UI, @Preview 가능
@Composable
fun HomeContent(
    uiState: HomeUiState,
    onEvent: (HomeEvent) -> Unit,
    onNavigateToAdd: () -> Unit,
) { /* UI 구현 */ }

@Preview(showBackground = true)
@Composable
private fun HomeContentPreview() {
    AXAssignmentTheme {
        HomeContent(
            uiState = HomeUiState(isLoading = false),
            onEvent = {},
            onNavigateToAdd = {},
        )
    }
}
```

### Step 5. UiState 상태 변형 처리
모든 상태 변형을 Content 내부에서 분기 처리한다:
```kotlin
when {
    uiState.isLoading -> LoadingContent()
    uiState.error != null -> ErrorContent(uiState.error)
    uiState.isEmpty -> EmptyContent()
    else -> DataContent(uiState)
}
```

### Step 6. 테마 값 사용 규칙
- 색상: `MaterialTheme.colorScheme.XXX` 또는 `core/theme/Color.kt` 변수 직접 사용
- 타이포: `MaterialTheme.typography.XXX`
- 여백: `Modifier.padding(16.dp)` 등 dp 직접 사용 (string resource 불필요)
- 절대 하드코딩된 색상 값(#3949AB) 사용 금지

### Step 7. 컴파일 검증
```bash
export JAVA_HOME="/c/Users/mojis/AppData/Local/Programs/Android Studio/jbr"
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew compileDebugKotlin 2>&1 | grep -E '(error|warning|BUILD)'
```
오류 없으면 `_workspace/{screenId}-build-result.txt`에 "BUILD SUCCESSFUL" 기록.
오류가 있으면 수정 후 재확인한다.

## 공통 패턴

### 금액 표시
```kotlin
import com.ax.assignment.core.util.toCurrencyString
// 사용: uiState.totalExpense.toCurrencyString()  → "50,000원"
```

### 날짜 표시
```kotlin
import com.ax.assignment.core.util.toFullDateString
// 사용: transaction.date.toFullDateString()  → "2026년 6월 8일"
```

### 거래 타입 색상
```kotlin
val amountColor = if (tx.type == TransactionType.INCOME) IncomeGreen else ExpenseRed
```

## 참고
- 자세한 아키텍처 패턴: `references/compose-patterns.md`
