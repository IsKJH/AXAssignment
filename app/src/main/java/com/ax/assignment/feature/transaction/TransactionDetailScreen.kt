package com.ax.assignment.feature.transaction

import android.widget.Toast

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ax.assignment.BudgetApplication
import com.ax.assignment.R
import com.ax.assignment.core.component.AppTopBar
import com.ax.assignment.core.component.DeleteConfirmDialog
import com.ax.assignment.core.navigation.Screen
import com.ax.assignment.core.theme.AXAssignmentTheme
import com.ax.assignment.core.theme.BrandLight
import com.ax.assignment.core.theme.CardPrimary
import com.ax.assignment.core.theme.ConfirmButtonBg
import com.ax.assignment.core.theme.DangerRed
import com.ax.assignment.core.theme.HomeExpenseAmount
import com.ax.assignment.core.theme.HomeIncomeAmount
import com.ax.assignment.core.theme.NavigationOn
import com.ax.assignment.core.theme.OnSurfaceVariant
import com.ax.assignment.core.theme.Surface
import com.ax.assignment.core.theme.TextDefault
import com.ax.assignment.core.theme.TextDescription
import com.ax.assignment.domain.model.Category
import com.ax.assignment.domain.model.RecurringScope
import com.ax.assignment.domain.model.Transaction
import com.ax.assignment.domain.model.TransactionType
import java.time.LocalDate
import java.time.LocalDateTime

