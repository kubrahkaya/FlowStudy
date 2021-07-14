package com.example.flowstudy.di

import android.content.Context
import androidx.room.Room
import com.example.flowstudy.data.api.SunflowerApi
import com.example.flowstudy.data.local.AppDatabase
import com.example.flowstudy.data.local.PlantDao
import com.example.flowstudy.repository.PlantRepository
import com.example.flowstudy.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Singleton
    @Provides
    fun provideSunFlowerService(): SunflowerApi {
        return Retrofit.Builder()
            .baseUrl("https://raw.githubusercontent.com/")
            .client(OkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SunflowerApi::class.java)
    }

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context) = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        Constants.DATABASE_NAME
    ).build()

    @Singleton
    @Provides
    fun provideDao(database: AppDatabase) = database.plantDao()

    @Singleton
    @Provides
    fun provideRepository(dao: PlantDao, api: SunflowerApi) = PlantRepository(dao, api)

}