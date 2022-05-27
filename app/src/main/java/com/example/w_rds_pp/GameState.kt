package com.example.w_rds_pp

import android.content.SharedPreferences
import com.example.w_rds_pp.GameStateHelper.serialize
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.lang.Double.max
import java.lang.Double.min
import java.util.*


interface GameState {
    val originalText: String
    val gmStr: GMStr
    val lettersToGuess: Set<Char>
    val alreadyUsedChars: Set<Char>
    val selectedGMChar: GMChar?
}

data class GameStateImpl(
    override val originalText: String,
    override val gmStr: GMStr,
    override val lettersToGuess: Set<Char>,
    override val alreadyUsedChars: Set<Char>,
    override val selectedGMChar: GMChar?
) : GameState

object GameStateHelper {
    fun new(text: String, difficulty: Double = 0.5, alphabet: List<Char> = Alphabets.EN): GameState {
        val normalizedText = text.uppercase()
        fun createMajorText(): Pair<String, Set<Char>> {
            val normalizedDifficulty = max(0.0, min(difficulty, 1.0))
            val Q = alphabet.intersect(normalizedText.asIterable().toSet())
            val nOfLetterToBeHidden = (Q.size * normalizedDifficulty).toInt()
            // todo check how shuffled deals with equal distribution
            val lettersToBeHidden = Q.shuffled().subList(0, nOfLetterToBeHidden).toHashSet()
            val majorText = normalizedText.map { if(lettersToBeHidden.contains(it)) '_' else it }.joinToString("")
            return majorText to lettersToBeHidden
        }
        fun createMinorText(): String {
            val m: Map<Char, Char> = alphabet.zip(alphabet.shuffled()).toMap()
            return normalizedText.map { m.getOrDefault(it, it) }.joinToString("")
        }
        val (major, lettersToGuess) = createMajorText()
        val minor = createMinorText()
        return GameStateImpl(normalizedText, GMStrHelper.fromStr(major, minor), lettersToGuess, emptySet(), null)
    }

    fun GameState.serialize(): String = GsonInstance.toJson(this)
    fun deserializeGameState(s: String): GameState = GsonInstance.fromJson(s, GameStateImpl::class.java)

    fun GameState.isCompleted(): Boolean =
        gmStr.map { it.major }.joinToString("") == originalText

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
    private val gs: GameState

    init {
        val serializedGS = pref.getString(prefKey, null)
        if(serializedGS == null) {
            gs = GameStateHelper.new("")
            update()
        } else {
            gs = GameStateHelper.deserializeGameState(serializedGS)
        }
    }

    override var originalText: String = gs.originalText
        set(value) { field = value; update() }
    override var gmStr: GMStr = gs.gmStr
        set(value) { field = value; update() }
    override var lettersToGuess: Set<Char> = gs.lettersToGuess
        set(value) { field = value; update() }
    override var alreadyUsedChars: Set<Char> = gs.alreadyUsedChars
        set(value) { field = value; update() }
    override var selectedGMChar: GMChar? = gs.selectedGMChar
        set(value) { field = value; update() }

    fun update() = pref.edit().putString(prefKey, serialize()).apply()
}
