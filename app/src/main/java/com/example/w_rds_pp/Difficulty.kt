package com.example.w_rds_pp

enum class Difficulty(
    val d: Double,
){
    EASY(0.51),
    NORMAL(0.79),
    HARD(0.91),
    ULTRA(1.0),
    ;//
    companion object {
        val DEFAULT = ULTRA
    }
}