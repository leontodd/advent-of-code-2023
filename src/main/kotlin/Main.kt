import kotlin.io.path.Path
import kotlin.io.path.readLines
import kotlin.math.pow

fun main(args: Array<String>) {
    //solveDay1()
    //solveDay2()
    //solveDay3()
    //solveDay4()
    solveDay5()
}

fun solveDay1() {
    val lines = Path("inputs/day1.txt").readLines()
    val calibrationValuesPart1 = lines.map { line ->
        val digits = line.filter { char -> char.isDigit() }
        "${digits.first()}${digits.last()}".toInt()
    }
    val answerPart1 = calibrationValuesPart1.sum()
    println(answerPart1)

    val digits = mapOf(
        "one" to "1",
        "two" to "2",
        "three" to "3",
        "four" to "4",
        "five" to "5",
        "six" to "6",
        "seven" to "7",
        "eight" to "8",
        "nine" to "9"
    )
    val calibrationValuesPart2 = lines.map { line ->
        val (_, firstDigitAsString) = line.findAnyOf(digits.keys + digits.values)!!
        val (_, lastDigitAsString) = line.findLastAnyOf(digits.keys + digits.values)!!
        val firstDigit =
            if (firstDigitAsString.length == 1) firstDigitAsString.single() else digits[firstDigitAsString]!!.single()
        val lastDigit =
            if (lastDigitAsString.length == 1) lastDigitAsString.single() else digits[lastDigitAsString]!!.single()
        "${firstDigit}${lastDigit}".toInt()
    }
    val answerPart2 = calibrationValuesPart2.sum()
    println(answerPart2)
}

fun solveDay2() {
    val lines = Path("inputs/day2.txt").readLines()

    val games = lines.associate { line ->
        val (gameString, setStrings) = line.split(": ")
        val gameNumber = gameString.split(" ")[1].toInt()
        gameNumber to setStrings.split("; ").map { setString ->
            setString.split(", ").map { cubesString ->
                val (quantityString, colour) = cubesString.split(" ")
                quantityString.toInt() to colour
            }
        }
    }

    val possibleGames = games.filter { (_, game) ->
        game.all { set ->
            set.all { (quantity, colour) ->
                when (colour) {
                    "red" -> quantity <= 12
                    "green" -> quantity <= 13
                    "blue" -> quantity <= 14
                    else -> false
                }
            }
        }
    }

    val answerPart1 = possibleGames.keys.sum()
    println(answerPart1)

    val powers = games.map { (_, game) ->
        val allCubes = game.flatten()
        val (fewestRedCubes, _) = allCubes.filter { (_, colour) -> colour == "red" }.maxBy { (quantity, _) -> quantity }
        val (fewestGreenCubes, _) = allCubes.filter { (_, colour) -> colour == "green" }
            .maxBy { (quantity, _) -> quantity }
        val (fewestBlueCubes, _) = allCubes.filter { (_, colour) -> colour == "blue" }
            .maxBy { (quantity, _) -> quantity }
        fewestRedCubes * fewestGreenCubes * fewestBlueCubes
    }

    val answerPart2 = powers.sum()
    println(answerPart2)
}

fun solveDay3() {
    val lines = Path("inputs/day3.txt").readLines()
    val engine = lines.map { line -> line.toList() }
    val partNumbers = mutableListOf<Int>()
    val charPositionsPartNumberMap = mutableMapOf<Pair<Int, Int>, Int>()
    val gears = mutableListOf<Pair<Int, Int>>()

    fun findAdjacentChars(y: Int, x: Int) = setOf(
        (y - 1 to x - 1) to engine.getOrNull(y - 1)?.getOrNull(x - 1),
        (y - 1 to x) to engine.getOrNull(y - 1)?.getOrNull(x),
        (y - 1 to x + 1) to engine.getOrNull(y - 1)?.getOrNull(x + 1),
        (y to x + 1) to engine.getOrNull(y)?.getOrNull(x + 1),
        (y + 1 to x + 1) to engine.getOrNull(y + 1)?.getOrNull(x + 1),
        (y + 1 to x) to engine.getOrNull(y + 1)?.getOrNull(x),
        (y + 1 to x - 1) to engine.getOrNull(y + 1)?.getOrNull(x - 1),
        (y to x - 1) to engine.getOrNull(y)?.getOrNull(x - 1)
    )

    for (y in engine.indices) {
        var currentNumber = ""
        var isPartNumber = false
        val currentNumberCharPositions = mutableListOf<Pair<Int, Int>>()
        for (x in engine[y].indices) {
            if (y == 45 && x == 137) {
                println()
            }
            if (engine[y][x].isDigit()) {
                currentNumber += engine[y][x]
                currentNumberCharPositions += y to x
                val adjacentSymbols = findAdjacentChars(y, x).filter { (_, char) -> char != null && !char.isDigit() && char != '.' }
                if (adjacentSymbols.isNotEmpty()) {
                    isPartNumber = true
                }
                gears += adjacentSymbols.filter { (_, char) -> char == '*' }.map { (yx, _) -> yx }
            } else {
                if (isPartNumber) charPositionsPartNumberMap += currentNumberCharPositions.map { it to currentNumber.toInt() }
                if (isPartNumber) partNumbers += currentNumber.toInt()
                currentNumber = ""
                currentNumberCharPositions.clear()
                isPartNumber = false
            }
        }
        if (isPartNumber) charPositionsPartNumberMap += currentNumberCharPositions.map { it to currentNumber.toInt() }
        if (isPartNumber) partNumbers += currentNumber.toInt()
    }

    val answerPart1 = partNumbers.sum()
    println(answerPart1)

    val gearParts = gears.distinct().map { (gearY, gearX) ->
        findAdjacentChars(gearY, gearX).filter { (_, char) -> char != null && char.isDigit() }.mapNotNull { (yx, _) ->
            val (y2, x2) = yx
            charPositionsPartNumberMap[y2 to x2]
        }.distinct()
    }.filter { it.size > 1 }

    val gearRatios = gearParts.map { it.reduce { acc, gearPart -> acc * gearPart } }

    val answerPart2 = gearRatios.sum()
    println(answerPart2)
}

fun solveDay4() {
    val lines = Path("inputs/day4.txt").readLines()

    val allCards = lines.associate { line ->
        val regex = "Card +(?<id>\\d+):(?<winningNumbers>[ \\d]+)\\|(?<numbers>[ \\d]+)".toRegex()
        val match = regex.find(line)!!
        val id = match.groups["id"]!!.value.toInt()
        val winningNumbers = match.groups["winningNumbers"]!!.value.split(" ").filter { it.isNotEmpty() }.map { it.toInt() }.toSet()
        val numbers = match.groups["numbers"]!!.value.split(" ").filter { it.isNotEmpty() }.map { it.toInt() }.toSet()
        val matches = winningNumbers.intersect(numbers).size
        id to matches
    }

    val points = allCards.map { (_, matches) ->
        2.0.pow(matches - 1).toInt()
    }

    val answerPart1 = points.sum()
    println(answerPart1)

    val processedCards = mutableListOf<Int>()
    val cardsToBeProcessed = ArrayDeque(allCards.keys)
    while (cardsToBeProcessed.isNotEmpty()) {
        val card = cardsToBeProcessed.removeFirst()
        val cardMatches = allCards[card]!!
        val cardsWon = (card + 1..card + cardMatches)
        processedCards += card
        cardsToBeProcessed += cardsWon
    }
    println(processedCards.size)
}

fun solveDay5() {

}