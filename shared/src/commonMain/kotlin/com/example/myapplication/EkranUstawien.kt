package com.example.myapplication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun EkranUstawien(
    stan: StanPrzepisow,
    onZmienKolejnosc: (Boolean) -> Unit = {},
    onZmienMaksCzas: (Int) -> Unit = {},
    onOdswiezZApi: () -> Unit = {},
    onWyczyscWszystko: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    ObserwatorCyklu("EkranUstawien")
    var potwierdzCzyszczenie by remember { mutableStateOf(false) }

    if (potwierdzCzyszczenie) {
        AlertDialog(
            onDismissRequest = { potwierdzCzyszczenie = false },
            title = { Text("Usuń wszystkie dane") },
            text = { Text("Spowoduje to usunięcie wszystkich przepisów z bazy i przywrócenie domyślnych ustawień.") },
            confirmButton = {
                TextButton(onClick = {
                    potwierdzCzyszczenie = false
                    onWyczyscWszystko()
                }) { Text("Usuń wszystko") }
            },
            dismissButton = {
                TextButton(onClick = { potwierdzCzyszczenie = false }) { Text("Anuluj") }
            }
        )
    }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Ustawienia", style = MaterialTheme.typography.titleLarge)

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Najnowsze przepisy najpierw",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = stan.najnowszeNajpierw,
                onCheckedChange = onZmienKolejnosc
            )
        }

        HorizontalDivider()

        Column {
            Text(
                text = "Maksymalny czas przygotowania: ${stan.maksCzasMinuty} min",
                style = MaterialTheme.typography.bodyLarge
            )
            Slider(
                value = stan.maksCzasMinuty.toFloat(),
                onValueChange = { onZmienMaksCzas(it.toInt()) },
                valueRange = 15f..180f,
                steps = 10
            )
        }

        HorizontalDivider()

        Button(
            onClick = onOdswiezZApi,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Pobierz ponownie przepisy z API")
        }

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = { potwierdzCzyszczenie = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Usuń wszystkie dane użytkownika", color = MaterialTheme.colorScheme.error)
        }
    }
}

@Preview
@Composable
fun EkranUstawienPodglad() {
    AppTheme {
        EkranUstawien(StanPrzepisow())
    }
}