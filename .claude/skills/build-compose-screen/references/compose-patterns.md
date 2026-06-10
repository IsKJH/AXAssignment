# Compose 구현 참고 패턴

## 주요 임포트
```kotlin
import androidx.compose.runtime.collectAsStateWithLifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ax.assignment.BudgetApplication
import com.ax.assignment.core.theme.AXAssignmentTheme
import com.ax.assignment.core.theme.IncomeGreen
import com.ax.assignment.core.theme.ExpenseRed
import com.ax.assignment.core.util.toCurrencyString
import com.ax.assignment.core.util.toFullDateString
```

## LazyColumn with DateHeader
거래 목록을 날짜별로 그룹핑할 때:
```kotlin
val grouped = uiState.transactions.groupBy { it.date }
LazyColumn {
    grouped.forEach { (date, txList) ->
        stickyHeader { DateHeader(date = date) }
        items(txList, key = { it.id }) { tx ->
            TransactionItem(transaction = tx, onClick = { onTransactionClick(tx.id) })
        }
    }
}
```

## Scaffold + TopAppBar
```kotlin
Scaffold(
    topBar = {
        TopAppBar(
            title = { Text(text = "2026년 6월", style = MaterialTheme.typography.titleMedium) },
            navigationIcon = {
                IconButton(onClick = { onEvent(HomeEvent.PrevMonth) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "이전 달")
                }
            },
            actions = {
                IconButton(onClick = { onEvent(HomeEvent.NextMonth) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "다음 달")
                }
            },
        )
    },
    floatingActionButton = {
        FloatingActionButton(onClick = onNavigateToAdd) {
            Icon(Icons.Default.Add, contentDescription = "거래 추가")
        }
    },
) { paddingValues ->
    LazyColumn(contentPadding = paddingValues) { ... }
}
```

## DatePicker (거래 추가 화면)
```kotlin
val datePickerState = rememberDatePickerState(
    initialSelectedDateMillis = uiState.date
        .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
)
DatePicker(state = datePickerState)
```

## TransactionItem 공유 컴포넌트 예시
```kotlin
@Composable
fun TransactionItem(
    transaction: Transaction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 카테고리 이모지 + 이름
        transaction.category?.let { cat ->
            Text(text = cat.emoji, fontSize = 24.sp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(text = cat.name, style = MaterialTheme.typography.bodyMedium)
                if (transaction.memo.isNotBlank()) {
                    Text(text = transaction.memo, style = MaterialTheme.typography.bodySmall,
                         color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        // 금액
        val color = if (transaction.type == TransactionType.INCOME) IncomeGreen else ExpenseRed
        val prefix = if (transaction.type == TransactionType.INCOME) "+" else "-"
        Text(
            text = "$prefix${transaction.amount.toCurrencyString()}",
            color = color,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
```

## SummaryCard 예시 (홈 화면 상단 요약)
```kotlin
@Composable
fun SummaryCard(
    totalIncome: Long,
    totalExpense: Long,
    balance: Long,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Primary),
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("잔액", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
            Text(balance.toCurrencyString(), color = Color.White,
                 style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth()) {
                // 수입
                Column(Modifier.weight(1f)) {
                    Text("수입", color = Color.White.copy(alpha = 0.7f))
                    Text(totalIncome.toCurrencyString(), color = IncomeGreenLight,
                         fontWeight = FontWeight.SemiBold)
                }
                // 지출
                Column(Modifier.weight(1f)) {
                    Text("지출", color = Color.White.copy(alpha = 0.7f))
                    Text(totalExpense.toCurrencyString(), color = ExpenseRedLight,
                         fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
```
