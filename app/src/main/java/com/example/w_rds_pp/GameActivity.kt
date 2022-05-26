package com.example.w_rds_pp

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

object Alphabets {
    val EN = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toList()
}

class GameActivity : AppCompatActivity() {
    private lateinit var keyboardView: KeyboardView
    private lateinit var gmView: GMView
    private lateinit var removeButton: Button
    private lateinit var resetButton: Button

    private lateinit var originalText: String
    private var selectedGMChar: GMChar? = null

    private lateinit var lettersToGuess: Set<Char>

    private lateinit var alreadyUsedChars: Set<Char>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        keyboardView = findViewById(R.id.keyboard_view)
        gmView = findViewById(R.id.gm_view)
        removeButton = findViewById(R.id.remove_btn)
        resetButton = findViewById(R.id.reset_btn)
        keyboardView.onClick = ::onKeyboardsKeyClicked
        gmView.onLetterSelected = ::onGMCharSelected

        originalText = "Why, dear boy, we don’t send wizards to Azkaban just for blowing up their aunts.".uppercase()

        val minorText = createMinorText(originalText)
        val (majorText, lettersToGuess) = createMajorText(originalText, 0.5)
        this.lettersToGuess = lettersToGuess
        gmView.gm = GMStrHelper.fromStr(majorText, minorText)

        keyboardView.disabledKeys = Alphabets.EN.filter { !lettersToGuess.contains(it) }.toSet()

        alreadyUsedChars = setOf()

        removeButton.setOnClickListener { resetSelected() }

        resetButton.setOnClickListener { resetAll() }
    }

    private fun onKeyboardsKeyClicked(c: Char){
        if(alreadyUsedChars.contains(c)){
            onGMCharSelected(gmView.gm.find { it.major == c }!!)
            return
        }

        if(selectedGMChar == null) return
        if (selectedGMChar!!.major == '_' || lettersToGuess.contains(selectedGMChar!!.major)) {
            gmView.gm = gmView.gm.map {
                if(it.minor == selectedGMChar!!.minor) {
                    if(it.major != '_'){
                        alreadyUsedChars = alreadyUsedChars.filter { x -> x != it.major }.toSet()
                    }
                    val newIt = it.withMajor(c)
                    selectedGMChar = newIt
                    newIt
                } else {
                    it
                }
            }
            alreadyUsedChars = alreadyUsedChars + setOf(c)
        }

        checkForCompletion()
    }

    private fun onGMCharSelected(gmChar: GMChar) {
        if(gmChar.major == '_' || lettersToGuess.contains(gmChar.major)){
            selectedGMChar = gmChar
            gmView.toBeHighlightedByMinor = setOf(gmChar.minor)
        } else {
            selectedGMChar = null
            gmView.toBeHighlightedByMinor = emptySet()
        }
    }

    private fun resetAll() {
        gmView.gm = gmView.gm.map { if (alreadyUsedChars.contains(it.major)) it.withMajor('_') else it }
        alreadyUsedChars = emptySet()
    }

    private fun resetSelected(){
        if(selectedGMChar == null) return
        alreadyUsedChars = alreadyUsedChars.filter { it != selectedGMChar!!.major }.toSet()
        gmView.gm = gmView.gm.map { if (it.minor == selectedGMChar!!.minor) it.withMajor('_') else it }
    }

    private fun checkForCompletion() {
        if(gmView.gm.map { it.major }.joinToString("") == originalText) {
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