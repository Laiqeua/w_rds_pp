package com.example.w_rds_pp

import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import com.google.gson.Gson
import kotlin.math.abs

val GsonInstance = Gson()

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

fun RectF.isInside(x: Float, y: Float): Boolean {
    return x > this.left && x < this.right && y < this.bottom && y > this.top
}

enum class FindFontSize {
    HEIGHT,
    WIDTH
}

fun findFontSize(n: Float, t: FindFontSize, min: Float = 10f, max: Float = 500f, testC: Char = 'W', paint: Paint = Paint()): Float {
    var left = min
    var right = max
    do {
        val textSize = (left + right) / 2
        paint.textSize = textSize
        val m = if(t == FindFontSize.HEIGHT) paint.measureCharHeight(testC) else paint.measureCharWidth(testC).toInt()
        if(m == n.toInt()){
            return textSize
        }
        if(m < n){
            left = textSize
        } else {
            right = textSize
        }
    } while (left < right)
    return left
}

fun Paint.getTextBounds(text: String): Rect {
    val bounds = Rect()
    this.getTextBounds(text, 0, text.length, bounds)
    return bounds
}

fun Paint.measureCharWidth(c: Char): Float = this.measureText("$c")
fun Paint.measureCharHeight(c: Char): Int  {
    val bounds = getTextBounds(String(charArrayOf(c)))
    return abs(bounds.top - bounds.bottom)
}

fun <T> List<T>.partition(maxSize: Int): List<List<T>> =
    if(size > maxSize)
        listOf(subList(0, maxSize)) + subList(maxSize, size).partition(maxSize)
    else listOf(this)
