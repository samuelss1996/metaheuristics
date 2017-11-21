package app.util

import java.io.File

class Cities(matrixFile: File) {
    private val distancesMatrix: Array<Array<Int>>
    val citiesCount: Int

    init {
        val distancesDynamicMatrix = mutableListOf<MutableList<Int>>()
        distancesDynamicMatrix.add(mutableListOf())

        matrixFile.readLines().forEach { line ->
            val row = mutableListOf<Int>()

            distancesDynamicMatrix.add(row)
            line.split("\t").forEach { row.add(it.toInt()) }
        }

        this.citiesCount = distancesDynamicMatrix.size
        this.distancesMatrix = Array(distancesDynamicMatrix.size, {i -> Array(this.citiesCount, {j -> distancesDynamicMatrix.getOrNull(i)?.getOrNull(j) ?: 0})})
    }

    fun getCost(solution: List<Int>) =
        this.getDistance(0, solution.first()) + (1 until solution.size).map { this.getDistance(solution[it - 1], solution[it]) }.sum() +
                this.getDistance(solution.last(), 0)

    private fun getDistance(city1: Int, city2: Int) = this.distancesMatrix[maxOf(city1, city2)][minOf(city1, city2)]
}