package com.ax.assignment.feature.transaction

import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ax.assignment.R
import com.ax.assignment.BudgetApplication
import com.ax.assignment.core.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import com.ax.assignment.core.theme.AXAssignmentTheme
import com.ax.assignment.core.theme.Pretendard
import com.ax.assignment.core.theme.BrandLight
import com.ax.assignment.core.theme.CardPrimary
import com.ax.assignment.core.theme.ConfirmButtonBg
import com.ax.assignment.core.theme.NavigationOn
import com.ax.assignment.core.theme.OnSurface
import com.ax.assignment.core.theme.OnSurfaceVariant
import com.ax.assignment.core.theme.Surface
import com.ax.assignment.core.theme.SurfaceVariant
import com.ax.assignment.core.theme.TextDefault
import com.ax.assignment.core.theme.TextDescription
import com.ax.assignment.domain.model.Category
import com.ax.assignment.domain.model.TransactionType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Composable
fun TransactionAddScreen(
    navController: NavController,
    periodStartArg: String? = null,
    periodEndArg: String? = null,
) {
    val context = LocalContext.current
    val app = context.applicationContext as BudgetApplication
    val viewModel: TransactionViewModel = viewModel(factory = TransactionViewModel.factory(app))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val today = LocalDate.now()
    val periodStart = remember(periodStartArg) {
        periodStartArg?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            ?: today.withDayOfMonth(1)
    }
    val periodEnd = remember(periodEndArg, periodStart) {
        periodEndArg?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            ?: periodStart.withDayOfMonth(periodStart.lengthOfMonth())
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            Toast.makeText(context, "저장되었습니다.", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
    }
    // Default the date into the viewed period once on entry; re-runs after a
    // category-select round trip would override a user-picked out-of-period date
    var defaultDateApplied by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!defaultDateApplied) {
            defaultDateApplied = true
            val clamped = uiState.date.coerceToDateRange(periodStart, periodEnd)
            if (clamped != uiState.date) {
                viewModel.onEvent(TransactionEvent.SetDate(clamped))
            }
        }
    }

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val categoryIdResult: Long by remember(savedStateHandle) {
        savedStateHandle?.getStateFlow("selected_category_id", -1L) ?: MutableStateFlow(-1L)
    }.collectAsStateWithLifecycle()
    LaunchedEffect(categoryIdResult, uiState.categories) {
        if (categoryIdResult == 0L) {
            // Deselected on the category screen — back out to 미분류
            viewModel.onEvent(TransactionEvent.SetCategory(null))
            savedStateHandle?.remove<Long>("selected_category_id")
        } else if (categoryIdResult > 0L && uiState.categories.isNotEmpty()) {
            val cat = uiState.categories.find { it.id == categoryIdResult }
            if (cat != null) {
                viewModel.onEvent(TransactionEvent.SetCategory(cat))
                savedStateHandle?.remove<Long>("selected_category_id")
            }
        }
    }

    TransactionAddContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = { navController.popBackStack() },
        onNavigateToCategorySelect = {
            navController.navigate(Screen.CategorySelect.createRoute(uiState.selectedCategory?.id ?: -1L))
        },
    )
}

