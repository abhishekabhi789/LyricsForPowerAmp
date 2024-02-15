package abhi.lyricsforpoweramp.model

/** Stores information while handling lyrics requests from PowerAmp */
data class LyricsRequestState(
    /**Used to decide whether to show the lyric chose button in lyrics list*/
    val isLaunchedFromPowerAmp: Boolean = false,
    /**Needed to send lyrics for requested track.*/
    val realId: Long? = null,
)