@Composable
fun TransactionDetailScreen(navController: NavController, transactionId: Long) {
    val app = LocalContext.current.applicationContext as BudgetApplication
    val viewModel: TransactionDetailViewModel = viewModel(factory = TransactionDetailViewModel.factory(app))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(transactionId) {
        viewModel.loadTransaction(transactionId)
    }
    val context = LocalContext.current
    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            Toast.makeText(context, "삭제되었습니다.", Toast.LENGTH_SHORT).show()
            navController.popBackStack(Screen.Home.route, inclusive = false)
        }
    }
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            Toast.makeText(context, "수정되었습니다.", Toast.LENGTH_SHORT).show()
            navController.popBackStack(Screen.Home.route, inclusive = false)
        }
    }

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val categoryIdResult: Long by remember(savedStateHandle) {
        savedStateHandle?.getStateFlow("selected_category_id", -1L) ?: kotlinx.coroutines.flow.MutableStateFlow(-1L)
    }.collectAsStateWithLifecycle()
    LaunchedEffect(categoryIdResult, uiState.categories) {
        if (categoryIdResult >= 0L && uiState.categories.isNotEmpty()) {
            val cat = uiState.categories.find { it.id == categoryIdResult }
            if (cat != null) {
                viewModel.onEvent(TransactionDetailEvent.SetCategory(cat))
                savedStateHandle?.remove<Long>("selected_category_id")
            }
        }
    }

    TransactionDetailContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = { navController.popBackStack() },
        onNavigateToCategorySelect = {
            navController.navigate(Screen.CategorySelect.createRoute(uiState.editCategory?.id ?: -1L))
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailContent(
    uiState: TransactionDetailUiState,
    onEvent: (TransactionDetailEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToCategorySelect: () -> Unit = {},
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showUnsavedDialog by remember { mutableStateOf(false) }
    var showDateTimePicker by remember { mutableStateOf(false) }
    var showRecurringEditSheet by remember { mutableStateOf(false) }
    var showRecurringDeleteSheet by remember { mutableStateOf(false) }
    var showUnregisterDialog by remember { mutableStateOf(false) }

    val isRecurring = uiState.transaction?.isRecurring == true

    BackHandler(enabled = uiState.isEditing) {
        if (uiState.hasChanges) showUnsavedDialog = true
        else onEvent(TransactionDetailEvent.CancelEditing)
    }

    // ── TopBar ──────────────────────────────────────────────────────────────
    // Read mode:  < 상세내역  편집(#559aff)
    // Edit mode:  < 상세내역  삭제(#da2e0b)
    val topBar: @Composable () -> Unit = {
        if (uiState.isEditing) {
            AppTopBar(
                title = "상세내역",
                onBack = {
                    if (uiState.hasChanges) showUnsavedDialog = true
                    else onEvent(TransactionDetailEvent.CancelEditing)
                },
                actions = {
                    TextButton(onClick = {
                        if (isRecurring) showRecurringDeleteSheet = true
                        else showDeleteDialog = true
                    }) {
                        Text(
                            text = "삭제",
                            color = DangerRed,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                        )
                    }
                },
                showDivider = false,
            )
        } else {
            AppTopBar(
                title = "상세내역",
                onBack = onNavigateBack,
                actions = {
                    TextButton(
                        onClick = { onEvent(TransactionDetailEvent.StartEditing) },
                        enabled = uiState.transaction != null,
                    ) {
                        Text(
                            text = "편집",
                            color = NavigationOn,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                        )
                    }
                },
                showDivider = false,
            )
        }
    }

    // ── Body ────────────────────────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface),
    ) {
        topBar()

        when {
            uiState.isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator(color = NavigationOn) }

            uiState.error != null -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) { Text(uiState.error, color = OnSurfaceVariant) }

            uiState.transaction != null -> {
                if (uiState.isEditing) {
                    EditingContent(
                        uiState = uiState,
                        onEvent = onEvent,
                        onSaveClick = {
                            when {
                                // 502:1277 — recurring checkbox was turned off in edit mode
                                isRecurring && !uiState.editIsRecurring -> showUnregisterDialog = true
                                isRecurring -> showRecurringEditSheet = true
                                else -> onEvent(TransactionDetailEvent.SaveEditing())
                            }
                        },
                        onCategoryClick = onNavigateToCategorySelect,
                        onDateClick = { showDateTimePicker = true },
                    )
                } else {
                    ReadOnlyContent(transaction = uiState.transaction)
                }
            }
        }
    }

    // ── Dialogs & Sheets ────────────────────────────────────────────────────
    if (showDeleteDialog) {
        DeleteConfirmDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = { onEvent(TransactionDetailEvent.Delete()) },
        )
    }

    if (showUnsavedDialog) {
        DeleteConfirmDialog(
            title = "저장하지 않고 나가시겠어요?",
            description = "변경 사항이 저장되지 않습니다.",
            confirmText = "나가기",
            onDismiss = { showUnsavedDialog = false },
            onConfirm = {
                showUnsavedDialog = false
                onEvent(TransactionDetailEvent.CancelEditing)
            },
        )
    }

    if (showUnregisterDialog) {
        // 502:1277 — confirm before turning a recurring entry into a normal one
        DeleteConfirmDialog(
            onDismiss = { showUnregisterDialog = false },
            onConfirm = { onEvent(TransactionDetailEvent.UnregisterRecurring) },
            title = "정기 등록을 해제할까요?",
            description = "이후 등록된 내역이 모두 삭제됩니다.",
            confirmText = "등록 해제",
        )
    }

    if (showRecurringEditSheet) {
        RecurringRangeBottomSheet(
            isEdit = true,
            onDismiss = { showRecurringEditSheet = false },
            onSelect = { scope ->
                showRecurringEditSheet = false
                onEvent(TransactionDetailEvent.SaveEditing(scope))
            },
        )
    }

    if (showRecurringDeleteSheet) {
        RecurringRangeBottomSheet(
            isEdit = false,
            onDismiss = { showRecurringDeleteSheet = false },
            onSelect = { scope ->
                showRecurringDeleteSheet = false
                onEvent(TransactionDetailEvent.Delete(scope))
            },
        )
    }



    if (showDateTimePicker) {
        FloatingDateTimePicker(
            initialDateTime = uiState.editDate,
            onDismiss = { showDateTimePicker = false },
            onConfirm = { dt ->
                onEvent(TransactionDetailEvent.SetDate(dt))
                showDateTimePicker = false
            },
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Read-only (368:1001 / 446:1302)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ReadOnlyContent(
    transaction: Transaction,
    modifier: Modifier = Modifier,
) {
    val amountPrefix = if (transaction.type == TransactionType.INCOME) "+" else "-"
    val amountColor = if (transaction.type == TransactionType.INCOME) HomeIncomeAmount else HomeExpenseAmount
    val dateLabel = if (transaction.type == TransactionType.INCOME) "수입일시" else "지출일시"
    val recurringLabel = if (transaction.type == TransactionType.INCOME) "정기 수입" else "정기 지출"

    // 446:1302: 정기 거래 → outer gap=8dp, inner group gap=32dp
    // 368:1001: 일반 거래 → outer gap=24dp (no label, just header + cards)
    if (transaction.isRecurring) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Surface)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, top = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // "정기 지출" / "정기 수입" caption — 14sp Medium #afafaf
            Text(
                text = recurringLabel,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 21.sp,
                color = TextDescription,
            )
            // Main group: header row + info cards (gap=32dp)
            Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
                DetailHeaderRow(
                    memo = transaction.memo,
                    amount = transaction.amount,
                    amountPrefix = amountPrefix,
                    amountColor = amountColor,
                )
                DetailInfoCards(
                    categoryName = transaction.category?.name ?: "미분류",
                    dateLabel = dateLabel,
                    dateValue = transaction.date.toDisplayString(),
                )
            }
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Surface)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, top = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            DetailHeaderRow(
                memo = transaction.memo,
                amount = transaction.amount,
                amountPrefix = amountPrefix,
                amountColor = amountColor,
            )
            DetailInfoCards(
                categoryName = transaction.category?.name ?: "미분류",
                dateLabel = dateLabel,
                dateValue = transaction.date.toDisplayString(),
            )
        }
    }
}

