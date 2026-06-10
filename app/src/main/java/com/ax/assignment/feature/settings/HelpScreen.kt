package com.ax.assignment.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ax.assignment.core.component.AppTopBar
import com.ax.assignment.core.theme.AXAssignmentTheme
import com.ax.assignment.core.theme.CardPrimary
import com.ax.assignment.core.theme.DividerColor
import com.ax.assignment.core.theme.OnSurface
import com.ax.assignment.core.theme.OnSurfaceVariant
import com.ax.assignment.core.theme.Surface

@Composable
fun HelpScreen(navController: NavController) {
    HelpContent(onNavigateBack = { navController.popBackStack() })
}

@Composable
fun HelpContent(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = "사용 방법",
                onNavigateBack = onNavigateBack,
            )
        },
        containerColor = Surface,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            HelpStep(
                number = "1",
                title = "거래 추가",
                description = "+ 버튼을 눌러 지출이나 수입을 입력하고 카테고리와 날짜를 선택합니다.",
            )
            HelpStep(
                number = "2",
                title = "정산 기간 변경",
                description = "설정의 시작일 설정에서 매달 정산을 시작할 날짜를 1~28일 중 선택합니다.",
            )
            HelpStep(
                number = "3",
                title = "내역 확인과 수정",
                description = "홈 화면의 거래 내역을 눌러 상세 정보를 확인하고 수정하거나 삭제합니다.",
            )
            HelpStep(
                number = "4",
                title = "통계 확인",
                description = "통계 탭에서 카테고리별 지출 비중과 최근 소비 흐름을 확인합니다.",
            )
        }
    }
}

@Composable
private fun HelpStep(
    number: String,
    title: String,
    description: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DividerColor, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(Surface)
            .padding(16.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = number,
            color = CardPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 1.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(
                text = title,
                color = OnSurface,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = description,
                color = OnSurfaceVariant,
                fontSize = 13.sp,
                lineHeight = 19.sp,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HelpContentPreview() {
    AXAssignmentTheme {
        HelpContent(onNavigateBack = {})
    }
}
