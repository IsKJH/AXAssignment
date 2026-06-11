package com.ax.assignment.feature.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ax.assignment.BudgetApplication
import com.ax.assignment.core.component.AppTopBar
import com.ax.assignment.core.theme.AXAssignmentTheme
import com.ax.assignment.core.theme.BrandLight
import com.ax.assignment.core.theme.NavigationOn
import com.ax.assignment.core.theme.Surface
import com.ax.assignment.core.theme.TextDefault
import com.ax.assignment.core.theme.TextDescription
import com.ax.assignment.core.util.toCurrencyString
import com.ax.assignment.domain.model.MonthlyExpense
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun SixMonthHistoryScreen(navController: NavController) {
    val app = LocalContext.current.applicationContext as BudgetApplication
    val viewModel: StatisticsViewModel = viewModel(factory = StatisticsViewModel.factory(app))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SixMonthHistoryContent(
        uiState = uiState,
        onBack = { navController.popBackStack() },
    )
}

@Composable
fun SixMonthHistoryContent(
    uiState: StatisticsUiState,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = "6개월 내역",
                onBack = onBack,
                showDivider = false,
            )
        },
        containerColor = Surface,
    ) { paddingValues ->
        when {
            uiState.isLoading -> Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            else -> {
                val trend = uiState.monthlyExpenseTrend
                val currentIndex = trend.size - 1
                var selectedIndex by remember { mutableStateOf(currentIndex.coerceAtLeast(0)) }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                ) {
                    Spacer(Modifier.height(16.dp))

                    // Header is fixed to the current period regardless of bar selection
                    val currentExpense = trend.getOrNull(currentIndex)
                    val currentMonth = currentExpense?.month ?: uiState.periodStart.monthValue
                    val currentTotal = currentExpense?.total ?: 0L

                    MonthChip(month = currentMonth)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = currentTotal.toCurrencyString(),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDefault,
                    )

                    Spacer(Modifier.height(70.dp))

                    BarChartSection(
                        trend = trend,
                        selectedIndex = selectedIndex,
                        currentIndex = currentIndex,
                        onBarSelected = { selectedIndex = it },
                    )

                    Spacer(Modifier.height(16.dp))

                    AverageCard(trend = trend)
                }
            }
        }
    }
}

@Composable
private fun MonthChip(month: Int) {
    Box(
        modifier = Modifier
            .height(29.dp)
            .background(BrandLight, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "${month}월",
            fontSize = 14.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Bold,
            color = NavigationOn,
        )
    }
}

