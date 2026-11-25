package com.example.myapplication

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

data class KryteriaFiltrowania(
    val fraza: String = "",
    val kategoriaId: Int = 0,
    val tylkoUgotowane: Boolean = false
)

val KryteriaFiltrowaniaSaver = listSaver<KryteriaFiltrowania, Any>(
    save = { listOf(it.fraza, it.kategoriaId, it.tylkoUgotowane) },
    restore = {
        KryteriaFiltrowania(
            fraza = it[0] as String,
            kategoriaId = it[1] as Int,
            tylkoUgotowane = it[2] as Boolean
        )
    }
)

@Composable
fun EkranListy(
    stan: StanPrzepisow,
    onWybierz: (Int) -> Unit = {},
    onDodaj: () -> Unit = {},
    onPonow: () -> Unit = {},
    onUsun: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    ObserwatorCyklu("EkranListy")
    var filtry by rememberSaveable(stateSaver = KryteriaFiltrowaniaSaver) {
        mutableStateOf(KryteriaFiltrowania())
    }
    var doUsuniecia by remember { mutableStateOf<Przepis?>(null) }

    val widoczne = stan.przepisy.filter { przepis ->
        val pasujeFraza = filtry.fraza.isBlank() ||
                przepis.nazwa.contains(filtry.fraza, ignoreCase = true) ||
                przepis.skladniki.contains(filtry.fraza, ignoreCase = true)
        val pasujeKat = filtry.kategoriaId == 0 || przepis.kategoriaId == filtry.kategoriaId
        val pasujeStan = !filtry.tylkoUgotowane || przepis.ugotowane
        pasujeFraza && pasujeKat && pasujeStan
    }

    doUsuniecia?.let { przepis ->
        AlertDialog(
            onDismissRequest = { doUsuniecia = null },
            title = { Text("Usuń przepis") },
            text = { Text("Czy na pewno chcesz usunąć \"${przepis.nazwa}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    onUsun(przepis.id)
                    doUsuniecia = null
                }) { Text("Usuń") }
            },
            dismissButton = {
                TextButton(onClick = { doUsuniecia = null }) { Text("Anuluj") }
            }
        )
    }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = onDodaj) {
                Text("+", style = MaterialTheme.typography.headlineSmall)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            CzasWAplikacji(Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
            OutlinedTextField(
                value = filtry.fraza,
                onValueChange = { filtry = filtry.copy(fraza = it) },
                label = { Text("Szukaj przepisu lub składnika") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filtry.kategoriaId == 0,
                    onClick = { filtry = filtry.copy(kategoriaId = 0) },
                    label = { Text("Wszystkie") }
                )
                stan.kategorie.forEach { kat ->
                    FilterChip(
                        selected = filtry.kategoriaId == kat.id,
                        onClick = { filtry = filtry.copy(kategoriaId = kat.id) },
                        label = { Text(kat.nazwa) }
                    )
                }
                FilterChip(
                    selected = filtry.tylkoUgotowane,
                    onClick = { filtry = filtry.copy(tylkoUgotowane = !filtry.tylkoUgotowane) },
                    label = { Text("Tylko ugotowane") }
                )
            }

            when {
                stan.ladowanie && stan.przepisy.isEmpty() ->
                    Ladowanie(Modifier.weight(1f))
                stan.blad != null && stan.przepisy.isEmpty() ->
                    Blad(stan.blad, onPonow, Modifier.weight(1f))
                widoczne.isEmpty() ->
                    Pusto(
                        tekst = if (stan.przepisy.isEmpty())
                            "Brak przepisów. Dotknij +, aby dodać pierwszy."
                        else
                            "Brak przepisów pasujących do filtrów.",
                        modifier = Modifier.weight(1f)
                    )
                else -> {
                    stan.blad?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(widoczne, key = { it.id }) { przepis ->
                            KartaPrzepisu(
                                przepis = przepis,
                                onClick = { onWybierz(przepis.id) },
                                onUsun = { doUsuniecia = przepis }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun EkranListyJasny() {
    AppTheme(darkTheme = false) {
        EkranListy(StanPrzepisow(przepisy = przykladowePrzepisy))
    }
}

@Preview
@Composable
fun EkranListyCiemny() {
    AppTheme(darkTheme = true) {
        EkranListy(StanPrzepisow(przepisy = przykladowePrzepisy))
    }
}

@Preview
@Composable
fun EkranListyBlad() {
    AppTheme {
        EkranListy(StanPrzepisow(blad = "Brak połączenia z Internetem."))
    }
}