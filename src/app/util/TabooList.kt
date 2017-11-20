package app.util

class TabooList(private val capacity: Int, private val valuesRange: Int) {
    private val presentMatrix = Array(this.valuesRange, {Array(this.valuesRange - 1, {false})})
    private val content = Array(this.capacity, {Array(2, {0})})
    private var currentPosition = 0
    private var filledCount = 0
    private var limitReached = false

    fun add(i: Int, j: Int) {
        this.presentMatrix[i][j] = true
        this.presentMatrix[this.content[this.currentPosition][0]][this.content[this.currentPosition][1]] = false

        this.content[this.currentPosition][0] = i
        this.content[this.currentPosition][1] = j

        this.currentPosition++
        this.filledCount = minOf(this.filledCount + 1, this.capacity)

        if(this.currentPosition >= this.capacity) {
            this.currentPosition = 0
            this.limitReached = true
        }
    }

    fun contains(i: Int, j: Int) = this.presentMatrix[i][j]

    override fun toString(): String {
        val stringBuilder = StringBuilder()

        for(i in (this.currentPosition until this.filledCount) + (0 until this.currentPosition)) {
            stringBuilder.append("\t${this.content[i][0]} ${this.content[i][1]}\n")
        }

        return stringBuilder.toString()
    }
}