package com.example.birdgame.model

import com.example.birdgame.R

data class Position(val row: Int, val col: Int)

enum class Player { PLAYER1, PLAYER2 }

enum class BirdType {
    REGULAR,
    SHOOTER,
    HIT_MAN,
    AGENT,
    BOMBER,
    BOSS
}

data class Bird(val type: BirdType, val player: Player, val drawableResId: Int)

fun getBirdDrawableResId(type: BirdType, player: Player): Int {
    return when (type) {
        BirdType.REGULAR -> if (player == Player.PLAYER1) R.drawable.regular else R.drawable.regular_red
        BirdType.SHOOTER -> if (player == Player.PLAYER1) R.drawable.shooter else R.drawable.shooter_red
        BirdType.HIT_MAN -> if (player == Player.PLAYER1) R.drawable.hitman else R.drawable.hitman_red
        BirdType.AGENT -> if (player == Player.PLAYER1) R.drawable.agent else R.drawable.agent_red
        BirdType.BOMBER -> if (player == Player.PLAYER1) R.drawable.bomber else R.drawable.bomber_red
        BirdType.BOSS -> if (player == Player.PLAYER1) R.drawable.boss else R.drawable.boss_red
    }
}

data class GameState(
    val board: Map<Position, Bird>,
    val boardWidth: Int,
    val boardHeight: Int,
    val currentPlayer: Player
)

const val MAX_BOARD_WIDTH = 4
const val MAX_BOARD_HEIGHT = 4

data class BoardBounds(
    val minRow: Int,
    val maxRow: Int,
    val minCol: Int,
    val maxCol: Int
)

fun calculateBoardBounds(board: Map<Position, Bird>, selectedPosition: Position? = null): BoardBounds {
    if (board.isEmpty()) {
        return BoardBounds(0, 0, 0, 0)
    }

    val rows = board.keys.map { it.row }
    val cols = board.keys.map { it.col }

    val minRow = rows.minOrNull() ?: 0
    val maxRow = rows.maxOrNull() ?: 0
    val minCol = cols.minOrNull() ?: 0
    val maxCol = cols.maxOrNull() ?: 0

    return BoardBounds(minRow, maxRow, minCol, maxCol)
}