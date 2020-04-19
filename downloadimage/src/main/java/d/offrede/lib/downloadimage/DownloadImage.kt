package d.offrede.lib.downloadimage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URL
import javax.net.ssl.HttpsURLConnection

private val TAG = "DownloadImage"

class DownloadImage {
    companion object {
        fun downloadImage(context: Context, id: String, url: String, event: () -> Unit = {}) {
            DownloadImageTask { bitmap ->
                bitmap?.let {
                    saveImage(context, id, it, event)
                }
            }.execute(url)
        }

        fun saveImage(context: Context, id: String, image: Bitmap, event: () -> Unit = {}) {
            val foStream: FileOutputStream
            try {
                foStream = context.openFileOutput("$id.jpg", Context.MODE_PRIVATE)
                image.compress(Bitmap.CompressFormat.PNG, 100, foStream)
                foStream.close()
            } catch (e: java.lang.Exception) {
                Log.e(TAG, "Ocorreu um erro ao salvar a imagem.\nImage: " + id)
                e.printStackTrace()
            }

            event()
        }

        fun loadImageBitmap(
            context: Context,
            imageName: String
        ): Bitmap? {
            var bitmap: Bitmap? = null
            val fiStream: FileInputStream
            try {
                fiStream = context.openFileInput("$imageName.jpg")
                bitmap = BitmapFactory.decodeStream(fiStream)
                fiStream.close()
            } catch (e: java.lang.Exception) {
                Log.e(TAG, "Ocorreu um erro ao resgatar a imagem.\nImage: " + imageName)
                e.printStackTrace()
            }
            return bitmap
        }
    }
}

private class DownloadImageTask(private val event: (Bitmap?) -> Unit = { _ -> }) :
    AsyncTask<String, Void, Bitmap>() {
    private fun downloadImageBitmap(sUrl: String): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            val inputStream: InputStream = URL(sUrl).openStream() // Download Image from URL
            bitmap = BitmapFactory.decodeStream(inputStream) // Decode Bitmap
            inputStream.close()
        } catch (e: Exception) {
            Log.e(TAG, "Ocorreu um erro ao realizar o download da imagem.\nUrl: " + sUrl)
            e.printStackTrace()
        }
        return bitmap
    }

    override fun doInBackground(vararg params: String) : Bitmap? {
        return downloadImageBitmap(params[0])
    }

    override fun onPostExecute(result: Bitmap?) {
        super.onPostExecute(result)
        result?.let {
            event(it)
        }
    }
}