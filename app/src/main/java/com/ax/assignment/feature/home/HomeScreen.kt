package com.ax.assignment.feature.home

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ax.assignment.R
import com.ax.assignment.BudgetApplication
import com.ax.assignment.core.component.EmptyStateView
import com.ax.assignment.core.component.MonthSelector
import com.ax.assignment.core.component.StaggeredAppear
import com.ax.assignment.core.component.SummaryCard
import com.ax.assignment.core.component.TransactionItem
import com.ax.assignment.core.component.rememberEntranceTime
import com.ax.assignment.core.navigation.Screen
import com.ax.assignment.core.theme.AXAssignmentTheme
import com.ax.assignment.core.theme.FabFill
import com.ax.assignment.core.theme.OnSurface
import com.ax.assignment.core.theme.Primary
import com.ax.assignment.core.theme.Surface
import com.ax.assignment.core.theme.TextDefault
import com.ax.assignment.domain.model.Category
import com.ax.assignment.domain.model.Transaction
import com.ax.assignment.domain.model.TransactionType
import java.time.LocalDate
import java.time.LocalDateTime

@Composable
fun HomeScreen(navController: NavController) {
    val app = LocalContext.current.applicationContext as BudgetApplication
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModel.factory(app))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateToAdd = {
            navController.navigate(
                Screen.TransactionAdd.createRoute(
                    periodStart = uiState.periodStart.toString(),
                    periodEnd = uiState.periodEnd.toString(),
                ),
            )
        },
        onNavigateToDetail = { id -> navController.navigate(Screen.TransactionDetail.createRoute(id)) },
    )
}

@Composable
fun HomeContent(
    uiState: HomeUiState,
    onEvent: (HomeEvent) -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
) {
    // 수평 스와이프 상태 추적 (이전/다음 주기 이동)
    var dragOffset by remember { mutableFloatStateOf(0f) }

    Scaffold(
        topBar = {
            HomeTopBar(
                periodLabel = buildPeriodLabel(uiState.periodStart, uiState.periodEnd),
                onPrev = { onEvent(HomeEvent.PrevMonth) },
                onNext = { onEvent(HomeEvent.NextMonth) },
            )
        },
        containerColor = Surface,
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Primary)
                }

                uiState.error != null -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = uiState.error,
                        color = OnSurface,
                    )
                }

                uiState.isEmpty -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragStart = { dragOffset = 0f },
                                onHorizontalDrag = { _, delta -> dragOffset += delta },
                                onDragEnd = {
                                    if (dragOffset > 50f) onEvent(HomeEvent.PrevMonth)
                                    else if (dragOffset < -50f) onEvent(HomeEvent.NextMonth)
                                    dragOffset = 0f
                                },
                            )
                        },
                    contentAlignment = Alignment.TopCenter,
                ) {
                    EmptyStateView(modifier = Modifier.padding(top = 100.dp))
                }

                else -> HomeListContent(
                    uiState = uiState,
                    onEvent = onEvent,
                    onNavigateToDetail = onNavigateToDetail,
                    contentPadding = paddingValues,
                )
            }

            HomeFab(
                onClick = onNavigateToAdd,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp),
            )
        }
    }
}

@Composable
private fun HomeFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.86f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "fabPressScale",
    )

    Box(
        modifier = modifier
            .size(56.dp)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .shadow(
                elevation = 10.dp,
                shape = CircleShape,
                ambientColor = Color(0x1A000000),
                spotColor = Color(0x1A000000),
            )
            .background(FabFill, CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_figma_add),
            contentDescription = "거래 추가",
            tint = Color.Unspecified,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun HomeTopBar(
    periodLabel: String,
    onPrev: () -> Unit,
    onNext: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface)
            .statusBarsPadding(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = "AX가계부",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDefault,
            )
        }

        // 월 네비게이션 행 — ‹ [기간 텍스트] ›
        MonthSelector(
            periodLabel = periodLabel,
            onPrev = onPrev,
            onNext = onNext,
        )
    }
}

