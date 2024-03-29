package com.example.w_rds_pp

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.JsonSyntaxException
import java.lang.Double.max
import java.lang.Double.min

interface GameState {
    val quote: Quote
    val gmStr: GMStr
    val lettersToGuess: Set<Char>
    val alreadyUsedChars: Set<Char>
    val selectedGMChar: GMChar?
    val howLongIsBeingSolvedSec: Int
    val difficulty: Difficulty

    fun serialize(): String = GsonInstance.toJson(this)
    fun isCompleted(): Boolean = gmStr.map { it.major }.joinToString("") == quote.quote.uppercase()

    companion object {
        fun new(quote: Quote, difficulty: Difficulty, alphabet: List<Char> = Alphabets.EN): GameState {
            val normalizedText = quote.quote.uppercase()
            val (major, lettersToGuess) = createMajorText(normalizedText, difficulty.d, alphabet)
            val minor = createMinorText(normalizedText, alphabet)
            return GameStateImpl(quote, GMStrHelper.fromStr(major, minor), lettersToGuess, emptySet(), null, 0, difficulty)
        }

        fun deserializeGameState(s: String): GameState? {
            return try {
                GsonInstance.fromJson(s, GameStateImpl::class.java)
            } catch (e: JsonSyntaxException) {
                null
            }
        }

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
            return deserializeGameState(serializedGS)
        }
    }
}

data class GameStateImpl(
    override val quote: Quote,
    override val gmStr: GMStr,
    override val lettersToGuess: Set<Char>,
    override val alreadyUsedChars: Set<Char>,
    override val selectedGMChar: GMChar?,
    override val howLongIsBeingSolvedSec: Int,
    override val difficulty: Difficulty,
) : GameState

interface MutableGameState : GameState {
    override var quote: Quote
    override var gmStr: GMStr
    override var lettersToGuess: Set<Char>
    override var alreadyUsedChars: Set<Char>
    override var selectedGMChar: GMChar?
    override var howLongIsBeingSolvedSec: Int
    override var difficulty: Difficulty
}

class MGS_AutoSaveToSystemPreferences(
    private val prefKey: String,
    private val pref: SharedPreferences,
) : MutableGameState {
    private val initGS: GameState = GameState.readImmutableGSFromPref(prefKey, pref) ?: GameState.new(Quote(null, "Error MGS autosave to pref", "error"), Difficulty.DEFAULT)

    override var quote: Quote = initGS.quote
        set(value) { field = value; update() }
    override var gmStr: GMStr = initGS.gmStr
        set(value) { field = value; update() }
    override var lettersToGuess: Set<Char> = initGS.lettersToGuess
        set(value) { field = value; update() }
    override var alreadyUsedChars: Set<Char> = initGS.alreadyUsedChars
        set(value) { field = value; update() }
    override var selectedGMChar: GMChar? = initGS.selectedGMChar
        set(value) { field = value; update() }
    override var howLongIsBeingSolvedSec: Int = initGS.howLongIsBeingSolvedSec
        set(value) { field = value; update() }
    override var difficulty: Difficulty = initGS.difficulty
        set(value) { field = value; update() }

    fun update() = saveToPref(prefKey, pref)

    companion object {
        fun GameState.saveToPref(prefKey: String, pref: SharedPreferences) = pref.edit().putString(prefKey, serialize()).apply()
        fun GameState.saveToPrefNow(prefKey: String, pref: SharedPreferences) = pref.edit().putString(prefKey, serialize()).commit()
    }
}

fun readGlobalGameStateFromSharedPreferences(pref: SharedPreferences): GameState? =
    GameState.readImmutableGSFromPref(PREF_KEY_CURRENT_GAME_STATE, pref)

fun readGlobalGameStateFromSharedPreferences(activity: Activity): GameState? =
    readGlobalGameStateFromSharedPreferences(activity.getSharedPreferences(PREF_NAME_GAME_STATE, Context.MODE_PRIVATE))


