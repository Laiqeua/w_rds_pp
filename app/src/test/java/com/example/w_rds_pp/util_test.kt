package com.example.w_rds_pp

import kotlin.math.abs
import kotlin.random.Random

val randomInstance = Random(System.currentTimeMillis())

val ALL_CHARS: Array<Char> = (Char.MIN_VALUE..Char.MAX_VALUE).toList().toTypedArray()

fun randomString(length: Int, charTable: Array<Char>): String {
    val b = StringBuilder()
    for(i in 1..length) {
        b.append(charTable[abs(randomInstance.nextInt()) % charTable.size])
    }
    return b.toString()
}

fun <T> randomLengthList(min: Int, until: Int, f: () -> T): List<T> {
    val n = randomInstance.nextInt(min, until)
    val list = mutableListOf<T>()
    for(i in 1..n) {
        list.add(f())
    }
    return list
}

fun randomIntList(min: Int, until: Int): List<Int> = randomLengthList(min, until) { randomInstance.nextInt() }

