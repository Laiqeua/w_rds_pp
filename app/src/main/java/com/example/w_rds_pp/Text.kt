package com.example.w_rds_pp

data class GM(
    val majorText: String,
    val minorText: String,
) {
    val zp: DS = majorText.zip(minorText)
}

typealias DS = List<Pair<Char, Char>>
