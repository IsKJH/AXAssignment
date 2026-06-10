package com.ax.assignment.feature.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ax.assignment.BudgetApplication
import com.ax.assignment.R
import com.ax.assignment.core.component.AppTopBar
import com.ax.assignment.core.navigation.Screen
import com.ax.assignment.core.theme.AXAssignmentTheme
import com.ax.assignment.core.theme.NavigationOn
import com.ax.assignment.core.theme.OnSurface
import com.ax.assignment.core.theme.SettingsBackground
import com.ax.assignment.core.theme.Surface
import com.ax.assignment.core.theme.TextDescription

@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val app = context.applicationContext as BudgetApplication
    val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(app))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsContent(
        uiState = uiState,
        onNavigateBack = { navController.popBackStack() },
        onNavigateToStartDay = { navController.navigate(Screen.StartDaySetting.route) },
        onReviewClick = { openStoreReview(context) },
        onShareClick = { shareApp(context) },
        onHelpClick = { navController.navigate(Screen.Help.route) },
    )
}

@Composable
fun SettingsContent(
    uiState: SettingsUiState,
    onNavigateBack: () -> Unit = {},
    onNavigateToStartDay: () -> Unit,
    onReviewClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onHelpClick: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            AppTopBar(title = "설정", onBack = onNavigateBack)
        },
        containerColor = SettingsBackground,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SettingsBackground)
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
        ) {
            SettingsSectionTitle("환경설정", Modifier.padding(top = 24.dp))
            SettingsGroup {
                SettingsRow(
                    icon = painterResource(R.drawable.ic_figma_volume_down),
                    title = "시작일 설정",
                    value = "매달 ${uiState.startDay}일",
                    valueColor = NavigationOn,
                    showChevron = true,
                    useFixedHeight = true,
                    onClick = onNavigateToStartDay,
                )
            }

            SettingsSectionTitle("고객 지원", Modifier.padding(top = 32.dp))
            SettingsGroup {
                SettingsRow(
                    icon = painterResource(R.drawable.ic_figma_kid_star),
                    title = "리뷰를 남겨주세요",
                    showChevron = true,
                    onClick = onReviewClick,
                )
                SettingsDivider()
                SettingsRow(
                    icon = painterResource(R.drawable.ic_figma_thumb_up),
                    title = "친구에게 앱 추천하기",
                    showChevron = true,
                    onClick = onShareClick,
                )
                SettingsDivider()
                SettingsRow(
                    icon = painterResource(R.drawable.ic_figma_help),
                    title = "사용 방법",
                    showChevron = true,
                    onClick = onHelpClick,
                )
            }

            SettingsSectionTitle("앱 정보", Modifier.padding(top = 32.dp))
            SettingsGroup {
                SettingsRow(
                    icon = painterResource(R.drawable.ic_figma_error),
                    title = "앱 버전",
                    value = "1.00",
                    valueColor = TextDescription,
                )
            }
        }
    }
}

private fun openStoreReview(context: android.content.Context) {
    val packageName = context.packageName
    val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    val webIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://play.google.com/store/apps/details?id=$packageName"),
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    runCatching {
        context.startActivity(marketIntent)
    }.recoverCatching {
        context.startActivity(webIntent)
    }.onFailure {
        Toast.makeText(context, "스토어를 열 수 없어요", Toast.LENGTH_SHORT).show()
    }
}

private fun shareApp(context: android.content.Context) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "AX 가계부")
        putExtra(Intent.EXTRA_TEXT, "AX 가계부로 수입과 지출을 간단히 관리해보세요.")
    }
    val chooser = Intent.createChooser(sendIntent, "앱 추천하기")
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    try {
        context.startActivity(chooser)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, "공유할 앱을 찾을 수 없어요", Toast.LENGTH_SHORT).show()
    }
}

@Composable
private fun SettingsSectionTitle(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        color = OnSurface,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        modifier = modifier.padding(bottom = 12.dp),
    )
}

@Composable
private fun SettingsGroup(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Surface),
    ) {
        content()
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        color = Color(0xFFEEEEEE),
        thickness = 1.dp,
    )
}

@Composable
private fun SettingsRow(
    icon: Painter,
    title: String,
    value: String? = null,
    valueColor: Color = TextDescription,
    showChevron: Boolean = false,
    useFixedHeight: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (useFixedHeight) Modifier.height(64.dp) else Modifier)
            .background(Surface)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(
                horizontal = 16.dp,
                vertical = if (useFixedHeight) 0.dp else 16.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(28.dp),
        )
        Text(
            text = title,
            color = OnSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
        )
        if (value != null) {
            Text(
                text = value,
                color = valueColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
        }
        if (showChevron) {
            Icon(
                painter = painterResource(R.drawable.ic_figma_chevron_right),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsContentPreview() {
    AXAssignmentTheme {
        SettingsContent(
            uiState = SettingsUiState(startDay = 25),
            onNavigateToStartDay = {},
        )
    }
}
