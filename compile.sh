#!/bin/bash

# Install the Kotlin compiler
curl -s https://get.sdkman.io | bash
source ~/.sdkman/bin/sdkman-init.sh
sdk install kotlin

# Compile the project
kotlinc app/MainApp.kt app/random/IRandom.kt app/random/FileRandom.kt app/random/StandardRandom.kt app/util/Cities.kt -jvm-target 1.6 -include-runtime -d SimulatedAnnealing.jar

echo "Compiled successfully to SimulatedAnnealing.jar. Execute with java -jar SimulatedAnnealing.jar"