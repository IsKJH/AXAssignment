package com.ax.assignment.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ax.assignment.core.component.AppTopBar
import com.ax.assignment.core.theme.AXAssignmentTheme
import com.ax.assignment.core.theme.BrandLight
import com.ax.assignment.core.theme.CardPrimary
import com.ax.assignment.core.theme.SettingsBackground
import com.ax.assignment.core.theme.Surface
import com.ax.assignment.core.theme.TextDefault
import com.ax.assignment.core.theme.TextDescription

private data class HelpItem(val title: String, val description: String)

private val helpItems = listOf(
    HelpItem(
        title = "거래 추가",
        description = "홈의 + 버튼으로 지출이나 수입을 입력합니다. 금액과 내역을 적고 " +
            "카테고리·일시를 선택하면 끝 — 날짜는 과거든 미래든 자유롭게 고를 수 있어요.",
    ),
    HelpItem(
        title = "정기 거래 등록",
        description = "매달 반복되는 구독료나 월급은 \"정기 지출/수입으로 등록\"을 체크하세요. " +
            "선택한 일자에 매달 자동으로 등록됩니다.",
    ),
    HelpItem(
        title = "내역 확인과 수정",
        description = "거래를 누르면 상세 화면에서 편집·삭제할 수 있습니다. " +
            "정기 거래는 이 달만, 이후 모두, 전체 중에서 적용 범위를 고를 수 있어요.",
    ),
    HelpItem(
        title = "정산 주기 설정",
        description = "설정 → 시작일 설정에서 매달 정산을 시작할 날짜를 1~28일 중 고르면 " +
            "홈과 통계가 그 주기 기준으로 바로 다시 계산됩니다.",
    ),
    HelpItem(
        title = "통계 보기",
        description = "통계 탭에서 카테고리별 지출 비중을 확인하세요. 도넛 차트를 누른 채 " +
            "문지르면 항목별 비중이 표시되고, 6개월 내역에서 소비 흐름을 볼 수 있습니다.",
    ),
    HelpItem(
        title = "주기 이동",
        description = "홈과 통계 화면을 좌우로 스와이프하거나 상단 화살표를 눌러 " +
            "이전·다음 주기의 내역을 확인할 수 있습니다.",
    ),
)

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
                onBack = onNavigateBack,
            )
        },
        containerColor = SettingsBackground,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SettingsBackground)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Cap content width so cards stay readable on tablets/landscape
            Column(
                modifier = Modifier.widthIn(max = 600.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                helpItems.forEachIndexed { index, item ->
                    HelpStep(
                        number = "${index + 1}",
                        title = item.title,
                        description = item.description,
                    )
                }
            }
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
            .background(Surface, RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(BrandLight, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = number,
                color = CardPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                color = TextDefault,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 24.sp,
            )
            Text(
                text = description,
                color = TextDescription,
                fontSize = 14.sp,
                lineHeight = 21.sp,
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
