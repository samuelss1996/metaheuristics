package app.random

import java.util.*

class StandardRandom : IRandom {
    private val random = Random()

    override fun next(): Double {
        return this.random.nextDouble()
    }
}