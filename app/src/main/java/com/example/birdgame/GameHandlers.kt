package com.example.birdgame

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.runtime.MutableState
import com.example.birdgame.model.Bird
import com.example.birdgame.model.BirdType
import com.example.birdgame.model.GameState
import com.example.birdgame.model.Player
import com.example.birdgame.model.Position
import com.example.birdgame.model.calculateBoardBounds

fun handleTap(
    position: Position,
    gameState: MutableState<GameState>,
    selectedPosition: MutableState<Position?>,
    selectedBird: MutableState<Bird?>,
    player1Birds: MutableList<Bird>,
    player2Birds: MutableList<Bird>,
    context: Context,
    onWin: (Player) -> Unit
) {
    val bird = gameState.value.board[position]
    val player = gameState.value.currentPlayer

    if (bird == null && selectedBird.value != null && selectedBird.value!!.player == player) {
        if (isPlacementValid(position, gameState.value.board, player)) {
            val updatedBoard = gameState.value.board.toMutableMap()
            updatedBoard[position] = selectedBird.value!!

            val (isValid, finalBoard) = isValidMoveAndGetUpdatedBoard(updatedBoard)

            if (isValid) {
                updateBoardAndCheckWin(gameState, finalBoard, if (player == Player.PLAYER1) Player.PLAYER2 else Player.PLAYER1, onWin)
                updatePlayerBirds(player, selectedBird.value!!, player1Birds, player2Birds, false)
                selectedBird.value = null
                selectedPosition.value = null
                val mediaPlayer = MediaPlayer.create(context, R.raw.kakaw)
                mediaPlayer.start()
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
    player1Birds: MutableList<Bird>,
    player2Birds: MutableList<Bird>,
    onWin: (Player) -> Unit // Add onWin callback
) {
    val bird = gameState.value.board[position]
    val player = gameState.value.currentPlayer

    if (bird != null && bird.player == player && bird.type != BirdType.BOSS) {
        val updatedBoard = gameState.value.board.toMutableMap()
        updatedBoard.remove(position)

        if (isMoveValidBoardState(updatedBoard)) {
            updateBoardAndCheckWin(gameState, updatedBoard, if (player == Player.PLAYER1) Player.PLAYER2 else Player.PLAYER1, onWin)
            updatePlayerBirds(player, bird, player1Birds, player2Birds, true)
            selectedPosition.value = null
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
    isBossMove: Boolean = false,
    onWin: (Player) -> Unit
) {
    val from = selectedPosition.value ?: return
    val bird = gameState.value.board[from] ?: return

    if (isMoveValid(from, to, bird.type, gameState.value.board) && gameState.value.board[to] == null) {
        val updatedBoard = gameState.value.board.toMutableMap()
        updatedBoard.remove(from)
        updatedBoard[to] = bird

        val (isValid, finalBoard) = isValidMoveAndGetUpdatedBoard(updatedBoard)

        if (isValid) {
            updateBoardAndCheckWin(gameState, finalBoard, if (gameState.value.currentPlayer == Player.PLAYER1 || isBossMove) Player.PLAYER2 else Player.PLAYER1, onWin)
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

private val allowedMovesMap = mapOf(
    BirdType.REGULAR to listOf(
        Position(-1, 0), Position(1, 0), Position(0, -1), Position(0, 1)
    ),
    BirdType.SHOOTER to listOf(
        Position(-1, -1), Position(-1, 1), Position(1, -1), Position(1, 1)
    ),
    BirdType.HIT_MAN to listOf(
        Position(-1, -1), Position(-1, 0), Position(-1, 1),
        Position(0, -1), Position(0, 1),
        Position(1, -1), Position(1, 0), Position(1, 1)
    ),
    BirdType.AGENT to listOf(
        Position(-1, 0), Position(1, 0), Position(0, -1), Position(0, 1)
    ),
    BirdType.BOMBER to listOf(
        Position(-2, -1), Position(-2, 1), Position(-1, -2), Position(-1, 2),
        Position(1, -2), Position(1, 2), Position(2, -1), Position(2, 1)
    ),
    BirdType.BOSS to listOf(
        Position(-1, -1), Position(-1, 0), Position(-1, 1),
        Position(0, -1), Position(0, 1),
        Position(1, -1), Position(1, 0), Position(1, 1)
    )
)

fun isMoveValid(from: Position, to: Position, birdType: BirdType, board: Map<Position, Bird>): Boolean {

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

    val allowedMoves = allowedMovesMap[birdType] ?: return false

    return allowedMoves.any { allowedMove ->
        val newRow = from.row + allowedMove.row
        val newCol = from.col + allowedMove.col
        Position(newRow, newCol) == to
    }
}

fun isPlacementValid(position: Position, board: Map<Position, Bird>, player: Player): Boolean {
    val adjacentPositions = getAdjacentPositions(position)

    var hasAdjacentOwnBird = false
    for (adjacentPosition in adjacentPositions) {
        val adjacentBird = board[adjacentPosition]
        if (adjacentBird != null && adjacentBird.player == player) {
            hasAdjacentOwnBird = true
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
        val adjacentBird = board[adjacentPosition]
        if (adjacentBird != null && adjacentBird.player != player && adjacentBird.type == BirdType.BOSS) {
            return false
        }
    }

    if (!hasAdjacentOwnBird) return false

    // Simulate placement and check bounds
    val simulatedBoard = board.toMutableMap()
    simulatedBoard[position] = Bird(BirdType.REGULAR, player, 0) // Dummy bird

    val occupiedRows = simulatedBoard.keys.map { it.row }.distinct().sorted()
    val occupiedCols = simulatedBoard.keys.map { it.col }.distinct().sorted()

    return occupiedRows.size <= 4 && occupiedCols.size <= 4
}

fun isMoveValidBoardState(board: Map<Position, Bird>): Boolean {
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

fun checkWinCondition(gameState: GameState): Player? {
    val bossPositions = gameState.board.filterValues { it.type == BirdType.BOSS }

    for ((position, boss) in bossPositions) {
        val adjacentPositions = listOf(
            Position(position.row - 1, position.col),
            Position(position.row + 1, position.col),
            Position(position.row, position.col - 1),
            Position(position.row, position.col + 1)
        )

        val surroundingBirds = adjacentPositions.count {
            gameState.board.containsKey(it) || isEdgePositionForBoss(it, gameState.board)
        }

        if (surroundingBirds == 4) {
            return if (boss.player == Player.PLAYER1) Player.PLAYER2 else Player.PLAYER1
        }
    }
    return null
}

fun isEdgePositionForBoss(position: Position, board: Map<Position, Bird>): Boolean {
    val occupiedRows = board.keys.map { it.row }.distinct().sorted()
    val occupiedCols = board.keys.map { it.col }.distinct().sorted()

    return occupiedRows.size == 4 && (position.row < occupiedRows.first() || position.row > occupiedRows.last()) ||
            occupiedCols.size == 4 && (position.col < occupiedCols.first() || position.col > occupiedCols.last())
}

fun updateBoardAndCheckWin(
    gameState: MutableState<GameState>,
    updatedBoard: Map<Position, Bird>,
    nextPlayer: Player,
    onWin: (Player) -> Unit
) {
    val newGameState = gameState.value.copy(board = updatedBoard, currentPlayer = nextPlayer)
    gameState.value = newGameState
    checkWinCondition(newGameState)?.let { winner ->
        onWin(winner)
    }
}

fun isValidMoveAndGetUpdatedBoard(
    newBoard: Map<Position, Bird>
): Pair<Boolean, Map<Position, Bird>> {
    val newBounds = calculateBoardBounds(newBoard)
    val isValid = isMoveValidBoardState(newBoard) &&
            newBounds.maxRow - newBounds.minRow + 1 <= 4 &&
            newBounds.maxCol - newBounds.minCol + 1 <= 4
    return Pair(isValid, newBoard)
}

fun updatePlayerBirds(
    player: Player,
    bird: Bird,
    player1Birds: MutableList<Bird>,
    player2Birds: MutableList<Bird>,
    isAdding: Boolean
) {
    if (player == Player.PLAYER1) {
        if (isAdding) player1Birds.add(bird) else player1Birds.remove(bird)
    } else {
        if (isAdding) player2Birds.add(bird) else player2Birds.remove(bird)
    }
}