//package com.pspdfkit.flutter.pspdfkit.util
//
//import android.content.Context
//import android.graphics.Bitmap
//import android.graphics.BitmapFactory
//import android.net.Uri
//import com.pspdfkit.document.PdfDocument
//import com.pspdfkit.document.PdfDocumentLoader
//import com.pspdfkit.document.processor.*
//import com.pspdfkit.utils.Size
//import java.io.File
//import java.io.FileNotFoundException
//import java.io.FileOutputStream
//import java.io.IOException
//import java.util.concurrent.LinkedBlockingQueue
//import java.util.concurrent.ThreadPoolExecutor
//import java.util.concurrent.TimeUnit
//
//class PdfUtils {
//
//    fun getThumbnail(documentPath: String?, context: Context?, thumbnailPath: String?): String? {
//        var newDoc: PdfDocument? = null
//        val uri = convertPathToUri(documentPath)
//        try {
//            newDoc = PdfDocumentLoader.openDocument(context!!, uri)
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//        val pageSize = newDoc!!.getPageSize(0)
//        val width = 840
//        val height = (pageSize.height * (width / pageSize.width)).toInt()
//        val pageBitmap = newDoc.renderPageToBitmap(context!!, 0, width, height)
//        val fileA = File(thumbnailPath)
//        var fileStream: FileOutputStream? = null
//        try {
//            fileStream = FileOutputStream(fileA)
//        } catch (e: FileNotFoundException) {
//            e.printStackTrace()
//        }
//        try {
//            fileStream!!.write(ImageUtils.convert(pageBitmap))
//        } catch (e: FileNotFoundException) {
//            e.printStackTrace()
//            return null
//        } catch (e: IOException) {
//            e.printStackTrace()
//            return null
//        } finally {
//            try {
//                fileStream!!.close()
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//        }
//        return fileA.path
//    }
//
//    fun getPages(docuPath: String?, context: Context): ArrayList<Any?> {
//        val threadPoolExecutor = ThreadPoolExecutor(5, 5, 0,
//                TimeUnit.MILLISECONDS, LinkedBlockingQueue())
//        var newDoc: PdfDocument? = null
//        val pageList=  ArrayList<Any?>()
//        try {
//            val uri = Uri.fromFile(File(docuPath))
//            newDoc = PdfDocumentLoader.openDocument(context, uri)
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//        val pageCount = newDoc!!.pageCount
//        for (pageIndex in 0 until pageCount)  // Page size is in PDF points (not pixels).
//        {
//            val pageSize = newDoc!!.getPageSize(pageIndex)
//            val width = 840
//            val height = (pageSize.height * (width / pageSize.width)).toInt()
//            val pageBitmap = newDoc.renderPageToBitmap(context, pageIndex, width, height)
//            pageList.add(ImageUtils.convert(pageBitmap))
//        }
//
//        // We define a target width for the resulting bitmap and use it to calculate the final height.
//        return pageList
//    }
//
//    fun addPages(docuPath: String?, context: Context, imageData: ArrayList<ByteArray>, destPath: String?): Any? {
//        val uri = Uri.fromFile(File(docuPath))
//        var newDoc: PdfDocument? = null
//        val allPages = getPages(docuPath, context) as ArrayList<String>
//        try {
//            newDoc = PdfDocumentLoader.openDocument(context, uri)
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//        val task = PdfProcessorTask.fromDocument(newDoc!!)
//        for (i in imageData.indices) {
//            val bitmap = BitmapFactory.decodeByteArray(imageData[i], 0, imageData[i].size)
//            val pageSize = newDoc.getPageSize(0)
//            val resizedBitmap = getResizedBitmap(bitmap, pageSize)
//            task.addNewPage(
//                    NewPage.emptyPage(NewPage.PAGE_SIZE_A4)
//                            .withPageItem(PageImage(resizedBitmap, PagePosition.CENTER))
//                            .build(), newDoc.pageCount)
//            allPages.add(ImageUtils.convert(resizedBitmap))
//        }
//
//
//// Save it to a new document.
//        val outputFile = File(destPath)
//        PdfProcessor.processDocumentAsync(task, outputFile).subscribe()
//        return allPages
//    }
//
//    fun splitPdfs(context: Context?, pagesToSplit: ArrayList<Int>?, documentUri: Uri?, destFilePaths: ArrayList<String?>): ArrayList<String> {
//        val pageSet: Set<Int> = HashSet(pagesToSplit)
//        var originalDocument: PdfDocument? = null
//        try {
//            originalDocument = PdfDocumentLoader.openDocument(context!!, documentUri!!)
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//        val newDocumentTask = PdfProcessorTask.empty()
//        var index = 0
//        for (i in pageSet) {
//            if (i < originalDocument!!.pageCount) {
//                newDocumentTask.addNewPage(NewPage.fromPage(originalDocument, i).build(),
//                        index++)
//            }
//        }
//        val originalDocumentTask = PdfProcessorTask.fromDocument(originalDocument!!).removePages(pageSet)
//        val pathList = ArrayList<String>()
//        val fileA = File(destFilePaths[0])
//        val fileB = File(destFilePaths[1])
//        PdfProcessor.processDocument(originalDocumentTask, fileA)
//        PdfProcessor.processDocument(newDocumentTask, fileB)
//        pathList.add(fileA.path)
//        pathList.add(fileB.path)
//        return pathList
//    }
//
//    private fun getResizedBitmap(image: Bitmap, size: Size): Bitmap {
//        var image = image
//        val imageHeight = image.height.toDouble()
//        val imageWidth = image.width.toDouble()
//        var shrinkFactor: Double
//        var finalWidth = imageWidth
//        var finalHeight = imageHeight
//        if (imageWidth > size.width) {
//            shrinkFactor = imageWidth / size.width
//            finalWidth = size.width.toDouble()
//            finalHeight = imageHeight / shrinkFactor
//        }
//        if (imageHeight > size.height) {
//            shrinkFactor = imageHeight / size.height
//            finalHeight = size.height.toDouble()
//            finalWidth = imageWidth / shrinkFactor
//        }
//        image = Bitmap.createScaledBitmap(image, finalWidth.toInt(), finalHeight.toInt(), true)
//        return image
//    }
//
//    fun mergePdf(pdfPaths: List<String?>, context: Context?, destinationPath: String?): String {
//        val documents: MutableList<PdfDocument> = ArrayList()
//        for (path in pdfPaths) {
//            try {
//                val temp = PdfDocumentLoader.openDocument(context!!,
//                        convertPathToUri(path))
//                documents.add(temp)
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//        }
//        val task = PdfProcessorTask.empty()
//        var totalPageCount = 0
//        for (eachDocument in documents) {
//            for (i in 0 until eachDocument.pageCount) {
//                // Increment the `totalPageCount` each time to add each new page
//                // to the end of the document.
//                // However, the pages can be inserted at any index you'd like.
//                task.addNewPage(NewPage.fromPage(eachDocument, i).build(), totalPageCount++)
//            }
//        }
//        val outputFile = File(destinationPath)
//        PdfProcessor.processDocument(task, outputFile)
//        return outputFile.path
//    }
//
//    fun convertPathToUri(pdfString: String?): Uri {
//        return Uri.fromFile(File(pdfString))
//    }
//
//    fun removePages(documentPath: String?, pagesToRemove: ArrayList<Int>?, context: Context?, destPath: String?) {
//        val uri = convertPathToUri(documentPath)
//        val indexToRemove: Set<Int> = HashSet(pagesToRemove)
//        var originalDocument: PdfDocument? = null
//        try {
//            originalDocument = PdfDocumentLoader.openDocument(context!!, uri)
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//        val originalDocumentTask = PdfProcessorTask.fromDocument(originalDocument!!).removePages(indexToRemove)
//        // Process to an output document.
//        val outputFile = File(destPath)
//        PdfProcessor.processDocument(originalDocumentTask, outputFile)
//    }
//}