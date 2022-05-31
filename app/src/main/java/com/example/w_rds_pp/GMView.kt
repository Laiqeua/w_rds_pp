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

data class GMTheme(
    val majorCharWidth: Float = 60f,
    val minorCharWidth: Float = 40f,
    val lineSpacing: Float = 50f,
    val xMargin: Float = 77f,
    val majorMinorYSpace: Float = 2f,
    val charSpace: Float = 6f,
    val majorCharColor: Int = Color.rgb(110, 110, 110),
    val minorCharColor: Int = Color.BLUE,
    val majorDefaultHLCharColor: Int = Color.RED,
    val majorHLCharColors: Map<GMHighlightCategoryID, Int> = mapOf(
        GMHLDefCatID.CURRENT to Color.RED,
        GMHLDefCatID.SET_FIXED to Color.BLACK
    ),
)

typealias GMHighlightCategoryID = String

object GMHLDefCatID {
    const val CURRENT: String = "current"
    const val SET_FIXED: String = "fixed"
}

class GMView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    var gm: GMStr = GMStrHelper.fromStr("_p_ _p_ _ q_ e_r__", "aqb cqa h ph rhegt")
        set(value) {
            field = value
            invalidate()
        }

    var tm: GMTheme = GMTheme()
        set(value) {
            field = value
            applyTheme()
            invalidate()
        }

    private val _hlIdWithCriteria: MutableMap<GMHighlightCategoryID, (GMChar) -> Boolean> = mutableMapOf()
    val hlIdWithCriteria: Map<GMHighlightCategoryID, (GMChar) -> Boolean> = _hlIdWithCriteria
    fun changeHLIdWithCriteria(id: GMHighlightCategoryID, criteria: ((GMChar) -> Boolean)?) {
        if(criteria == null) _hlIdWithCriteria.remove(id) else _hlIdWithCriteria[id] = criteria
        invalidate()
    }

    var onLetterSelected: (GMChar) -> Unit = { Log.d(TAG, "onLetterSelected: ds=${it}") }

    private val minorPaint = Paint()
    private val majorPaint = Paint()

    private var boxes: List<Box> = listOf()

    private var cachedComputations: Computations? = null
    private var cachedComputationsDependencies: ComputationDependencies? = null

    init {
        isFocusable = true
        isFocusableInTouchMode = true
        applyTheme()
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
        val m = computeForWidth(width)
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
        val words = text
            .split { p -> p.major == ' ' }
            .flatMap { it.partition(maxCharactersInLine) }

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
        val m = computeForWidth(canvas.width)

        var x = tm.xMargin
        var y = m.deltaY

        val boxes = mutableListOf<Box>()
        for(line in m.processedText) {
            for(word in line) {
                for(gmChar in word) {
                    val Q = _hlIdWithCriteria.toList().filter { (_, f) -> f(gmChar) }
                    val p = if (Q.isNotEmpty()) createHLPaint(Q[0].first) else majorPaint
                    canvas.printCharacter(gmChar.major, x, y, p)
                    canvas.printCharacter(gmChar.minor, x, y + tm.majorCharWidth + tm.majorMinorYSpace, minorPaint)
                    val boxRect = RectF(x, y - tm.majorCharWidth, x + tm.majorCharWidth, y + m.deltaY - tm.lineSpacing)
                    boxes.add(Box(gmChar, boxRect))
                    x += m.deltaX
                }
                x += m.deltaX
            }
            x = tm.xMargin
            y += m.deltaY
        }

        this.boxes = boxes
    }

    private fun computeForWidth(width: Int): Computations {
        if(cachedComputationsDependencies?.eq(width, this) == true && cachedComputations != null)
            return cachedComputations!!

        val maxNCharsInLine: Int = ((width - tm.xMargin - tm.xMargin) / (tm.majorCharWidth + tm.charSpace)).toInt()
        val processedText: List<List<GMStr>> = processText(gm, maxNCharsInLine)
        val deltaY: Float = tm.lineSpacing + tm.majorCharWidth + tm.minorCharWidth + tm.majorMinorYSpace
        val deltaX: Float = tm.majorCharWidth + tm.charSpace

        val computations = Computations(maxNCharsInLine, processedText, deltaY, deltaX)
        cachedComputations = computations
        cachedComputationsDependencies = ComputationDependencies(width, gm, tm)

        return computations
    }

    private fun applyTheme() {
        minorPaint.textSize = findFontSize(tm.minorCharWidth, FindFontSize.WIDTH, paint = minorPaint)
        minorPaint.color = tm.minorCharColor

        val majorTextSize = findFontSize(tm.majorCharWidth, FindFontSize.WIDTH, paint = majorPaint)
        majorPaint.textSize = majorTextSize
        majorPaint.color = tm.majorCharColor
    }

    private fun createHLPaint(hlId: GMHighlightCategoryID): Paint =
        Paint().apply {
            isAntiAlias = true
            textSize = findFontSize(tm.majorCharWidth, FindFontSize.WIDTH)
            color = tm.majorHLCharColors.getOrDefault(hlId, tm.majorDefaultHLCharColor)
        }

    private data class Box(val gmChar: GMChar, val rectangle: RectF)

    private data class Computations (
        val maxNCharsInLine: Int,
        val processedText: List<List<GMStr>>,
        val deltaY: Float,
        val deltaX: Float
    )

    private data class ComputationDependencies(
        val width: Int,
        val gmStr: GMStr,
        val theme: GMTheme
    ) {
        fun eq(width: Int, view: GMView): Boolean = this.width == width && view.gm == gmStr && view.tm == theme
    }

    companion object {
        val TAG: String = GMView::class.java.name
    }
}