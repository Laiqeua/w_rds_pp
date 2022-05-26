package com.example.w_rds_pp

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

object Alphabets {
    val EN = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toList()
}

class GameState (
    var originalText: String,
    var gmStr: GMStr,
    var lettersToGuess: Set<Char>,
    var alreadyUsedChars: Set<Char>,
    var selectedGMChar: GMChar? = null,
)

class GameActivity : AppCompatActivity() {
    private lateinit var keyboardView: KeyboardView
    private lateinit var gmView: GMView
    private lateinit var removeButton: Button
    private lateinit var resetButton: Button

    private lateinit var gs: GameState

    private var gm: GMStr = emptyList()
        get() = gs.gmStr
        set(value){
            field = value
            gmView.gm = value
            gs.gmStr = value
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        keyboardView = findViewById(R.id.keyboard_view)
        gmView = findViewById(R.id.gm_view)
        removeButton = findViewById(R.id.remove_btn)
        resetButton = findViewById(R.id.reset_btn)
        keyboardView.onClick = ::onKeyboardsKeyClicked
        gmView.onLetterSelected = ::onGMCharSelected


        val originalText = "Why, dear boy, we don’t send wizards to Azkaban just for blowing up their aunts.".uppercase()

        val minorText = createMinorText(originalText)
        val (majorText, lettersToGuess) = createMajorText(originalText, 0.5)

        gs = GameState(originalText, GMStrHelper.fromStr(majorText, minorText), lettersToGuess, emptySet())
        gmView.gm = gs.gmStr

        keyboardView.disabledKeys = Alphabets.EN.filter { !lettersToGuess.contains(it) }.toSet()
        removeButton.setOnClickListener { resetSelected() }
        resetButton.setOnClickListener { resetAll() }
    }

    private fun onKeyboardsKeyClicked(c: Char){
        if(gs.alreadyUsedChars.contains(c)){
            onGMCharSelected(gm.find { it.major == c }!!)
            return
        }

        if(gs.selectedGMChar == null) return
        if (gs.selectedGMChar!!.major == '_' || gs.lettersToGuess.contains(gs.selectedGMChar!!.major)) {
            gm = gm.map {
                if(it.minor == gs.selectedGMChar!!.minor) {
                    if(it.major != '_'){
                        gs.alreadyUsedChars = gs.alreadyUsedChars.filter { x -> x != it.major }.toSet()
                    }
                    val newIt = it.withMajor(c)
                    gs.selectedGMChar = newIt
                    newIt
                } else {
                    it
                }
            }
            gs.alreadyUsedChars = gs.alreadyUsedChars + setOf(c)
        }

        checkForCompletion()
    }

    private fun onGMCharSelected(gmChar: GMChar) {
        if(gmChar.major == '_' || gs.lettersToGuess.contains(gmChar.major)){
            gs.selectedGMChar = gmChar
            gmView.toBeHighlightedByMinor = setOf(gmChar.minor)
        } else {
            gs.selectedGMChar = null
            gmView.toBeHighlightedByMinor = emptySet()
        }
    }

    private fun resetAll() {
        gm = gm.map { if (gs.alreadyUsedChars.contains(it.major)) it.withMajor('_') else it }
        gs.alreadyUsedChars = emptySet()
    }

    private fun resetSelected(){
        if(gs.selectedGMChar == null) return
        gs.alreadyUsedChars = gs.alreadyUsedChars.filter { it != gs.selectedGMChar!!.major }.toSet()
        gm = gm.map { if (it.minor == gs.selectedGMChar!!.minor) it.withMajor('_') else it }
    }

    private fun checkForCompletion() {
        if(gm.map { it.major }.joinToString("") == gs.originalText) {
            Toast.makeText(applicationContext, "Completed", Toast.LENGTH_LONG).show()
        }
    }

    private fun createMajorText(text: String, difficulty: Double, alphabet: List<Char> = Alphabets.EN): Pair<String, Set<Char>> {
        // todo add validation - difficulty should be in (0, 1]
        // todo learn why intelli recommends to convert to set in order to improve performance
        val Q = alphabet.intersect(text.asIterable().toSet())
        val nOfLetterToBeHidden = (Q.size * difficulty).toInt()
        val lettersToBeHidden = Q.shuffled().subList(0, nOfLetterToBeHidden).toHashSet()
        val majorText = text.map { if(lettersToBeHidden.contains(it)) '_' else it }.joinToString("")
        return majorText to lettersToBeHidden
    }

    private fun createMinorText(text: String, alphabet: List<Char> = Alphabets.EN): String {
        val m: Map<Char, Char> = alphabet.zip(alphabet.shuffled()).toMap()
        return text.map { m.getOrDefault(it, it) }.joinToString("")
    }
}