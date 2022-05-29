package com.example.w_rds_pp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.min

data class GMChar(
    val i: Int,
    val major: Char,
    val minor: Char
){
    fun withMinor(c: Char) = GMChar(i, major, c)
    fun withMajor(c: Char) = GMChar(i, c, minor)
}

typealias GMStr = List<GMChar>
object GMStrHelper {
    fun fromStr(major: String, minor: String): GMStr {
        val n = min(major.length, minor.length)
        val majorStr = major.substring(0, n)
        val minorStr = minor.substring(0, n)
        return majorStr.zip(minorStr).mapIndexed { i, it -> GMChar(i, it.first, it.second) }
    }
}


class GMView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    // todo optimization processedText may be computed to many time

    var gm: GMStr = GMStrHelper.fromStr("_p_ _p_ _ q_ e_r__", "aqb cqa h ph rhegt")
        set(value) {
            field = value
            invalidate()
        }

    var toBeHighlightedByMinor: Set<Char> = setOf()
        set(value) {
            field = value
            invalidate()
        }

    var onLetterSelected: (GMChar) -> Unit = { Log.d(TAG, "onLetterSelected: ds=${it}") }

    private val minorPaint = Paint()
    private val majorPaint = Paint()
    private val majorHLPaint = Paint()

    private val majorLetterSize = 60
    private val minorLetterSize = 40

    private val lineSpacing = 50
    private val xMargin = 77

    private val majorMinorYSpace = 2

    private val charSpace = 6

    private var boxes: List<Box> = listOf()

    init {
        isFocusable = true
        isFocusableInTouchMode = true

        minorPaint.textSize = 40f
        minorPaint.color = Color.BLUE

        majorPaint.textSize = 60f
        majorPaint.color = Color.BLACK

        majorHLPaint.textSize = 60f
        majorHLPaint.color = Color.RED
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        canvas ?: return
        drawText(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = computeHeight(width)
        setMeasuredDimension(width, height)
    }

    private fun computeHeight(width: Int): Int {
        val m = computeMeasurements(width)
        return ((m.processedText.size + 1) * m.deltaY).toInt()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if(event.action == MotionEvent.ACTION_DOWN) {
            val x = event.x
            val y = event.y
            Log.d(TAG, "onTouchEvent: ACTION DOWN, x: ${x}, y: ${y}")
            val boxes = boxes.filter { it.rectangle.isInside(x, y) }
            if(boxes.isEmpty()) {
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
        val m = computeMeasurements(canvas.width)

        var x = xMargin.toFloat()
        var y = m.deltaY

        val boxes = mutableListOf<Box>()
        for(line in m.processedText) {
            for(word in line) {
                for(gmChar in word) {
                    val p = if(toBeHighlightedByMinor.contains(gmChar.minor)) majorHLPaint else majorPaint
                    canvas.printCharacter(gmChar.major, x, y, p)
                    canvas.printCharacter(gmChar.minor, x, y + majorLetterSize + majorMinorYSpace, minorPaint)
                    boxes.add(Box(gmChar, RectF(x, y - majorLetterSize, x + majorLetterSize, y + m.deltaY - lineSpacing)))
                    x += m.deltaX
                }
                x += m.deltaX
            }
            x = xMargin.toFloat()
            y += m.deltaY
        }

        this.boxes = boxes
    }

    private fun Canvas.printCharacter(c: Char, x: Float, y: Float, paint: Paint) {
        val wWidth = paint.measureCharWidth('W')
        val cWidth = paint.measureCharWidth(c)
        val additionalMargin: Float = if(wWidth > cWidth) { (wWidth - cWidth) / 2 } else 0f
        drawText(charArrayOf(c), 0, 1, x + additionalMargin, y, paint)
    }

    private fun computeMeasurements(width: Int): Measurements {
        val maxNCharsInLine: Int = (width - xMargin - xMargin) / (majorLetterSize + charSpace)
        val processedText: List<List<GMStr>> = processText(gm, maxNCharsInLine)
        val deltaY: Float = (lineSpacing + majorLetterSize + minorLetterSize + majorMinorYSpace).toFloat()
        val deltaX: Float = (majorLetterSize + charSpace).toFloat()

        return Measurements(maxNCharsInLine, processedText, deltaY, deltaX)
    }

    private data class Box(val gmChar: GMChar, val rectangle: RectF)

    private data class Measurements(
        val maxNCharsInLine: Int,
        val processedText: List<List<GMStr>>,
        val deltaY: Float,
        val deltaX: Float
    )

    companion object {
        val TAG: String = GMView::class.java.name
    }
}