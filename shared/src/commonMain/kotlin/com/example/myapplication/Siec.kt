package com.example.myapplication

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class PrzepisyOdpowiedzDto(
    val recipes: List<PrzepisDto> = emptyList()
)

@Serializable
data class PrzepisDto(
    val id: Int = 0,
    val name: String = "",
    val ingredients: List<String> = emptyList(),
    val prepTimeMinutes: Int = 10,
    val cookTimeMinutes: Int = 15,
    val mealType: List<String> = emptyList()
)

@Serializable
data class NowyPrzepisDto(
    val name: String,
    val ingredients: List<String>,
    val prepTimeMinutes: Int,
    val cookTimeMinutes: Int
)

fun PrzepisDto.naModel(): Przepis {
    val typ = mealType.firstOrNull()?.lowercase() ?: ""
    val katId = when {
        typ.contains("breakfast") -> 1
        typ.contains("lunch") -> 2
        typ.contains("dinner") -> 3
        typ.contains("dessert") || typ.contains("snack") -> 4
        else -> 2
    }
    val katNazwa = domyslneKategorie.firstOrNull { it.id == katId }?.nazwa ?: "Obiad"
    return Przepis(
        id = id,
        nazwa = name.ifBlank { "Bez nazwy" },
        skladniki = ingredients.joinToString(", ").ifBlank { "Brak składników" },
        czasMinuty = (prepTimeMinutes + cookTimeMinutes).coerceAtLeast(5),
        kategoriaId = katId,
        kategoriaNazwa = katNazwa,
        ugotowane = false
    )
}

internal fun HttpClientConfig<*>.konfiguracjaKlienta() {
    expectSuccess = true
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    install(HttpTimeout) {
        connectTimeoutMillis = 5_000
        requestTimeoutMillis = 10_000
    }
}

fun tworzKlienta(): HttpClient = HttpClient { konfiguracjaKlienta() }

internal fun tworzKlienta(silnik: HttpClientEngine): HttpClient =
    HttpClient(silnik) { konfiguracjaKlienta() }