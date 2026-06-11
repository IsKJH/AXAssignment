package com.ax.assignment.feature.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ax.assignment.BudgetApplication
import com.ax.assignment.R
import com.ax.assignment.core.component.AppTopBar
import com.ax.assignment.core.component.DonutChart
import com.ax.assignment.core.component.StaggeredAppear
import com.ax.assignment.core.component.rememberEntranceTime
import com.ax.assignment.core.navigation.Screen
import com.ax.assignment.core.theme.AXAssignmentTheme
import com.ax.assignment.core.theme.CategoryColors
import com.ax.assignment.core.theme.HomeExpenseAmount
import com.ax.assignment.core.theme.HomeIncomeAmount
import com.ax.assignment.core.theme.NavigationOn
import com.ax.assignment.core.theme.Surface
import com.ax.assignment.core.theme.TextDefault
import com.ax.assignment.core.theme.TextDescription
import com.ax.assignment.core.util.periodSwipe
import com.ax.assignment.core.util.toCurrencyString
import com.ax.assignment.domain.model.CategorySummary
import java.time.LocalDate
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun StatisticsScreen(navController: NavController) {
    val app = LocalContext.current.applicationContext as BudgetApplication
    val viewModel: StatisticsViewModel = viewModel(factory = StatisticsViewModel.factory(app))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    StatisticsContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateToSixMonthHistory = { navController.navigate(Screen.SixMonthHistory.route) },
    )
}

