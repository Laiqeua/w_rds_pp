package com.example.w_rds_pp

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.lifecycleScope

// todo try to remove !!s

class GameFragment : Fragment() {
    private lateinit var gs_prefKey: String
    private lateinit var gs: MutableGameState

    private lateinit var keyboardView: KeyboardView
    private lateinit var gmView: GMView
    private lateinit var removeButton: Button
    private lateinit var resetButton: Button

    private var gm: GMStr = emptyList()
        get() = gs.gmStr
        set(value){
            field = value
            gmView.gm = value
            gs.gmStr = value
        }

    var onPuzzleCompleted: () -> Unit = {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            gs_prefKey = it.getString(ARG_PREF_KEY, "q")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val pref = requireActivity().getSharedPreferences(GAME_SATE_PREF_NAME, Activity.MODE_PRIVATE)
        gs = MGS_AutoSaveToSystemPreferences(gs_prefKey, pref)

        val v = inflater.inflate(R.layout.fragment_game, container, false)

        keyboardView = v.findViewById(R.id.keyboard_view)
        gmView = v.findViewById(R.id.gm_view)
        removeButton = v.findViewById(R.id.remove_btn)
        resetButton = v.findViewById(R.id.reset_btn)

        keyboardView.onClick = ::onKeyboardsKeyClicked
        gmView.onLetterSelected = ::onGMCharSelected
        removeButton.setOnClickListener { resetSelected() }
        resetButton.setOnClickListener { resetAll() }

        keyboardView.disabledKeys = Alphabets.EN.filter { !gs.lettersToGuess.contains(it) }.toSet()

        gmView.gm = gs.gmStr

        keyboardView.animationScope = lifecycleScope

        return v
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
        if(gs.isCompleted()) {
            onPuzzleCompleted()
        }
    }

    companion object {
        // todo why intellij do not generate tag automatically ?
        val TAG: String = GameFragment::class.java.name

        const val ARG_PREF_KEY = "gs_PrefKey"

        // todo why java static
        @JvmStatic
        fun newInstance(prefKey: String) =
            GameFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PREF_KEY, prefKey)
                }
            }

    }
}