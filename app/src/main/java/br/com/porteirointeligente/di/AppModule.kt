package br.com.porteirointeligente.di

import android.content.Context
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
 * Módulo de injeção de dependência — versão 100% offline com dados criptografados.
 *
 * Apenas fornece o banco Room (SQLite com SQLCipher) e seus DAOs.
 * Sem dependências de rede (Retrofit, OkHttp, Firebase).
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.create(context)

    @Provides
    fun provideVisitDao(database: AppDatabase): VisitDao = database.visitDao()

    @Provides
    fun provideOwnerDao(database: AppDatabase): OwnerDao = database.ownerDao()
}
