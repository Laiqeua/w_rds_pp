package com.example.w_rds_pp

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