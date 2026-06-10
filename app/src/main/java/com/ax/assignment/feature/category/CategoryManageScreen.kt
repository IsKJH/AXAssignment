package com.ax.assignment.feature.category

import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.res.painterResource
import com.ax.assignment.R
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface as M3Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ax.assignment.BudgetApplication
import com.ax.assignment.core.component.AppTopBar
import com.ax.assignment.core.component.DeleteConfirmDialog
import com.ax.assignment.core.theme.AXAssignmentTheme
import com.ax.assignment.core.theme.Pretendard
import com.ax.assignment.core.theme.BrandLight
import com.ax.assignment.core.theme.CategorySelectedBg
import com.ax.assignment.core.theme.ConfirmButtonBg
import com.ax.assignment.core.theme.DividerColor
import com.ax.assignment.core.theme.ExpenseRed
import com.ax.assignment.core.theme.FabFill
import com.ax.assignment.core.theme.IconGray
import com.ax.assignment.core.theme.NavigationOn
import com.ax.assignment.core.theme.OnSurface
import com.ax.assignment.core.theme.OnSurfaceVariant
import com.ax.assignment.core.theme.OutlineSoft
import com.ax.assignment.core.theme.Primary
import com.ax.assignment.core.theme.Surface
import com.ax.assignment.core.theme.SurfaceVariant
import com.ax.assignment.core.theme.TextDefault
import com.ax.assignment.core.theme.TextDescription
import com.ax.assignment.domain.model.Category
import com.ax.assignment.domain.model.TransactionType

@Composable
fun CategoryManageScreen(
    navController: NavController,
    isSelectMode: Boolean = false,
    initialSelectedId: Long = -1L,
) {
    val app = LocalContext.current.applicationContext as BudgetApplication
    val viewModel: CategoryViewModel = viewModel(factory = CategoryViewModel.factory(app))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CategoryManageContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = { navController.popBackStack() },
        initialSelectedId = initialSelectedId,
        onCategorySelected = if (isSelectMode) { category ->
            navController.previousBackStackEntry?.savedStateHandle?.set("selected_category_id", category.id)
            navController.popBackStack()
        } else null,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManageContent(
    uiState: CategoryUiState,
    onEvent: (CategoryEvent) -> Unit,
    onNavigateBack: () -> Unit,
    initialSelectedId: Long = -1L,
    onCategorySelected: ((Category) -> Unit)? = null,
) {
    val context = LocalContext.current
    var selectedCategoryId by remember(uiState.categories) {
        val id = if (initialSelectedId >= 0L) initialSelectedId
            else uiState.categories.firstOrNull { it.type == TransactionType.EXPENSE }?.id ?: 0L
        mutableLongStateOf(id)
    }
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<Category?>(null) }

    val expenseCategories = remember(uiState.categories) {
        uiState.categories.filter { it.type == TransactionType.EXPENSE }
    }
    val defaultCategories = expenseCategories.filter { it.isDefault }
    val customCategories = expenseCategories.filter { !it.isDefault }
    val canAddCustom = customCategories.size < CategoryViewModel.MAX_CUSTOM_CATEGORIES

    Scaffold(
        topBar = {
            AppTopBar(
                title = "카테고리",
                onBack = onNavigateBack,
                showDivider = false,
                titleWeight = FontWeight.Bold,
            )
        },
        containerColor = Surface,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CategorySectionTitle("기본")
            defaultCategories.forEach { category ->
                CategoryListItem(
                    category = category,
                    isSelected = category.id == selectedCategoryId,
                    onClick = {
                        if (onCategorySelected != null) {
                            onCategorySelected(category)
                        } else {
                            selectedCategoryId = if (selectedCategoryId == category.id) 0L else category.id
                        }
                    },
                )
            }

            Spacer(Modifier.height(24.dp))
            CategorySectionTitle("내 카테고리")
            customCategories.forEach { category ->
                CategoryListItem(
                    category = category,
                    isSelected = category.id == selectedCategoryId,
                    onClick = {
                        if (onCategorySelected != null) {
                            onCategorySelected(category)
                        } else {
                            selectedCategoryId = if (selectedCategoryId == category.id) 0L else category.id
                        }
                    },
                    onEdit = {
                        editingCategory = category
                        showEditor = true
                    },
                    onDelete = { deleteTarget = category },
                )
            }
            AddCategoryRow(
                enabled = canAddCustom,
                onClick = {
                    if (canAddCustom) {
                        editingCategory = null
                        showEditor = true
                    } else {
                        Toast.makeText(context, "최대 7개까지 추가 가능합니다", Toast.LENGTH_SHORT).show()
                    }
                },
            )
        }
    }

    if (showEditor) {
        CategoryAddSheet(
            categories = uiState.categories,
            editingCategory = editingCategory,
            onDismiss = { showEditor = false },
            onSave = { name ->
                val target = editingCategory
                if (target == null) {
                    onEvent(CategoryEvent.Add(name, "•", "#90A4AE", TransactionType.EXPENSE))
                } else {
                    onEvent(CategoryEvent.Update(target, name))
                }
                showEditor = false
            },
        )
    }

    deleteTarget?.let { category ->
        DeleteConfirmDialog(
            onDismiss = { deleteTarget = null },
            onConfirm = {
                onEvent(CategoryEvent.Delete(category))
                if (selectedCategoryId == category.id) selectedCategoryId = 0L
                // DeleteConfirmDialog 내부에서 onConfirm 후 onDismiss 자동 호출되므로
                // deleteTarget = null 은 onDismiss 에서 처리됨
            },
            title = "카테고리를 삭제할까요?",
        )
    }
}

