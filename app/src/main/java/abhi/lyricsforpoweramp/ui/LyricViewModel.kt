package abhi.lyricsforpoweramp.ui

import abhi.lyricsforpoweramp.LyricsApiHelper
import abhi.lyricsforpoweramp.PowerAmpIntentUtils
import abhi.lyricsforpoweramp.model.InputState
import abhi.lyricsforpoweramp.model.Lyric
import abhi.lyricsforpoweramp.model.LyricsRequestState
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Collections.emptyList


class LyricViewModel : ViewModel() {
    private val TAG = javaClass.simpleName

    private val _lyricsRequestState = MutableStateFlow(LyricsRequestState())

    /** Carries lyrics request information from PowerAmp, which is an instance of [LyricsRequestState] */
    val lyricsRequestState: StateFlow<LyricsRequestState> = _lyricsRequestState.asStateFlow()

    private val _inputState = MutableStateFlow(InputState())

    /** Carries user inputs, which is an instance of [InputState] */
    val inputState = _inputState.asStateFlow()

    private var _searchResults = MutableStateFlow<List<Lyric>>(emptyList())

    /** Search results as [List]<[Lyric]>*/
    val searchResults = _searchResults.asStateFlow()

    /** Holds the current search job, inorder to cancel it if needed.*/
    private var searchJob: Job? = null

    /** updates [lyricsRequestState]*/
    fun updateLyricsRequestDetails(newState: LyricsRequestState) {
        _lyricsRequestState.update { newState }
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

    /** Will send the chosen lyrics to PowerAmp.
     * @return [Boolean] indicating request attempt result*/
    fun chooseThisLyrics(context: Context, lyric: String): Boolean {
        return PowerAmpIntentUtils.sendLyricResponse(
            context = context,
            realId = _lyricsRequestState.value.realId!!,
            lyrics = lyric
        )
    }
}