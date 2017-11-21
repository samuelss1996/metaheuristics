package app.random

import java.io.File
import java.text.NumberFormat
import java.util.*

class FileRandom(private val file: File) : IRandom {
    private val reader = this.file.bufferedReader()
    private val numberParser = NumberFormat.getInstance(Locale.US)

    override fun next() = this.numberParser.parse(this.reader.readLine()).toDouble()
}