package com.example.myapplication

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okio.Path.Companion.toPath

const val NAZWA_PLIKU_USTAWIEN = "ustawienia_przepisnik.preferences_pb"

fun tworzUstawienia(sciezka: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { sciezka().toPath() }
    )

interface Ustawienia {
    val najnowszeNajpierw: Flow<Boolean>
    val maksCzasMinuty: Flow<Int>
    suspend fun ustawKolejnosc(najnowsze: Boolean)
    suspend fun ustawMaksCzas(minuty: Int)
    suspend fun wyczysc()
}

class UstawieniaDataStore(
    private val magazyn: DataStore<Preferences>
) : Ustawienia {
    private val kluczKolejnosc = booleanPreferencesKey("najnowsze_najpierw")
    private val kluczMaksCzas = intPreferencesKey("maks_czas_minuty")

    override val najnowszeNajpierw: Flow<Boolean> =
        magazyn.data.map { it[kluczKolejnosc] ?: true }

    override val maksCzasMinuty: Flow<Int> =
        magazyn.data.map { it[kluczMaksCzas] ?: 120 }

    override suspend fun ustawKolejnosc(najnowsze: Boolean) {
        magazyn.edit { it[kluczKolejnosc] = najnowsze }
    }

    override suspend fun ustawMaksCzas(minuty: Int) {
        magazyn.edit { it[kluczMaksCzas] = minuty }
    }

    override suspend fun wyczysc() {
        magazyn.edit { it.clear() }
    }
}

class Zaleznosci(
    val baza: BazaDanych,
    val ustawienia: DataStore<Preferences>
)