package com.example.myapplication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EkranSzczegolow(
    przepis: Przepis,
    pokazWstecz: Boolean = true,
    onWstecz: () -> Unit = {},
    onPrzelaczUgotowane: (Przepis) -> Unit = {},
    onUsun: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    ObserwatorCyklu("EkranSzczegolow")
    var potwierdzUsuniecie by remember { mutableStateOf(false) }

    if (potwierdzUsuniecie) {
        AlertDialog(
            onDismissRequest = { potwierdzUsuniecie = false },
            title = { Text("Usuń przepis") },
            text = { Text("Czy na pewno chcesz usunąć \"${przepis.nazwa}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    potwierdzUsuniecie = false
                    onUsun(przepis.id)
                }) { Text("Usuń") }
            },
            dismissButton = {
                TextButton(onClick = { potwierdzUsuniecie = false }) { Text("Anuluj") }
            }
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(przepis.nazwa) },
                navigationIcon = {
                    if (pokazWstecz) {
                        TextButton(onClick = onWstecz) { Text("Wstecz") }
                    }
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
            Text(
                text = "Kategoria: ${przepis.kategoriaNazwa}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Czas przygotowania: ${przepis.czasMinuty} min",
                style = MaterialTheme.typography.bodyLarge
            )
            HorizontalDivider()
            Text(
                text = "Składniki",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = przepis.skladniki,
                style = MaterialTheme.typography.bodyLarge
            )
            HorizontalDivider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Oznacz jako ugotowane",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = przepis.ugotowane,
                    onCheckedChange = { onPrzelaczUgotowane(przepis) }
                )
            }
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = { potwierdzUsuniecie = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Usuń przepis", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Preview
@Composable
fun EkranSzczegolowPodglad() {
    AppTheme {
        EkranSzczegolow(przepis = przykladowePrzepisy.first())
    }
}