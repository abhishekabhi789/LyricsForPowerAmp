package io.github.abhishekabhi789.lyricsforpoweramp.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.abhishekabhi789.lyricsforpoweramp.helpers.LrclibApiHelper
import io.github.abhishekabhi789.lyricsforpoweramp.model.InputState
import io.github.abhishekabhi789.lyricsforpoweramp.model.Lyrics
import io.github.abhishekabhi789.lyricsforpoweramp.utils.AppPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Collections.emptyList


class AppViewmodel : ViewModel() {

    private val _inputState = MutableStateFlow(InputState())

    /** Carries inputs from PowerAmp or user, which is an instance of [InputState] */
    val inputState = _inputState.asStateFlow()

    private var _searchResults = MutableStateFlow<List<Lyrics>>(emptyList())

    /** Search results as [List]<[Lyrics]>*/
    val searchResults = _searchResults.asStateFlow()

    /** Holds the current search job, inorder to cancel it if needed.*/
    private var searchJob: Job? = null

    private val _appTheme = MutableStateFlow(AppPreference.AppTheme.Auto)

    /** Current App theme */
    val appTheme = _appTheme.asStateFlow()

    private var _isSearching = MutableStateFlow(false)

    /** Status about search */
    val isSearching = _isSearching.asStateFlow()

    private var _isInputValid = MutableStateFlow(true)

    /** Stores if input is valid for a search operation */
    val isInputValid = _isInputValid.asStateFlow()

    /** Updates app theme */
    fun updateTheme(theme: AppPreference.AppTheme) {
        _appTheme.update { theme }
    }

    /** Updates [inputState]*/
    fun updateInputState(newState: InputState) {
        _inputState.update { newState }
    }

    /** Ensures user inputs are suffice to perform search */
    private fun isValidInput(): Boolean {
        return when (_inputState.value.searchMode) {
            InputState.SearchMode.Coarse -> _inputState.value.queryString.isNotEmpty()
            InputState.SearchMode.Fine -> !inputState.value.queryTrack.trackName.isNullOrEmpty()
        }
    }

    /**Abort search*/
    fun abortSearch() {
        searchJob?.cancel()
        Log.i(TAG, "abortSearch: aborting lyrics search")
    }

    /**Performs search for the [inputState]*/
    fun performSearch(onSearchSuccess: () -> Unit, onSearchFail: (errorMsg: String) -> Unit) {
        val isInputValid = isValidInput()
        if (!isInputValid) {
            Log.e(TAG, "performSearch: can't search with invalid input ${_inputState.value}")
            updateInputValidStatus(false)
            return
        }
        updateSearchStatus(true)
        _searchResults.update { emptyList() }
        searchJob?.cancel()
        val searchQuery: Any = when (_inputState.value.searchMode) {
            InputState.SearchMode.Coarse -> _inputState.value.queryString
            InputState.SearchMode.Fine -> _inputState.value.queryTrack
        }
        searchJob = viewModelScope.launch {
            withContext(Dispatchers.IO) {
                LrclibApiHelper.searchLyricsForTrack(searchQuery,
                    onResult = { list -> _searchResults.update { list } },
                    onError = { error -> onSearchFail(error) })
            }
            if (searchJob?.isCancelled == true) {
                onSearchFail("Cancelled")
            }
            if (_searchResults.value.isNotEmpty()) {
                onSearchSuccess()
            }
        }
        searchJob?.invokeOnCompletion {
            updateSearchStatus(false)
            Log.d(TAG, "performSearch: search job completed")
        }
    }

    private fun updateSearchStatus(isSearching: Boolean) {
        _isSearching.update { isSearching }
    }

    private fun updateInputValidStatus(isInputValid: Boolean) {
        _isInputValid.update { isInputValid }
    }

    /** Call this when after updating the mandatory fields to clear the error*/
    fun clearInvalidInputError() {
        updateInputValidStatus(true)
    }

    companion object {
        private const val TAG = "LyricViewModel"
    }
}
