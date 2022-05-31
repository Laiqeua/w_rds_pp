package com.example.w_rds_pp

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.w_rds_pp.databinding.FragmentGameBinding
import java.util.Timer
import java.util.TimerTask

class GameFragment private constructor(): Fragment() {
    private lateinit var gs: MutableGameState

    private lateinit var b: FragmentGameBinding

    private var gmStr: GMStr = emptyList()
        get() = gs.gmStr
        set(value){ field = value; b.gmView.gm = value; gs.gmStr = value }

    var onPuzzleCompleted: () -> Unit = {}

    private lateinit var timer: Timer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val exMsg = "onCreate: Missing required argument ARG_GS_PREF_INFO, GameFragment should be created using GameFragment.instance()"
        arguments ?: throw Exception(exMsg)
        val gspi = arguments!!.getSerializable(ARG_GS_PREF_INFO) as GameStatePrefInfo
        val pref = requireActivity().getSharedPreferences(gspi.name, Activity.MODE_PRIVATE)
        gs = MGS_AutoSaveToSystemPreferences(gspi.key, pref)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        b = FragmentGameBinding.inflate(inflater, container, false)
        b.keyboardView.onClick = ::onKeyboardsKeyClicked
        b.gmView.onLetterSelected = ::onGMCharSelected
        b.removeBtn.setOnClickListener { resetSelected() }
        b.resetBtn.setOnClickListener { resetAll() }

        b.keyboardView.disabledKeys = Alphabets.EN.filter { !gs.lettersToGuess.contains(it) }.toSet()

        b.gmView.gm = gs.gmStr

        b.keyboardView.animationScope = lifecycleScope

        updateTimerView()
        setFixedLetterOnGMView()

        return b.root
    }

    override fun onResume() {
        super.onResume()
        createAndStartTimer()
    }

    override fun onPause() {
        super.onPause()
        stopAndRemoveTimer()
    }

    private fun createAndStartTimer() {
        timer = Timer(false)
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                gs.howLongIsBeingSolvedSec += 1
                updateTimerView()
            }
        }, 1000, 1000)
    }

    private fun stopAndRemoveTimer(){
        timer.cancel()
    }

    private fun onKeyboardsKeyClicked(c: Char){
        if(gs.alreadyUsedChars.contains(c)){
            onGMCharSelected(gmStr.find { it.major == c }!!)
            return
        }
        if(gs.selectedGMChar == null) return
        if (gs.selectedGMChar!!.major == '_' || gs.lettersToGuess.contains(gs.selectedGMChar!!.major)) {
            gmStr = gmStr.map {
                if(it.minor == gs.selectedGMChar!!.minor) {
                    if(it.major != '_'){
                        gs.alreadyUsedChars = gs.alreadyUsedChars.filter { x -> x != it.major }.toHashSet()
                    }
                    it.withMajor(c)
                } else {
                    it
                }
            }
            gs.selectedGMChar = gs.selectedGMChar!!.withMajor(c)
            gs.alreadyUsedChars = gs.alreadyUsedChars + setOf(c)
            // if user fills character we jump to next character to be filled if such exits
            val nextGMChar = gmStr.find { it.i > gs.selectedGMChar!!.i && it.major == '_'}
            if(nextGMChar != null) onGMCharSelected(nextGMChar)
        }

        checkForCompletion()
    }

    private fun onGMCharSelected(gmChar: GMChar) {
        if(gmChar.major == '_' || gs.lettersToGuess.contains(gmChar.major)){
            gs.selectedGMChar = gmChar
            b.gmView.changeHLIdWithCriteria(GMHLDefCatID.CURRENT) { c -> c.minor == gmChar.minor }
        } else {
            gs.selectedGMChar = null
            b.gmView.changeHLIdWithCriteria(GMHLDefCatID.CURRENT) { false }
        }
    }

    private fun resetAll() {
        gmStr = gmStr.map { if (gs.alreadyUsedChars.contains(it.major)) it.withMajor('_') else it }
        gs.alreadyUsedChars = emptySet()
    }

    private fun resetSelected(){
        if(gs.selectedGMChar == null) return
        gs.alreadyUsedChars = gs.alreadyUsedChars.filter { it != gs.selectedGMChar!!.major }.toSet()
        gmStr = gmStr.map { if (it.minor == gs.selectedGMChar!!.minor) it.withMajor('_') else it }
    }

    private fun checkForCompletion() {
        if(gs.isCompleted()) {
            stopAndRemoveTimer()
            onPuzzleCompleted()
        }
    }

    private fun updateTimerView(){
        b.timeView.text = timerFormatter(gs.howLongIsBeingSolvedSec)
    }

    private fun setFixedLetterOnGMView() {
        val fixedCharIndexes = gmStr
            .filter { it.major != '_' && !gs.lettersToGuess.contains(it.major) }
            .map { it.i }.toSet()
        b.gmView.changeHLIdWithCriteria(GMHLDefCatID.SET_FIXED) { fixedCharIndexes.contains(it.i) }
    }

    companion object {
        val TAG: String = GameFragment::class.java.name
        const val ARG_GS_PREF_INFO = "ARG_GS_PREF_INFO"
        fun newInstance(gspi: GameStatePrefInfo) =
            GameFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_GS_PREF_INFO, gspi)
                }
            }
    }
}