@Composable
fun TransactionAddContent(
    uiState: TransactionUiState,
    onEvent: (TransactionEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToCategorySelect: () -> Unit = {},
) {
    var showDateTimePicker by remember { mutableStateOf(false) }

    val isConfirmEnabled = remember(uiState.amount, uiState.memo) {
        (uiState.amount.toLongOrNull() ?: 0L) > 0 && uiState.memo.isNotBlank()
    }

    Scaffold(
        topBar = {
            TransactionAddTopBar(onBack = onNavigateBack)
        },
        containerColor = Surface,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                TypeToggle(
                    type = uiState.type,
                    onTypeChange = { onEvent(TransactionEvent.SetType(it)) },
                )
                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    RecurringRow(
                        isRecurring = uiState.isRecurring,
                        type = uiState.type,
                        dayOfMonth = uiState.date.dayOfMonth,
                        onToggle = { onEvent(TransactionEvent.ToggleRecurring) },
                    )
                    InputFieldsGroup(
                        amount = uiState.amount,
                        memo = uiState.memo,
                        type = uiState.type,
                        onAmountChange = { onEvent(TransactionEvent.SetAmount(it)) },
                        onMemoChange = { onEvent(TransactionEvent.SetMemo(it)) },
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Income is not categorized — the row only applies to expenses
                        if (uiState.type == TransactionType.EXPENSE) {
                            CategorySelectorRow(
                                selectedCategory = uiState.selectedCategory,
                                onClick = onNavigateToCategorySelect,
                            )
                        }
                        DateSelectorRow(
                            dateTime = uiState.date,
                            type = uiState.type,
                            onClick = { showDateTimePicker = true },
                        )
                    }
                }
            }

            ConfirmButton(
                enabled = isConfirmEnabled,
                onClick = { onEvent(TransactionEvent.Save) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            )
        }
    }

    if (showDateTimePicker) {
        FloatingDateTimePicker(
            initialDateTime = uiState.date,
            onDismiss = { showDateTimePicker = false },
            onConfirm = { dt ->
                onEvent(TransactionEvent.SetDate(dt))
                showDateTimePicker = false
            },
        )
    }
}

@Composable
private fun TransactionAddTopBar(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface)
            .statusBarsPadding()
            .height(56.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(28.dp)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_figma_chevron_left),
                contentDescription = "뒤로",
                tint = TextDefault,
                modifier = Modifier.size(28.dp),
            )
        }
        Text(
            text = "거래 내역 추가",
            color = TextDefault,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 30.sp,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun ConfirmButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ConfirmButtonBg,
            contentColor = Surface,
            disabledContainerColor = TextDescription,
            disabledContentColor = SurfaceVariant,
        ),
        contentPadding = PaddingValues(vertical = 14.dp),
    ) {
        Text("확인", fontSize = 16.sp, fontWeight = FontWeight.Bold, lineHeight = 24.sp)
    }
}

@Composable
private fun InputFieldsGroup(
    amount: String,
    memo: String,
    type: TransactionType,
    onAmountChange: (String) -> Unit,
    onMemoChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        AmountInputField(amount = amount, onAmountChange = onAmountChange)
        MemoInputField(memo = memo, type = type, onMemoChange = onMemoChange)
    }
}

