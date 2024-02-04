package abhi.lyricsforpoweramp.ui.search

import abhi.lyricsforpoweramp.R
import abhi.lyricsforpoweramp.model.Track
import abhi.lyricsforpoweramp.ui.utils.TextInput
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.InterpreterMode
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun SearchUi(
    queryString: String?,
    queryTrack: Track?,
    coarseSearchMode: Boolean,
    onModeChange: (Boolean) -> Unit,
    onQueryChange: (String?) -> Unit,
    onQueryTrackChange: (Track?) -> Unit,
    onSearch: (Any) -> Unit
) {
    var searchQuery: String? by remember { mutableStateOf(queryString) }
    val searchTrack: Track? by remember { mutableStateOf(queryTrack) }
    var emptyInputError: Boolean by remember { mutableStateOf(false) }
    var trackTitle: String? by remember { mutableStateOf(queryTrack?.trackName) }
    var albumName: String? by remember { mutableStateOf(queryTrack?.albumName) }
    var artistName: String? by remember { mutableStateOf(queryTrack?.artistName) }
    onQueryTrackChange(Track(trackTitle, artistName, albumName, null, null))
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = Modifier.animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessHigh))
    ) {
        var tabIndex by remember { mutableIntStateOf(if (coarseSearchMode) 0 else 1) }
        val tabs =
            listOf(stringResource(R.string.coarse_search), stringResource(R.string.fine_search))

        Column(modifier = Modifier.fillMaxWidth()) {
            TabRow(selectedTabIndex = tabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        text = { Text(title) },
                        selected = tabIndex == index,
                        onClick = {
                            tabIndex = index
                            emptyInputError = false
                        },
                    )
                }
            }
            when (tabIndex) {
                0 -> onModeChange(true)
                1 -> onModeChange(false)
            }
        }
        Spacer(modifier = Modifier.padding(8.dp))
        if (coarseSearchMode) {
            TextInput(
                label = stringResource(R.string.coarse_search_query),
                icon = Icons.Outlined.Edit,
                text = searchQuery,
                isError = emptyInputError
            ) {
                emptyInputError = false //resetting error on input
                searchQuery = it
                onQueryChange(it)
            }
        }
        if (!coarseSearchMode) {
            TextInput(
                label = stringResource(R.string.track_title),
                icon = Icons.Outlined.MusicNote,
                text = trackTitle,
                isError = emptyInputError
            ) {
                emptyInputError = false //resetting error on input
                trackTitle = it
                searchTrack?.trackName = it
                onQueryTrackChange(searchTrack)

            }
            TextInput(
                label = stringResource(R.string.artists),
                icon = Icons.Outlined.InterpreterMode,
                text = artistName,
                isError = false
            ) {
                artistName = it
                searchTrack?.artistName = it
                onQueryTrackChange(searchTrack)
            }
            TextInput(
                label = stringResource(R.string.album_name),
                icon = Icons.Outlined.Album,
                text = albumName,
                isError = false //no need to use error on these fields
            ) {
                albumName = it
                searchTrack?.albumName = it
                onQueryTrackChange(searchTrack)
            }
        }
        Spacer(modifier = Modifier.padding(8.dp))
        OutlinedButton(onClick = {
            if (coarseSearchMode && searchQuery != null) {
                onSearch.invoke(searchQuery!!)
            } else if (!coarseSearchMode && !trackTitle.isNullOrEmpty()) {
                onSearch.invoke(Track(trackTitle, artistName, albumName, null, null))
            } else emptyInputError = true
        }) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = stringResource(R.string.search)
            )
            Spacer(modifier = Modifier.padding(4.dp))
            Text(text = "Search")
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun PreviewSearchUi() {
    SearchUi(
        queryString = "Lorum Ipsum",
        queryTrack = Track("Track Name", "Artist Name", "Album Name", null, null),
        coarseSearchMode = false,
        onModeChange = {},
        onQueryChange = {},
        onQueryTrackChange = {},
        onSearch = {}
    )
}