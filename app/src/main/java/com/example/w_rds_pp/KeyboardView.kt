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
import java.lang.Float.min

typealias KeyboardSpecification = List<List<Char>>

object KeyboardSpecifications {
    val QWERTY: KeyboardSpecification = listOf(
        listOf('Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P'),
        listOf('A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L'),
        listOf('Z', 'X', 'C', 'V', 'B', 'N', 'M')
    )
    val PL: KeyboardSpecification = listOf(
        listOf('Ą', 'Ć', 'Ę', 'Ł', 'Ń', 'Ó', 'Ś', 'Ź', 'Ż'),
    ) + QWERTY
}

class KeyboardView(context: Context, attrs: AttributeSet): View(context, attrs) {
    var keyboardSpecification: KeyboardSpecification = KeyboardSpecifications.QWERTY
    set(value) {
        field = value
        invalidate()
    }

    var disabledKeys: Set<Char> = setOf('K', 'F')
    set(value) {
        field = value
        invalidate()
    }
    var activeKeys: Set<Char> = setOf('A', 'B')
    set(value) {
        field = value
        invalidate()
    }

    var onClick: (Char) -> Unit = { c ->
        Log.d(TAG, "onClick(default): c = $c")
    }

    private class PaintGroup {
        val bg: Paint = Paint()
        val text: Paint = Paint()
    }

    private val keyPG = PaintGroup()
    private val activeKeyPG = PaintGroup()
    private val disabledKeyPG = PaintGroup()

    private fun updateTextSize(value: Float) {
        keyPG.text.textSize = value
        activeKeyPG.text.textSize = value
        disabledKeyPG.text.textSize = value
    }

    private fun findPaintGroup(c: Char) =
        if(disabledKeys.contains(c))
            disabledKeyPG
        else if(activeKeys.contains(c))
            activeKeyPG
        else
            keyPG

    private val marginToWidth = 0.020f

    private val keyHeightToItsWidth = 1.6f
    private val spaceBetweenKeysToWidth = 0.01f

    private val keyTextSizeToKeySize = 0.8f

    private var fontSizeAdjustedFor: Pair<Float, Float> = Pair(-1f, -1f)
    private var boxes: List<Pair<Char, RectF>> = emptyList()

    init {
        // todo why is intellij not showing color picker ?
        disabledKeyPG.bg.color = Color.rgb(220, 220, 220)
        keyPG.bg.color = Color.rgb(180, 180, 180)
        activeKeyPG.bg.color = Color.rgb(160, 160, 160)

        keyPG.text.isAntiAlias = true
        activeKeyPG.text.isAntiAlias = true
        disabledKeyPG.text.isAntiAlias = true

        disabledKeyPG.text.color = Color.rgb(100, 100, 100)
        activeKeyPG.text.color = Color.rgb(50, 50, 50)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // todo it may be improved
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val m = computeMeasures(width)

        val desiredHeight = keyboardSpecification.size * (m.betweenKeysSpace + m.keyHeight) + m.betweenKeysSpace
        setMeasuredDimension(widthMeasureSpec, desiredHeight.toInt())
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if(event.action == MotionEvent.ACTION_DOWN) {
            val x = event.x
            val y = event.y
            Log.d(GMView.TAG, "onTouchEvent: ACTION DOWN, x: ${x}, y: ${y}")
            val boxes = boxes.filter { it.second.isInside(x, y) }
            if(boxes.isEmpty()) {
                Log.d(GMView.TAG, "onTouchEvent: 0 matching")
                return true
            } else if(boxes.size > 1) {
                Log.d(GMView.TAG, "onTouchEvent: >1 matching, choosing first box, all boxes: " + boxes.fold("") { acc, bc -> "$acc$bc; " })
            }
            onClick(boxes[0].first)
        }
        return true
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        canvas?.run { drawKeyboard(canvas) }
    }

    data class Measurements(val margin: Float, val kw: Float, val betweenKeysSpace: Float,
                            val maxNOfKeysInRow: Int, val keyWidth: Float, val keyHeight: Float)

    private fun computeMeasures(width: Int): Measurements {
        val margin = width * marginToWidth
        val kw: Float = width - (2 * margin)
        val betweenKeysSpace: Float = kw * spaceBetweenKeysToWidth

        val maxNOfKeysInRow = keyboardSpecification.maxOf { it.size }

        val keyWidth = (kw - ((betweenKeysSpace - 1) * maxNOfKeysInRow)) / maxNOfKeysInRow
        val keyHeight: Float = keyWidth * keyHeightToItsWidth
        return Measurements(margin, kw, betweenKeysSpace, maxNOfKeysInRow, keyWidth, keyHeight)
    }

    private fun drawKeyboard(canvas: Canvas) {
        val m = computeMeasures(canvas.width)
        adjustFontSize(m.keyWidth * keyTextSizeToKeySize, m.keyHeight * keyTextSizeToKeySize)

        val boxes: MutableList<Pair<Char, RectF>> = mutableListOf()

        var y: Float = m.margin + m.keyHeight + m.betweenKeysSpace
        for(row in keyboardSpecification) {
            val additionalRowMargin = (m.maxNOfKeysInRow - row.size) * (m.keyWidth + m.betweenKeysSpace) / 2
            var x = m.margin + additionalRowMargin
            for (c in row) {
                val pg = findPaintGroup(c)

                val rect = RectF(x, (y - m.keyHeight), (x + m.keyWidth), y)
                boxes.add(Pair(c, rect))
                canvas.drawRect(rect, pg.bg)

                val additionalKeyTextXMargin = (m.keyWidth - pg.text.measureCharWidth(c)) / 2
                val additionalKeyTextYMargin = (m.keyHeight - pg.text.measureCharHeight(c)) / 2
                canvas.drawText(charArrayOf(c), 0, 1, x + additionalKeyTextXMargin, y - additionalKeyTextYMargin, pg.text)

                x += m.keyWidth + m.betweenKeysSpace
            }
            y += m.keyHeight + m.betweenKeysSpace
        }

        this.boxes = boxes
    }

    private fun adjustFontSize(keyWidth: Float, keyHeight: Float){
        if(fontSizeAdjustedFor.first == keyWidth && fontSizeAdjustedFor.second == keyHeight) {
            return
        }
        updateTextSize(min(findFontSize(keyWidth, FindFontSize.WIDTH), findFontSize(keyHeight, FindFontSize.HEIGHT)))
    }

    companion object {
        val TAG: String = KeyboardView::class.java.name
    }
}