private fun LocalDateTime.coerceToDateRange(start: LocalDate, end: LocalDate): LocalDateTime {
    val date = toLocalDate()
    return when {
        date < start -> LocalDateTime.of(start, toLocalTime())
        date > end -> LocalDateTime.of(end, toLocalTime())
        else -> this
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Floating date-time picker (Dialog-based, all corners rounded, slide-up anim)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun FloatingDateTimePicker(
    initialDateTime: LocalDateTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalDateTime) -> Unit,
) {
    val today = LocalDate.now()
    // Spec 16:172 — no date entry restriction; ±1 year also covers editing
    // future recurring instances (up to 11 months ahead)
    val dates = remember(today) {
        val endDate = today.plusYears(1)
        generateSequence(today.minusYears(1)) { date ->
            date.plusDays(1).takeIf { it <= endDate }
        }.toList()
    }
    val todayIndex = dates.indexOfFirst { it == today }.coerceAtLeast(0)

    var selectedDateIdx by remember {
        mutableIntStateOf(dates.indexOfFirst { it == initialDateTime.toLocalDate() }.let { if (it < 0) todayIndex else it })
    }
    var selectedAmPm by remember { mutableIntStateOf(if (initialDateTime.hour < 12) 0 else 1) }
    var selectedHour by remember {
        mutableIntStateOf(initialDateTime.hour.let { if (it % 12 == 0) 11 else (it % 12) - 1 })
    }
    var selectedMinute by remember { mutableIntStateOf(initialDateTime.minute) }

    val dateLabels = remember(today, dates) {
        dates.map { if (it == today) "오늘" else "${it.monthValue}월 ${it.dayOfMonth}일" }
    }
    val amPmLabels = listOf("오전", "오후")
    val hourLabels = (1..12).map { it.toString() }
    val minuteLabels = (0..59).map { String.format("%02d", it) }

    val offset = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        offset.animateTo(0f, animationSpec = tween(300, easing = FastOutSlowInEasing))
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            val sheetOffset = maxHeight * offset.value

            // scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1F1F1F).copy(alpha = 0.8f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss,
                    ),
            )

            Surface(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp)
                    .width(328.dp)
                    .offset(y = sheetOffset)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { /* consume click */ },
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 24.dp,
                tonalElevation = 0.dp,
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .background(Color(0xFFD2D2D2), RoundedCornerShape(999.dp)),
                    )

                    // Figma 368:1414 — single full-width highlight band behind all wheel columns
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(232.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .background(BrandLight, RoundedCornerShape(8.dp)),
                        )
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(0.dp),
                        ) {
                            WheelColumn(
                                items = dateLabels,
                                selectedIndex = selectedDateIdx,
                                onSelected = { selectedDateIdx = it },
                                modifier = Modifier.weight(1f),
                                highlightColor = Color.Transparent,
                                textAlign = Alignment.CenterHorizontally,
                            )
                            WheelColumn(
                                items = amPmLabels,
                                selectedIndex = selectedAmPm,
                                onSelected = { selectedAmPm = it },
                                modifier = Modifier.weight(1f),
                                highlightColor = Color.Transparent,
                            )
                            WheelColumn(
                                items = hourLabels,
                                selectedIndex = selectedHour,
                                onSelected = { selectedHour = it },
                                modifier = Modifier.weight(1f),
                                highlightColor = Color.Transparent,
                            )
                            WheelColumn(
                                items = minuteLabels,
                                selectedIndex = selectedMinute,
                                onSelected = { selectedMinute = it },
                                modifier = Modifier.weight(1f),
                                highlightColor = Color.Transparent,
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFEEEEEE),
                                contentColor = TextDefault,
                            ),
                        ) {
                            Text("닫기", fontSize = 16.sp, fontWeight = FontWeight.Bold, lineHeight = 24.sp)
                        }
                        Button(
                            onClick = {
                                val date = dates[selectedDateIdx]
                                val hour24 = when {
                                    selectedAmPm == 0 && hourLabels[selectedHour] == "12" -> 0
                                    selectedAmPm == 1 && hourLabels[selectedHour] != "12" -> hourLabels[selectedHour].toInt() + 12
                                    else -> hourLabels[selectedHour].toInt()
                                }
                                onConfirm(LocalDateTime.of(date, LocalTime.of(hour24, selectedMinute)))
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NavigationOn),
                        ) {
                            Text("완료", fontSize = 16.sp, fontWeight = FontWeight.Bold, lineHeight = 24.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun WheelColumn(
    items: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    highlightColor: Color = Color(0xFFF3F4F6),
    textAlign: Alignment.Horizontal = Alignment.CenterHorizontally,
) {
    val listHeight = 232.dp
    val itemHeight = 32.dp
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = selectedIndex.coerceAtLeast(0)
    )
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val centerIndex by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val center = (info.viewportStartOffset + info.viewportEndOffset) / 2
            info.visibleItemsInfo.minByOrNull { kotlin.math.abs((it.offset + it.size / 2) - center) }?.index
        }
    }
    LaunchedEffect(listState) {
        snapshotFlow { centerIndex }.distinctUntilChanged().collect { idx ->
            if (idx != null && idx in items.indices) onSelected(idx)
        }
    }
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress to centerIndex }.distinctUntilChanged().collect { (isScrolling, idx) ->
            if (!isScrolling && idx != null && idx in items.indices) {
                onSelected(idx)
            }
        }
    }
    LaunchedEffect(selectedIndex) {
        if (!listState.isScrollInProgress) {
            listState.animateScrollToItem(selectedIndex.coerceIn(items.indices))
        }
    }

    Box(
        modifier = modifier.height(listHeight),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(highlightColor, RoundedCornerShape(8.dp))
        )
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = textAlign,
            contentPadding = PaddingValues(vertical = (listHeight - itemHeight) / 2),
            flingBehavior = snapFlingBehavior,
        ) {
            items(items.size) { index ->
                val isSel = index == (centerIndex ?: selectedIndex)
                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth()
                        .clickable { onSelected(index) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = items[index],
                        color = if (isSel) TextDefault else TextDescription,
                        fontSize = if (isSel) 18.sp else 16.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared UI components (used by TransactionDetailScreen as well)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun TypeToggle(type: TransactionType, onTypeChange: (TransactionType) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFEEEEEE))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        listOf(TransactionType.EXPENSE to "지출", TransactionType.INCOME to "수입").forEach { (t, label) ->
            val isSel = type == t
            val selBg = if (t == TransactionType.EXPENSE) Color(0xFFEE3D3D) else Color(0xFF60D551)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .then(if (isSel) Modifier.background(selBg) else Modifier)
                    .clickable { onTypeChange(t) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    color = if (isSel) Color.White else Color(0xFFAFAFAF),
                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                )
            }
        }
    }
}

