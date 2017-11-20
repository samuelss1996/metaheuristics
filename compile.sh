#!/bin/bash

# Install the Kotlin compiler
curl -s https://get.sdkman.io | bash
source ~/.sdkman/bin/sdkman-init.sh
sdk install kotlin

# Compile the project
kotlinc app/MainApp.kt app/random/IRandom.kt app/random/FileRandom.kt app/random/StandardRandom.kt app/util/Cities.kt app/util/TabooList.kt -jvm-target 1.6 -include-runtime -d TabooSearch.jar

echo "Compiled successfully to TabooSeach.jar. Execute with java -jar TabooSearch.jar"