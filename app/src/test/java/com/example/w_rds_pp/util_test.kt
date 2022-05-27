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
