package com.ax.assignment.core.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import com.ax.assignment.R
import com.ax.assignment.core.theme.AXAssignmentTheme
import com.ax.assignment.core.theme.TextDefault

@Composable
fun MonthSelector(
    periodLabel: String,
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

        Text(
            text = periodLabel,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextDefault,
            textAlign = TextAlign.Center,
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
            periodLabel = "6월1일 ~ 6월30일",
            onPrev = {},
            onNext = {},
        )
    }
}
