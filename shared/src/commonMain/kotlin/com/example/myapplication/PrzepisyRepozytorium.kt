package com.example.myapplication

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

interface PrzepisyRepozytorium {
    val kategorie: Flow<List<Kategoria>>
    val przepisy: Flow<List<Przepis>>
    suspend fun inicjalizujKategorie()
    suspend fun odswiez()
    suspend fun dodaj(nazwa: String, skladniki: String, czasMinuty: Int, kategoriaId: Int)
    suspend fun aktualizuj(przepis: Przepis)
    suspend fun przelaczUgotowane(przepis: Przepis)
    suspend fun usun(id: Int)
    suspend fun wyczyscWszystko()
}

class PrzepisyRepozytoriumOffline(
    private val api: PrzepisyApi,
    private val przepisDao: PrzepisDao,
    private val kategoriaDao: KategoriaDao
) : PrzepisyRepozytorium {
    override val kategorie: Flow<List<Kategoria>> =
        kategoriaDao.obserwuj().map { lista -> lista.map { it.naModel() } }

    override val przepisy: Flow<List<Przepis>> =
        combine(przepisDao.obserwuj(), kategoriaDao.obserwuj()) { przepisyLista, kategorieLista ->
            val mapaKategorii = kategorieLista.associate { it.id to it.nazwa }
            przepisyLista.map { it.naModel(mapaKategorii) }
        }

    override suspend fun inicjalizujKategorie() {
        kategoriaDao.wstawWszystkie(domyslneKategorie.map { it.naEncje() })
    }

    override suspend fun odswiez() {
        inicjalizujKategorie()
        val pobrane = api.pobierz()
        przepisDao.wstawWszystkie(pobrane.map { it.naEncje() })
    }

    override suspend fun dodaj(
        nazwa: String,
        skladniki: String,
        czasMinuty: Int,
        kategoriaId: Int
    ) {
        inicjalizujKategorie()
        runCatching { api.dodaj(nazwa, skladniki, czasMinuty, kategoriaId) }
        przepisDao.wstaw(
            PrzepisEntity(
                nazwa = nazwa.trim(),
                skladniki = skladniki.trim(),
                czasMinuty = czasMinuty,
                kategoriaId = kategoriaId,
                ugotowane = false
            )
        )
    }

    override suspend fun aktualizuj(przepis: Przepis) {
        przepisDao.aktualizuj(przepis.naEncje())
    }

    override suspend fun przelaczUgotowane(przepis: Przepis) {
        przepisDao.aktualizuj(przepis.copy(ugotowane = !przepis.ugotowane).naEncje())
    }

    override suspend fun usun(id: Int) {
        przepisDao.usun(id)
    }

    override suspend fun wyczyscWszystko() {
        przepisDao.wyczysc()
    }
}