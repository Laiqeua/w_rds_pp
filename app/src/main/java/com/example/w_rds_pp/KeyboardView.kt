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
import kotlinx.coroutines.*
import java.lang.Float.min
import java.util.Hashtable


/**
 * Key animation won't work unless you set animationScope
 */
class KeyboardView(context: Context, attrs: AttributeSet): View(context, attrs) {
    var animationScope: CoroutineScope? = null

    var keyboardSpecification: KeyboardSpecification = KeyboardSpecifications.QWERTY
        set(value) { field = value; invalidate() }

    var disabledKeys: Set<Char> = emptySet()
        set(value) { field = value; invalidate() }

    var onClick: (Char) -> Unit = { }

    private class PaintGroup {
        val bg: Paint = Paint()
        val text: Paint = Paint()
    }

    private val keyPG = PaintGroup()
    private val disabledKeyPG = PaintGroup()

    private val marginToWidth = 0.020f

    private val keyHeightToItsWidth = 1.6f
    private val spaceBetweenKeysToWidth = 0.01f

    private val keyTextSizeToKeySize = 0.8f

    private var fontSizeAdjustedFor: Pair<Float, Float> = Pair(-1f, -1f)
    private var boxes: List<Pair<Char, RectF>> = emptyList()

    private val animationStartColor = 180
    private val animationEndColor = 200
    private val animationStepTimeMilliSec = 5L

    private val animations: MutableMap<Char, PaintGroup> = Hashtable()

    init {
        disabledKeyPG.bg.color = Color.rgb(220, 220, 220)
        keyPG.bg.color = Color.rgb(180, 180, 180)

        keyPG.text.isAntiAlias = true
        disabledKeyPG.text.isAntiAlias = true

        disabledKeyPG.text.color = Color.rgb(100, 100, 100)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // todo it may be improved
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val m = computeForWidth(width)

        val desiredHeight = 2 * m.margin + keyboardSpecification.size * (m.betweenKeysSpace + m.keyHeight)
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
            val c = boxes[0].first
            if(!disabledKeys.contains(c)) {
                startAnimationForKey(c)
                onClick(c)
            }
        }
        return true
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        canvas?.run { drawKeyboard(canvas) }
    }

    private data class Computations(val margin: Float, val kw: Float, val betweenKeysSpace: Float,
                                    val maxNOfKeysInRow: Int, val keyWidth: Float, val keyHeight: Float)

    private fun computeForWidth(width: Int): Computations {
        val margin = width * marginToWidth
        val kw: Float = width - (2 * margin)
        val betweenKeysSpace: Float = kw * spaceBetweenKeysToWidth

        val maxNOfKeysInRow = keyboardSpecification.maxOf { it.size }

        val keyWidth = (kw - ((betweenKeysSpace - 1) * maxNOfKeysInRow)) / maxNOfKeysInRow
        val keyHeight: Float = keyWidth * keyHeightToItsWidth
        return Computations(margin, kw, betweenKeysSpace, maxNOfKeysInRow, keyWidth, keyHeight)
    }

    private fun drawKeyboard(canvas: Canvas) {
        val m = computeForWidth(canvas.width)
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

    private fun updateTextSize(value: Float) {
        keyPG.text.textSize = value
        animations.forEach { (_, pg) -> pg.text.textSize = value }
        disabledKeyPG.text.textSize = value
    }

    private fun findPaintGroup(c: Char) =
        if(disabledKeys.contains(c))
            disabledKeyPG
        else if(animations.keys.contains(c))
            animations[c]!!
        else
            keyPG

    private fun createAnimationPaintGroup(): PaintGroup = PaintGroup().apply {
        bg.color = keyPG.bg.color
        text.isAntiAlias = true
        text.textSize = keyPG.text.textSize
    }

    private fun startAnimationForKey(c: Char) {
        if(animations.contains(c)) return
        animationScope ?: return
        animations[c] = createAnimationPaintGroup()
        animationScope!!.launch {
            val range = (animationStartColor..animationEndColor) +
                        (animationStartColor..animationEndColor).reversed()
            val pg = animations[c]!!
            for(i in range) {
                pg.bg.color = Color.rgb(i, i, i)
                invalidate()
                delay(animationStepTimeMilliSec)
            }
            animations.remove(c)
            invalidate()
        }
    }

    companion object {
        val TAG: String = KeyboardView::class.java.name
    }
}