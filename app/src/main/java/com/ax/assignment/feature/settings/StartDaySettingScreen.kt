package com.ax.assignment.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ax.assignment.BudgetApplication
import com.ax.assignment.core.component.AppTopBar
import com.ax.assignment.core.theme.AXAssignmentTheme
import com.ax.assignment.core.theme.BrandLight
import com.ax.assignment.core.theme.ConfirmButtonBg
import com.ax.assignment.core.theme.NavigationOn
import com.ax.assignment.core.theme.Surface
import com.ax.assignment.core.theme.SurfaceVariant
import com.ax.assignment.core.theme.TextDefault
import com.ax.assignment.core.theme.TextDescription
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun StartDaySettingScreen(navController: NavController) {
    val app = LocalContext.current.applicationContext as BudgetApplication
    val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(app))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    StartDaySettingContent(
        startDay = uiState.startDay,
        onSave = { day ->
            viewModel.onEvent(SettingsEvent.SetStartDay(day))
            navController.popBackStack()
        },
        onNavigateBack = { navController.popBackStack() },
    )
}

@Composable
fun StartDaySettingContent(
    startDay: Int,
    onSave: (Int) -> Unit,
    onNavigateBack: () -> Unit,
) {
    var selectedDay by remember(startDay) { mutableIntStateOf(startDay.coerceIn(1, 28)) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "시작일 설정",
                onNavigateBack = onNavigateBack,
            )
        },
        bottomBar = {
            Button(
                onClick = { onSave(selectedDay) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ConfirmButtonBg,
                    contentColor = Surface,
                    disabledContainerColor = TextDescription,
                    disabledContentColor = SurfaceVariant,
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("확인", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color(0xFFEEEEEE),
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .padding(top = 24.dp),
        ) {
            Text("정산 시작일", color = TextDefault, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(
                text = "매달 이 날짜를 시작으로 한 주기를 계산합니다.",
                color = TextDefault,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
            DayPicker(
                selectedDay = selectedDay,
                onDaySelected = { selectedDay = it },
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}

@Composable
private fun DayPicker(
    selectedDay: Int,
    onDaySelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val days = remember { (1..28).toList() }
    val listHeight = 240.dp
    val itemHeight = 48.dp
    val selectedIndex = (selectedDay - 1).coerceIn(days.indices)

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = selectedIndex,
    )
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    val centerIndex by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val center = (info.viewportStartOffset + info.viewportEndOffset) / 2
            info.visibleItemsInfo.minByOrNull { kotlin.math.abs((it.offset + it.size / 2) - center) }?.index
        }
    }

    // 스크롤 중 중앙 아이템 변경 시 선택 업데이트
    LaunchedEffect(listState) {
        snapshotFlow { centerIndex }.distinctUntilChanged().collect { idx ->
            if (idx != null && idx in days.indices) onDaySelected(days[idx])
        }
    }
    // 스크롤 멈춤 시 선택 확정
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress to centerIndex }
            .distinctUntilChanged()
            .collect { (isScrolling, idx) ->
                if (!isScrolling && idx != null && idx in days.indices) {
                    onDaySelected(days[idx])
                }
            }
    }
    // 외부에서 selectedDay 변경 시 해당 항목으로 스크롤
    LaunchedEffect(selectedIndex) {
        if (!listState.isScrollInProgress) {
            listState.animateScrollToItem(selectedIndex.coerceIn(days.indices))
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(listHeight)
            .background(Surface, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        // 선택 항목 배경 하이라이트
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
                .background(BrandLight),
        )
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = (listHeight - itemHeight) / 2),
            flingBehavior = snapFlingBehavior,
        ) {
            items(days.size) { index ->
                val isSelected = index == (centerIndex ?: selectedIndex)
                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth()
                        .clickable { onDaySelected(days[index]) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "${days[index]}일",
                        color = if (isSelected) NavigationOn else TextDescription,
                        fontSize = if (isSelected) 18.sp else 16.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StartDaySettingPreview() {
    AXAssignmentTheme {
        StartDaySettingContent(
            startDay = 25,
            onSave = {},
            onNavigateBack = {},
        )
    }
}
