import android.content.ClipData
import android.content.ClipDescription
import android.os.Bundle
import android.view.View
import androidx.annotation.IntRange
import androidx.core.view.get
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pspdfkit.configuration.PdfConfiguration
import com.pspdfkit.listeners.OnVisibilityChangedListener
import com.pspdfkit.ui.PdfThumbnailGrid
import com.pspdfkit.ui.PdfUiFragment
import java.util.*


class CustomUiFragment : PdfUiFragment(){

    private var pdfThumbnailGrid: PdfThumbnailGrid? = null

    override fun onDocumentLoaded(document: com.pspdfkit.document.PdfDocument) {
        super.onDocumentLoaded(document)


        pdfThumbnailGrid = pspdfKitViews.thumbnailGridView
       // pdfThumbnailGrid?.enterDocumentEditingMode()
        pdfThumbnailGrid?.isLongClickable=true
        pdfThumbnailGrid?.get(0)?.setOnLongClickListener(onLongClickListener);
//        pdfThumbnailGrid?.isDocumentEditorEnabled = true
//        pdfThumbnailGrid?.isSelected=true
//        pdfThumbnailGrid?.performLongClick()
        pdfThumbnailGrid?.addOnPageClickListener { pdfThumbnailGrid, i ->
            // Perform any actions.

        }



            // Perform any actions.
          //  Snackbar.make(pdfThumbnailGrid, "Page $i clicked", Snackbar.LENGTH_SHORT).show()
    }
   // private val onVisibilityChangedListener =
    private val onLongClickListener = View.OnLongClickListener { view: View ->
        (view as? FloatingActionButton)?.let {
            view.hide()

            // First we create the `ClipData.Item` that we will need for the `ClipData`.
            // The `ClipData` carries the information of what is being dragged.
            // If you look at the main activity layout XML, you'll see that we've stored
            // color values for each of the FABs as their tags.
            val item = ClipData.Item(it.tag as? CharSequence)

            // We create a `ClipData` for the drag action and save the color as plain
            // text using `ClipDescription.MIMETYPE_TEXT_PLAIN`.
            val dragData = ClipData(
                    it.tag as? CharSequence,
                    arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                    item)

            // Instantiates the drag shadow builder, which is the class we will use
            // to draw a shadow of the dragged object. The implementation details
            // are in the rest of the article.


            // Start the drag. The new method is called `startDragAndDrop()` instead
            // of `startDrag()`, so we'll use it on the newer API.

            true
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onDestroy() {
        super.onDestroy()
        pdfThumbnailGrid?.isDocumentEditorEnabled = false
    }
    override fun onPageChanged(document: com.pspdfkit.document.PdfDocument, @IntRange(from = 0.toLong()) pageIndex: Int) {
        super.onPageChanged(document, pageIndex)
    }

    override fun onStop() {
        super.onStop()
        pdfThumbnailGrid?.isDocumentEditorEnabled = false
    }

    override fun onDocumentSaved(document: com.pspdfkit.document.PdfDocument) {
        super.onDocumentSaved(document)
    }

}