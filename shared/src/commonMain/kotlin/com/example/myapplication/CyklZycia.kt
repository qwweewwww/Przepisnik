package com.example.myapplication

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import kotlinx.coroutines.delay

@Composable
fun ObserwatorCyklu(nazwa: String) {
    DisposableEffect(Unit) {
        println("CYKL [$nazwa]: kompozycja dodana")
        onDispose { println("CYKL [$nazwa]: kompozycja usunięta") }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        println("CYKL [$nazwa]: ON_START")
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        println("CYKL [$nazwa]: ON_RESUME")
    }
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        println("CYKL [$nazwa]: ON_PAUSE")
    }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        println("CYKL [$nazwa]: ON_STOP")
    }
}

@Composable
fun CzasWAplikacji(modifier: Modifier = Modifier) {
    var sekundy by rememberSaveable { mutableStateOf(0) }
    var widoczna by remember { mutableStateOf(true) }

    LifecycleEventEffect(Lifecycle.Event.ON_START) { widoczna = true }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { widoczna = false }

    LaunchedEffect(widoczna) {
        while (widoczna) {
            delay(1000)
            sekundy++
        }
    }

    Text(
        text = "Czas w aplikacji: $sekundy s",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}