@Composable
internal fun RecurringRow(
    isRecurring: Boolean,
    type: TransactionType,
    dayOfMonth: Int,
    onToggle: () -> Unit,
) {
    var showTooltip by remember { mutableStateOf(false) }
    val label = if (type == TransactionType.EXPENSE) "정기 지출로 등록" else "정기 수입으로 등록"

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(
                if (isRecurring) R.drawable.ic_figma_checkbox_on else R.drawable.ic_figma_checkbox_off,
            ),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier
                .size(24.dp)
                .clickable { onToggle() },
        )
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 14.sp, lineHeight = 21.sp, color = OnSurface, fontWeight = FontWeight.Medium)
        Box {
            Icon(
                painter = painterResource(R.drawable.ic_figma_help),
                contentDescription = "도움말",
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { showTooltip = !showTooltip },
            )
            if (showTooltip) {
                Popup(
                    alignment = Alignment.CenterStart,
                    // Tail sits at the bubble's vertical center — keep y at 0 so the
                    // tail points exactly at the help icon's center
                    offset = androidx.compose.ui.unit.IntOffset(
                        x = with(LocalDensity.current) { 24.dp.roundToPx() },
                        y = 0,
                    ),
                    onDismissRequest = { showTooltip = false },
                    properties = PopupProperties(focusable = true),
                ) {
                    Row(
                        modifier = Modifier
                            .width(168.dp)
                            .height(48.dp)
                            .clip(TooltipBubbleShape)
                            .background(CardPrimary)
                            .padding(start = 18.dp, top = 6.dp, end = 6.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "매달 ${dayOfMonth}일에 자동 등록할 수 있어요.",
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = "×",
                            color = Color.White,
                            fontSize = 20.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .padding(start = 2.dp)
                                .clickable { showTooltip = false },
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun AmountInputField(amount: String, onAmountChange: (String) -> Unit) {
    val style = TextStyle(fontFamily = Pretendard, fontSize = 22.sp, fontWeight = FontWeight.Bold, lineHeight = 33.sp, color = OnSurface)
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(35.dp)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.weight(1f)) {
                if (amount.isEmpty()) {
                    Text("금액을 입력해 주세요.", style = style.copy(color = Color(0xFFAFAFAF)))
                }
                BasicTextField(
                    value = amount,
                    onValueChange = { v -> onAmountChange(v.filter { it.isDigit() }) },
                    textStyle = style,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = ThousandSeparatorTransformation,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("원", style = style)
                Icon(
                    painter = painterResource(
                        if (amount.isNotEmpty()) R.drawable.ic_figma_cancel_active else R.drawable.ic_figma_cancel_inactive,
                    ),
                    contentDescription = "지우기",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable(enabled = amount.isNotEmpty()) { onAmountChange("") },
                )
            }
        }
        HorizontalDivider(color = Color(0xFF272727), thickness = 1.dp)
    }
}

@Composable
internal fun MemoInputField(memo: String, type: TransactionType, onMemoChange: (String) -> Unit) {
    val style = TextStyle(fontFamily = Pretendard, fontSize = 22.sp, fontWeight = FontWeight.Bold, lineHeight = 33.sp, color = OnSurface)
    val placeholder = if (type == TransactionType.EXPENSE) "지출처를 입력해 주세요." else "수입처를 입력해 주세요."
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(35.dp)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.weight(1f)) {
                if (memo.isEmpty()) Text(placeholder, style = style.copy(color = Color(0xFFAFAFAF)))
                BasicTextField(
                    value = memo,
                    onValueChange = onMemoChange,
                    textStyle = style,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
            Icon(
                painter = painterResource(
                    if (memo.isNotEmpty()) R.drawable.ic_figma_cancel_active else R.drawable.ic_figma_cancel_inactive,
                ),
                contentDescription = "지우기",
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(24.dp)
                    .clickable(enabled = memo.isNotEmpty()) { onMemoChange("") },
            )
        }
        HorizontalDivider(color = Color(0xFF272727), thickness = 1.dp)
    }
}

private object TooltipBubbleShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val radius = with(density) { 8.dp.toPx() }
        val tailWidth = with(density) { 11.dp.toPx() }
        val tailHeight = with(density) { 12.dp.toPx() }
        val tailCenterY = size.height / 2f
        val right = size.width
        val bottom = size.height

        val path = Path().apply {
            moveTo(tailWidth + radius, 0f)
            lineTo(right - radius, 0f)
            quadraticTo(right, 0f, right, radius)
            lineTo(right, bottom - radius)
            quadraticTo(right, bottom, right - radius, bottom)
            lineTo(tailWidth + radius, bottom)
            quadraticTo(tailWidth, bottom, tailWidth, bottom - radius)
            lineTo(tailWidth, tailCenterY + tailHeight / 2f)
            lineTo(0f, tailCenterY)
            lineTo(tailWidth, tailCenterY - tailHeight / 2f)
            lineTo(tailWidth, radius)
            quadraticTo(tailWidth, 0f, tailWidth + radius, 0f)
            close()
        }
        return Outline.Generic(path)
    }
}

