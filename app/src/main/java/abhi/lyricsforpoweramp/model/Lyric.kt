package abhi.lyricsforpoweramp.model

import com.google.gson.annotations.SerializedName

/**This data class represents each item from the API response*/
data class Lyric(
    @SerializedName("trackName") val trackName: String,
    @SerializedName("artistName") val artistName: String,
    @SerializedName("albumName") val albumName: String,
    @SerializedName("plainLyrics") val plainLyrics: String?,
    @SerializedName("syncedLyrics") val syncedLyrics: String?,
    @SerializedName("duration") val duration: Int
) {
    fun getFormattedDuration(): String {
        val hours = duration / 3600
        val minutes = (duration % 3600) / 60
        val seconds = duration % 60
        val formatted = buildString {
            if (hours > 0) append("$hours:")
            append(String.format("%02d:%02d", minutes, seconds))
        }
        return formatted
    }
}