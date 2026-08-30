package br.com.porteirointeligente.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import br.com.porteirointeligente.data.local.dao.OwnerDao
import br.com.porteirointeligente.data.local.dao.VisitDao
import br.com.porteirointeligente.data.local.entity.OwnerEntity
import br.com.porteirointeligente.data.local.entity.VisitEntity

/**
 * Banco de dados Room do aplicativo.
 *
 * Os dados são armazenados localmente em SQLite.
 * Os campos pessoais são cifrados antes de chegar ao SQLite por LocalDataCrypto,
 * com chave protegida pelo Android Keystore. O FBE do Android acrescenta uma
 * camada de proteção para os arquivos do aplicativo quando o aparelho está bloqueado.
 *
 * Para backup externo, utilize a funcionalidade "Backup para Google Drive" nos Ajustes.
 */
@Database(
    entities = [
        VisitEntity::class,
        OwnerEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun visitDao(): VisitDao
    abstract fun ownerDao(): OwnerDao

    companion object {
        const val DATABASE_NAME = "porteiro_inteligente.db"

        /**
         * Adiciona a relação opcional entre visitas e moradores sem apagar o histórico.
         * A versão 6 não possuía a coluna ownerId nem a foreign key.
         */
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS visits_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        ownerId INTEGER,
                        nome TEXT NOT NULL,
                        documento TEXT NOT NULL,
                        apartamento TEXT NOT NULL,
                        telefone TEXT NOT NULL,
                        motivo TEXT NOT NULL,
                        dataEntrada INTEGER NOT NULL,
                        dataSaida INTEGER,
                        status TEXT NOT NULL,
                        FOREIGN KEY(ownerId) REFERENCES owners(id) ON UPDATE NO ACTION ON DELETE SET NULL
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    INSERT INTO visits_new
                        (id, ownerId, nome, documento, apartamento, telefone, motivo, dataEntrada, dataSaida, status)
                    SELECT id,
                        CASE WHEN (SELECT COUNT(*) FROM owners) = 1
                             THEN (SELECT id FROM owners LIMIT 1)
                             ELSE NULL END,
                        nome, documento, apartamento, telefone, motivo, dataEntrada, dataSaida, status
                    FROM visits
                    """.trimIndent()
                )
                database.execSQL("DROP TABLE visits")
                database.execSQL("ALTER TABLE visits_new RENAME TO visits")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_visits_ownerId ON visits(ownerId)")
            }
        }

        /**
         * Vincula visitas que ficaram sem proprietário em uma migração anterior quando há
         * exatamente um morador. Registros ambíguos continuam sem vínculo para não misturar
         * históricos entre moradores diferentes.
         */
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    UPDATE visits
                    SET ownerId = (SELECT id FROM owners LIMIT 1)
                    WHERE ownerId IS NULL
                      AND (SELECT COUNT(*) FROM owners) = 1
                    """.trimIndent()
                )
            }
        }

        /**
         * Cria a instância do banco de dados.
         */
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .addMigrations(MIGRATION_6_7, MIGRATION_7_8)
                .build()
    }
}
