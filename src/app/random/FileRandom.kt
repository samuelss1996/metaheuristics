package app.random

import java.io.File
import java.lang.Double
import java.text.NumberFormat
import java.util.*

class FileRandom(private val file: File) : IRandom {
    private val reader = this.file.bufferedReader()

    override fun next() = Double.valueOf(this.reader.readLine())
}