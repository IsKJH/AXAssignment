package com.ax.assignment.core.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Statistics : Screen("statistics")
    object TransactionAdd : Screen("transaction/add?periodStart={periodStart}&periodEnd={periodEnd}") {
        fun createRoute(periodStart: String, periodEnd: String) =
            "transaction/add?periodStart=$periodStart&periodEnd=$periodEnd"
    }
    object TransactionEdit : Screen("transaction/edit/{transactionId}") {
        fun createRoute(id: Long) = "transaction/edit/$id"
    }
    object TransactionDetail : Screen("transaction/detail/{transactionId}") {
        fun createRoute(id: Long) = "transaction/detail/$id"
    }
    object Settings : Screen("settings")
    object StartDaySetting : Screen("settings/start-day")
    object Help : Screen("settings/help")
    object SixMonthHistory : Screen("statistics/six-month")
    object CategoryManage : Screen("category/manage")
    object CategorySelect : Screen("category/select?selectedId={selectedId}") {
        fun createRoute(selectedId: Long) = "category/select?selectedId=$selectedId"
    }
}
