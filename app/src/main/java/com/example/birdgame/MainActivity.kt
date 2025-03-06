package com.example.birdgame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.birdgame.ui.theme.BirdGameTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BirdGameTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.background2),
                        contentDescription = "App Background",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = 0.8f
                    )
                    val showStartScreen = remember { mutableStateOf(true) }

                    if (showStartScreen.value) {
                        StartScreen(
                            onNewGameClick = {
                                showStartScreen.value = false
                            },
                            onContinueGameClick = {
                                showStartScreen.value = false
                            }
                        )
                    } else {
                        Surface(modifier = Modifier.fillMaxSize(), color = Color.Transparent) {
                            Ka_kawBoard()
                        }
                    }
                }
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun BirdGamePreview() {
        BirdGameTheme {
            Ka_kawBoard()
        }
    }
}