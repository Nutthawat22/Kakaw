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

fun getDoveDrawableResId(type: BirdType, player: Player): Int {
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

fun calculateBoardBounds(board: Map<Position, Bird>): BoardBounds {
    val keys = board.keys
    val minRow = keys.minOfOrNull { it.row } ?: 0
    val maxRow = keys.maxOfOrNull { it.row } ?: 0
    val minCol = keys.minOfOrNull { it.col } ?: 0
    val maxCol = keys.maxOfOrNull { it.col } ?: 0

    val width = maxCol - minCol + 1
    val height = maxRow - minRow + 1

    val adjustedMinRow = if (height > MAX_BOARD_HEIGHT) {
        (minRow + maxRow - MAX_BOARD_HEIGHT + 1) / 2
    } else {
        minRow
    }

    val adjustedMaxRow = if (height > MAX_BOARD_HEIGHT) {
        adjustedMinRow + MAX_BOARD_HEIGHT - 1
    } else {
        maxRow
    }

    val adjustedMinCol = if (width > MAX_BOARD_WIDTH) {
        (minCol + maxCol - MAX_BOARD_WIDTH + 1) / 2
    } else {
        minCol
    }

    val adjustedMaxCol = if (width > MAX_BOARD_WIDTH) {
        adjustedMinCol + MAX_BOARD_WIDTH - 1
    } else {
        maxCol
    }

    return BoardBounds(adjustedMinRow, adjustedMaxRow, adjustedMinCol, adjustedMaxCol)
}