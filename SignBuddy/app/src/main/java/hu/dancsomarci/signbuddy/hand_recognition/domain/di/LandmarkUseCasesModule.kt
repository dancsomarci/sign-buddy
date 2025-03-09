package hu.dancsomarci.signbuddy.hand_recognition.domain.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.dancsomarci.signbuddy.hand_recognition.data.LandmarkService
import hu.dancsomarci.signbuddy.hand_recognition.domain.model.LandmarkSequence
import hu.dancsomarci.signbuddy.hand_recognition.domain.usecases.DeleteLandmarkSequenceUseCase
import hu.dancsomarci.signbuddy.hand_recognition.domain.usecases.GetLandmarkSequenceUseCase
import hu.dancsomarci.signbuddy.hand_recognition.domain.usecases.LandmarkSequenceUseCases
import hu.dancsomarci.signbuddy.hand_recognition.domain.usecases.LoadLandmarkSequencesUseCase
import hu.dancsomarci.signbuddy.hand_recognition.domain.usecases.SaveLandmarkSequenceUseCase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class LandmarkUseCasesModule {
    @Provides
    @Singleton
    fun provideLandmarkUseCases(
        service: LandmarkService
    ): LandmarkSequenceUseCases = LandmarkSequenceUseCases(
        loadSequence = GetLandmarkSequenceUseCase(service),
        loadSequences = LoadLandmarkSequencesUseCase(service),
        saveSequence = SaveLandmarkSequenceUseCase(service),
        deleteSequence = DeleteLandmarkSequenceUseCase(service)
    )
}
