package br.com.porteirointeligente.di

import android.content.Context
import androidx.room.Room
import br.com.porteirointeligente.data.local.AppDatabase
import br.com.porteirointeligente.data.local.dao.OwnerDao
import br.com.porteirointeligente.data.local.dao.VisitDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de injeção de dependência do Hilt.
 *
 * Fornece as instâncias de banco de dados e DAOs para a aplicação.
 * Futuras dependências (CameraX, ZXing, networking, etc.) podem
 * ser adicionadas aqui.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        AppDatabase.DATABASE_NAME
    )
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    fun provideVisitDao(database: AppDatabase): VisitDao = database.visitDao()

    @Provides
    fun provideOwnerDao(database: AppDatabase): OwnerDao = database.ownerDao()
}
