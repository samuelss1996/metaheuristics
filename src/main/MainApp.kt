package main

import java.io.File
import java.util.*

val CITIES_COUNT = 10
val DISTANCES_FILE = "distancias_10.txt"

val distancesMap = mutableMapOf<Set<Int>, Int>()

fun main(args: Array<String>) {
    var currentSolution = generateInitialSolution()
    loadFile()

    do {
        val bestNeighbor = currentSolution
        var currentNeighbor = currentSolution

        while(getCost(currentNeighbor) < getCost(bestNeighbor)) {
            currentNeighbor = generateNeighbor(currentSolution) ?: break
        }

        if(getCost(currentNeighbor) < getCost(currentSolution)) {
            currentSolution = currentNeighbor
        }
    } while(getCost(currentNeighbor) < getCost(bestNeighbor))

    println(currentSolution)
    println(getCost(currentSolution))
}

fun generateInitialSolution(): List<Int> {
    val cities = (1 until CITIES_COUNT).toMutableList()
    val result = mutableListOf<Int>()
    val random = Random()

    while(cities.isNotEmpty()) {
        result.add(cities.removeAt(random.nextInt(cities.size)))
    }

    return result
}

fun loadFile() {
    File(DISTANCES_FILE).readLines().forEachIndexed { lineIndex, line ->
        line.split("\t").forEachIndexed { fieldIndex, field ->
            distancesMap.put(setOf(lineIndex + 1, fieldIndex), field.toInt())
        }
    }
}

fun getDistance(city1: Int, city2: Int): Int {
    return distancesMap[setOf(if(city1 > city2) city1 else city2, if(city1 < city2) city1 else city2)]!!
}

fun getCost(solution: List<Int>): Int {
    return getDistance(0, solution.first()) + (1 until solution.size).map { getDistance(solution[it - 1], solution[it]) }
            .sum() + getDistance(solution.last(), 0)
}

fun generateNeighbor(solution: List<Int>): List<Int>? {
    val random = Random()
    var result = solution.toMutableList()

    val(firstIndex, secondIndex) = generateValidSwapIndices(random.nextInt(result.size), random.nextInt(result.size))

    if(firstIndex < CITIES_COUNT) {
        result[firstIndex] = result[secondIndex].also { result[secondIndex] = result[firstIndex] }
        return result
    } else {
        return null
    }
}

fun generateValidSwapIndices(firstRandom: Int, secondRandom: Int): Pair<Int, Int> {
    val generatedIndices = mutableSetOf<Set<Int>>()

    var firstValid = firstRandom
    var secondValid = secondRandom

    while(generatedIndices.contains(setOf(firstValid, secondValid)) || firstValid == secondValid) {
        secondValid++

        if(secondValid >= firstValid) {
            firstValid++
            secondValid = 0
        }
    }

    generatedIndices.add(setOf(firstValid, secondValid))
    return Pair(firstValid, secondValid)
}