@Composable
fun StatisticsContent(
    uiState: StatisticsUiState,
    onEvent: (StatisticsEvent) -> Unit,
    onNavigateToSixMonthHistory: () -> Unit,
) {
    Scaffold(
        topBar = { AppTopBar(title = "통계", showDivider = false) },
        containerColor = Surface,
    ) { paddingValues ->
        when {
            uiState.isLoading -> Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            uiState.error != null -> Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) { Text(uiState.error) }

            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .periodSwipe(
                        onPrev = { onEvent(StatisticsEvent.PrevMonth) },
                        onNext = { onEvent(StatisticsEvent.NextMonth) },
                    )
                    .verticalScroll(rememberScrollState()),
            ) {
                PeriodSelectorRow(
                    start = uiState.periodStart,
                    end = uiState.periodEnd,
                    onPrev = { onEvent(StatisticsEvent.PrevMonth) },
                    onNext = { onEvent(StatisticsEvent.NextMonth) },
                )
                Spacer(Modifier.height(24.dp))
                DonutSection(uiState = uiState)
                Spacer(Modifier.height(16.dp))
                TextSection(uiState = uiState)
                Spacer(Modifier.height(16.dp))
                CategorySection(
                    uiState = uiState,
                    onNavigateToSixMonthHistory = onNavigateToSixMonthHistory,
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun PeriodSelectorRow(
    start: LocalDate,
    end: LocalDate,
    onPrev: () -> Unit,
    onNext: () -> Unit,
) {
    val rangeText = "${start.monthValue}월${start.dayOfMonth}일 ~ ${end.monthValue}월${end.dayOfMonth}일"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(36.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onPrev, modifier = Modifier.size(28.dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "이전 주기",
                modifier = Modifier.size(28.dp),
                tint = TextDefault,
            )
        }
        Text(
            text = rangeText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextDefault,
            textAlign = TextAlign.Center,
        )
        IconButton(onClick = onNext, modifier = Modifier.size(28.dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "다음 주기",
                modifier = Modifier.size(28.dp),
                tint = TextDefault,
            )
        }
    }
}

@Composable
private fun DonutSection(uiState: StatisticsUiState) {
    val donut = uiState.donutSegments
    val segments = donut.map { (summary, ratio) -> categoryColor(summary) to ratio }

    // While pressing/scrubbing the ring, center shows the touched category's name + share
    var selectedIndex by remember(donut) { mutableStateOf<Int?>(null) }
    val haptic = LocalHapticFeedback.current

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        // Figma 488:898 (SVG 실측): ring stroke 18, inner disc 125 → 9.5dp white gap
        DonutChart(
            segments = segments,
            diameter = 180.dp,
            strokeWidth = 18.dp,
            centerDiameter = 125.dp,
            emptyColor = Color(0xFFEEEEEE),
            onSegmentPress = { index ->
                if (index != null && index != selectedIndex) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
                selectedIndex = index
            },
            centerContent = {
                val selected = selectedIndex?.let { donut.getOrNull(it) }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (selected != null) {
                        val (summary, ratio) = selected
                        Text(
                            text = summary.category?.name ?: "미분류",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = categoryColor(summary),
                            maxLines = 1,
                        )
                        Text(
                            text = "${(ratio * 100).roundToInt()}%",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = categoryColor(summary),
                            textAlign = TextAlign.Center,
                        )
                    } else {
                        Text(
                            text = "총 지출",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextDefault,
                        )
                        Text(
                            text = if (uiState.isEmptyExpense) "0%" else "100%",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDefault,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            },
        )
    }
}

@Composable
private fun TextSection(uiState: StatisticsUiState) {
    val balance = uiState.balance
    val balanceColor = if (balance >= 0L) HomeIncomeAmount else HomeExpenseAmount
    val balancePrefix = if (balance >= 0L) "+" else "-"
    val balanceAbs = abs(balance)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        val tops = uiState.topCategories
        val topText = buildAnnotatedString {
            append("가장 소비가 많은 카테고리는 ")
            if (tops.isEmpty()) {
                withStyle(SpanStyle(color = TextDescription, fontWeight = FontWeight.Bold)) {
                    append("없음")
                }
            } else {
                val topColor = categoryColor(tops.first())
                val names = tops.map { it.category?.name ?: "미분류" }
                withStyle(SpanStyle(color = topColor, fontWeight = FontWeight.Bold)) {
                    when {
                        names.size == 1 -> append(names[0])
                        names.size <= 3 -> append(names.joinToString(", "))
                        else -> append("${names[0]}, ${names[1]}, ${names[2]}...")
                    }
                }
            }
        }
        Text(
            text = topText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextDefault,
        )

        val balanceLine = buildAnnotatedString {
            append("남은 금액은 ")
            withStyle(
                SpanStyle(color = balanceColor, fontWeight = FontWeight.Bold, fontSize = 16.sp),
            ) {
                append("$balancePrefix${balanceAbs.toCurrencyString()} ")
            }
        }
        Text(text = balanceLine, fontSize = 14.sp, color = TextDefault)
    }
}

@Composable
private fun CategorySection(
    uiState: StatisticsUiState,
    onNavigateToSixMonthHistory: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            // Enabled when the latest 6 months contain any expense,
            // independent of which period is being browsed
            val hasData = uiState.monthlyExpenseTrend.any { it.total > 0 }
            Box(
                modifier = Modifier
                    .height(26.dp)
                    .background(
                        color = if (hasData) NavigationOn else Color(0xFFD2D2D2),
                        shape = RoundedCornerShape(4.dp),
                    )
                    .then(
                        if (hasData) Modifier.clickable { onNavigateToSixMonthHistory() }
                        else Modifier,
                    )
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "6개월 내역",
                    fontSize = 12.sp,
                    lineHeight = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (hasData) Color.White else Color(0xFF898989),
                )
            }
        }

        if (uiState.isEmptyExpense) {
            Spacer(Modifier.height(80.dp))
            // Same fade/float entrance as list rows — empty state greets like content does
            StaggeredAppear(index = 0, entranceTime = rememberEntranceTime()) {
                EmptyExpenseBody()
            }
        } else {
            val entranceTime = rememberEntranceTime()
            val comparisons = uiState.categoryComparisons
            comparisons.forEachIndexed { index, comparison ->
                StaggeredAppear(index = index, entranceTime = entranceTime) {
                    CategoryRow(
                        comparison = comparison,
                        showDivider = index < comparisons.size - 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyExpenseBody() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_figma_bar_chart),
            contentDescription = null,
            modifier = Modifier.size(88.dp),
            tint = Color(0xFFD2D2D2),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "아직 사용한 이력이 없어요.",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextDefault,
        )
    }
}

@Composable
private fun CategoryRow(comparison: CategoryComparison, showDivider: Boolean) {
    val summary = comparison.summary
    val color = categoryColor(summary)
    val percentInt = (comparison.percentage * 100).roundToInt()
    val diffAmount = summary.totalAmount - comparison.prevAmount
    val diffInMan = diffAmount / 10_000L

    val manFormat = java.text.NumberFormat.getNumberInstance(java.util.Locale.KOREA)
    val (diffText, diffColor) = when {
        // Spec 488:1046 — no last-month data counts as "0원" (gray), not an increase
        comparison.prevAmount == 0L -> "지난달 대비 0 원" to TextDescription
        diffInMan > 0 -> "지난달 대비 +${manFormat.format(diffInMan)} 만원" to HomeExpenseAmount
        diffInMan < 0 -> "지난달 대비 -${manFormat.format(-diffInMan)} 만원" to NavigationOn
        else -> "지난달 대비 0 원" to TextDescription
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(74.dp)
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.width(124.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(color, CircleShape),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = summary.category?.name ?: "미분류",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextDefault,
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = summary.totalAmount.toCurrencyString(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDefault,
                    )
                    Text(
                        text = diffText,
                        fontSize = 12.sp,
                        color = diffColor,
                    )
                }
                Spacer(Modifier.width(16.dp))
                Box(
                    modifier = Modifier
                        .height(18.dp)
                        .background(color.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "${percentInt}%",
                        fontSize = 12.sp,
                        lineHeight = 12.sp,
                        color = color,
                    )
                }
            }
        }
        if (showDivider) {
            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
        }
    }
}

internal fun categoryColor(summary: CategorySummary): Color {
    val cat = summary.category ?: return Color(0xFFAFAFAF)
    val parsed = runCatching { Color(android.graphics.Color.parseColor(cat.colorHex)) }.getOrNull()
    if (parsed != null) return parsed
    val idx = ((cat.id % CategoryColors.size).toInt() + CategoryColors.size) % CategoryColors.size
    return CategoryColors[idx]
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun StatisticsContentEmptyPreview() {
    AXAssignmentTheme {
        StatisticsContent(
            uiState = StatisticsUiState(isLoading = false),
            onEvent = {},
            onNavigateToSixMonthHistory = {},
        )
    }
}