@Composable
private fun DetailHeaderRow(
    memo: String,
    amount: Long,
    amountPrefix: String,
    amountColor: androidx.compose.ui.graphics.Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = memo.ifBlank { "내역 없음" },
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 33.sp,
            color = TextDefault,
        )
        Text(
            text = "$amountPrefix${"%,d".format(amount)}원",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 33.sp,
            color = amountColor,
        )
    }
}

@Composable
private fun DetailInfoCards(
    categoryName: String,
    dateLabel: String,
    dateValue: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DetailInfoCard(label = "카테고리", value = categoryName)
        DetailInfoCard(label = dateLabel, value = dateValue)
    }
}

@Composable
private fun DetailInfoCard(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrandLight, RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 21.sp,
            color = TextDefault,
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 21.sp,
            color = CardPrimary,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Edit mode (368:1211) — 거래추가 화면과 동일 레이아웃, 버튼만 "수정"
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EditingContent(
    uiState: TransactionDetailUiState,
    onEvent: (TransactionDetailEvent) -> Unit,
    onSaveClick: () -> Unit,
    onCategoryClick: () -> Unit,
    onDateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isSaveEnabled = remember(uiState.editAmount, uiState.editMemo) {
        (uiState.editAmount.toLongOrNull() ?: 0L) > 0 && uiState.editMemo.isNotBlank()
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            TypeToggle(
                type = uiState.editType,
                onTypeChange = { onEvent(TransactionDetailEvent.SetType(it)) },
            )
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                RecurringRow(
                    isRecurring = uiState.editIsRecurring,
                    type = uiState.editType,
                    dayOfMonth = uiState.editDate.dayOfMonth,
                    onToggle = { onEvent(TransactionDetailEvent.ToggleRecurring) },
                )
                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    AmountInputField(
                        amount = uiState.editAmount,
                        onAmountChange = { onEvent(TransactionDetailEvent.SetAmount(it)) },
                    )
                    MemoInputField(
                        memo = uiState.editMemo,
                        type = uiState.editType,
                        onMemoChange = { onEvent(TransactionDetailEvent.SetMemo(it)) },
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CategorySelectorRow(
                        selectedCategory = uiState.editCategory,
                        onClick = onCategoryClick,
                    )
                    DateSelectorRow(
                        dateTime = uiState.editDate,
                        type = uiState.editType,
                        onClick = onDateClick,
                    )
                }
            }
        }

        // 수정 버튼 — 하단 고정 (368:1211: #272727 bg, 48dp, 12dp radius, "수정" 16sp Bold white)
        Button(
            onClick = onSaveClick,
            enabled = isSaveEnabled,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ConfirmButtonBg,
                contentColor = Surface,
                disabledContainerColor = TextDescription,
                disabledContentColor = Surface,
            ),
        ) {
            Text(
                text = "수정",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 24.sp,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────

private fun LocalDateTime.toDisplayString(): String =
    "${monthValue}월 ${dayOfMonth}일 ${String.format("%02d:%02d", hour, minute)}"

// ─────────────────────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────────────────────

// ─────────────────────────────────────────────────────────────────────────────
// Recurring range bottom sheet (446:814 수정 / 446:897 삭제)
// ─────────────────────────────────────────────────────────────────────────────

private data class RecurringOption(val iconRes: Int, val label: String)

@Composable
internal fun RecurringRangeBottomSheet(
    isEdit: Boolean,
    onDismiss: () -> Unit,
    onSelect: (RecurringScope) -> Unit,
) {
    val title = if (isEdit) "정기 등록된 내역을 수정할까요?" else "정기 등록된 내역을 삭제할까요?"
    val options = if (isEdit) listOf(
        RecurringScope.THIS_MONTH to RecurringOption(R.drawable.ic_figma_edit_calendar, "이 달만 수정"),
        RecurringScope.THIS_AND_FUTURE to RecurringOption(R.drawable.ic_figma_contract_edit, "이후 내역 모두 수정"),
        RecurringScope.ALL to RecurringOption(R.drawable.ic_figma_edit_document, "전체 수정"),
    ) else listOf(
        RecurringScope.THIS_MONTH to RecurringOption(R.drawable.ic_figma_event_busy, "이 달만 삭제"),
        RecurringScope.THIS_AND_FUTURE to RecurringOption(R.drawable.ic_figma_contract_delete, "이후 내역 모두 삭제"),
        RecurringScope.ALL to RecurringOption(R.drawable.ic_figma_delete, "전체 삭제"),
    )

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
                    ) { },
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
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            Text(
                                text = title,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 30.sp,
                                color = TextDefault,
                            )
                        }
                        options.forEach { (scope, opt) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onSelect(scope) }
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    painter = painterResource(opt.iconRes),
                                    contentDescription = null,
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(24.dp),
                                )
                                Text(
                                    text = opt.label,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 24.sp,
                                    color = TextDefault,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "SCR-04 ReadOnly (368:1001)")
