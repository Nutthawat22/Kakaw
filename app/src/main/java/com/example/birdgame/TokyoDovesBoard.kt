package com.example.birdgame

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.example.birdgame.model.Dove
import com.example.birdgame.model.DoveType
import com.example.birdgame.model.GameState
import com.example.birdgame.model.MAX_BOARD_HEIGHT
import com.example.birdgame.model.MAX_BOARD_WIDTH
import com.example.birdgame.model.Player
import com.example.birdgame.model.Position
import com.example.birdgame.model.calculateBoardBounds
import com.example.birdgame.model.getDoveDrawableResId

fun initializeGame(): GameState {
    val boardWidth = 3
    val boardHeight = 4
    val board = mutableMapOf<Position, Dove>()

    board[Position(2, 1)] =
        Dove(DoveType.BOSS, Player.PLAYER1, getDoveDrawableResId(DoveType.BOSS, Player.PLAYER1))
    board[Position(1, 1)] =
        Dove(DoveType.BOSS, Player.PLAYER2, getDoveDrawableResId(DoveType.BOSS, Player.PLAYER2))

    return GameState(board, boardWidth, boardHeight, Player.PLAYER1)
}

private fun createPlayerBirds(player: Player): List<Dove> {
    return listOf(
        Dove(DoveType.REGULAR, player, getDoveDrawableResId(DoveType.REGULAR, player)),
        Dove(DoveType.SHOOTER, player, getDoveDrawableResId(DoveType.SHOOTER, player)),
        Dove(DoveType.HIT_MAN, player, getDoveDrawableResId(DoveType.HIT_MAN, player)),
        Dove(DoveType.AGENT, player, getDoveDrawableResId(DoveType.AGENT, player)),
        Dove(DoveType.BOMBER, player, getDoveDrawableResId(DoveType.BOMBER, player))
    )
}

@Composable
fun PlayerBirdLine(
    doves: List<Dove>,
    selectedDove: Dove?,
    onDoveClick: (Dove, Offset) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
        ,horizontalArrangement = Arrangement.SpaceAround
    ) {
        doves.forEach { dove ->
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clickable {
                        onDoveClick(
                            dove,
                            Offset.Zero
                        )
                    }
                    .border(
                        width = if (dove == selectedDove) 2.dp else 0.dp,
                        color = if (dove == selectedDove) Color.Green else Color.Transparent,
                        shape = RectangleShape
                    )
            ) {
                Image(
                    painter = painterResource(id = dove.drawableResId),
                    contentDescription = dove.type.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (dove.player == Player.PLAYER2) { // Flip red birds
                                Modifier.scale(scaleX = -1f, scaleY = -1f)
                            } else {
                                Modifier
                            }
                        )
                )
            }
        }
    }
}

