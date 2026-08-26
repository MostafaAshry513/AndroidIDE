package com.example.ui.viewmodel

/**
 * Primary Stage representation for the Mobile IDE.
 * Guarantees that only ONE primary workspace tool occupies the screen at any given time,
 * maximizing 100% of the mobile phone screen pixels for the active task.
 */
enum class PrimaryStage(val label: String, val iconName: String) {
    EDITOR("Code", "code"),
    TERMINAL("Terminal", "terminal"),
    WEB_PREVIEW("Preview", "web"),
    EXPLORER("Files", "folder"),
    AI_COPILOT("Copilot", "smart_toy"),
    GIT_DIFF("Diff", "compare"),
    SETTINGS("Settings", "settings"),
    GLOBAL_SEARCH("Search", "search")
}
