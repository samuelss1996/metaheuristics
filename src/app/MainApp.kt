package app

import app.random.FileRandom
import app.random.IRandom
import app.random.StandardRandom
import app.util.Cities
import app.util.TabooList
import java.io.File

const val TOTAL_ITERATIONS = 10000
const val TABOO_CAPACITY = 100
const val MAX_ITERATIONS_WITHOUT_IMPROVEMENT = 100

fun main(args: Array<String>) {
    when(args.size) {
        1 -> Main(args[0]).run()
        2 -> Main(args[0], FileRandom(File(args[1]))).run()
        else -> {
            println("Argumentos:")
            println("\t- 1 [Obligatorio].- El fichero del que leer las distancias entre ciudades")
            println("\t- 2 [Opcional].- El fichero del que leer los números aleatorios")
        }
    }
}

class Main(distancesFile: String, private val random: IRandom = StandardRandom()) {
    private val cities = Cities(File(distancesFile))
    private val tabooList = TabooList(TABOO_CAPACITY, this.cities.citiesCount - 1)
    private var resetsCount = 0

    fun run() {
        var bestSolution = generateInitialSolution()
        var currentSolution = bestSolution
        var iterationsWithoutImprovement = 0
        var bestIteration = 0

        for(i in 1..TOTAL_ITERATIONS) {
            println("ITERACION: $i")

            currentSolution = generateBestNeighbor(currentSolution)

            if(this.cities.getCost(currentSolution) < this.cities.getCost(bestSolution)) {
                bestSolution = currentSolution
                iterationsWithoutImprovement = 0
                bestIteration = i
            }

            println("\tRECORRIDO: ${currentSolution.toString().replace("[", "").replace("]", "").replace(",", "")} ")
            println("\tCOSTE (km): ${this.cities.getCost(currentSolution)}")
            println("\tITERACIONES SIN MEJORA: $iterationsWithoutImprovement")
            println("\tLISTA TABU:")
            println("$tabooList")

            if(iterationsWithoutImprovement >= MAX_ITERATIONS_WITHOUT_IMPROVEMENT) {
                iterationsWithoutImprovement = 0
                currentSolution = bestSolution
                this.resetsCount++

                this.tabooList.clear()
                println("***************\nREINICIO: ${this.resetsCount}\n***************\n")
            }

            iterationsWithoutImprovement++
        }

        println("\nMEJOR SOLUCION: ")
        println("\tRECORRIDO: ${bestSolution.toString().replace("[", "").replace("]", "").replace(",", "")} ")
        println("\tCOSTE (km): ${this.cities.getCost(bestSolution)}")
        println("\tITERACION: $bestIteration")
    }

    private fun generateInitialSolution(): List<Int> {
        val result = mutableListOf<Int>()

        while(result.size < this.cities.citiesCount - 1) {
            var current = Math.floor(this.random.next() * (this.cities.citiesCount - 1)).toInt()

            do {
                current %= (this.cities.citiesCount - 1)
                current++
            } while(result.contains(current))

            result.add(current)
        }

        println("RECORRIDO INICIAL")
        println("\tRECORRIDO: ${result.toString().replace("[","").replace("]","").replace(",","")} ")
        println("\tCOSTE (km): ${this.cities.getCost(result)}\n")

        return result
    }

    private fun generateBestNeighbor(solution: List<Int>): List<Int> {
        var bestCost = Int.MAX_VALUE
        var bestNeighbor = solution
        var bestI = 0
        var bestJ = 0

        for(i in 1 until this.cities.citiesCount - 1) {
            for(j in 0 until i) {
                val currentNeighbor = solution.toMutableList()
                currentNeighbor[i] = currentNeighbor[j].also { currentNeighbor[j] = currentNeighbor[i] }

                val currentCost = this.cities.getCost(currentNeighbor)

                if(currentCost < bestCost && !this.tabooList.contains(i, j)) {
                    bestNeighbor = currentNeighbor
                    bestCost = currentCost

                    bestI = i
                    bestJ = j
                }
            }
        }

        this.tabooList.add(bestI, bestJ)
        println("\tINTERCAMBIO: ($bestI, $bestJ)")

        return bestNeighbor
    }
}