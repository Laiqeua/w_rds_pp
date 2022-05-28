package com.example.w_rds_pp

import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs
import kotlin.reflect.full.companionObject
import kotlin.reflect.jvm.isAccessible

class CreateMajorText_Test {
    private val method = GameState::class.companionObject!!.members.find { it.name == "createMajorText" }!!.apply { isAccessible = true }
    private fun createMajorText(text: String, difficulty: Double, alphabet: List<Char>): Pair<String, Set<Char>> {

        return method.call(GameState, text, difficulty, alphabet) as Pair<String, Set<Char>>
    }

    @Test
    fun zeroDifficulty() {
        for(i in 1..50){
            val text = randomString(abs(randomInstance.nextInt()) % 200, ALL_CHARS)
            val (resultText, set) = createMajorText(text, 0.0, Alphabets.EN)
            assertTrue(set.isEmpty())
            assertEquals(text, resultText)
        }
    }

    @Test
    fun oneDifficulty(){
        for(i in 1..50){
            val text = randomString(abs(randomInstance.nextInt()) % 200, ALL_CHARS)
            val expectedText = text.map { if(Alphabets.EN.contains(it)) '_' else it }.joinToString("")
            val (resultText, set) = createMajorText(text, 1.0, Alphabets.EN)
            assertEquals(Alphabets.EN.intersect(text.toSet()), set)
            assertEquals(expectedText, resultText)
        }
    }

}