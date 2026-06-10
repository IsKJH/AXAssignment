package com.ax.assignment.core.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ax.assignment.R
import com.ax.assignment.core.theme.AXAssignmentTheme
import com.ax.assignment.core.theme.FabFill
import com.ax.assignment.core.theme.HomeExpenseAmount
import com.ax.assignment.core.theme.HomeProgressFill
import com.ax.assignment.core.theme.Surface
import com.ax.assignment.core.theme.WarningOrange
import com.ax.assignment.core.util.toCurrencyString
import kotlin.math.roundToInt

@Composable
fun SummaryCard(
    totalIncome: Long,
    totalExpense: Long,
    budgetRatio: Float,
    isExceeded: Boolean,
    modifier: Modifier = Modifier,
) {
    val exceededAmount = (totalExpense - totalIncome).coerceAtLeast(0L)
    // Figma 코멘트 정책: % 소수점은 반올림 (최소 1% 보정 없음)
    val percent = (budgetRatio * 100).roundToInt()
    val targetFraction = if (isExceeded) 1f else budgetRatio.coerceIn(0f, 1f)
    val progressFraction by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "summaryProgress",
    )
    // Count-up effect for the headline expense amount
    val animatedExpense by animateFloatAsState(
        targetValue = totalExpense.toFloat(),
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "summaryExpense",
    )
    val progressColor by animateColorAsState(
        targetValue = when {
            isExceeded || budgetRatio >= 1f -> HomeExpenseAmount
            budgetRatio >= 0.8f -> WarningOrange
            else -> HomeProgressFill
        },
        animationSpec = tween(500),
        label = "summaryProgressColor",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(if (isExceeded) 164.dp else 139.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(FabFill)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "수입 ${totalIncome.toCurrencyString().removeSuffix("원")}",
                color = Surface,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 21.sp,
            )
            Text(
                text = "${animatedExpense.toLong().toCurrencyString()} 사용했어요.",
                color = Surface,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 33.sp,
            )
            if (isExceeded) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_figma_error),
                        contentDescription = null,
                        tint = Surface,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = buildAnnotatedString {
                            append("수입의 ")
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(exceededAmount.toCurrencyString())
                            }
                            append(" 초과하셨습니다.")
                        },
                        color = Surface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 21.sp,
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "$percent% 사용중",
                color = Surface,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 21.sp,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(Surface),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = progressFraction)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(100.dp))
                        .background(progressColor),
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "정상")
@Composable
private fun SummaryCardNormalPreview() {
    AXAssignmentTheme {
        SummaryCard(
            totalIncome = 2_800_000L,
            totalExpense = 1_975_500L,
            budgetRatio = 0.7f,
            isExceeded = false,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, name = "초과")
@Composable
private fun SummaryCardExceededPreview() {
    AXAssignmentTheme {
        SummaryCard(
            totalIncome = 2_800_000L,
            totalExpense = 3_360_000L,
            budgetRatio = 1.2f,
            isExceeded = true,
            modifier = Modifier.padding(16.dp),
        )
    }
}
