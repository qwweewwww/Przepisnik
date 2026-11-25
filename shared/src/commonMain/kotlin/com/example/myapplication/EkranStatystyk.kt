package com.example.myapplication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun EkranStatystyk(
    stan: StanPrzepisow,
    modifier: Modifier = Modifier
) {
    ObserwatorCyklu("EkranStatystyk")
    val liczba = stan.przepisy.size
    val ugotowane = stan.przepisy.count { it.ugotowane }
    val sredniCzas = if (liczba > 0) stan.przepisy.sumOf { it.czasMinuty } / liczba else 0
    val postep = if (liczba > 0) ugotowane.toFloat() / liczba else 0f

    BoxWithConstraints(modifier.fillMaxSize()) {
        val szeroki = maxWidth >= 600.dp
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Podsumowanie przepisów", style = MaterialTheme.typography.titleLarge)

            if (szeroki) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    KartaStatystyki("Wszystkie przepisy", "$liczba", Modifier.weight(1f))
                    KartaStatystyki("Ugotowane", "$ugotowane z $liczba", Modifier.weight(1f))
                }
            } else {
                KartaStatystyki("Wszystkie przepisy", "$liczba")
                KartaStatystyki("Ugotowane", "$ugotowane z $liczba")
            }

            KartaStatystyki("Średni czas przygotowania", "$sredniCzas min")

            Spacer(Modifier.height(4.dp))
            Text(
                text = "Postęp gotowania: ${(postep * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium
            )
            LinearProgressIndicator(
                progress = { postep },
                modifier = Modifier.fillMaxWidth().height(10.dp)
            )

            Spacer(Modifier.height(8.dp))
            Text("Według kategorii", style = MaterialTheme.typography.titleMedium)
            stan.kategorie.forEach { kat ->
                val ile = stan.przepisy.count { it.kategoriaId == kat.id }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(kat.nazwa, style = MaterialTheme.typography.bodyLarge)
                    Text("$ile", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Preview
@Composable
fun EkranStatystykPodglad() {
    AppTheme {
        EkranStatystyk(StanPrzepisow(przepisy = przykladowePrzepisy))
    }
}