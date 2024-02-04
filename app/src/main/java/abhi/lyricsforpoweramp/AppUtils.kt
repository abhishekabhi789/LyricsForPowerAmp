package abhi.lyricsforpoweramp

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast

const val CONTENT_ANIMATION_DURATION = 500

/**show the text as toast*/
fun String.toToast(context: Context) {
    Handler(Looper.getMainLooper()).post {
        Toast.makeText(context, this, Toast.LENGTH_LONG).show()
    }
}