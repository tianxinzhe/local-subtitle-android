package com.lemonsubtitle.ui.navigation

sealed class Screen(val route: String) {
    data object Studio : Screen("studio")
    data object SubtitleEdit : Screen("subtitle_edit")
    data object ModelManager : Screen("model_manager")
    data object Settings : Screen("settings")
}
