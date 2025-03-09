package hu.dancsomarci.signbuddy.hand_recognition.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.dancsomarci.signbuddy.hand_recognition.data.LandmarkService
import hu.dancsomarci.signbuddy.hand_recognition.data.firebase.FirebaseLandmarkService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LandmarkServiceModule {
    @Binds
    @Singleton
    abstract fun provideLandmarkService(landmarkService: FirebaseLandmarkService): LandmarkService
}
