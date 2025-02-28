package com.example.birdgame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
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
                        contentScale = ContentScale.Crop, // Or ContentScale.FillBounds
                        alpha = 0.8f
                    )

                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.Transparent
                    ) {
                        KakawBoard()
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
        KakawBoard()
    }
}