package app

import app.random.FileRandom
import app.random.IRandom
import app.random.StandardRandom
import app.util.Cities
import java.io.File
import java.text.DecimalFormat
import java.util.*
import kotlin.math.floor
import kotlin.math.ln

const val MU = 0.01
const val PHI = 0.5
const val MAX_ITERATIONS = 10000
const val MAX_TESTED_CANDIDATES = 80
const val MAX_ACCEPTED_CANDIDATES = 20

data class Candidate(val solution: List<Int>, val cost: Int)

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
    private var initialTemperature = 0.0

    fun run() {
        var currentSolution = this.generateInitialSolution()
        var currentCost = this.cities.getCost(currentSolution)
        var bestSolution = currentSolution
        var bestCost = currentCost
        var temperature = this.initialTemperature
        var iterations = 0
        var bestIteration = 0
        var testedCandidates = 0
        var acceptedCandidates = 0
        var coolingCount = 0

        while(iterations < MAX_ITERATIONS) {
            while(iterations < MAX_ITERATIONS && testedCandidates < MAX_TESTED_CANDIDATES && acceptedCandidates < MAX_ACCEPTED_CANDIDATES) {
                iterations++
                testedCandidates++
                println("ITERACION: $iterations")

                val (candidateSolution, candidateCost) = this.generateCandidateSolution(currentSolution)
                val delta = candidateCost - currentCost
                val exponential = Math.pow(Math.E, -delta / temperature)

                println("\tDELTA: $delta")
                println("\tTEMPERATURA: ${temperature.toOutput()}")
                println("\tVALOR DE LA EXPONENCIAL: ${exponential.toOutput()}")

                if(this.random.next() < exponential || delta < 0) {
                    currentSolution = candidateSolution
                    currentCost = candidateCost
                    acceptedCandidates++

                    if(currentCost < bestCost) {
                        bestSolution = currentSolution
                        bestCost = currentCost
                        bestIteration = iterations
                    }

                    println("\tSOLUCION CANDIDATA ACEPTADA")
                }

                println("\tCANDIDATAS PROBADAS: $testedCandidates, ACEPTADAS: $acceptedCandidates\n")
            }

            if(iterations < MAX_ITERATIONS) {
                coolingCount++
                temperature = this.initialTemperature / (1 + coolingCount)
                testedCandidates = 0
                acceptedCandidates = 0

                println("============================\nENFRIAMIENTO: $coolingCount\n============================")
                println("TEMPERATURA: ${temperature.toOutput()}\n")
            } else {
                println("\nMEJOR SOLUCION: ")
                println("\tRECORRIDO: ${bestSolution.toOutput()} ")
                println("\tFUNCION OBJETIVO (km): $bestCost")
                println("\tITERACION: $bestIteration")
                println("\tmu = $MU, phi = $PHI")
            }
        }
    }

    private fun generateInitialSolution(): List<Int> {
        val result = mutableListOf<Int>()

        for(i in 1 until this.cities.citiesCount) {
            result.add((1 until this.cities.citiesCount).filter { !result.contains(it) }.minBy { this.cities.getDistance(result.lastOrNull() ?: 0, it) }!!)
        }

        this.initialTemperature = (MU / -ln(PHI)) * this.cities.getCost(result)

        println("SOLUCION INICIAL:")
        println("\tRECORRIDO: ${result.toOutput()} ")
        println("\tFUNCION OBJETIVO (km): ${this.cities.getCost(result)}")
        println("\tTEMPERATURA INICIAL: ${this.initialTemperature.toOutput()}\n")

        return result
    }

    private fun generateCandidateSolution(currentSolution: List<Int>): Candidate {
        val result = currentSolution.toMutableList()
        val a = this.random.next()
        val cityIndexToMove = floor(a * (this.cities.citiesCount - 1)).toInt()
        var bestCost = Int.MAX_VALUE
        var bestPosition = 0

        val city = result.removeAt(cityIndexToMove)

        for(i in (0..result.size) - cityIndexToMove) {
            result.add(i, city)
            val currentCost = this.cities.getCost(result)

            if(currentCost < bestCost) {
                bestCost = currentCost
                bestPosition = i
            }

            result.removeAt(i)
        }

        result.add(bestPosition, city)

        println("\tINDICE CIUDAD: $cityIndexToMove")
        println("\tCIUDAD: $city")
        println("\tINDICE INSERCION: $bestPosition")
        println("\tRECORRIDO: ${result.toOutput()} ")
        println("\tFUNCION OBJETIVO (km): $bestCost")

        return Candidate(result, bestCost)
    }

    private fun Double.toOutput(): String? {
        val format = DecimalFormat.getInstance(Locale.US)
        format.maximumFractionDigits = 6
        format.minimumFractionDigits = 6
        format.isGroupingUsed = false

        return format.format(this)
    }

    private fun List<Int>.toOutput() = this.toString().replace("[","").replace("]","").replace(",","")
}