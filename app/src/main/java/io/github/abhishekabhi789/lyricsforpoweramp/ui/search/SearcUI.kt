package io.github.abhishekabhi789.lyricsforpoweramp.ui.search

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.InterpreterMode
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.abhishekabhi789.lyricsforpoweramp.LyricViewModel
import io.github.abhishekabhi789.lyricsforpoweramp.R
import io.github.abhishekabhi789.lyricsforpoweramp.model.InputState
import io.github.abhishekabhi789.lyricsforpoweramp.ui.utils.TextInput
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchUi(viewModel: LyricViewModel, onSearchComplete: (String?) -> Unit) {
    var emptyInputError: Boolean by remember { mutableStateOf(false) }
    var isSearching: Boolean by remember { mutableStateOf(false) }
    val inputState by viewModel.inputState.collectAsState()
    LaunchedEffect(inputState.searchMode) {
//        clear error when changing search tab
        emptyInputError = false
    }
    val scope = rememberCoroutineScope()
    val tabs = InputState.SearchMode.entries
    val pagerState = rememberPagerState(
        pageCount = { tabs.size },
        initialPage = tabs.indexOf(inputState.searchMode)
    )
    if (isSearching) {
        Dialog(
            onDismissRequest = { isSearching = false; viewModel.abortSearch() },
            DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            )
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                CircularProgressIndicator()
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        var searchButtonPosition by remember { mutableStateOf(Offset.Zero) }
        var searchButtonSize by remember { mutableStateOf(IntSize.Zero) }
        val offset by animateIntOffsetAsState(
            targetValue = IntOffset(
                x = searchButtonPosition.x.toInt() - searchButtonSize.width.div(2),
                y = searchButtonPosition.y.toInt() + searchButtonSize.height.div(2),
            ),
            label = "search_btn_animation"
        )
        Column(
            modifier = Modifier.animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessHigh))
        ) {
            TabRow(selectedTabIndex = pagerState.currentPage) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        text = { Text(stringResource(id = tab.label)) },
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch { pagerState.animateScrollToPage(index) }
                            viewModel.updateInputState(inputState.copy(searchMode = tab))
                        },
                    )
                }
            }
            HorizontalPager(
                state = pagerState,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxSize()
            ) { pageIndex ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Spacer(modifier = Modifier.padding(8.dp))
                    when (tabs[pageIndex]) {

                        InputState.SearchMode.Coarse -> {
                            TextInput(
                                label = stringResource(R.string.coarse_search_query),
                                icon = Icons.Outlined.Edit,
                                text = inputState.queryString,
                                isError = emptyInputError
                            ) {
                                emptyInputError = false //resetting error on input
                                viewModel.updateInputState(inputState.copy(queryString = it))
                            }
                            Spacer(modifier = Modifier.onGloballyPositioned {
                                searchButtonPosition = it.positionInParent()
                            })
                        }

                        InputState.SearchMode.Fine -> {
                            Column(
                                modifier = Modifier
                                    .verticalScroll(rememberScrollState())
                                    .wrapContentHeight()
                            ) {
                                TextInput(
                                    label = stringResource(R.string.track_title),
                                    icon = Icons.Outlined.MusicNote,
                                    text = inputState.queryTrack.trackName,
                                    isError = emptyInputError
                                ) {
                                    emptyInputError = false //resetting error on input
                                    viewModel.updateInputState(
                                        inputState.copy(
                                            queryTrack = inputState.queryTrack.copy(
                                                trackName = it
                                            )
                                        )
                                    )
                                }
                                TextInput(
                                    label = stringResource(R.string.artists),
                                    icon = Icons.Outlined.InterpreterMode,
                                    text = inputState.queryTrack.artistName,
                                    isError = false
                                ) {
                                    viewModel.updateInputState(
                                        inputState.copy(
                                            queryTrack = inputState.queryTrack.copy(
                                                artistName = it
                                            )
                                        )
                                    )
                                }
                                TextInput(
                                    label = stringResource(R.string.album_name),
                                    icon = Icons.Outlined.Album,
                                    text = inputState.queryTrack.albumName,
                                    isError = false //no need to use error on these fields
                                ) {
                                    viewModel.updateInputState(
                                        inputState.copy(
                                            queryTrack = inputState.queryTrack.copy(
                                                albumName = it
                                            )
                                        )
                                    )
                                }
                            }
                            Spacer(
                                modifier = Modifier
                                    .padding(bottom = 12.dp)
                                    .onGloballyPositioned {
                                        searchButtonPosition = it.positionInParent()
                                    })

                        }
                    }
                }
            }
        }
        OutlinedButton(
            onClick = {
                if (viewModel.isValidInput()) {
                    isSearching = true
                    viewModel.performSearch(
                        onSearchSuccess = {
                            isSearching = false
                            onSearchComplete(null)
                        },
                        onSearchFail = {
                            isSearching = false
                            onSearchComplete(it)
                        })
                } else emptyInputError = true
            },
            modifier = Modifier
                .padding(vertical = 16.dp)
                .onGloballyPositioned { searchButtonSize = it.size }
                .offset { offset }
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = stringResource(R.string.search)
            )
            Spacer(modifier = Modifier.padding(4.dp))
            Text(text = stringResource(id = R.string.search))
        }
    }
}