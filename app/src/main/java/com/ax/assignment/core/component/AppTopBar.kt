package com.ax.assignment.core.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ax.assignment.core.theme.AXAssignmentTheme
import com.ax.assignment.core.theme.DividerColor
import com.ax.assignment.core.theme.OnSurface
import com.ax.assignment.core.theme.Surface

/**
 * 앱 공통 TopBar.
 * - onBack = null 이면 뒤로가기 버튼 없음 (설정·통계 화면 등 최상위 화면용)
 * - actions: 우측 버튼 영역 (아이콘 버튼 또는 텍스트 버튼)
 * - showDivider: 하단 구분선 표시 여부 (default = true)
 */
@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    showDivider: Boolean = true,
    titleWeight: FontWeight = FontWeight.Bold,
) {
    Column(
        modifier = modifier
            .background(Surface)
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // Figma top-bar: 좌우 16 + 상하 12 패딩 → 높이 56
                .height(56.dp)
                .background(Surface)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Leading: 뒤로가기 버튼 또는 빈 공간 (고정 폭)
            Box(
                modifier = Modifier
                    .width(64.dp)
                    .height(56.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (onBack != null) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(40.dp),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "뒤로",
                            tint = OnSurface,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                }
            }

            // Center: 제목
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                color = OnSurface,
                fontSize = 20.sp,
                fontWeight = titleWeight,
                textAlign = TextAlign.Center,
            )

            // Trailing: 액션 버튼 영역 (좌측과 동일한 폭으로 맞춰서 중앙 정렬 강제)
            Row(
                modifier = Modifier
                    .width(64.dp)
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
            ) {
                actions()
            }
        }

        if (showDivider) {
            HorizontalDivider(
                color = DividerColor,
                thickness = 1.dp,
            )
        }
    }
}

/**
 * 이전 시그니처와의 하위 호환 래퍼.
 * feature 화면들이 onNavigateBack + action 파라미터로 호출하는 경우를 지원한다.
 */
@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
    action: (@Composable () -> Unit)? = null,
    titleWeight: FontWeight = FontWeight.Bold,
) {
    AppTopBar(
        title = title,
        modifier = modifier,
        onBack = onNavigateBack,
        actions = { action?.invoke() },
        titleWeight = titleWeight,
    )
}

@Preview(showBackground = true)
@Composable
private fun AppTopBarWithBackPreview() {
    AXAssignmentTheme {
        AppTopBar(
            title = "거래 내역 추가",
            onBack = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AppTopBarNoBackPreview() {
    AXAssignmentTheme {
        AppTopBar(
            title = "설정",
        )
    }
}
