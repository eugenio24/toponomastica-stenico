package com.ferrarieugenio.toponomastica_stenico_app.util.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.content.res.ResourcesCompat
import com.ferrarieugenio.toponomastica_stenico_app.R
import com.ferrarieugenio.toponomastica_stenico_app.data.model.Toponym
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.maplibre.android.utils.BitmapUtils
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarkerIconCache @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // todo consider changing to LruCache when implementing dynamic loading on map
    private val unselectedBitmaps = mutableMapOf<Int, Bitmap>()
    private val selectedBitmaps = mutableMapOf<Int, Bitmap>()

    private val defaultUnselectedBitmap: Bitmap = run {
        ResourcesCompat.getDrawable(context.resources, R.drawable.ic_marker_unselected, null)?.let {
            BitmapUtils.getBitmapFromDrawable(it)
        } ?: throw IllegalStateException("Failed to load default unselected marker bitmap")
    }

    private val defaultSelectedBitmap: Bitmap = run {
        ResourcesCompat.getDrawable(context.resources, R.drawable.ic_marker_selected, null)?.let {
            BitmapUtils.getBitmapFromDrawable(it)
        } ?: throw IllegalStateException("Failed to load default selected marker bitmap")
    }

    fun getUnselectedBitmap(id: Int): Bitmap? = unselectedBitmaps[id]
    fun getSelectedBitmap(id: Int): Bitmap? = selectedBitmaps[id]

    fun getDefaultUnselectedBitmap(): Bitmap = defaultUnselectedBitmap
    fun getDefaultSelectedBitmap(): Bitmap = defaultSelectedBitmap

    private fun putUnselectedBitmap(id: Int, bitmap: Bitmap) {
        unselectedBitmaps[id] = bitmap
    }

    private fun putSelectedBitmap(id: Int, bitmap: Bitmap) {
        selectedBitmaps[id] = bitmap
    }

    suspend fun preloadBitmaps(toponyms: List<Toponym>) = withContext(Dispatchers.IO) {
        for (toponym in toponyms) {
            val id = toponym.id

            if (!unselectedBitmaps.containsKey(id)) {
                val unselectedPath = "$UNSELECTED_DIR/marker_$id.png"
                val bitmap = loadBitmapFromAssets(context, unselectedPath)
                bitmap?.let { putUnselectedBitmap(id, it) }
            }

            if (!selectedBitmaps.containsKey(id)) {
                val selectedPath = "$SELECTED_DIR/marker_$id.png"
                val bitmap = loadBitmapFromAssets(context, selectedPath)
                bitmap?.let { putSelectedBitmap(id, it) }
            }
        }
    }

    private fun loadBitmapFromAssets(context: Context, path: String): Bitmap? {
        return try {
            context.assets.open(path).use {
                BitmapFactory.decodeStream(it)
            }
        } catch (e: IOException) {
            null
        }
    }

    companion object {
        private const val UNSELECTED_DIR = "marker-icons/unselected"
        private const val SELECTED_DIR = "marker-icons/selected"
    }
}
