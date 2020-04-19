package d.offrede.libdownloadimage.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import d.offrede.lib.downloadimage.DownloadImage
import d.offrede.lib.downloadimage.loadDownloadImage
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadImages()
    }

    private fun loadImages() {
        imageViewFirstOnline.loadDownloadImage(
            "1",
            "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b4/The_Sun_by_the_Atmospheric_Imaging_Assembly_of_NASA%27s_Solar_Dynamics_Observatory_-_20100819.jpg/628px-The_Sun_by_the_Atmospheric_Imaging_Assembly_of_NASA%27s_Solar_Dynamics_Observatory_-_20100819.jpg",
            true
        )

        imageViewFirstOffline.loadDownloadImage(
            "2",
            "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b4/The_Sun_by_the_Atmospheric_Imaging_Assembly_of_NASA%27s_Solar_Dynamics_Observatory_-_20100819.jpg/628px-The_Sun_by_the_Atmospheric_Imaging_Assembly_of_NASA%27s_Solar_Dynamics_Observatory_-_20100819.jpg"
        )
    }

    private fun clickReload() {
        imageViewFirstOnline.setImageResource(0)
        imageViewFirstOffline.setImageResource(0)
        loadImages()
    }

    fun onClick(view: View) {
        when (view.id) {
            R.id.buttonReload -> clickReload()
            R.id.buttonDeleteOnline -> DownloadImage.deleteImage(this, "1")
            R.id.buttonDeleteOffline -> DownloadImage.deleteImage(this, "2")
        }
    }

}
