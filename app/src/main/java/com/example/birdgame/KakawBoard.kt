package com.example.birdgame

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.example.birdgame.model.Bird
import com.example.birdgame.model.BirdType
import com.example.birdgame.model.BoardBounds
import com.example.birdgame.model.GameState
import com.example.birdgame.model.MAX_BOARD_HEIGHT
import com.example.birdgame.model.MAX_BOARD_WIDTH
import com.example.birdgame.model.Player
import com.example.birdgame.model.Position
import com.example.birdgame.model.calculateBoardBounds
import com.example.birdgame.model.getBirdDrawableResId

fun initializeGame(): GameState {
    val boardWidth = 3
    val boardHeight = 4
    val board = mutableMapOf<Position, Bird>()

    board[Position(2, 1)] =
        Bird(BirdType.BOSS, Player.PLAYER1, getBirdDrawableResId(BirdType.BOSS, Player.PLAYER1))
    board[Position(1, 1)] =
        Bird(BirdType.BOSS, Player.PLAYER2, getBirdDrawableResId(BirdType.BOSS, Player.PLAYER2))

    return GameState(board, boardWidth, boardHeight, Player.PLAYER1)
}

private fun createPlayerBirds(player: Player): List<Bird> {
    return listOf(
        Bird(BirdType.REGULAR, player, getBirdDrawableResId(BirdType.REGULAR, player)),
        Bird(BirdType.SHOOTER, player, getBirdDrawableResId(BirdType.SHOOTER, player)),
        Bird(BirdType.HIT_MAN, player, getBirdDrawableResId(BirdType.HIT_MAN, player)),
        Bird(BirdType.AGENT, player, getBirdDrawableResId(BirdType.AGENT, player)),
        Bird(BirdType.BOMBER, player, getBirdDrawableResId(BirdType.BOMBER, player))
    )
}

@Composable
fun PlayerBirdLine(
    birds: List<Bird>, selectedBird: Bird?, onBirdClick: (Bird, Offset) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        birds.forEach { bird ->
            Box(modifier = Modifier
                .size(80.dp)
                .clickable {
                    onBirdClick(
                        bird, Offset.Zero
                    )
                }
                .border(
                    width = if (bird == selectedBird) 2.dp else 0.dp,
                    color = if (bird == selectedBird) Color.Green else Color.Transparent,
                    shape = RectangleShape
                )) {
                Image(
                    painter = painterResource(id = bird.drawableResId),
                    contentDescription = bird.type.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (bird.player == Player.PLAYER2) { // Flip red birds
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
fun Ka_kawBoard() {
    val gameState = remember { mutableStateOf(initializeGame()) }
    val boardBounds = remember { derivedStateOf { calculateBoardBounds(gameState.value.board) } }
    val player1Birds = remember { mutableStateListOf<Bird>() }
    val player2Birds = remember { mutableStateListOf<Bird>() }
    val selectedBird = remember { mutableStateOf<Bird?>(null) }
    val selectedPosition = remember { mutableStateOf<Position?>(null) }
    val showDialog = remember { mutableStateOf(false) }
    val moveMode = remember { mutableStateOf(false) }
    val showWinDialog = remember { mutableStateOf<Player?>(null) }
    LaunchedEffect(Unit) {
        player1Birds.addAll(createPlayerBirds(Player.PLAYER1))
        player2Birds.addAll(createPlayerBirds(Player.PLAYER2))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .pointerInput(Unit) {
                detectTapGestures { _ ->
                    selectedBird.value = null
                }
            }) {
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
            birds = player2Birds,
            selectedBird = selectedBird.value
        ) { bird, _ ->
            selectedBird.value = bird
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
            val horizontalScrollState = rememberScrollState()
            val context = LocalContext.current

            Box(
                modifier = Modifier
                    .size(boardWidthDp, boardHeightDp)
                    .horizontalScroll(horizontalScrollState)
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
                                    onWin = { winner -> showWinDialog.value = winner }
                                )
                            } else {
                                val bird = gameState.value.board[position]
                                if (bird != null && bird.player == gameState.value.currentPlayer) {
                                    selectedPosition.value = position
                                    showDialog.value = true
                                } else {
                                    handleTap(
                                        position,
                                        gameState,
                                        selectedPosition,
                                        selectedBird,
                                        player1Birds,
                                        player2Birds,
                                        context,
                                        onWin = { winner -> showWinDialog.value = winner }
                                    )
                                }
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.size(boardWidthDp, boardHeightDp)) {
                    val density = Density(density = this.density, fontScale = 1f)
                    drawRect(Color.Transparent, Offset.Zero, size)
                    for (row in 0..boardHeight) {
                        drawLine(
                            color = Color(0xFFBDBCBC),
                            start = Offset(0f, row * cellSize.toPx()),
                            end = Offset(boardWidthDp.toPx(), row * cellSize.toPx()),
                            strokeWidth = 5f
                        )
                    }
                    for (col in 0..boardWidth) {
                        drawLine(
                            color = Color(0xFFBDBCBC),
                            start = Offset(col * cellSize.toPx(), 0f),
                            end = Offset(col * cellSize.toPx(), boardHeightDp.toPx()),
                            strokeWidth = 5f
                        )
                    }

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
                                    drawSquare(this, offset, cellSize, Color(0xFFBDBCBC), density)
                                } else if (selectedBird.value != null && isPlacementValid(
                                        position,
                                        gameState.value.board,
                                        selectedBird.value!!.player
                                    )
                                ) {
                                    drawSquare(this, offset, cellSize, Color(0xFFBDBCBC), density)
                                } else {
                                    drawSquare(this, offset, cellSize, Color.Transparent, density)
                                }
                            } else {
                                drawSquare(this, offset, cellSize, Color.Transparent, density)
                            }
                        }
                    }
                }

                // Overlay images
                gameState.value.board.forEach { (position, bird) ->
                    key(position) {
                        DrawBird(position, bird, boardBounds.value, cellSize)
                    }
                }
            }
            BirdActionDialog(showDialog = showDialog.value,
                selectedPosition = selectedPosition.value,
                gameState = gameState.value,
                onMove = {
                    showDialog.value = false
                    moveMode.value = true
                },
                onRemove = {
                    showDialog.value = false
                    selectedPosition.value?.let { position ->
                        handleRemove(
                            position,
                            gameState,
                            selectedPosition,
                            player1Birds,
                            player2Birds,
                            onWin = { winner -> showWinDialog.value = winner }
                        )
                    }
                },
                onDismiss = { showDialog.value = false }
            )
            if (showWinDialog.value != null) {
                AlertDialog(
                    onDismissRequest = { showWinDialog.value = null },
                    title = { Text("Game Over") },
                    text = { Text("${if (showWinDialog.value == Player.PLAYER1) "Green" else "Red"} Player Wins!") },
                    confirmButton = {
                        Button(onClick = { showWinDialog.value = null }) {
                            Text("OK")
                        }
                    }
                )
            }
        }
        PlayerBirdLine(
            birds = player1Birds,
            selectedBird = selectedBird.value
        ) { bird, _ ->
            selectedBird.value = bird
        }
    }
}

