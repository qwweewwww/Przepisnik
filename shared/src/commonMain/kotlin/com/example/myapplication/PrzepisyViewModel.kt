package com.example.myapplication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.serialization.ContentConvertException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StanPrzepisow(
    val przepisy: List<Przepis> = emptyList(),
    val kategorie: List<Kategoria> = domyslneKategorie,
    val najnowszeNajpierw: Boolean = true,
    val maksCzasMinuty: Int = 120,
    val ladowanie: Boolean = false,
    val zapisywanie: Boolean = false,
    val blad: String? = null
)

private data class StanOperacji(
    val ladowanie: Boolean = false,
    val zapisywanie: Boolean = false,
    val blad: String? = null
)

class PrzepisyViewModel(
    private val repozytorium: PrzepisyRepozytorium,
    private val ustawienia: Ustawienia
) : ViewModel() {
    private val operacje = MutableStateFlow(StanOperacji())

    val stan: StateFlow<StanPrzepisow> = combine(
        repozytorium.przepisy,
        repozytorium.kategorie,
        ustawienia.najnowszeNajpierw,
        ustawienia.maksCzasMinuty,
        operacje
    ) { przepisy, kategorie, najnowsze, maksCzas, op ->
        val posortowane = if (najnowsze) przepisy else przepisy.reversed()
        val przefiltrowane = posortowane.filter { it.czasMinuty <= maksCzas }
        StanPrzepisow(
            przepisy = przefiltrowane,
            kategorie = if (kategorie.isEmpty()) domyslneKategorie else kategorie,
            najnowszeNajpierw = najnowsze,
            maksCzasMinuty = maksCzas,
            ladowanie = op.ladowanie,
            zapisywanie = op.zapisywanie,
            blad = op.blad
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StanPrzepisow(ladowanie = true)
    )

    init {
        odswiez()
    }

    fun odswiez() = uruchom {
        operacje.update { it.copy(ladowanie = true, blad = null) }
        try {
            repozytorium.odswiez()
        } finally {
            operacje.update { it.copy(ladowanie = false) }
        }
    }

    fun dodaj(
        nazwa: String,
        skladniki: String,
        czasMinuty: Int,
        kategoriaId: Int,
        poZapisie: () -> Unit
    ) = uruchom {
        operacje.update { it.copy(zapisywanie = true, blad = null) }
        try {
            repozytorium.dodaj(nazwa, skladniki, czasMinuty, kategoriaId)
            poZapisie()
        } finally {
            operacje.update { it.copy(zapisywanie = false) }
        }
    }

    fun przelaczUgotowane(przepis: Przepis) = uruchom {
        repozytorium.przelaczUgotowane(przepis)
    }

    fun usun(id: Int) = uruchom {
        repozytorium.usun(id)
    }

    fun zmienKolejnosc(najnowsze: Boolean) = uruchom {
        ustawienia.ustawKolejnosc(najnowsze)
    }

    fun zmienMaksCzas(minuty: Int) = uruchom {
        ustawienia.ustawMaksCzas(minuty)
    }

    fun wyczyscWszystko() = uruchom {
        repozytorium.wyczyscWszystko()
        ustawienia.wyczysc()
    }

    private fun uruchom(praca: suspend () -> Unit) {
        viewModelScope.launch {
            operacje.update { it.copy(blad = null) }
            try {
                praca()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                operacje.update { it.copy(blad = opisBledu(e)) }
            }
        }
    }
}

internal fun opisBledu(e: Exception): String = when (e) {
    is HttpRequestTimeoutException ->
        "Serwer nie odpowiada. Spróbuj ponownie."
    is ClientRequestException ->
        "Żądanie odrzucone (kod ${e.response.status.value})."
    is ServerResponseException ->
        "Błąd serwera (kod ${e.response.status.value})."
    is ContentConvertException ->
        "Odpowiedź serwera ma nieoczekiwany format."
    else ->
        "Brak połączenia z Internetem."
}