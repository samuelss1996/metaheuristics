package app

import app.random.FileRandom
import app.random.IRandom
import app.random.StandardRandom
import app.util.Cities
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

    fun run() {
        this.generateInitialSolution()
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

        println("SOLUCION INICIAL:")
        println("\tRECORRIDO: ${result.toString().replace("[","").replace("]","").replace(",","")} ")
        println("\tFUNCION OBJETIVO (km): ${this.cities.getCost(result)}\n")

        return result
    }
}