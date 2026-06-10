package com.ax.assignment.core.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ax.assignment.R
import com.ax.assignment.core.theme.AXAssignmentTheme
import com.ax.assignment.core.theme.HomeExpenseAmount
import com.ax.assignment.core.theme.HomeIncomeAmount
import com.ax.assignment.core.theme.NavigationOff
import com.ax.assignment.core.theme.TextDefault
import com.ax.assignment.core.theme.TextDescription
import com.ax.assignment.core.util.toCurrencyString
import com.ax.assignment.domain.model.Category
import com.ax.assignment.domain.model.Transaction
import com.ax.assignment.domain.model.TransactionType
import java.time.LocalDateTime

@Composable
fun TransactionItem(
    transaction: Transaction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isIncome = transaction.type == TransactionType.INCOME
    val amountColor = if (isIncome) HomeIncomeAmount else HomeExpenseAmount
    val amountPrefix = if (isIncome) "+" else "-"
    val categoryLabel = transaction.category?.name ?: if (isIncome) "수입" else "미분류"
    // Uncategorized falls back to gray, matching the statistics screen
    val dotColor = transaction.category?.let {
        runCatching { Color(android.graphics.Color.parseColor(it.colorHex)) }.getOrElse { NavigationOff }
    } ?: TextDescription

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top,
                modifier = Modifier.weight(1f),
            ) {
                Box(
                    modifier = Modifier
                        .width(10.dp)
                        .height(18.dp),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(dotColor),
                    )
                }

                Column {
                    Text(
                        text = categoryLabel,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 24.sp,
                        color = TextDefault,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (transaction.memo.isNotBlank()) {
                        Text(
                            text = transaction.memo,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 21.sp,
                            color = TextDefault,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                Text(
                    text = "$amountPrefix${transaction.amount.toCurrencyString().removeSuffix("원")}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 24.sp,
                    color = amountColor,
                )
                Icon(
                    painter = painterResource(R.drawable.ic_figma_chevron_right),
                    contentDescription = null,
                    tint = TextDefault,
                    modifier = Modifier.size(28.dp),
                )
            }
        }

        HorizontalDivider(
            color = Color(0xFFEEEEEE),
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TransactionItemExpensePreview() {
    AXAssignmentTheme {
        TransactionItem(
            transaction = Transaction(
                id = 1,
                amount = 6_500,
                type = TransactionType.EXPENSE,
                category = Category(
                    id = 1,
                    name = "식비",
                    emoji = "",
                    colorHex = "#FFAC11",
                    type = TransactionType.EXPENSE,
                ),
                memo = "스타벅스",
                date = LocalDateTime.now(),
            ),
            onClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TransactionItemIncomePreview() {
    AXAssignmentTheme {
        TransactionItem(
            transaction = Transaction(
                id = 2,
                amount = 2_800_000,
                type = TransactionType.INCOME,
                category = null,
                memo = "급여",
                date = LocalDateTime.now(),
            ),
            onClick = {},
        )
    }
}
