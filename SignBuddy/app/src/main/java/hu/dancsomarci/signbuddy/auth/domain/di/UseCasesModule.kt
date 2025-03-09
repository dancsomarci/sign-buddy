package hu.dancsomarci.signbuddy.auth.domain.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.dancsomarci.signbuddy.auth.domain.usecases.IsEmailValidUseCase
import hu.dancsomarci.signbuddy.auth.domain.usecases.PasswordsMatchUseCase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class UseCasesModule {

    @Provides
    @Singleton
    fun provideIsEmailValidUseCase(
    ): IsEmailValidUseCase = IsEmailValidUseCase()

    @Provides
    @Singleton
    fun providePasswordsMatchUseCase(
    ): PasswordsMatchUseCase = PasswordsMatchUseCase()
}