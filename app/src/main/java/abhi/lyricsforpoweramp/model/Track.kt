package abhi.lyricsforpoweramp.model

/**This data class carries track info from and to PowerAmp.*/
data class Track(
    var trackName: String?,
    var artistName: String?,
    var albumName: String?,
    var duration: Int?,
    val lyrics: String?
)