@Composable
fun TokyoDovesBoard() {
    val gameState = remember { mutableStateOf(initializeGame()) }
    val boardBounds = remember { derivedStateOf { calculateBoardBounds(gameState.value.board) } }
    val player1Doves = remember { mutableStateListOf<Dove>() }
    val player2Doves = remember { mutableStateListOf<Dove>() }
    val selectedDove = remember { mutableStateOf<Dove?>(null) }
    val selectedPosition = remember { mutableStateOf<Position?>(null) }
    val showDialog = remember { mutableStateOf(false) }
    val moveMode = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        player1Doves.addAll(createPlayerBirds(Player.PLAYER1))
        player2Doves.addAll(createPlayerBirds(Player.PLAYER2))
    }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        Text(
            text = "${if (gameState.value.currentPlayer == Player.PLAYER1) "Green" else "Red"} Player's Turn",
            fontSize = 30.sp,
            color = if (gameState.value.currentPlayer == Player.PLAYER1) {
                Color(0xFF044F3B)
            } else {
                Color(0xFF730E3C)
            },
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE9E9E9))
        )

        PlayerBirdLine(
            doves = player2Doves,
            selectedDove = selectedDove.value
        ) { dove, _ ->
            selectedDove.value = dove
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val boardWidth =
                minOf(boardBounds.value.maxCol - boardBounds.value.minCol + 3, MAX_BOARD_WIDTH + 2)
            val boardHeight =
                minOf(boardBounds.value.maxRow - boardBounds.value.minRow + 3, MAX_BOARD_HEIGHT + 2)

            val cellSize = 80.dp
            val boardWidthDp = boardWidth * cellSize
            val boardHeightDp = boardHeight * cellSize

            Box(
                modifier = Modifier
                    .size(boardWidthDp, boardHeightDp)
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val col =
                                (offset.x / cellSize.toPx()).toInt() + boardBounds.value.minCol - 1
                            val row =
                                (offset.y / cellSize.toPx()).toInt() + boardBounds.value.minRow - 1
                            val position = Position(row, col)

                            if (moveMode.value) {
                                handleMoveSelection(
                                    position,
                                    gameState,
                                    selectedPosition,
                                    moveMode,
                                )
                            } else {
                                val dove = gameState.value.board[position]
                                if (dove != null && dove.player == gameState.value.currentPlayer) {
                                    selectedPosition.value = position
                                    showDialog.value = true
                                } else {
                                    handleTap(
                                        position,
                                        gameState,
                                        selectedPosition,
                                        selectedDove,
                                        player1Doves,
                                        player2Doves
                                    )
                                }
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.size(boardWidthDp, boardHeightDp)) {
                    drawRect(Color.Transparent, Offset.Zero, size)

                    for (row in 0 until boardHeight) {
                        for (col in 0 until boardWidth) {
                            val actualRow = row + boardBounds.value.minRow - 1
                            val actualCol = col + boardBounds.value.minCol - 1
                            val position = Position(actualRow, actualCol)
                            val offset = Offset(col * cellSize.toPx(), row * cellSize.toPx())

                            if (gameState.value.board[position] == null) {
                                if (moveMode.value && selectedPosition.value != null && isMoveValid(
                                        selectedPosition.value!!,
                                        position,
                                        gameState.value.board[selectedPosition.value!!]!!.type,
                                        gameState.value.board
                                    ) && isMoveValidBoardState(
                                        gameState.value.board.toMutableMap().apply {
                                            this.remove(selectedPosition.value!!); this[position] =
                                            gameState.value.board[selectedPosition.value!!]!!
                                        })
                                ) {
                                    drawRect( // Highlight valid moves
                                        color = Color(0xFFBDBCBC),
                                        topLeft = offset,
                                        size = Size(cellSize.toPx(), cellSize.toPx())
                                    )
                                } else if (selectedDove.value != null && isPlacementValid(
                                        position,
                                        gameState.value.board,
                                        selectedDove.value!!.player
                                    )
                                ) {
                                    drawRect(
                                        color = Color(0xFFBDBCBC),
                                        topLeft = offset,
                                        size = Size(cellSize.toPx(), cellSize.toPx())
                                    )
                                } else {
                                    drawRect(
                                        color = Color.Transparent,
                                        topLeft = offset,
                                        size = Size(cellSize.toPx(), cellSize.toPx())
                                    )
                                }
                            } else {
                                // Draw empty space
                                drawRect(
                                    color = Color.Transparent,
                                    topLeft = offset,
                                    size = Size(cellSize.toPx(), cellSize.toPx())
                                )
                            }
                        }
                    }
                }

                // Overlay images
                val density = LocalDensity.current
                gameState.value.board.forEach { (position, dove) ->
                    key(position) {
                        val offset = Offset(
                            (position.col - boardBounds.value.minCol + 1) * with(density) { cellSize.toPx() },
                            (position.row - boardBounds.value.minRow + 1) * with(density) { cellSize.toPx() }
                        )
                        Image(
                            painter = painterResource(id = dove.drawableResId),
                            contentDescription = dove.type.name,
                            modifier = Modifier
                                .size(cellSize)
                                .offset(
                                    with(density) { offset.x.toDp() },
                                    with(density) { offset.y.toDp() })
                                .then(
                                    if (dove.player == Player.PLAYER2) { // Flip red birds
                                        Modifier.scale(scaleX = -1f, scaleY = -1f)
                                    } else {
                                        Modifier
                                    }
                                )
                        )
                    }
                }
            }

            if (showDialog.value) {
                AlertDialog(
                    onDismissRequest = { showDialog.value = false },
                    title = {
                        Text(
                            text = selectedPosition.value?.let {
                                "Choose action for ${gameState.value.board[it]?.type?.name ?: "Selected"} Bird"
                            } ?: "Choose Action"
                        )
                    },
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            selectedPosition.value?.let { position ->
                                gameState.value.board[position]?.let { dove ->
                                    Image(
                                        painter = painterResource(id = dove.drawableResId),
                                        contentDescription = dove.type.name,
                                        modifier = Modifier.size(200.dp)
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            showDialog.value = false
                            moveMode.value = true
                        }) {
                            Text("Move")
                        }
                    },
                    dismissButton = {
                        if (gameState.value.board[selectedPosition.value]?.type != DoveType.BOSS) {
                            val updatedBoard = gameState.value.board.toMutableMap()
                            updatedBoard.remove(selectedPosition.value)

                            if (isMoveValidBoardState(updatedBoard)) {
                                Button(onClick = {
                                    showDialog.value = false
                                    selectedPosition.value?.let { position ->
                                        handleRemove(
                                            position,
                                            gameState,
                                            selectedPosition,
                                            player1Doves,
                                            player2Doves
                                        )
                                    }
                                }) {
                                    Text("Remove")
                                }
                            }
                        }
                    }
                )
            }
        }
        PlayerBirdLine(
            doves = player1Doves,
            selectedDove = selectedDove.value
        ) { dove, _ ->
            selectedDove.value = dove
        }
    }
}