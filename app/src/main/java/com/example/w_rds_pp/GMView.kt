package com.example.w_rds_pp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.example.w_rds_pp.GMStrHelper.withAllHighlightsRemoved
import com.example.w_rds_pp.GMStrHelper.withLettersHighlighted
import kotlin.math.min

data class GMChar(
    val major: Char,
    val minor: Char,
    val highlight: Boolean = false
){
    fun withHighlight(hl: Boolean) = GMChar(major, minor, hl)
}

typealias GMStr = List<GMChar>
object GMStrHelper {
    fun fromStr(major: String, minor: String): GMStr {
        val n = min(major.length, minor.length)
        val majorStr = major.substring(0, n)
        val minorStr = minor.substring(0, n)
        return majorStr.zip(minorStr).map { GMChar(it.first, it.second) }
    }

    fun GMStr.hl(f: (GMChar) -> Boolean) = this.map { it.withHighlight(f(it)) }
    fun GMStr.withAllHighlightsRemoved(): GMStr = hl { false }
    fun GMStr.withLettersHighlighted(letters: Collection<Char>): GMStr = hl { letters.contains(it.major) }
}


class GMView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    var gm: GMStr = GMStrHelper.fromStr("_p_ _p_ _ q_ e_r__", "aqb cqa h ph rhegt").withLettersHighlighted(setOf('p'))
        set(value) {
            field = value
            invalidate()
        }

    var onLetterSelected: (GMChar) -> Unit = { ds ->
        Log.d(TAG, "onLetterSelected: ds=${ds}")
        this.gm = gm.withAllHighlightsRemoved()
                    .withLettersHighlighted(setOf(ds.major))
        Log.d(TAG, "new GM: ${gm}")
    }

    private val minorPaint = Paint()
    private val majorPaint = Paint()
    private val majorHLPaint = Paint()

    private val majorLetterSize = 90
    private val minorLetterSize = 60

    private val lineSpacing = 50
    private val xMargin = 77

    private val majorMinorYSpace = 14

    private val charSpace = 5

    private var boxes: List<Box> = listOf()

    init {
        isFocusable = true
        isFocusableInTouchMode = true

        minorPaint.textSize = 60f
        minorPaint.color = Color.GREEN

        majorPaint.textSize = 90f
        majorPaint.color = Color.BLACK

        majorHLPaint.textSize = 90f
        majorHLPaint.color = Color.BLUE
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        drawText(canvas!!)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if(event.action == MotionEvent.ACTION_DOWN) {
            val x = event.x
            val y = event.y
            Log.d(TAG, "onTouchEvent: ACTION DOWN, x: ${x}, y: ${y}")
            val boxes = boxes.filter { it.rectangle.isInside(x, y) }
            if(boxes.size == 0) {
                Log.d(TAG, "onTouchEvent: 0 matching")
                return true
            } else if(boxes.size > 1) {
                Log.d(TAG, "onTouchEvent: >1 matching, choosing first box, all boxes: " + boxes.fold("") { acc, bc -> "$acc$bc; " })
            }
            onLetterSelected(boxes[0].gmChar)
        }

        return true
    }

    private fun processText(text: GMStr, maxCharactersInLine: Int) : List<List<GMStr>> {
        fun splitWordIntoWords(word: GMStr): List<GMStr> =
            if(word.size > maxCharactersInLine)
                (listOf(word.subList(0, maxCharactersInLine))
                        + splitWordIntoWords(word.subList(maxCharactersInLine - 1, word.size)))
            else listOf(word)

        val words = text
            .split { p -> p.major == ' ' }
            .flatMap { splitWordIntoWords(it) }

        val lines = mutableListOf<List<GMStr>>()

        var nCharactersInCurrentLine = -1
        var currentLine = mutableListOf<GMStr>()
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
        val maxNCharsInLine: Int = (canvas.width - xMargin - xMargin) / (majorLetterSize + charSpace)

        val processedText: List<List<GMStr>> = processText(gm, maxNCharsInLine)

        val deltaY = lineSpacing + majorLetterSize + minorLetterSize + majorMinorYSpace

        var x = xMargin.toFloat()
        var y = deltaY.toFloat()

        val deltaX = majorLetterSize + charSpace

        val boxes = mutableListOf<Box>()

        fun printMajorCharacter(gmChar: GMChar) {
            canvas.drawText(charArrayOf(gmChar.major), 0, 1, x, y, if(gmChar.highlight) majorHLPaint else majorPaint )
        }
        fun printMinorCharacter(c: Char) =
            canvas.drawText(charArrayOf(c), 0, 1, x, y + majorLetterSize + majorMinorYSpace, minorPaint)
        fun moveToNewLine() { x = xMargin.toFloat(); y += deltaY }
        fun moveToNextCharacter() { x += deltaX }
        fun addToBoxesCord(gmChar: GMChar) =
            boxes.add(Box(gmChar, RectF(x, y - majorLetterSize, x + majorLetterSize, y + deltaY - lineSpacing)))

        for(line in processedText) {
            for(word in line) {
                for(gmChar in word) {
                    printMajorCharacter(gmChar)
                    printMinorCharacter(gmChar.minor)
                    addToBoxesCord(gmChar)
                    moveToNextCharacter()
                }
                moveToNextCharacter()
            }
            moveToNewLine()
        }

        this.boxes = boxes
    }

    private data class Box(val gmChar: GMChar, val rectangle: RectF)

    companion object {
        val TAG: String = GMView::class.java.name
    }
}