@Composable
fun BirdActionDialog(
    showDialog: Boolean,
    selectedPosition: Position?,
    gameState: GameState,
    onMove: () -> Unit,
    onRemove: () -> Unit,
    onDismiss: () -> Unit
) {
    if (showDialog && selectedPosition != null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Choose action for ${gameState.board[selectedPosition]?.type?.name ?: "Selected"} Bird"
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    gameState.board[selectedPosition]?.let { bird ->
                        Image(
                            painter = painterResource(id = bird.drawableResId),
                            contentDescription = bird.type.name,
                            modifier = Modifier.size(200.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = onMove) {
                    Text("Move")
                }
            },
            dismissButton = {
                if (gameState.board[selectedPosition]?.type != BirdType.BOSS) {
                    val updatedBoard = gameState.board.toMutableMap()
                    updatedBoard.remove(selectedPosition)

                    if (isMoveValidBoardState(updatedBoard)) {
                        Button(onClick = onRemove) {
                            Text("Remove")
                        }
                    }
                }
            }
        )
    }
}

private fun drawSquare(
    drawScope: DrawScope, offset: Offset, cellSize: Dp, highlightColor: Color, density: Density
) {
    with(density) {
        drawScope.drawRect(
            color = highlightColor, topLeft = offset, size = Size(cellSize.toPx(), cellSize.toPx())
        )
    }
}

@Composable
fun DrawBird(
    position: Position,
    bird: Bird,
    boardBounds: BoardBounds,
    cellSize: Dp
) {
    val density = LocalDensity.current
    val offset = Offset(
        (position.col - boardBounds.minCol + 1) * with(density) { cellSize.toPx() },
        (position.row - boardBounds.minRow + 1) * with(density) { cellSize.toPx() }
    )
    Image(
        painter = painterResource(id = bird.drawableResId),
        contentDescription = bird.type.name,
        modifier = Modifier
            .size(cellSize)
            .offset(
                with(density) { offset.x.toDp() },
                with(density) { offset.y.toDp() })
            .then(
                if (bird.player == Player.PLAYER2) { // Flip red birds
                    Modifier.scale(scaleX = -1f, scaleY = -1f)
                } else {
                    Modifier
                }
            )
    )
}