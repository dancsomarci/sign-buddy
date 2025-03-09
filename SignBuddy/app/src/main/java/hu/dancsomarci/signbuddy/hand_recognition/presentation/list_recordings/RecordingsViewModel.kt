package hu.dancsomarci.signbuddy.hand_recognition.presentation.list_recordings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.dancsomarci.signbuddy.hand_recognition.data.LandmarkSequenceEntity
import hu.dancsomarci.signbuddy.hand_recognition.data.LandmarkService
import hu.dancsomarci.signbuddy.hand_recognition.domain.model.asLandmarkSequence
import hu.dancsomarci.signbuddy.hand_recognition.domain.usecases.LandmarkSequenceUseCases
import hu.dancsomarci.signbuddy.hand_recognition.presentation.model.LandmarkSequenceUi
import hu.dancsomarci.signbuddy.hand_recognition.presentation.model.asLandmarkSequenceUi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordingsViewModel @Inject constructor(
    private val landmarkUseCases: LandmarkSequenceUseCases
): ViewModel() {

    private val _state = MutableStateFlow(RecordingsState())
    val state = _state.asStateFlow()

    init {
        loadRecordings()
    }

    private fun loadRecordings() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                CoroutineScope(coroutineContext).launch(Dispatchers.IO) {
                    val sequences = landmarkUseCases.loadSequences().getOrThrow()
                        .map { it.asLandmarkSequenceUi() }
                    _state.update { it.copy(
                        isLoading = false,
                        recordings = sequences
                    ) }
                }
            } catch (e: Exception) {
                _state.update {  it.copy(
                    isLoading = false,
                    error = e
                ) }
            }
        }
    }

    fun deleteRecording(idx: Int){
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val recording = _state.value.recordings[idx]
            try {
                CoroutineScope(coroutineContext).launch(Dispatchers.IO) {
                    landmarkUseCases.deleteSequence(recording.id)
                    _state.update { it.copy(
                        isLoading = false,
                        recordings = _state.value.recordings.filterIndexed { i, _ -> i != idx }
                    ) }
                }
            } catch (e: Exception) {
                _state.update {  it.copy(
                    isLoading = false,
                    error = e
                ) }
            }
        }
    }

}

data class RecordingsState(
    val isLoading: Boolean = false,
    val error: Throwable? = null,
    val isError: Boolean = error != null,
    val recordings: List<LandmarkSequenceUi> = emptyList()
)