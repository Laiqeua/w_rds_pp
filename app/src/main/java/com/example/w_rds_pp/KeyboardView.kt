package com.example.w_rds_pp

import android.content.Context
import android.graphics.*
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
}

class KeyboardView(context: Context, attrs: AttributeSet): View(context, attrs) {
    var keyboardSpecification: KeyboardSpecification = KeyboardSpecifications.QWERTY

    var onClick: (Char) -> Unit = { c ->
        Log.d(TAG, "onClick(default): c = $c")
    }

    private val keyBGPaint = Paint()
    private val keyTextPaint = Paint()
    private val margin = 20f

    private val keyHeightToItsWidth = 1.6f
    private val spaceBetweenKeysToWidth = 0.01f

    private val keyTextSizeToKeySize = 0.8f

    private var fontSizeAdjustedFor: Pair<Float, Float> = Pair(-1f, -1f)
    private var boxes: List<Pair<Char, RectF>> = emptyList()

    init {
        keyBGPaint.color = Color.LTGRAY
//        keyTextPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        keyTextPaint.isAntiAlias = true
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

    private fun drawKeyboard(canvas: Canvas) {
        val kw: Float = canvas.width - (2 * margin)
        val betweenKeysSpace: Float = kw * spaceBetweenKeysToWidth

        val maxNOfKeysInRow = keyboardSpecification.maxOf { it.size }

        val keyWidth = (kw - ((betweenKeysSpace - 1) * maxNOfKeysInRow)) / maxNOfKeysInRow
        val keyHeight: Float = keyWidth * keyHeightToItsWidth

        adjustFontSize(keyWidth * keyTextSizeToKeySize, keyHeight * keyTextSizeToKeySize)

        val boxes: MutableList<Pair<Char, RectF>> = mutableListOf()

        var y: Float = margin + keyHeight + betweenKeysSpace
        for(row in keyboardSpecification) {
            val additionalRowMargin = (maxNOfKeysInRow - row.size) * (keyWidth + betweenKeysSpace) / 2
            var x = margin + additionalRowMargin
            for (c in row) {
                val rect = RectF(x, (y - keyHeight), (x + keyWidth), y)
                boxes.add(Pair(c, rect))
                canvas.drawRect(rect, keyBGPaint)

                val additionalKeyTextXMargin = (keyWidth - keyTextPaint.measureCharWidth(c)) / 2
                val additionalKeyTextYMargin = (keyHeight - keyTextPaint.measureCharHeight(c)) / 2
                canvas.drawText(charArrayOf(c), 0, 1, x + additionalKeyTextXMargin, y - additionalKeyTextYMargin, keyTextPaint)

                x += keyWidth + betweenKeysSpace
            }
            y += keyHeight + betweenKeysSpace
        }

        this.boxes = boxes
    }

    private fun adjustFontSize(keyWidth: Float, keyHeight: Float){
        if(fontSizeAdjustedFor.first == keyWidth && fontSizeAdjustedFor.second == keyHeight) {
            return
        }
        keyTextPaint.textSize = min(findFontSize(keyWidth, FindFontSize.WIDTH), findFontSize(keyHeight, FindFontSize.HEIGHT))
    }

    companion object {
        val TAG: String = KeyboardView::class.java.name
    }
}