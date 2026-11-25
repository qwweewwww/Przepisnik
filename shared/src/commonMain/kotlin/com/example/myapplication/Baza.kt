package com.example.myapplication

import androidx.room3.ConstructedBy
import androidx.room3.Dao
import androidx.room3.Database
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.PrimaryKey
import androidx.room3.Query
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import androidx.room3.Update
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "kategorie")
data class KategoriaEntity(
    @PrimaryKey val id: Int,
    val nazwa: String
)

@Entity(
    tableName = "przepisy",
    foreignKeys = [
        ForeignKey(
            entity = KategoriaEntity::class,
            parentColumns = ["id"],
            childColumns = ["kategoriaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("kategoriaId")]
)
data class PrzepisEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nazwa: String,
    val skladniki: String,
    val czasMinuty: Int,
    val kategoriaId: Int,
    val ugotowane: Boolean = false
)

fun KategoriaEntity.naModel() = Kategoria(id, nazwa)
fun Kategoria.naEncje() = KategoriaEntity(id, nazwa)

fun PrzepisEntity.naModel(kategorie: Map<Int, String>) = Przepis(
    id = id,
    nazwa = nazwa,
    skladniki = skladniki,
    czasMinuty = czasMinuty,
    kategoriaId = kategoriaId,
    kategoriaNazwa = kategorie[kategoriaId] ?: "Inne",
    ugotowane = ugotowane
)

fun Przepis.naEncje() = PrzepisEntity(
    id = id,
    nazwa = nazwa,
    skladniki = skladniki,
    czasMinuty = czasMinuty,
    kategoriaId = kategoriaId,
    ugotowane = ugotowane
)

@Dao
interface KategoriaDao {
    @Query("SELECT * FROM kategorie ORDER BY id ASC")
    fun obserwuj(): Flow<List<KategoriaEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun wstawWszystkie(kategorie: List<KategoriaEntity>)
}

@Dao
interface PrzepisDao {
    @Query("SELECT * FROM przepisy ORDER BY id DESC")
    fun obserwuj(): Flow<List<PrzepisEntity>>

    @Query("SELECT * FROM przepisy WHERE nazwa LIKE '%' || :fraza || '%' ORDER BY czasMinuty ASC")
    fun szukaj(fraza: String): Flow<List<PrzepisEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun wstawWszystkie(przepisy: List<PrzepisEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun wstaw(przepis: PrzepisEntity)

    @Update
    suspend fun aktualizuj(przepis: PrzepisEntity)

    @Query("DELETE FROM przepisy WHERE id = :id")
    suspend fun usun(id: Int)

    @Query("DELETE FROM przepisy")
    suspend fun wyczysc()
}

@Database(entities = [KategoriaEntity::class, PrzepisEntity::class], version = 1)
@ConstructedBy(BazaDanychConstructor::class)
abstract class BazaDanych : RoomDatabase() {
    abstract fun kategoriaDao(): KategoriaDao
    abstract fun przepisDao(): PrzepisDao
}

@Suppress("KotlinNoActualForExpect")
expect object BazaDanychConstructor : RoomDatabaseConstructor<BazaDanych> {
    override fun initialize(): BazaDanych
}

fun tworzBaze(builder: RoomDatabase.Builder<BazaDanych>): BazaDanych =
    builder.setDriver(BundledSQLiteDriver()).build()