@Composable
private fun DetailReadOnlyPreview() {
    AXAssignmentTheme {
        TransactionDetailContent(
            uiState = TransactionDetailUiState(
                isLoading = false,
                transaction = Transaction(
                    id = 1,
                    amount = 6_300,
                    type = TransactionType.EXPENSE,
                    category = Category(id = 1, name = "식비", emoji = "", colorHex = "#FFAC11", type = TransactionType.EXPENSE),
                    memo = "이디야",
                    date = LocalDate.of(2025, 5, 29).atTime(16, 0),
                    isRecurring = false,
                ),
            ),
            onEvent = {},
            onNavigateBack = {},
        )
    }
}

@Preview(showBackground = true, name = "SCR-04 ReadOnly 정기지출 (446:1302)")
@Composable
private fun DetailReadOnlyRecurringPreview() {
    AXAssignmentTheme {
        TransactionDetailContent(
            uiState = TransactionDetailUiState(
                isLoading = false,
                transaction = Transaction(
                    id = 1,
                    amount = 6_300,
                    type = TransactionType.EXPENSE,
                    category = Category(id = 1, name = "식비", emoji = "", colorHex = "#FFAC11", type = TransactionType.EXPENSE),
                    memo = "이디야",
                    date = LocalDate.of(2025, 5, 29).atTime(16, 0),
                    isRecurring = true,
                ),
            ),
            onEvent = {},
            onNavigateBack = {},
        )
    }
}

@Preview(showBackground = true, name = "SCR-04 Editing (368:1211)")
@Composable
private fun DetailEditingPreview() {
    AXAssignmentTheme {
        TransactionDetailContent(
            uiState = TransactionDetailUiState(
                isLoading = false,
                isEditing = true,
                transaction = Transaction(
                    id = 1,
                    amount = 6_300,
                    type = TransactionType.EXPENSE,
                    category = Category(id = 1, name = "식비", emoji = "", colorHex = "#FFAC11", type = TransactionType.EXPENSE),
                    memo = "이디야",
                    date = LocalDate.of(2025, 5, 29).atTime(16, 0),
                    isRecurring = false,
                ),
                editAmount = "6300",
                editType = TransactionType.EXPENSE,
                editMemo = "이디야",
                editCategory = Category(id = 1, name = "식비", emoji = "", colorHex = "#FFAC11", type = TransactionType.EXPENSE),
                editDate = LocalDate.of(2025, 5, 29).atTime(16, 0),
                editIsRecurring = false,
            ),
            onEvent = {},
            onNavigateBack = {},
        )
    }
}
