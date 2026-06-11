package com.ax.assignment.core.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ax.assignment.R
import com.ax.assignment.core.theme.AXAssignmentTheme
import com.ax.assignment.core.theme.TextDefault
import com.ax.assignment.core.theme.TextDescription

/**
 * SCR-01 기준 빈 상태 뷰.
 *
 * - 일러스트: 88×88dp receipt_long icon
 * - 타이틀: 18sp Bold, TextDefault
 * - 서브텍스트: 14sp Regular, TextDescription
 */
@Composable
fun EmptyStateView(
    modifier: Modifier = Modifier,
    title: String = "아직 입력한 내역이 없어요!",
    subtitle: String = "+버튼으로 지출 관리를 시작하세요.",
) {
    // Same fade/float entrance as list rows — empty states greet like content does
    StaggeredAppear(index = 0, entranceTime = rememberEntranceTime(), modifier = modifier) {
        EmptyStateBody(title = title, subtitle = subtitle)
    }
}

@Composable
private fun EmptyStateBody(title: String, subtitle: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_figma_receipt_long),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(88.dp),
        )

        Spacer(Modifier.height(12.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDefault,
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = TextDescription,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStateViewPreview() {
    AXAssignmentTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(androidx.compose.ui.graphics.Color.White),
            contentAlignment = Alignment.Center,
        ) {
            EmptyStateView()
        }
    }
}
