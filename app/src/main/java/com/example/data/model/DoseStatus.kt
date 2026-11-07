package com.example.data.model

enum class DoseStatus(val displayName: String) {
    UPCOMING("Upcoming"),
    DUE_NOW("Due Now"),
    TAKEN("Taken"),
    SNOOZED("Snoozed"),
    SKIPPED("Skipped"),
    MISSED("Missed")
}
