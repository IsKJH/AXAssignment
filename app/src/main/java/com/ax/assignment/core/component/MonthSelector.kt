package com.ax.assignment.core.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ax.assignment.core.theme.AXAssignmentTheme
import com.ax.assignment.core.theme.TextDefault
import com.ax.assignment.core.theme.TextDescription
import com.ax.assignment.R
import com.ax.assignment.core.util.periodRangeParts
import java.time.LocalDate

/**
 * Period label shared by the home and statistics selectors. The Figma day range
 * stays on its own line in the default color; for periods outside the current
 * year a quiet grey year tag sits on a second line below (a calendar-style
 * two-line header), which keeps year-straddling ranges like "2025~2026" tidy
 * instead of wrapping mid-line.
 */
@Composable
fun PeriodLabelText(
    start: LocalDate,
    end: LocalDate,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
) {
    val (base, year) = periodRangeParts(start, end, today)
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = base,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextDefault,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
        if (year != null) {
            Text(
                text = year,
                fontSize = 13.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextDescription,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}

@Composable
fun MonthSelector(
    periodStart: LocalDate,
    periodEnd: LocalDate,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clickable(onClick = onPrev),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_figma_chevron_left),
                contentDescription = "이전 주기",
                tint = TextDefault,
                modifier = Modifier.size(28.dp),
            )
        }

        PeriodLabelText(
            start = periodStart,
            end = periodEnd,
            modifier = Modifier.weight(1f),
        )

        Box(
            modifier = Modifier
                .size(28.dp)
                .clickable(onClick = onNext),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_figma_chevron_right),
                contentDescription = "다음 주기",
                tint = TextDefault,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MonthSelectorPreview() {
    AXAssignmentTheme {
        MonthSelector(
            periodStart = LocalDate.of(2026, 6, 1),
            periodEnd = LocalDate.of(2026, 6, 30),
            onPrev = {},
            onNext = {},
        )
    }
}
