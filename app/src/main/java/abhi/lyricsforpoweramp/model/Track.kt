package abhi.lyricsforpoweramp.model

/**This data class carries track info from and to PowerAmp.*/
data class Track(
    val trackName: String,
    val artistName: String?,
    val albumName: String?,
    val duration: Int?,
    val syncedLyrics: String?
)