package com.example.birdgame

import androidx.compose.runtime.MutableState
import com.example.birdgame.model.Dove
import com.example.birdgame.model.DoveType
import com.example.birdgame.model.GameState
import com.example.birdgame.model.Player
import com.example.birdgame.model.Position
import com.example.birdgame.model.calculateBoardBounds
import kotlin.math.abs

fun handleTap(
    position: Position,
    gameState: MutableState<GameState>,
    selectedPosition: MutableState<Position?>,
    selectedDove: MutableState<Dove?>,
    player1Doves: MutableList<Dove>,
    player2Doves: MutableList<Dove>
) {
    val dove = gameState.value.board[position]
    val player = gameState.value.currentPlayer

    if (dove == null && selectedDove.value != null && selectedDove.value!!.player == player) {
        if (isPlacementValid(position, gameState.value.board, player)) {
            val updatedBoard = gameState.value.board.toMutableMap()
            updatedBoard[position] = selectedDove.value!!
            val newBounds = calculateBoardBounds(updatedBoard)

            if (newBounds.maxRow - newBounds.minRow + 1 <= 4 && newBounds.maxCol - newBounds.minCol + 1 <= 4) {
                gameState.value = gameState.value.copy(board = updatedBoard, currentPlayer = if (player == Player.PLAYER1) Player.PLAYER2 else Player.PLAYER1)
                if (player == Player.PLAYER1) {
                    player1Doves.remove(selectedDove.value!!)
                } else {
                    player2Doves.remove(selectedDove.value!!)
                }

                selectedDove.value = null
                selectedPosition.value = null

            } else {
                selectedPosition.value = null
            }
        } else {
            selectedPosition.value = null
        }
    } else {
        selectedPosition.value = null
    }
}

fun handleRemove(
    position: Position,
    gameState: MutableState<GameState>,
    selectedPosition: MutableState<Position?>,
    player1Doves: MutableList<Dove>,
    player2Doves: MutableList<Dove>
) {
    val dove = gameState.value.board[position]
    val player = gameState.value.currentPlayer

    if (dove != null && dove.player == player && dove.type != DoveType.BOSS) {
        val updatedBoard = gameState.value.board.toMutableMap()
        updatedBoard.remove(position)

        if (isMoveValidBoardState(updatedBoard)) {
            gameState.value = gameState.value.copy(
                board = updatedBoard,
                currentPlayer = if (player == Player.PLAYER1) Player.PLAYER2 else Player.PLAYER1
            )
            selectedPosition.value = null

            if (player == Player.PLAYER1) {
                player1Doves.add(dove)
            } else {
                player2Doves.add(dove)
            }

        } else {
            selectedPosition.value = null
        }
    }
}

fun handleMoveSelection(
    to: Position,
    gameState: MutableState<GameState>,
    selectedPosition: MutableState<Position?>,
    moveMode: MutableState<Boolean>,
    isBossMove: Boolean = false
) {
    val from = selectedPosition.value ?: return
    val dove = gameState.value.board[from] ?: return

    if (isMoveValid(from, to, dove.type, gameState.value.board) && gameState.value.board[to] == null) {
        val updatedBoard = gameState.value.board.toMutableMap()
        updatedBoard.remove(from)
        updatedBoard[to] = dove
        val newBounds = calculateBoardBounds(updatedBoard)

        if (isMoveValidBoardState(updatedBoard) && newBounds.maxRow - newBounds.minRow + 1 <= 4 && newBounds.maxCol - newBounds.minCol + 1 <= 4) {
            gameState.value = gameState.value.copy(
                board = updatedBoard,
                currentPlayer = if (gameState.value.currentPlayer == Player.PLAYER1 || isBossMove) Player.PLAYER2 else Player.PLAYER1
            )
            selectedPosition.value = null
            moveMode.value = false

        } else {
            selectedPosition.value = null
            moveMode.value = false
        }
    } else {
        selectedPosition.value = null
        moveMode.value = false
    }
}

