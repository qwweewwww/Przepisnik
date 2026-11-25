package com.example.myapplication


data class Kategoria(
    val id: Int,
    val nazwa: String
)

data class Przepis(
    val id: Int,
    val nazwa: String,
    val skladniki: String,
    val czasMinuty: Int,
    val kategoriaId: Int,
    val kategoriaNazwa: String = "",
    val ugotowane: Boolean = false
)

val domyslneKategorie = listOf(
    Kategoria(1, "Śniadanie"),
    Kategoria(2, "Obiad"),
    Kategoria(3, "Kolacja"),
    Kategoria(4, "Deser")
)

val przykladowePrzepisy = listOf(
    Przepis(1, "Jajecznica na maśle", "Jajka, masło, szczypiorek, sól", 10, 1, "Śniadanie", true),
    Przepis(2, "Owsianka z owocami", "Płatki owsiane, mleko, banan, borówki", 15, 1, "Śniadanie", false),
    Przepis(3, "Spaghetti Bolognese", "Makaron, mięso mielone, pomidory, czosnek", 35, 2, "Obiad", true),
    Przepis(4, "Zupa pomidorowa", "Bulion, przecier pomidorowy, ryż, śmietana", 40, 2, "Obiad", false),
    Przepis(5, "Kurczak z ryżem", "Pierś z kurczaka, ryż, brokuły, przyprawy", 30, 2, "Obiad", true),
    Przepis(6, "Sałatka grecka", "Pomidory, ogórek, feta, oliwki, oliwa", 15, 3, "Kolacja", false),
    Przepis(7, "Tosty z awokado", "Chleb, awokado, cytryna, pieprz", 10, 3, "Kolacja", true),
    Przepis(8, "Sernik na zimno", "Twaróg, galaretka, herbatniki, owoce", 45, 4, "Deser", false),
    Przepis(9, "Naleśniki z serem", "Mąka, mleko, jajka, twaróg, cukier", 25, 4, "Deser", true),
    Przepis(10, "Szarlotka domowa", "Jabłka, mąka, masło, cynamon, cukier", 60, 4, "Deser", false)
)