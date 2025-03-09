package hu.dancsomarci.signbuddy.auth.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.dancsomarci.signbuddy.auth.data.AuthService
import hu.dancsomarci.signbuddy.auth.data.FirebaseAuthService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthServiceModule {

    @Binds
    @Singleton
    abstract fun provideAuthService(authService: FirebaseAuthService): AuthService
}