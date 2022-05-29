package com.example.w_rds_pp

import org.junit.Assert.*
import org.junit.Test


class ListPartitionTest {
    @Test
    fun flattenShouldEqualWhatBeforePartition() {
        val list = (('a'..'z') + ('A'..'Z')).toList()
        assertEquals(list, list.partition(5).flatten())
    }

    @Test
    fun afterPartitionNoElementShouldBeLongerThanMaxSize(){
        for(maxSize in (1..10) + (300..500)) {
            val randomList = randomIntList(maxSize + 1, maxSize * 10)
            randomList.partition(maxSize).forEach {
                assertTrue(it.size <= maxSize)
            }
        }
    }

    @Test
    fun rangeSumTest(){
        val first = 1
        val last = 100
        val n = last
        val list = (first..last).toList()
        val sum: Int = (first + last) * n / 2
        for(maxSize in 1..last + 1) {
            val actualSum = list.partition(maxSize).sumOf { it.sum() }
            assertEquals(actualSum, sum)
        }
    }

    @Test
    fun empty(){
        for(i in 1..5){
            assertEquals(listOf(emptyList<Any>()), emptyList<Any>().partition(i))
        }
    }

    @Test
    fun massTests() {
        for(i in 1..100){
            val maxSize = 9
            var list = randomIntList(0, maxSize)
            assertEquals("<maxsize", list.partition(maxSize), listOf(list))

            list = randomIntList(maxSize, maxSize + 1)
            assertEquals("=maxsize", list.partition(maxSize), listOf(list))

            list = randomIntList(0, maxSize + 1)
            assertEquals("<=maxsize", list.partition(maxSize), listOf(list))
        }
    }
}