@Composable
private fun HomeListContent(
    uiState: HomeUiState,
    onEvent: (HomeEvent) -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    contentPadding: PaddingValues,
) {
    var dragOffset by remember { mutableFloatStateOf(0f) }

    // 날짜별 그룹화 (최신 날짜 순 내림차순)
    val grouped = remember(uiState.transactions) {
        uiState.transactions
            .groupBy { it.date.toLocalDate() }
            .entries
            .sortedByDescending { it.key }
    }

    val entranceTime = rememberEntranceTime()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { dragOffset = 0f },
                    onHorizontalDrag = { _, delta -> dragOffset += delta },
                    onDragEnd = {
                        if (dragOffset > 50f) onEvent(HomeEvent.PrevMonth)
                        else if (dragOffset < -50f) onEvent(HomeEvent.NextMonth)
                        dragOffset = 0f
                    },
                )
            },
        contentPadding = contentPadding,
    ) {
        // 요약 카드 — 수입/지출 요약 + 프로그레스
        item {
            SummaryCard(
                totalIncome = uiState.totalIncome,
                totalExpense = uiState.totalExpense,
                budgetRatio = uiState.budgetRatio,
                isExceeded = uiState.isExceeded,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 24.dp),
            )
        }

        // 날짜별 섹션 헤더 + 거래 항목 — animateItem으로 삭제 시 빈자리 메꿈/등장 페이드,
        // 첫 진입 시엔 통계 화면과 같은 스태거 등장
        var staggerIndex = 0
        grouped.forEach { (date, txList) ->
            val headerIndex = staggerIndex++
            item(key = "header-$date") {
                StaggeredAppear(index = headerIndex, entranceTime = entranceTime, modifier = Modifier.animateItem()) {
                    DateSectionHeader(date = date)
                }
            }
            txList.forEach { tx ->
                val rowIndex = staggerIndex++
                item(key = tx.id) {
                    StaggeredAppear(index = rowIndex, entranceTime = entranceTime, modifier = Modifier.animateItem()) {
                        TransactionItem(
                            transaction = tx,
                            onClick = { onNavigateToDetail(tx.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DateSectionHeader(date: LocalDate, modifier: Modifier = Modifier) {
    Text(
        text = "${date.monthValue}월 ${date.dayOfMonth}일",
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 21.sp,
        color = TextDefault,
        modifier = modifier.padding(start = 16.dp, end = 16.dp, bottom = 4.dp),
    )
}

/** Latest Figma target: "6월1일 ~ 6월30일" */
private fun buildPeriodLabel(start: LocalDate, end: LocalDate): String {
    val startStr = "${start.monthValue}월${start.dayOfMonth}일"
    val endStr = "${end.monthValue}월${end.dayOfMonth}일"
    return "$startStr ~ $endStr"
}

// ---- Previews ----

@Preview(showBackground = true, name = "SCR-01 빈 상태")
@Composable
private fun HomeContentEmptyPreview() {
    AXAssignmentTheme {
        HomeContent(
            uiState = HomeUiState(
                isLoading = false,
                transactions = emptyList(),
                periodStart = LocalDate.of(2026, 6, 1),
                periodEnd = LocalDate.of(2026, 6, 30),
            ),
            onEvent = {},
            onNavigateToAdd = {},
            onNavigateToDetail = {},
        )
    }
}

private val previewTransactions = listOf(
    Transaction(
        id = 1,
        amount = 6_500,
        type = TransactionType.EXPENSE,
        category = Category(
            id = 1,
            name = "식비",
            emoji = "🍽️",
            colorHex = "#5E92F3",
            type = TransactionType.EXPENSE,
        ),
        memo = "스타벅스",
        date = LocalDateTime.of(2026, 6, 3, 10, 0),
    ),
    Transaction(
        id = 2,
        amount = 2_800_000,
        type = TransactionType.INCOME,
        category = null,
        memo = "월급",
        date = LocalDateTime.of(2026, 6, 1, 9, 0),
    ),
)

@Preview(showBackground = true, name = "SCR-02 거래 있음")
@Composable
private fun HomeContentWithDataPreview() {
    AXAssignmentTheme {
        HomeContent(
            uiState = HomeUiState(
                isLoading = false,
                transactions = previewTransactions,
                totalIncome = 2_800_000L,
                totalExpense = 1_975_500L,
                periodStart = LocalDate.of(2026, 6, 1),
                periodEnd = LocalDate.of(2026, 6, 30),
            ),
            onEvent = {},
            onNavigateToAdd = {},
            onNavigateToDetail = {},
        )
    }
}
