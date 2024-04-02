package io.github.abhishekabhi789.lyricsforpoweramp.model

/**This data class carries track info from and to PowerAmp.*/
data class Track(
    var trackName: String? = null,
    var artistName: String? = null,
    var albumName: String? = null,
    var duration: Int? = null,
    val realId: Long? = null,
    val lyrics: Lyrics? = null
)