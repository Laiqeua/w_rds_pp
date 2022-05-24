package com.example.w_rds_pp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.View

// todo gm len(minor) should == len(major)

class GMView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    var gm: GM = GM("_p_ _p_ _ q_ e_r__", "aqb cqa h ph rhegt")

    var onLetterSelected: (Char, Int) -> Int = { c, i ->
        Log.d(TAG, "onLetterSelected: c=${c}, i=${i}")
    }
    private val minorPaint = Paint()
    private val majorPaint = Paint()
    private val underlinePaint = Paint()

    private val majorLetterSize = 90
    private val minorLetterSize = 60

    private val lineSpacing = 30
    private val xMargin = 50

    private val majorMinorYSpace = 20

    private val charSpace = 5


    init {
        isFocusable = true
        isFocusableInTouchMode = true

        minorPaint.textSize = 60f
        minorPaint.color = Color.BLUE

        majorPaint.textSize = 90f
        minorPaint.color = Color.GREEN
    }

    private fun processText(text: DS, maxCharactersInLine: Int) : List<List<DS>> {
        fun splitWordIntoWords(word: DS): List<DS> =
            if(word.size > maxCharactersInLine)
                (listOf(word.subList(0, maxCharactersInLine))
                        + splitWordIntoWords(word.subList(maxCharactersInLine - 1, word.size)))
            else listOf(word)

        // todo move to util
        fun <T> List<T>.split(condition: (T) -> Boolean): List<List<T>> {
            val result = mutableListOf<List<T>>()
            var current = mutableListOf<T>()
            for(it in this) {
                if(condition(it)){
                    if(current.isNotEmpty()){
                        result.add(current)
                        current = mutableListOf()
                    }
                } else {
                    current.add(it)
                }
            }
            if(current.isNotEmpty()){
                result.add(current)
            }
            return result
        }

        val words = text
            .split { p -> p.first == ' ' }
            .flatMap { splitWordIntoWords(it) }

        val lines = mutableListOf<List<DS>>()

        var nCharactersInCurrentLine = -1
        var currentLine = mutableListOf<DS>()
        for(word in words){
            if(nCharactersInCurrentLine + 1 + word.size > maxCharactersInLine) {
                lines.add(currentLine)
                currentLine = mutableListOf()
                nCharactersInCurrentLine = -1
            }
            nCharactersInCurrentLine += 1 + word.size
            currentLine.add(word)
        }
        if(currentLine.isNotEmpty()){
            lines.add(currentLine)
        }
        return lines
    }

    private fun drawText(canvas: Canvas) {
        val maxNCharInLine: Int = (canvas.width - xMargin - xMargin) / (majorLetterSize + charSpace)

        val majorProcessedText: List<List<DS>> = processText(gm.zp, maxNCharInLine)

        val deltaY = lineSpacing + majorLetterSize + minorLetterSize + majorMinorYSpace

        var x = xMargin.toFloat()
        var y = deltaY.toFloat()

        val deltaX = majorLetterSize + charSpace

        fun printUnderline(){
            val path = Path()
            path.moveTo(x, y + majorLetterSize + majorMinorYSpace)
            path.lineTo(x + majorLetterSize, y + majorLetterSize + majorMinorYSpace)
            canvas.drawPath(path, underlinePaint)
        }

        fun printMajorCharacter(c: Char){
            canvas.drawText(charArrayOf(c), 0, 1, x, y, majorPaint)
        }

        fun printMinorCharacter(c: Char){
            canvas.drawText(charArrayOf(c), 0, 1, x, y + majorLetterSize + majorMinorYSpace, minorPaint)
        }

        fun moveToNewLine() {
            x = xMargin.toFloat()
            y += deltaY
        }

        fun moveToNextCharacter(){
            x += deltaX
        }

        for(line in majorProcessedText) {
            for(word in line) {
                for(ds in word) {
                    printMajorCharacter(ds.first)
                    printMinorCharacter(ds.second)
                    printUnderline()
                    moveToNextCharacter()
                }
                moveToNextCharacter() // space
            }
            moveToNewLine()
        }



    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        drawText(canvas!!)
    }

    companion object {
        val TAG: String = GMView::class.java.name
    }
}