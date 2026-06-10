package com.ax.assignment.core.theme

import androidx.compose.ui.graphics.Color

// --- Primary ---
val Primary = Color(0xFF3D2ED1)          // Figma 실제값 (#3D2ED1, 진보라)
val PrimaryLight = Color(0xFFE5E0FF)     // Primary 10% alpha 틴트, 카테고리 선택 배경
val PrimaryDark = Color(0xFF2419A8)      // Primary 어두운 버전

// --- Neutral / Surface ---
val Background = Color(0xFFF5F6FA)
val Surface = Color(0xFFFFFFFF)
val SettingsBackground = Color(0xFFEEEEEE)
val SurfaceVariant = Color(0xFFF7F7FA)   // Figma: 입력 배경, 카드 배경 (#F7F7FA)
val OnPrimary = Color(0xFFFFFFFF)
val OnBackground = Color(0xFF1A1A1F)
val OnSurface = Color(0xFF1A1A1F)        // Figma: #1A1A1F
val OnSurfaceVariant = Color(0xFF737380) // Figma: #737380

// --- Semantic ---
val IncomeGreen = Color(0xFF129E75)      // Figma 실제값 (#129E75, 수입 금액 색상)
val IncomeOnCard = Color(0xFFB2FFB2)     // Figma: 카드 위 수입 레이블 (#B2FFB2)
val ExpenseRed = Color(0xFFD9402E)       // Figma 실제값 (#D9402E, 지출 탭/토글 색상)
val DangerRed = Color(0xFFDA2E0B)        // 삭제 버튼 (#DA2E0B)
val WarningOrange = Color(0xFFFF8C42)    // 프로그레스 80~99% 경고

// --- Progress Bar ---
val ProgressNormal = Color(0xFFA69DFF)   // Figma: 0~79% 정상 (#A69DFF)
val ProgressWarning = WarningOrange      // 80~99% 경고
val ProgressExceeded = Color(0xFFEE3D3D) // 100%+ 초과 (#EE3D3D)

// --- Chart ---
val TrackGray = Color(0xFFF7F7FA)        // 바 차트 트랙 배경
val ChartBarColor = Color(0xFF3D2ED1)    // 바 차트 바 색상 (Primary와 동일)
val BalancePositive = Color(0xFF129E75)  // 남은 금액 양수 (IncomeGreen과 동일)

// --- Misc ---
val DividerColor = Color(0xFFDCDEE3)     // Figma: 구분선 (#DCDEE3)
val OutlineSoft = Color(0xFFE0E0E5)      // Figma: 연한 구분선 (#E0E0E5)
val CardPrimary = Color(0xFF559AFF)      // Figma: 카테고리/날짜 선택값 파란 텍스트 (#559AFF)
val BrandLight = Color(0xFFEEF5FF)       // Figma: 카테고리/일시 선택 행 배경 (#EEF5FF)
val CardShadow = Color(0x1A000000)

// --- 하위 호환 별칭 (기존 feature 화면이 사용하는 이름 유지) ---
val ExpenseText = ExpenseRed             // 지출 텍스트 색상 (#D9402E)
val LinkBlue = CardPrimary               // 링크/선택값 파란색 (#338CFF)

// --- Category Colors (도넛 차트, 카테고리 도트) ---
val CategoryColors = listOf(
    Color(0xFF5E92F3), // 식비 — 파랑
    Color(0xFFFF8A65), // 교통 — 주황
    Color(0xFF81C784), // 쇼핑 — 초록
    Color(0xFFBA68C8), // 의료 — 보라
    Color(0xFF4DD0E1), // 여가 — 청록
    Color(0xFFFFD54F), // 주거 — 노랑
    Color(0xFFF06292), // 교육 — 분홍
    Color(0xFF90A4AE), // 미분류 — 회색
)

// --- Splash ---
val SplashGreen = Color(0xFF7BA67E)
val SplashBackground = Color(0xFFF9F7EF)

// --- Icon Gray (사용자 카테고리 수정/삭제 아이콘 배경) ---
val IconGray = Color(0xFFD1D6DE)

// --- CategorySelectedBg (카테고리 선택 배경) ---
val CategorySelectedBg = Color(0xFFE5E0FF)

// --- ConfirmButtonBg (확인/결정 버튼 배경, Figma #272727) ---
val ConfirmButtonBg = Color(0xFF272727)

// --- Latest Home / Navigation targets (Figma 368:676) ---
val TextDefault = Color(0xFF272727)
val TextDescription = Color(0xFFAFAFAF)
val NavigationOn = Color(0xFF559AFF)
val NavigationOff = Color(0xFFAFAFAF)
val FabFill = Color(0xFF559AFF)
val HomeProgressFill = Color(0xFFFFE311)
val HomeExpenseAmount = Color(0xFFEE3D3D)
val HomeIncomeAmount = Color(0xFF60D551)
