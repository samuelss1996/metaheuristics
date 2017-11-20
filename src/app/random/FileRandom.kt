package app.random

import java.io.File

class FileRandom(private val file: File) : IRandom {
    private val reader = this.file.bufferedReader()

    override fun next() = this.reader.readLine()?.toDouble() ?: throw NoSuchElementException("Leidos más números de los neecesarios")
}