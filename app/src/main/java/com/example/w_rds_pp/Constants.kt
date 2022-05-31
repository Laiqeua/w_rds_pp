package com.example.w_rds_pp

const val PREF_NAME_GAME_STATE = "GameStatePreferences3"
const val PREF_KEY_CURRENT_GAME_STATE = "CurrentGame"
const val DB_NAME = "WordsAppDatabase5"

data class GameStatePrefInfo(val name: String, val key: String): java.io.Serializable
val CURRENT_GAME_PREF_INFO = GameStatePrefInfo(PREF_NAME_GAME_STATE, PREF_KEY_CURRENT_GAME_STATE)
