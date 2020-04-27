package d.offrede.lib.downloadimage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.ImageView
import d.offrede.lib.downloadimage.DownloadImage.Companion.downloadImage
import d.offrede.lib.downloadimage.DownloadImage.Companion.hasImage
import d.offrede.lib.downloadimage.DownloadImage.Companion.loadImageBitmap
import kotlinx.coroutines.*
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

private const val TAG = "DownloadImage"

fun ImageView.loadDownloadImage(
    id: String,
    url: String,
    firstOnline: Boolean = false
) {
    if (!firstOnline && hasImage(this.context, id)) {
        this.setImageBitmap(
            loadImageBitmap(
                this.context,
                id
            )
        )
    } else {
        CoroutineScope(Dispatchers.Main).launch {
            val a = withContext(Dispatchers.IO) {
                downloadImage(
                    this@loadDownloadImage.context,
                    id,
                    url
                )
            }

            if (a) {
                this@loadDownloadImage.setImageBitmap(
                    loadImageBitmap(
                        this@loadDownloadImage.context,
                        id
                    )
                )
            }
        }
    }
}

class DownloadImage {
    companion object {
        fun downloadImage(
            context: Context,
            id: String,
            url: String
        ): Boolean {
            downloadImageTask(
                url
            )?.let {
                saveImage(context, id, it)
                return true
            } ?: return false
        }

        fun saveImage(
            context: Context,
            id: String,
            image: Bitmap
        ) {
            val foStream: FileOutputStream
            try {
                foStream = context.openFileOutput("$id.png", Context.MODE_PRIVATE)
                image.compress(Bitmap.CompressFormat.PNG, 100, foStream)
                foStream.close()
            } catch (e: java.lang.Exception) {
                Log.e(TAG, "Ocorreu um erro ao salvar a imagem.\nImage: " + id)
                e.printStackTrace()
            }
        }

        fun loadImageBitmap(
            context: Context,
            imageName: String
        ): Bitmap? {
            var bitmap: Bitmap? = null
            val fiStream: FileInputStream
            try {
                fiStream = context.openFileInput("$imageName.png")
                bitmap = BitmapFactory.decodeStream(fiStream)
                fiStream.close()
            } catch (e: java.lang.Exception) {
                Log.e(TAG, "Ocorreu um erro ao resgatar a imagem.\nImage: " + imageName)
                e.printStackTrace()
            }
            return bitmap
        }

        fun hasImage(
            context: Context,
            imageName: String
        ) = context.getFileStreamPath("$imageName.png").exists()

        fun deleteImage(
            context: Context,
            imageName: String
        ) = context.getFileStreamPath("$imageName.png").delete()

        private fun downloadImageTask(
            url: String
        ): Bitmap? {
            var bitmap: Bitmap? = null
            var urlConnection: HttpURLConnection? = null
            try {
                val uri = URL(url)
                urlConnection = uri.openConnection() as HttpURLConnection
                if (urlConnection.responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream: InputStream = urlConnection.inputStream
                    bitmap = BitmapFactory.decodeStream(inputStream)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ocorreu um erro ao realizar o download da imagem.\nUrl: " + url)
                Log.d(TAG, e.toString())
            } finally {
                urlConnection?.disconnect()
                return bitmap
            }
        }
    }
}