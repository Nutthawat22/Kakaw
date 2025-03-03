package com.example.birdgame.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.birdgame.R

@Composable
fun StartScreen(onNewGameClick: () -> Unit, onContinueGameClick: () -> Unit) {
    Image(
        painter = painterResource(id = R.drawable.startbg),
        contentDescription = "App Background",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
        alpha = 0.8f
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(148.dp))

        Button(
            onClick = onNewGameClick, colors = ButtonColors(
                Color(0xFF912856),
                Color(0xFF912856),
                Color(0xFF912856),
                Color(0xFF912856),
            )
        ) {
            Text(
                text = "NEW GAME", fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onContinueGameClick, colors = ButtonColors(
                Color(0xFF23674E),
                Color(0xFF23674E),
                Color(0xFF23674E),
                Color(0xFF23674E),
            )
        ) {
            Text(
                text = "continue", fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}