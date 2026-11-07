package br.com.porteirointeligente.data.local

import android.content.ContentValues
import android.content.Context
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {

    private val dbName = "migration-test.db"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        "br.com.porteirointeligente.data.local.AppDatabase",
        FrameworkSQLiteOpenHelperFactory()
    )

    private fun insertOwner(db: androidx.sqlite.db.SupportSQLiteDatabase, id: Long = 1L) {
        val values = ContentValues().apply {
            put("id", id)
            put("nome", "Morador Teste")
            put("nomeCondominio", "Condomínio Teste")
            put("endereco", "Rua Teste, 1")
            put("cep", "01001000")
            put("apartamento", "42")
            put("telefone", "11999998888")
            put("qrCodePayload", "https://porteiro-inteligente-2026.vercel.app/scan/v2.1.payload")
            put("dataCadastro", 1_700_000_000_000L)
            put("isOffline", 0)
            put("offlineMessage", "")
        }
        db.insert("owners", 0, values)
    }

    private fun insertVisit(db: androidx.sqlite.db.SupportSQLiteDatabase, id: Long = 1L) {
        val values = ContentValues().apply {
            put("id", id)
            put("nome", "Visitante Teste")
            put("documento", "12345678900")
            put("apartamento", "42")
            put("telefone", "11988887777")
            put("motivo", "Entrega")
            put("dataEntrada", 1_700_000_000_000L)
            put("status", "ENTRADA_REGISTRADA")
        }
        db.insert("visits", 0, values)
    }

    @Test
    fun migrate6To7_linksVisitWhenSingleOwner() {
        val db = helper.createDatabase(dbName, 6)
        insertOwner(db)
        insertVisit(db)
        db.close()

        val migrated = helper.runMigrationsAndValidate(
            dbName, 7, true, AppDatabase.MIGRATION_6_7
        )
        migrated.query("SELECT ownerId FROM visits WHERE id = 1").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(1L, cursor.getLong(0))
        }
        migrated.close()
    }

    @Test
    fun migrate6To9_preservesDataAndAddsPlaca() {
        val db = helper.createDatabase(dbName + "-chain", 6)
        insertOwner(db)
        insertVisit(db)
        db.close()

        val migrated = helper.runMigrationsAndValidate(
            dbName + "-chain", 9, true,
            AppDatabase.MIGRATION_6_7,
            AppDatabase.MIGRATION_7_8,
            AppDatabase.MIGRATION_8_9
        )
        migrated.query("SELECT ownerId, placa FROM visits WHERE id = 1").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(1L, cursor.getLong(0))
            assertTrue(cursor.isNull(1))
        }
        migrated.close()
    }

    @Test
    fun migrate7To9_keepsNullOwnerIdWhenMultipleOwners() {
        val db = helper.createDatabase(dbName + "-multi", 7)
        // dois moradores -> ownerId deve permanecer nulo na migração 7->8
        insertOwner(db, 1L)
        insertOwner(db, 2L)
        val values = ContentValues().apply {
            put("id", 1L)
            put("nome", "Visitante Teste")
            put("documento", "12345678900")
            put("apartamento", "42")
            put("telefone", "11988887777")
            put("motivo", "Entrega")
            put("dataEntrada", 1_700_000_000_000L)
            put("status", "ENTRADA_REGISTRADA")
            putNull("ownerId")
        }
        db.insert("visits", 0, values)
        db.close()

        val migrated = helper.runMigrationsAndValidate(
            dbName + "-multi", 9, true,
            AppDatabase.MIGRATION_7_8,
            AppDatabase.MIGRATION_8_9
        )
        migrated.query("SELECT ownerId FROM visits WHERE id = 1").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertTrue(cursor.isNull(0))
        }
        migrated.close()
    }
}
