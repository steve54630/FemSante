package com.audreyRetournayDiet.femSante.data.entities

data class AppUser(
    val id: String,
    val lifetimeAccess: Boolean,
    val email: String,
    val password: String,
)
