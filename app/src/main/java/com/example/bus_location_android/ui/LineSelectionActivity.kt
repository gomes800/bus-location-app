package com.example.bus_location_android.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Space
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.provider.FontsContractCompat.Columns

class LineSelectionActivity : ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent{
            LineSelectionScreen { selectedLine ->
                val intent = Intent(this, MapsActivity::class.java).apply {
                    putExtra("SELECTED_LINE", selectedLine)
                }
                startActivity(intent)
                finish()
            }
        }
    }
}

@Composable
fun LineSelectionScreen(onLineSelected: (String) -> Unit) {
    var textState by remember { mutableStateOf(TextFieldValue()) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Digite a linha do ônibus: ")
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = textState,
            onValueChange = { textState = it },
            label = { Text("Número da linha") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onLineSelected(textState.text) }) {
            Text("Confirmar")
        }
    }
}