@Composable
private fun BarChartSection(
    trend: List<MonthlyExpense>,
    selectedIndex: Int,
    currentIndex: Int,
    onBarSelected: (Int) -> Unit,
) {
    if (trend.isEmpty()) return

    val haptic = LocalHapticFeedback.current
    val maxTotal = trend.maxOf { it.total }.coerceAtLeast(1L)
    val maxBarHeight = 120.dp

    // Bars grow from the baseline on entry
    val growth = remember { Animatable(0f) }
    LaunchedEffect(trend) {
        growth.snapTo(0f)
        growth.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
    }

    // One tap handler over the whole chart (bars, gaps and month labels) so
    // near-miss taps between bars still select the closest month. Slot
    // boundaries from uniform division always fall inside the 16dp gaps.
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .pointerInput(trend.size) {
                detectTapGestures { offset ->
                    val slotWidth = size.width / trend.size.toFloat()
                    val index = (offset.x / slotWidth).toInt().coerceIn(0, trend.size - 1)
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onBarSelected(index)
                }
            },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(166.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            trend.forEachIndexed { index, expense ->
                val fullHeight = if (expense.total == 0L) 4f
                else expense.total.toFloat() / maxTotal * maxBarHeight.value
                val barHeightDp = (fullHeight * growth.value).coerceAtLeast(4f).dp
                // Bar color is fixed to the current period; tapping only moves the tooltip
                val isCurrentPeriod = index == currentIndex
                val barColor = if (isCurrentPeriod) NavigationOn else Color(0xFFEEEEEE)

                // Selected column is lifted above siblings so the overflowing tooltip
                // is not covered by bars drawn later in the Row.
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .zIndex(if (index == selectedIndex) 1f else 0f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    if (index == selectedIndex) {
                        // Pop-in: bubble scales up from the tail when selection moves
                        val pop = remember(selectedIndex) { Animatable(0f) }
                        LaunchedEffect(selectedIndex) {
                            pop.animateTo(1f, tween(220, easing = FastOutSlowInEasing))
                        }
                        // unbounded wrap must sit OUTSIDE graphicsLayer: with alpha < 1 the
                        // layer composites offscreen at its own size, so a column-sized layer
                        // would clip the bubble's overflowing sides during the pop
                        Box(
                            modifier = Modifier
                                .wrapContentWidth(unbounded = true)
                                .graphicsLayer {
                                    alpha = pop.value
                                    scaleX = 0.8f + 0.2f * pop.value
                                    scaleY = 0.8f + 0.2f * pop.value
                                    transformOrigin = TransformOrigin(0.5f, 1f)
                                },
                        ) {
                            TooltipBubble(
                                text = expense.total.toCurrencyString(),
                                isCurrentPeriod = isCurrentPeriod,
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(barHeightDp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(barColor),
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            trend.forEachIndexed { index, expense ->
                // Bold label is fixed to the current period month
                val isCurrentPeriod = index == currentIndex
                Text(
                    text = "${expense.month}월",
                    modifier = Modifier.weight(1f),
                    fontSize = 14.sp,
                    fontWeight = if (isCurrentPeriod) FontWeight.Bold else FontWeight.Medium,
                    color = if (isCurrentPeriod) TextDefault else TextDescription,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun TooltipBubble(text: String, isCurrentPeriod: Boolean) {
    val accentColor = if (isCurrentPeriod) NavigationOn else TextDescription
    val borderColor = if (isCurrentPeriod) NavigationOn else Color(0xFFE0E0E0)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.wrapContentWidth(unbounded = true),
    ) {
        Box(
            modifier = Modifier
                .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                .background(Color.White, RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 5.dp),
        ) {
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Visible,
            )
        }
        Canvas(modifier = Modifier.width(14.dp).height(7.dp)) {
            val w = size.width
            val h = size.height
            val strokePx = 1.dp.toPx()
            // White fill drawn 1px higher to mask the bubble's bottom border at the joint
            val fill = Path().apply {
                moveTo(0f, -strokePx)
                lineTo(w, -strokePx)
                lineTo(w / 2f, h - strokePx)
                close()
            }
            drawPath(fill, Color.White)
            drawLine(borderColor, Offset(0f, 0f), Offset(w / 2f, h - strokePx), strokePx)
            drawLine(borderColor, Offset(w, 0f), Offset(w / 2f, h - strokePx), strokePx)
        }
    }
}

@Composable
private fun AverageCard(trend: List<MonthlyExpense>) {
    // Figma reference: 2,445,000 = sum of 6 months / 6 (zero months included)
    val average = if (trend.isEmpty()) 0L else trend.sumOf { it.total } / trend.size

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrandLight, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "6개월 평균 소비 금액",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextDefault,
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = average.toCurrencyString(),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextDefault,
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun SixMonthHistoryPreview() {
    AXAssignmentTheme {
        SixMonthHistoryContent(
            uiState = StatisticsUiState(
                isLoading = false,
                monthlyExpenseTrend = listOf(
                    MonthlyExpense(2026, 1, 2_001_000),
                    MonthlyExpense(2026, 2, 1_750_000),
                    MonthlyExpense(2026, 3, 2_250_000),
                    MonthlyExpense(2026, 4, 1_250_000),
                    MonthlyExpense(2026, 5, 2_500_000),
                    MonthlyExpense(2026, 6, 2_800_000),
                ),
            ),
            onBack = {},
        )
    }
}
