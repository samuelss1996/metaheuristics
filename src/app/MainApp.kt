package app

import app.random.FileRandom
import app.random.IRandom
import app.random.StandardRandom
import app.util.Cities
import java.io.File
import java.text.DecimalFormat
import java.util.*
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

const val POPULATION_SIZE = 100
const val MAX_ITERATIONS = 1000
const val REPRODUCTION_PROBABILITY = 0.9
const val MUTATION_PROBABILITY = 0.01

enum class GenerationStrategy{RANDOM, GREEDY}
data class Descendants(val son1: Individual, val son2: Individual)
data class BestSolution(val iteration: Int, val individual: Individual)
data class Individual(val path: MutableList<Int>, var cost: Int) {
    override fun toString(): String {
        return "{FUNCION OBJETIVO (km): ${this.cost}, RECORRIDO: ${this.path.toOutput()} }"
    }
}

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

    fun run() {
        var population = mutableListOf<Individual>()

        println("POBLACION INICIAL")

        for(i in 0 until POPULATION_SIZE) {
            val generationStrategy = if(i < POPULATION_SIZE / 2) GenerationStrategy.RANDOM else GenerationStrategy.GREEDY
            val individual = this.generateIndividual(generationStrategy)

            population.add(individual)
            println("INDIVIDUO $i = $individual")
        }

        var bestSolution = BestSolution(1, population.first())

        for(iteration in 1..MAX_ITERATIONS) {
            println("\nITERACION: $iteration, SELECCION")
            val tournamentsWinners = this.celebrateTournaments(population)

            println("\nITERACION: $iteration, CRUCE ")
            val newGeneration = this.celebrateBreedingSeason(tournamentsWinners)

            println("ITERACION: $iteration, MUTACION")
            this.travelToTheNuclearPlant(newGeneration)

            population = population.sortedBy { it.cost }.take(2).sortedByDescending { it.cost }.toMutableList()
            population.addAll(newGeneration.sortedBy { it.cost })

            println("\nITERACION: $iteration, REEMPLAZO")
            population.forEachIndexed { index, individual -> println("INDIVIDUO $index = $individual")}

            bestSolution = minOf(bestSolution, BestSolution(iteration, population.minBy { it.cost }!!), compareBy { it.individual.cost })
        }

        println("\n\nMEJOR SOLUCION: ")
        println("RECORRIDO: ${bestSolution.individual.path.toOutput()} ")
        println("FUNCION OBJETIVO (km): ${bestSolution.individual.cost}")
        println("ITERACION: ${bestSolution.iteration}")
    }

    private fun generateIndividual(generationStrategy: GenerationStrategy): Individual {
        val path = mutableListOf<Int>()

        when(generationStrategy) {
            GenerationStrategy.GREEDY -> {
                path.add(floor(this.random.next() * (this.cities.citiesCount - 1) + 1).toInt())

                for(i in 1 until this.cities.citiesCount - 1) {
                    path.add((1 until this.cities.citiesCount).filter { !path.contains(it) }.minBy { this.cities.getDistance(path.last(), it) }!!)
                }
            }

            GenerationStrategy.RANDOM -> {
                while(path.size < this.cities.citiesCount - 1) {
                    var current = floor(this.random.next() * (this.cities.citiesCount - 1)).toInt()

                    do {
                        current %= (this.cities.citiesCount - 1)
                        current++
                    } while(path.contains(current))

                    path.add(current)
                }
            }
        }

        return Individual(path, this.cities.getCost(path))
    }

    private fun celebrateTournaments(population: List<Individual>): List<Individual> {
        val winners = mutableListOf<Individual>()

        for(tournament in 0 until population.size - 2) {
            val participant1 = floor(this.random.next() * population.size).toInt()
            val participant2 = floor(this.random.next() * population.size).toInt()
            val winner = minOf(participant1, participant2, compareBy { population[it].cost })

            winners.add(Individual(population[winner].path.toMutableList(), population[winner].cost))
            println("\tTORNEO $tournament: $participant1 $participant2 GANA $winner")
        }

        return winners
    }

    private fun celebrateBreedingSeason(population: List<Individual>): List<Individual> {
        val newGeneration = mutableListOf<Individual>()

        for(i in 0 until population.size step 2) {
            val random = this.random.next()
            val parent1 = population[i]
            val parent2 = population[i + 1]

            println("\tCRUCE: ($i, ${i + 1}) (ALEATORIO: ${random.toOutput()})")
            println("\t\tPADRE: = $parent1")
            println("\t\tPADRE: = $parent2")

            if (random < REPRODUCTION_PROBABILITY) {
                val (son1, son2) = this.generateDescendants(parent1, parent2)

                newGeneration.add(son1)
                newGeneration.add(son2)
            } else {
                println("\t\tNO SE CRUZA\n")

                newGeneration.add(parent1)
                newGeneration.add(parent2)
            }
        }

        return newGeneration
    }

    private fun generateDescendants(parent1: Individual, parent2: Individual): Descendants {
        val cutPoint1 = floor(this.random.next() * (this.cities.citiesCount - 1)).toInt()
        val cutPoint2 = floor(this.random.next() * (this.cities.citiesCount - 1)).toInt()
        val son1Path = MutableList(this.cities.citiesCount - 1, {0})
        val son2Path = MutableList(this.cities.citiesCount - 1, {0})

        for(son in 0 until 2) {
            val min = min(cutPoint1, cutPoint2)
            val max = max(cutPoint1, cutPoint2)
            val outerSequence = (max + 1 until this.cities.citiesCount - 1) + (0 until min)
            val currentSon = if(son == 0) son1Path else son2Path
            val sameParent = if(son == 0) parent1.path else parent2.path
            val otherParent = if(son == 1) parent1.path else parent2.path

            for(i in (min..max) + outerSequence) {
                when(i) {
                    in min..max -> currentSon[i] = sameParent[i]
                    else -> currentSon[i] = (outerSequence + (min..max)).map { otherParent[it] }.first { !currentSon.contains(it) }
                }
            }
        }

        val result = Descendants(Individual(son1Path, this.cities.getCost(son1Path)), Individual(son2Path, this.cities.getCost(son2Path)))

        println("\t\tCORTES: ($cutPoint1, $cutPoint2)")
        println("\t\tHIJO: = ${result.son1}")
        println("\t\tHIJO: = ${result.son2}\n")

        return result
    }

    private fun travelToTheNuclearPlant(population: List<Individual>) {
        population.forEachIndexed { index, individual ->
            println("\tINDIVIDUO $index")
            println("\tRECORRIDO ANTES: ${individual.path.toOutput()} ")

            individual.path.forEachIndexed { position, _ ->
                val random = this.random.next()
                print("\t\tPOSICION: $position (ALEATORIO ${random.toOutput()}) ")

                if(random < MUTATION_PROBABILITY) {
                    val swapWith = floor(this.random.next() * (this.cities.citiesCount - 1)).toInt()

                    individual.path[position] = individual.path[swapWith].also { individual.path[swapWith] = individual.path[position] }
                    individual.cost = this.cities.getCost(individual.path)
                    println("INTERCAMBIO CON: $swapWith")
                } else {
                    println("NO MUTA")
                }
            }

            println("\tRECORRIDO DESPUES: ${individual.path.toOutput()} \n")
        }
    }
}

private fun Double.toOutput(): String? {
    val format = DecimalFormat.getInstance(Locale.US)
    format.maximumFractionDigits = 6
    format.minimumFractionDigits = 6
    format.isGroupingUsed = false

    return format.format(this)
}

private fun List<Int>.toOutput() = this.toString().replace("[","").replace("]","").replace(",","")