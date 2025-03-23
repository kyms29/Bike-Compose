package com.ymsu.bike_compose.di

import android.app.Application
import com.ymsu.bike_compose.room.AppDatabase
import com.ymsu.bike_compose.room.RoomRepository
import com.ymsu.bike_compose.room.FavoriteStationDao
import com.ymsu.bike_compose.room.SettingsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object Module {

    @Provides
    @Singleton
    fun provideDatabase(application: Application):AppDatabase{
        return AppDatabase.getInstance(application)
    }

    @Provides
    @Singleton
    fun provideFavoriteDao(appDatabase: AppDatabase): FavoriteStationDao {
        return appDatabase.favoriteStationDao()
    }

    @Provides
    @Singleton
    fun provideSettingsDao(appDatabase: AppDatabase): SettingsDao {
        return appDatabase.settingsDao()
    }

    @Provides
    @Singleton
    fun provideFavoriteRepository(favoriteStationDao: FavoriteStationDao,
                                  settingsDao: SettingsDao):RoomRepository{
        return RoomRepository(favoriteStationDao,settingsDao)
    }
}