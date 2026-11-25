package com.example.myapplication

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(zaleznosci: Zaleznosci) {
    AppTheme {
        ObserwatorCyklu("App")
        val viewModel = viewModel {
            PrzepisyViewModel(
                repozytorium = PrzepisyRepozytoriumOffline(
                    api = PrzepisyApiKtor(),
                    przepisDao = zaleznosci.baza.przepisDao(),
                    kategoriaDao = zaleznosci.baza.kategoriaDao()
                ),
                ustawienia = UstawieniaDataStore(zaleznosci.ustawienia)
            )
        }
        val stan by viewModel.stan.collectAsStateWithLifecycle()

        var zakladka by rememberSaveable { mutableIntStateOf(0) }
        var pokazFormularz by rememberSaveable { mutableStateOf(false) }
        var wybranyId by rememberSaveable { mutableStateOf<Int?>(null) }

        val wybranyPrzepis = stan.przepisy.firstOrNull { it.id == wybranyId }
        if (wybranyId != null && wybranyPrzepis == null) {
            wybranyId = null
        }

        if (pokazFormularz) {
            EkranFormularza(
                kategorie = stan.kategorie,
                zapisywanie = stan.zapisywanie,
                bladSieci = stan.blad,
                onWstecz = { pokazFormularz = false },
                onZapisz = { nazwa, skladniki, czas, katId ->
                    viewModel.dodaj(nazwa, skladniki, czas, katId) {
                        pokazFormularz = false
                    }
                }
            )
        } else {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Przepiśnik") },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                },
                bottomBar = {
                    NavigationBar {
                        NavigationBarItem(
                            selected = zakladka == 0,
                            onClick = { zakladka = 0 },
                            icon = { Text("🍲") },
                            label = { Text("Przepisy") }
                        )
                        NavigationBarItem(
                            selected = zakladka == 1,
                            onClick = { zakladka = 1 },
                            icon = { Text("📊") },
                            label = { Text("Statystyki") }
                        )
                        NavigationBarItem(
                            selected = zakladka == 2,
                            onClick = { zakladka = 2 },
                            icon = { Text("⚙") },
                            label = { Text("Ustawienia") }
                        )
                    }
                }
            ) { innerPadding ->
                Box(Modifier.padding(innerPadding).fillMaxSize()) {
                    when (zakladka) {
                        0 -> BoxWithConstraints(Modifier.fillMaxSize()) {
                            val szeroki = maxWidth >= 600.dp
                            if (szeroki) {
                                Row(Modifier.fillMaxSize()) {
                                    EkranListy(
                                        stan = stan,
                                        onWybierz = { wybranyId = it },
                                        onDodaj = { pokazFormularz = true },
                                        onPonow = viewModel::odswiez,
                                        onUsun = { id ->
                                            if (wybranyId == id) wybranyId = null
                                            viewModel.usun(id)
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (wybranyPrzepis != null) {
                                        EkranSzczegolow(
                                            przepis = wybranyPrzepis,
                                            pokazWstecz = false,
                                            onPrzelaczUgotowane = viewModel::przelaczUgotowane,
                                            onUsun = { id ->
                                                wybranyId = null
                                                viewModel.usun(id)
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    } else {
                                        Pusto("Wybierz przepis z listy", Modifier.weight(1f))
                                    }
                                }
                            } else if (wybranyPrzepis != null) {
                                EkranSzczegolow(
                                    przepis = wybranyPrzepis,
                                    pokazWstecz = true,
                                    onWstecz = { wybranyId = null },
                                    onPrzelaczUgotowane = viewModel::przelaczUgotowane,
                                    onUsun = { id ->
                                        wybranyId = null
                                        viewModel.usun(id)
                                    }
                                )
                            } else {
                                EkranListy(
                                    stan = stan,
                                    onWybierz = { wybranyId = it },
                                    onDodaj = { pokazFormularz = true },
                                    onPonow = viewModel::odswiez,
                                    onUsun = viewModel::usun
                                )
                            }
                        }
                        1 -> EkranStatystyk(stan = stan)
                        2 -> EkranUstawien(
                            stan = stan,
                            onZmienKolejnosc = viewModel::zmienKolejnosc,
                            onZmienMaksCzas = viewModel::zmienMaksCzas,
                            onOdswiezZApi = viewModel::odswiez,
                            onWyczyscWszystko = viewModel::wyczyscWszystko
                        )
                    }
                }
            }
        }
    }
}