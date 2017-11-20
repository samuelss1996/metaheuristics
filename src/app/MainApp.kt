package app

import app.random.FileRandom
import app.random.IRandom
import app.random.StandardRandom
import java.io.File
import kotlin.system.exitProcess

const val TOTAL_ITERATIONS = 10000

var citiesCount = 0
var distancesFile = ""
var distancesMatrix = arrayOf(arrayOf(0))
val tabooList = mutableListOf<Pair<Int, Int>>()
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

    var bestSolution = generateInitialSolution()
    var currentSolution = bestSolution
    var iterationsWithoutImprovement = 0

    for(i in 1..TOTAL_ITERATIONS) {
        println("ITERACION: $i")

        currentSolution = generateBestNeighbor(currentSolution)

        if(getCost(currentSolution) < getCost(bestSolution)) {
            bestSolution = currentSolution
            iterationsWithoutImprovement = 0
        }

        println("\tRECORRIDO: ${currentSolution.toString().replace("[", "").replace("]", "").replace(",", "")} ")
        println("\tCOSTE (km): ${getCost(currentSolution)}")
        println("\tITERACIONES SIN MEJORA: $iterationsWithoutImprovement")
        println("\tLISTA TABU:")

        tabooList.forEach { println("\t${it.first} ${it.second}")}

        println()

        iterationsWithoutImprovement++
    }
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

    println("RECORRIDO INICIAL")
    println("\tRECORRIDO: ${result.toString().replace("[","").replace("]","").replace(",","")} ")
    println("\tCOSTE (km): ${getCost(result)}\n")

    return result
}

fun generateBestNeighbor(solution: List<Int>): List<Int> {
    var bestCost = Int.MAX_VALUE
    var bestNeighbor = solution
    var bestI = 0
    var bestJ = 0

    for(i in 1 until citiesCount - 1) {
        for(j in 0 until i) {
            val currentNeighbor = solution.toMutableList()
            currentNeighbor[i] = currentNeighbor[j].also { currentNeighbor[j] = currentNeighbor[i] }

            val currentCost = getCost(currentNeighbor)

            if(currentCost < bestCost && !tabooList.contains(Pair(i, j))) {
                bestNeighbor = currentNeighbor
                bestCost = currentCost

                bestI = i
                bestJ = j
            }
        }
    }

    tabooList.add(Pair(bestI, bestJ))
    if(tabooList.size > citiesCount) {
        tabooList.removeAt(0)
    }

    println("\tINTERCAMBIO: ($bestI, $bestJ)")

    return bestNeighbor
}

fun getDistance(city1: Int, city2: Int): Int = distancesMatrix[city1][city2]

fun getCost(solution: List<Int>): Int {
    return getDistance(0, solution.first()) + (1 until solution.size).map { getDistance(solution[it - 1], solution[it]) }
            .sum() + getDistance(solution.last(), 0)
}

fun loadFile() {
    val distancesDynamicMatrix = mutableListOf<MutableList<Int>>()
    distancesDynamicMatrix.add(mutableListOf())

    File(distancesFile).readLines().forEach { line ->
        val row = mutableListOf<Int>()

        distancesDynamicMatrix.add(row)
        line.split("\t").forEach { row.add(it.toInt()) }
    }

    citiesCount = distancesDynamicMatrix.size
    distancesMatrix = Array(distancesDynamicMatrix.size, {i -> Array(distancesDynamicMatrix.size, {j -> distancesDynamicMatrix.getOrNull(i)?.getOrNull(j) ?: 0})})
}