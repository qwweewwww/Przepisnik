package com.example.myapplication

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

data class DaneFormularza(
    val nazwa: String = "",
    val skladniki: String = "",
    val czasTekst: String = "20",
    val kategoriaId: Int = 1
)

val DaneFormularzaSaver = listSaver<DaneFormularza, Any>(
    save = { listOf(it.nazwa, it.skladniki, it.czasTekst, it.kategoriaId) },
    restore = {
        DaneFormularza(
            nazwa = it[0] as String,
            skladniki = it[1] as String,
            czasTekst = it[2] as String,
            kategoriaId = it[3] as Int
        )
    }
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EkranFormularza(
    kategorie: List<Kategoria> = domyslneKategorie,
    zapisywanie: Boolean = false,
    bladSieci: String? = null,
    onWstecz: () -> Unit = {},
    onZapisz: (String, String, Int, Int) -> Unit = { _, _, _, _ -> }
) {
    ObserwatorCyklu("EkranFormularza")
    var dane by rememberSaveable(stateSaver = DaneFormularzaSaver) {
        mutableStateOf(DaneFormularza())
    }
    var pokazBledy by rememberSaveable { mutableStateOf(false) }

    val czasLiczba = dane.czasTekst.toIntOrNull()
    val bladNazwy = pokazBledy && dane.nazwa.isBlank()
    val bladSkladnikow = pokazBledy && dane.skladniki.isBlank()
    val bladCzasu = pokazBledy && (czasLiczba == null || czasLiczba <= 0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nowy przepis") },
                navigationIcon = {
                    TextButton(onClick = onWstecz) { Text("Wstecz") }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = dane.nazwa,
                onValueChange = { dane = dane.copy(nazwa = it) },
                label = { Text("Nazwa przepisu *") },
                isError = bladNazwy,
                supportingText = if (bladNazwy) {
                    { Text("Podaj nazwę przepisu") }
                } else null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = dane.skladniki,
                onValueChange = { dane = dane.copy(skladniki = it) },
                label = { Text("Składniki (oddzielone przecinkiem) *") },
                isError = bladSkladnikow,
                supportingText = if (bladSkladnikow) {
                    { Text("Wpisz przynajmniej jeden składnik") }
                } else null,
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = dane.czasTekst,
                onValueChange = { dane = dane.copy(czasTekst = it) },
                label = { Text("Czas przygotowania (minuty) *") },
                isError = bladCzasu,
                supportingText = if (bladCzasu) {
                    { Text("Podaj liczbę większą od 0") }
                } else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Kategoria", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                kategorie.forEach { kat ->
                    FilterChip(
                        selected = dane.kategoriaId == kat.id,
                        onClick = { dane = dane.copy(kategoriaId = kat.id) },
                        label = { Text(kat.nazwa) }
                    )
                }
            }

            bladSieci?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    pokazBledy = true
                    if (dane.nazwa.isNotBlank() && dane.skladniki.isNotBlank() && czasLiczba != null && czasLiczba > 0) {
                        onZapisz(dane.nazwa, dane.skladniki, czasLiczba, dane.kategoriaId)
                    }
                },
                enabled = !zapisywanie,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (zapisywanie) "Zapisywanie..." else "Zapisz przepis")
            }
        }
    }
}

@Preview
@Composable
fun EkranFormularzaPodglad() {
    AppTheme {
        EkranFormularza()
    }
}