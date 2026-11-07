package com.example.data.model

enum class MedicationType(val displayName: String, val iconName: String) {
    TABLET("Tablet", "pill"),
    CAPSULE("Capsule", "capsule"),
    SYRUP("Syrup", "liquid"),
    DROPS("Drops", "droplet"),
    INJECTION("Injection", "syringe"),
    INHALER("Inhaler", "inhaler"),
    CREAM("Cream", "tube"),
    OTHER("Other", "medicine")
}
