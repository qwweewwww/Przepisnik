package com.example.myapplication

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room3.Room
import androidx.room3.RoomDatabase

private var baza: BazaDanych? = null
private var ustawienia: DataStore<Preferences>? = null

fun zaleznosciAndroid(context: Context): Zaleznosci {
    val app = context.applicationContext
    val b = baza ?: tworzBaze(budowniczyBazy(app)).also { baza = it }
    val u = ustawienia ?: tworzUstawienia {
        app.filesDir.resolve(NAZWA_PLIKU_USTAWIEN).absolutePath
    }.also { ustawienia = it }
    return Zaleznosci(b, u)
}

private fun budowniczyBazy(app: Context): RoomDatabase.Builder<BazaDanych> =
    Room.databaseBuilder<BazaDanych>(
        context = app,
        name = app.getDatabasePath("przepisnik.db").absolutePath
    )