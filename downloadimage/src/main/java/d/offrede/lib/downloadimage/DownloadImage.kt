package d.offrede.lib.downloadimage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import android.widget.ImageView
import d.offrede.lib.downloadimage.DownloadImage.Companion.downloadImage
import d.offrede.lib.downloadimage.DownloadImage.Companion.hasImage
import d.offrede.lib.downloadimage.DownloadImage.Companion.loadImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
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
        downloadImage(
            this.context,
            id,
            url,
            { imageBitmap ->
                this.setImageBitmap(imageBitmap)
            },
            {
                if (hasImage(this.context, id)) {
                    Log.e(TAG, "Usando imagem salva em disco.")
                    this.setImageBitmap(
                        loadImageBitmap(
                            this.context,
                            id
                        )
                    )
                }
            }
        )
    }
}

class DownloadImage {
    companion object {
        fun downloadImage(
            context: Context,
            id: String,
            url: String,
            success: ((Bitmap) -> Unit)? = { _ -> },
            failure: (() -> Unit)? = {}
        ) = runBlocking<Unit> {
            downloadImageFlow(url).collect { bitmap ->
                bitmap?.let {
                    saveImage(context, id, it, success)
                }
            }.runCatching {
                failure?.invoke()
            }
        }

        fun saveImage(
            context: Context,
            id: String,
            image: Bitmap,
            event: ((Bitmap) -> Unit)? = { _ -> }
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

            event?.invoke(image)
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

        private fun downloadImageFlow(
            url: String
        ): Flow<Bitmap?> = flow {
            var urlConnection: HttpURLConnection? = null
            var bitmap: Bitmap? = null
            try {
                val uri = URL(url)
                urlConnection = uri.openConnection() as HttpURLConnection
                if (urlConnection.responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream: InputStream = urlConnection.inputStream
                    bitmap = BitmapFactory.decodeStream(inputStream)
                }
                emit(bitmap)
            } catch (e: Exception) {
                Log.e(TAG, "Ocorreu um erro ao realizar o download da imagem.\nUrl: " + url)
                Log.d(TAG, e.toString())
                urlConnection?.disconnect()
            } finally {
                urlConnection?.disconnect()
            }
        }.flowOn(Dispatchers.IO)
    }
}