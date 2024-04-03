package io.github.abhishekabhi789.lyricsforpoweramp

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.abhishekabhi789.lyricsforpoweramp.model.InputState
import io.github.abhishekabhi789.lyricsforpoweramp.model.Lyrics
import io.github.abhishekabhi789.lyricsforpoweramp.model.Track
import io.github.abhishekabhi789.lyricsforpoweramp.utils.AppPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Collections.emptyList


class LyricViewModel : ViewModel() {
    private val TAG = javaClass.simpleName
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

    /** Updates app theme */
    fun updateTheme(theme: AppPreference.AppTheme) {
        _appTheme.update { theme }
    }

    /** updates requested track in [inputState].
     * @param track an instance of [Track] may contain track info from PowerAmp]*/
    fun updateLyricsRequestDetails(track: Track) {
        _inputState.update { _inputState.value.copy(queryTrack = track) }
    }

    /** Updates [inputState]*/
    fun updateInputState(newState: InputState) {
        _inputState.update { newState }
    }

    /** Ensures user inputs are suffice to perform search*/
    fun isValidInput(): Boolean {
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
    fun performSearch(onSearchSuccess: () -> Unit, onSearchFail: (String) -> Unit) {
        _searchResults.update { emptyList() }
        searchJob?.cancel()
        val searchQuery: Any = when (_inputState.value.searchMode) {
            InputState.SearchMode.Coarse -> _inputState.value.queryString
            InputState.SearchMode.Fine -> _inputState.value.queryTrack
        }
        searchJob = viewModelScope.launch {
            withContext(Dispatchers.IO) {
                LyricsApiHelper.getLyricsForTrack(searchQuery, onResult = { list ->
                    _searchResults.update { list }
                }, onError = { error -> onSearchFail(error) })
            }
            if (searchJob?.isCancelled == true) {
                onSearchFail("Cancelled")
            }
            if (_searchResults.value.isNotEmpty()) {
                onSearchSuccess()
            }
        }
    }

    /** Will send the chosen lyrics to PowerAmp. Should call when have realId obtained
     * @return [Boolean] indicating request attempt result*/
    fun chooseThisLyrics(context: Context, lyrics: Lyrics): Boolean {
        return PowerAmpIntentUtils.sendLyricResponse(
            context = context,
            realId = _inputState.value.queryTrack.realId!!,
            lyrics = lyrics
        )
    }
}