@Composable
private fun CategorySectionTitle(text: String) {
    Text(
        text = text,
        color = TextDefault,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun CategoryListItem(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
) {
    val borderColor = if (isSelected) NavigationOn else Color(0xFFEEEEEE)
    val background = if (isSelected) BrandLight else Surface
    val contentColor = if (isSelected) NavigationOn else TextDefault

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(background, RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp),
            )
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = category.name,
            color = contentColor,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f),
        )
        if (isSelected) {
            Icon(
                painter = painterResource(R.drawable.ic_figma_check),
                contentDescription = "선택됨",
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp),
            )
        }
        if (onEdit != null && onDelete != null) {
            Spacer(modifier = Modifier.width(20.dp))
            Icon(
                painter = painterResource(R.drawable.ic_figma_border_color),
                contentDescription = "수정",
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = onEdit),
            )
            Spacer(modifier = Modifier.width(20.dp))
            Icon(
                painter = painterResource(R.drawable.ic_figma_cancel_filled),
                contentDescription = "삭제",
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = onDelete),
            )
        }
    }
}

@Composable
private fun AddCategoryRow(
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val textColor = if (enabled) TextDefault else OnSurfaceVariant
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrandLight, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(FabFill, RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_figma_add),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(20.dp),
            )
        }
        Text(
            text = "카테고리추가",
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

// ───────────────────────────────────────────────
// SCR-06: 카테고리 추가 / 수정 바텀시트
// ───────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryAddSheet(
    categories: List<Category>,
    editingCategory: Category?,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var name by remember(editingCategory) { mutableStateOf(editingCategory?.name.orEmpty()) }
    var showUnsavedNotice by remember { mutableStateOf(false) }

    val trimmedName = name.trim()
    val normalizedEditingName = editingCategory?.name?.trim()
    val hasDuplicate = categories.any {
        it.id != editingCategory?.id && it.name.trim().equals(trimmedName, ignoreCase = true)
    }
    val isValid = trimmedName.isNotBlank() && trimmedName.length <= 12 && !hasDuplicate
    val isSaveEnabled = isValid && trimmedName != normalizedEditingName

    // SCR-06 정책: 입력 중 외부 탭/뒤로가기 → 미저장 안내 후 닫기
    val hasUnsavedInput = trimmedName.isNotBlank() && trimmedName != normalizedEditingName.orEmpty()
    val requestDismiss: () -> Unit = {
        if (hasUnsavedInput) showUnsavedNotice = true else onDismiss()
    }

    val focusRequester = remember { FocusRequester() }
    val offset = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        offset.animateTo(0f, animationSpec = tween(300, easing = FastOutSlowInEasing))
        // SCR-06 정책: 시트 진입 시 키보드 자동 활성화
        focusRequester.requestFocus()
    }

    if (showUnsavedNotice) {
        UnsavedCategoryDialog(
            onConfirm = {
                showUnsavedNotice = false
                onDismiss()
            },
        )
    }

    Dialog(
        onDismissRequest = requestDismiss,
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

            // 딤 오버레이
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xCC1F1F1F))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { requestDismiss() },
            )

            // 바텀시트 본체
            M3Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .offset(y = sheetOffset)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { },
                shape = RoundedCornerShape(16.dp),
                color = Surface,
                shadowElevation = 24.dp,
                tonalElevation = 0.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    // 드래그 핸들
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .background(Color(0xFFD2D2D2), RoundedCornerShape(2.dp)),
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // 타이틀
                    Text(
                        text = if (editingCategory == null) "카테고리 추가하기" else "카테고리 수정하기",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDefault,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(Modifier.height(8.dp))

                    // 이름 입력 필드
                    CategoryNameField(
                        name = name,
                        onNameChange = { input -> if (input.length <= 12) name = input },
                        isError = hasDuplicate,
                        focusRequester = focusRequester,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    // 중복 오류 메시지
                    if (hasDuplicate) {
                        Text(
                            text = "이미 있는 카테고리 이름입니다",
                            color = ExpenseRed,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // 저장 버튼
                    Button(
                        onClick = { onSave(trimmedName) },
                        enabled = isSaveEnabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ConfirmButtonBg,
                            contentColor = Surface,
                            disabledContainerColor = TextDescription,
                            disabledContentColor = SurfaceVariant,
                        ),
                    ) {
                        Text("저장", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryNameField(
    name: String,
    onNameChange: (String) -> Unit,
    isError: Boolean,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = remember { FocusRequester() },
) {
    var isFocused by remember { mutableStateOf(false) }

    val borderWidth = if (isFocused || name.isNotEmpty() || isError) 2.dp else 1.dp
    val borderColor = when {
        isError -> ExpenseRed
        isFocused || name.isNotEmpty() -> NavigationOn
        else -> Color(0xFFEEEEEE)
    }

    Row(
        modifier = modifier
            .background(Surface, RoundedCornerShape(8.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(1f)) {
            if (name.isEmpty()) {
                Text(
                    text = "새 카테고리 이름을 입력해 주세요.",
                    color = Color(0xFF898989),
                    fontSize = 16.sp,
                )
            }
            BasicTextField(
                value = name,
                onValueChange = onNameChange,
                singleLine = true,
                textStyle = TextStyle(fontFamily = Pretendard, fontSize = 16.sp, color = TextDefault),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged { isFocused = it.isFocused },
            )
        }
        if (name.isNotEmpty()) {
            Icon(
                painter = painterResource(R.drawable.ic_figma_cancel_filled),
                contentDescription = "지우기",
                tint = Color.Unspecified,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(24.dp)
                    .clickable { onNameChange("") },
            )
        }
    }
}

// SCR-06 정책: 미저장 입력 상태에서 시트를 닫을 때 안내
@Composable
private fun UnsavedCategoryDialog(onConfirm: () -> Unit) {
    Dialog(onDismissRequest = onConfirm) {
        M3Surface(
            shape = RoundedCornerShape(16.dp),
            color = Surface,
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "카테고리가 저장되지 않았어요",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDefault,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, bottom = 32.dp),
                )
                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ConfirmButtonBg,
                        contentColor = Surface,
                    ),
                ) {
                    Text("확인", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun categoryColor(category: Category): Color =
    runCatching { Color(android.graphics.Color.parseColor(category.colorHex)) }
        .getOrDefault(Color(0xFF90A4AE))

// ───────────────────────────────────────────────
// Previews
// ───────────────────────────────────────────────

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CategoryManageContentPreview() {
    AXAssignmentTheme {
        CategoryManageContent(
            uiState = CategoryUiState(
                isLoading = false,
                categories = listOf(
                    Category(1, "식비", "", "#5E92F3", TransactionType.EXPENSE, isDefault = true),
                    Category(2, "교통", "", "#FF8A65", TransactionType.EXPENSE, isDefault = true),
                    Category(3, "쇼핑", "", "#81C784", TransactionType.EXPENSE, isDefault = true),
                    Category(8, "생필품", "", "#90A4AE", TransactionType.EXPENSE),
                ),
            ),
            onEvent = {},
            onNavigateBack = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 400)
@Composable
private fun CategoryAddSheetPreview() {
    AXAssignmentTheme {
        CategoryAddSheet(
            categories = listOf(
                Category(1, "식비", "", "#5E92F3", TransactionType.EXPENSE, isDefault = true),
            ),
            editingCategory = null,
            onDismiss = {},
            onSave = {},
        )
    }
}
