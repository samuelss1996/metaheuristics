@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")

package app.random

import java.io.File
import java.lang.Double

class FileRandom(private val file: File) : IRandom {
    private val reader = this.file.bufferedReader()

    override fun next() = Double.valueOf(this.reader.readLine())
}