fun isMoveValid(from: Position, to: Position, doveType: DoveType, board: Map<Position, Dove>): Boolean {
    val rowDiff = abs(from.row - to.row)
    val colDiff = abs(from.col - to.col)

    val adjacentPositions = getAdjacentPositions(to)

    if (!adjacentPositions.any { board.containsKey(it) }) {
        return false
    }

    // Simulate move and check bounds
    val simulatedBoard = board.toMutableMap()
    simulatedBoard[to] = simulatedBoard[from]!!
    simulatedBoard.remove(from)

    val occupiedRows = simulatedBoard.keys.map { it.row }.distinct().sorted()
    val occupiedCols = simulatedBoard.keys.map { it.col }.distinct().sorted()

    if (occupiedRows.size > 4 || occupiedCols.size > 4) return false

    return when (doveType) {
        DoveType.REGULAR -> rowDiff + colDiff == 1
        DoveType.SHOOTER -> rowDiff == 1 && colDiff == 1
        DoveType.HIT_MAN -> rowDiff <= 1 && colDiff <= 1 && (rowDiff != 0 || colDiff != 0)
        DoveType.AGENT -> {
            if ((rowDiff == 0 && colDiff != 0) || (rowDiff != 0 && colDiff == 0)) {
                val rowDirection = if (to.row > from.row) 1 else if (to.row < from.row) -1 else 0
                val colDirection = if (to.col > from.col) 1 else if (to.col < from.col) -1 else 0

                var currentRow = from.row + rowDirection
                var currentCol = from.col + colDirection

                while (currentRow != to.row || currentCol != to.col) {
                    if (board.containsKey(Position(currentRow, currentCol))) {
                        return false
                    }
                    currentRow += rowDirection
                    currentCol += colDirection
                }
                true
            } else {
                false
            }
        }
        DoveType.BOMBER -> (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2)
        DoveType.BOSS -> rowDiff <= 1 && colDiff <= 1 && (rowDiff != 0 || colDiff != 0)
    }
}

fun isPlacementValid(position: Position, board: Map<Position, Dove>, player: Player): Boolean {
    val adjacentPositions = getAdjacentPositions(position)

    var hasAdjacentOwnDove = false
    for (adjacentPosition in adjacentPositions) {
        val adjacentDove = board[adjacentPosition]
        if (adjacentDove != null && adjacentDove.player == player) {
            hasAdjacentOwnDove = true
            break
        }
    }

    val directAdjacentPositions = listOf(
        Position(position.row - 1, position.col),
        Position(position.row + 1, position.col),
        Position(position.row, position.col - 1),
        Position(position.row, position.col + 1)
    )

    for (adjacentPosition in directAdjacentPositions) {
        val adjacentDove = board[adjacentPosition]
        if (adjacentDove != null && adjacentDove.player != player && adjacentDove.type == DoveType.BOSS) {
            return false
        }
    }

    if (!hasAdjacentOwnDove) return false

    // Simulate placement and check bounds
    val simulatedBoard = board.toMutableMap()
    simulatedBoard[position] = Dove(DoveType.REGULAR, player, 0) // Dummy dove

    val occupiedRows = simulatedBoard.keys.map { it.row }.distinct().sorted()
    val occupiedCols = simulatedBoard.keys.map { it.col }.distinct().sorted()

    return occupiedRows.size <= 4 && occupiedCols.size <= 4
}

fun isMoveValidBoardState(board: Map<Position, Dove>): Boolean {
    if (board.isEmpty()) return true

    val visited = mutableSetOf<Position>()
    val queue = mutableListOf(board.keys.first())
    visited.add(board.keys.first())

    while (queue.isNotEmpty()) {
        val currentPosition = queue.removeAt(0)
        val adjacentPositions = listOf(
            Position(currentPosition.row - 1, currentPosition.col),
            Position(currentPosition.row + 1, currentPosition.col),
            Position(currentPosition.row, currentPosition.col - 1),
            Position(currentPosition.row, currentPosition.col + 1),
            Position(currentPosition.row - 1, currentPosition.col - 1),
            Position(currentPosition.row - 1, currentPosition.col + 1),
            Position(currentPosition.row + 1, currentPosition.col - 1),
            Position(currentPosition.row + 1, currentPosition.col + 1)
        )

        for (adjacentPosition in adjacentPositions) {
            if (board.containsKey(adjacentPosition) && !visited.contains(adjacentPosition)) {
                visited.add(adjacentPosition)
                queue.add(adjacentPosition)
            }
        }
    }

    return visited.size == board.size
}

private fun getAdjacentPositions(position: Position): List<Position> {
    return listOf(
        Position(position.row - 1, position.col),
        Position(position.row + 1, position.col),
        Position(position.row, position.col - 1),
        Position(position.row, position.col + 1),
        Position(position.row - 1, position.col - 1),
        Position(position.row - 1, position.col + 1),
        Position(position.row + 1, position.col - 1),
        Position(position.row + 1, position.col + 1)
    )
}