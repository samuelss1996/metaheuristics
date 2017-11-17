package app

import app.random.FileRandom
import app.random.IRandom
import app.random.StandardRandom
import java.io.File
import kotlin.system.exitProcess

var citiesCount = 0
var distancesFile = ""
val distancesMap = mutableMapOf<Set<Int>, Int>()
var random: IRandom = StandardRandom()

fun main(args: Array<String>) {
    when(args.size) {
        1 -> distancesFile = args[0]
        2 -> {
            distancesFile = args[0]
            random = FileRandom(File(args[1]))
        }
        else -> {
            println("Argumentos:")
            println("\t- 1 [Obligatorio].- El fichero del que leer las distancias entre ciudades")
            println("\t- 2 [Opcional].- El fichero del que leer los números aleatorios")

            exitProcess(0)
        }
    }

    run()
}

fun run() {
    loadFile()

    var currentSolution = generateInitialSolution()
    var solutionIndex = 0

    do {
        val bestNeighbor = currentSolution
        val bestNeighborCost = getCost(bestNeighbor)

        println("SOLUCION S_$solutionIndex -> $currentSolution; ${bestNeighborCost}km")

        val currentNeighbor = generateNeighbors(bestNeighbor, bestNeighborCost)

        if(getCost(currentNeighbor) < getCost(currentSolution)) {
            currentSolution = currentNeighbor
        }

        solutionIndex++
    } while(getCost(currentNeighbor) < bestNeighborCost)

    println("Solución final: 0${currentSolution}0\nDistancia: ${getCost(currentSolution)}km")
}

fun generateInitialSolution(): List<Int> {
    val result = mutableListOf<Int>()

    while(result.size < citiesCount - 1) {
        var current = Math.floor(random.next() * (citiesCount - 1)).toInt()

        do {
            current %= (citiesCount - 1)
            current++
        } while(result.contains(current))

        result.add(current)
    }

    return result
}

fun generateNeighbors(bestNeighbor: List<Int>, bestNeighborCost: Int): List<Int> {
    val generated = mutableSetOf<Pair<Int, Int>>()

    var currentNeighbor = bestNeighbor.toMutableList()
    var currentNeighborCost = getCost(currentNeighbor)
    var neighborIndex = 0

    while(currentNeighborCost >= bestNeighborCost && generated.size < (citiesCount - 1) * (citiesCount - 2) / 2) {
        currentNeighbor = bestNeighbor.toMutableList()

        var index1 = Math.floor(random.next() * (citiesCount - 1)).toInt()
        var index2 = Math.floor(random.next() * (citiesCount - 1)).toInt()

        if(index2 > index1) index1 = index2.also { index2 = index1 }

        var currentIndex1 = index1
        var currentIndex2 = index2

        while(generated.contains(Pair(currentIndex1, currentIndex2)) || currentIndex1 == currentIndex2) {
            currentIndex2++

            if(currentIndex2 >= currentIndex1) {
                currentIndex1 = (currentIndex1 + 1) % (citiesCount - 1)
                currentIndex2 = 0
            }
        }

        generated.add(Pair(currentIndex1, currentIndex2))
        currentNeighbor[currentIndex1] = currentNeighbor[currentIndex2].also { currentNeighbor[currentIndex2] = currentNeighbor[currentIndex1] }
        currentNeighborCost = getCost(currentNeighbor)

        println("\tVECINO V_$neighborIndex -> Intercambio: ($currentIndex1, $currentIndex2); $currentNeighbor; ${currentNeighborCost}km")
        neighborIndex++
    }

    println()
    return currentNeighbor
}

fun getDistance(city1: Int, city2: Int): Int {
    return distancesMap[setOf(if(city1 > city2) city1 else city2, if(city1 < city2) city1 else city2)]!!
}

fun getCost(solution: List<Int>): Int {
    return getDistance(0, solution.first()) + (1 until solution.size).map { getDistance(solution[it - 1], solution[it]) }
            .sum() + getDistance(solution.last(), 0)
}

fun loadFile() {
    File(distancesFile).readLines().forEachIndexed { lineIndex, line ->
        line.split("\t").forEachIndexed { fieldIndex, field ->
            distancesMap.put(setOf(lineIndex + 1, fieldIndex), field.toInt())
            citiesCount = lineIndex + 2
        }
    }
}