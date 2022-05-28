package com.example.w_rds_pp

import android.content.SharedPreferences
import java.lang.Double.max
import java.lang.Double.min

interface GameState {
    val originalText: String
    val gmStr: GMStr
    val lettersToGuess: Set<Char>
    val alreadyUsedChars: Set<Char>
    val selectedGMChar: GMChar?

    fun serialize(): String = GsonInstance.toJson(this)
    fun isCompleted(): Boolean = gmStr.map { it.major }.joinToString("") == originalText

    companion object {
        fun new(text: String, difficulty: Double = 0.5, alphabet: List<Char> = Alphabets.EN): GameState {
            val normalizedText = text.uppercase()
            val (major, lettersToGuess) = GameState.createMajorText(normalizedText, difficulty, alphabet)
            val minor = GameState.createMinorText(normalizedText, alphabet)
            return GameStateImpl(normalizedText, GMStrHelper.fromStr(major, minor), lettersToGuess, emptySet(), null)
        }

        fun deserializeGameState(s: String): GameState = GsonInstance.fromJson(s, GameStateImpl::class.java)

        private fun createMajorText(text: String, difficulty: Double, alphabet: List<Char>): Pair<String, Set<Char>> {
            val normalizedDifficulty = max(0.0, min(difficulty, 1.0))
            val Q = alphabet.intersect(text.asIterable().toSet())
            val nOfLetterToBeHidden = (Q.size * normalizedDifficulty).toInt()
            // todo check how shuffled deals with equal distribution
            val lettersToBeHidden = Q.shuffled().subList(0, nOfLetterToBeHidden).toHashSet()
            val majorText = text.map { if(lettersToBeHidden.contains(it)) '_' else it }.joinToString("")
            return majorText to lettersToBeHidden
        }

        private fun createMinorText(text: String, alphabet: List<Char>): String {
            val m: Map<Char, Char> = alphabet.zip(alphabet.shuffled()).toMap()
            return text.map { m.getOrDefault(it, it) }.joinToString("")
        }

        fun readImmutableGSFromPref(prefKey: String, pref: SharedPreferences): GameState? {
            val serializedGS = pref.getString(prefKey, null) ?: return null
            return GameState.deserializeGameState(serializedGS)
        }
    }
}

data class GameStateImpl(
    override val originalText: String,
    override val gmStr: GMStr,
    override val lettersToGuess: Set<Char>,
    override val alreadyUsedChars: Set<Char>,
    override val selectedGMChar: GMChar?
) : GameState {

}

interface MutableGameState : GameState {
    override var originalText: String
    override var gmStr: GMStr
    override var lettersToGuess: Set<Char>
    override var alreadyUsedChars: Set<Char>
    override var selectedGMChar: GMChar?
}

class MGS_AutoSaveToSystemPreferences(
    private val prefKey: String,
    private val pref: SharedPreferences,
) : MutableGameState {
    private val initGS: GameState = GameState.readImmutableGSFromPref(prefKey, pref) ?: GameState.new("")

    override var originalText: String = initGS.originalText
        set(value) { field = value; update() }
    override var gmStr: GMStr = initGS.gmStr
        set(value) { field = value; update() }
    override var lettersToGuess: Set<Char> = initGS.lettersToGuess
        set(value) { field = value; update() }
    override var alreadyUsedChars: Set<Char> = initGS.alreadyUsedChars
        set(value) { field = value; update() }
    override var selectedGMChar: GMChar? = initGS.selectedGMChar
        set(value) { field = value; update() }

    fun update() = saveToPref(prefKey, pref)

    companion object {
        fun GameState.saveToPref(prefKey: String, pref: SharedPreferences) = pref.edit().putString(prefKey, serialize()).apply()
        fun GameState.saveToPrefNow(prefKey: String, pref: SharedPreferences) = pref.edit().putString(prefKey, serialize()).commit()
    }
}
