package com.example.myapplication

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

interface PrzepisyApi {
    suspend fun pobierz(): List<Przepis>
    suspend fun dodaj(nazwa: String, skladniki: String, czasMinuty: Int, kategoriaId: Int): Przepis
}

class PrzepisyApiKtor(
    private val klient: HttpClient = tworzKlienta()
) : PrzepisyApi {
    override suspend fun pobierz(): List<Przepis> =
        klient.get("$ADRES/recipes") {
            parameter("limit", 15)
        }.body<PrzepisyOdpowiedzDto>().recipes.map { it.naModel() }

    override suspend fun dodaj(
        nazwa: String,
        skladniki: String,
        czasMinuty: Int,
        kategoriaId: Int
    ): Przepis {
        val dto = klient.post("$ADRES/recipes/add") {
            contentType(ContentType.Application.Json)
            setBody(
                NowyPrzepisDto(
                    name = nazwa,
                    ingredients = skladniki.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                    prepTimeMinutes = czasMinuty / 2,
                    cookTimeMinutes = czasMinuty - (czasMinuty / 2)
                )
            )
        }.body<PrzepisDto>()
        val katNazwa = domyslneKategorie.firstOrNull { it.id == kategoriaId }?.nazwa ?: "Obiad"
        return Przepis(
            id = if (dto.id > 0) dto.id else 0,
            nazwa = nazwa,
            skladniki = skladniki,
            czasMinuty = czasMinuty,
            kategoriaId = kategoriaId,
            kategoriaNazwa = katNazwa,
            ugotowane = false
        )
    }

    private companion object {
        const val ADRES = "https://dummyjson.com"
    }
}