@Composable
internal fun CategorySelectorRow(selectedCategory: Category?, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(BrandLight)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "카테고리 선택",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 21.sp,
            color = OnSurface,
            modifier = Modifier.weight(1f),
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                selectedCategory?.name ?: "미분류",
                color = CardPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 21.sp,
            )
            Icon(
                painter = painterResource(R.drawable.ic_figma_chevron_right),
                contentDescription = null,
                tint = TextDefault,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
internal fun DateSelectorRow(dateTime: LocalDateTime, type: TransactionType, onClick: () -> Unit) {
    val label = if (type == TransactionType.EXPENSE) "지출일시" else "수입일시"
    val dateText = "${dateTime.monthValue}월 ${dateTime.dayOfMonth}일 ${String.format("%02d:%02d", dateTime.hour, dateTime.minute)}"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(BrandLight)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 21.sp,
            color = OnSurface,
            modifier = Modifier.weight(1f),
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                dateText,
                color = CardPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 21.sp,
            )
            Icon(
                painter = painterResource(R.drawable.ic_figma_chevron_right),
                contentDescription = null,
                tint = TextDefault,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────────────────────

private object ThousandSeparatorTransformation : VisualTransformation {
    override fun filter(text: androidx.compose.ui.text.AnnotatedString): TransformedText {
        val original = text.text
        if (original.isEmpty()) return TransformedText(text, OffsetMapping.Identity)

        val formatted = original.toLongOrNull()?.let { "%,d".format(it) } ?: original

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                var digits = 0
                for (i in formatted.indices) {
                    if (formatted[i] != ',') {
                        if (digits == offset) return i
                        digits++
                    }
                }
                return formatted.length
            }
            override fun transformedToOriginal(offset: Int): Int =
                formatted.substring(0, offset.coerceAtMost(formatted.length)).count { it != ',' }
        }
        return TransformedText(androidx.compose.ui.text.AnnotatedString(formatted), offsetMapping)
    }
}

@Preview(showBackground = true, name = "SCR-03 거래 내역 추가")
@Composable
private fun TransactionAddContentPreview() {
    AXAssignmentTheme {
        TransactionAddContent(
            uiState = TransactionUiState(isLoading = false),
            onEvent = {},
            onNavigateBack = {},
        )
    }
}
