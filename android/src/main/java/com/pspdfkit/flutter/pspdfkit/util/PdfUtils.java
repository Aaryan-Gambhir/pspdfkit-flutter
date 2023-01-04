package com.pspdfkit.flutter.pspdfkit.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import com.pspdfkit.configuration.rendering.PageRenderConfiguration;
import com.pspdfkit.document.PdfDocument;
import com.pspdfkit.document.PdfDocumentLoader;
import com.pspdfkit.document.processor.NewPage;
import com.pspdfkit.document.processor.PageImage;
import com.pspdfkit.document.processor.PagePosition;
import com.pspdfkit.document.processor.PdfProcessor;
import com.pspdfkit.document.processor.PdfProcessorTask;
import com.pspdfkit.utils.Size;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.flutter.plugin.common.MethodChannel;
import io.reactivex.disposables.Disposable;

public class PdfUtils {

    public static String getThumbnail(String documentPath,Context context,String thumbnailPath){
        PdfDocument newDoc = null;
        Uri uri=convertPathToUri(documentPath);
        try {
            newDoc = PdfDocumentLoader.openDocument(context,uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        final Size pageSize = newDoc.getPageSize(0);
        final int width = 840;
        final int height = (int) (pageSize.height * (width / pageSize.width));
        Bitmap pageBitmap = newDoc.renderPageToBitmap(context, 0, width, height);
        File fileA=new File(thumbnailPath);
        FileOutputStream fileStream = null;
        try {
            fileStream = new FileOutputStream(fileA);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {

            fileStream.write(ImageUtils.convert(pageBitmap));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }finally {
            try {
                fileStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fileA.getPath();
    }

    public static  ArrayList<byte[]> getPages(String docuPath,Context context){
        PdfDocument newDoc = null;
        ArrayList<byte[]> pageList =new ArrayList();
        try {
            Uri uri=Uri.fromFile(new File(docuPath));
            newDoc = PdfDocumentLoader.openDocument(context,uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int pageCount = newDoc.getPageCount();
        for(int pageIndex = 0;pageIndex < pageCount;pageIndex++)
        // Page size is in PDF points (not pixels).
        {
            final Size pageSize = newDoc.getPageSize(pageIndex);
            final int width = 840;
            final int height = (int) (pageSize.height * (width / pageSize.width));
            Bitmap pageBitmap = newDoc.renderPageToBitmap(context, pageIndex, width, height);
            pageList.add(ImageUtils.convert(pageBitmap));
        }
        // We define a target width for the resulting bitmap and use it to calculate the final height.
        return pageList;
    }


    public  static ArrayList<byte[]>  addPages(String docuPath, Context context, ArrayList<byte[]> imageData, String destPath, MethodChannel.Result result){

        Uri uri=Uri.fromFile(new File(docuPath));
        PdfDocument newDoc=null;
        ArrayList<byte[]> allPages=getPages(docuPath,context);

        try {
            newDoc = PdfDocumentLoader.openDocument(context,uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        final PdfProcessorTask task = PdfProcessorTask.fromDocument(newDoc);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
          for(int i =0; i<imageData.size();i++){
            final Bitmap bitmap = BitmapFactory.decodeByteArray(imageData.get(i),0,imageData.get(i).length,options);
            final Size pageSize = newDoc.getPageSize(0);
            Bitmap resizedBitmap=getResizedBitmap(bitmap,pageSize);
          
            task.addNewPage(
                    NewPage.emptyPage(NewPage.PAGE_SIZE_A4)
                            .withPageItem(new PageImage(resizedBitmap, PagePosition.CENTER))
                            .build(),newDoc .getPageCount());
            allPages.add(ImageUtils.convert(resizedBitmap));
        }


// Save it to a new document.
        final File outputFile = new File(destPath);
        PdfProcessor.processDocumentAsync(task, outputFile).subscribe();
        return allPages;
    }
    public static ArrayList<String> splitPdfs(Context context,ArrayList<Integer> pagesToSplit,Uri documentUri,ArrayList<String> destFilePaths){
        Set<Integer> pageSet = new HashSet<>(pagesToSplit);
        PdfDocument originalDocument = null;
        try {
            originalDocument = PdfDocumentLoader.openDocument(context,documentUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        PdfProcessorTask newDocumentTask = PdfProcessorTask.empty();
        int index = 0;
        for (int i : pageSet) {
            if (i < originalDocument.getPageCount()) {
                newDocumentTask.addNewPage(NewPage.fromPage(originalDocument, i).build(),
                        index++);
            }
        }

        PdfProcessorTask originalDocumentTask =
                PdfProcessorTask.fromDocument(originalDocument).removePages(pageSet);
        ArrayList<String> pathList=new ArrayList<String>();
        File fileA = new File(destFilePaths.get(0));
        File fileB = new File(destFilePaths.get(1));

        PdfProcessor.processDocument(originalDocumentTask, fileA);
        PdfProcessor.processDocument(newDocumentTask, fileB);
        pathList.add(fileA.getPath());
        pathList.add(fileB.getPath());
        return pathList;
    }

    private static Bitmap getResizedBitmap(Bitmap image,Size size) {
        double imageHeight = image.getHeight();
        double imageWidth = image.getWidth();
        double shrinkFactor;
        if(imageWidth > size.width){
            shrinkFactor = imageWidth / size.width;
            imageWidth = size.width;
            imageHeight = imageHeight / shrinkFactor;
        }
        if (imageHeight > size.height ) {
            shrinkFactor = imageHeight / size.height;
            imageHeight = size.height;
            imageWidth = imageWidth / shrinkFactor;
        }
        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(image,(int)imageWidth,(int)imageHeight,false);
        return resizedBitmap;
    }
    public  static int getPageCount(String path,Context context){
        PdfDocument pdfDocument=null;
        try{
            pdfDocument =  PdfDocumentLoader.openDocument(context,
                    convertPathToUri(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        pdfDocument.getPageCount();
        return  pdfDocument.getPageCount();
    }
    public static String mergePdf(List<String> pdfPaths,Context context,String destinationPath){
        final List<PdfDocument> documents = new ArrayList<>();
        for(String path : pdfPaths){
            try{
                final PdfDocument temp =  PdfDocumentLoader.openDocument(context,
                        convertPathToUri(path));
                documents.add(temp);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        final PdfProcessorTask task = PdfProcessorTask.empty();
        int totalPageCount = 0;
        for (PdfDocument eachDocument : documents) {
            for (int i = 0; i < eachDocument.getPageCount(); i++) {
                // Increment the `totalPageCount` each time to add each new page
                // to the end of the document.
                // However, the pages can be inserted at any index you'd like.
                task.addNewPage(NewPage.fromPage(eachDocument, i).build(), totalPageCount++);
            }
        }

        final File outputFile = new File(destinationPath);
        PdfProcessor.processDocument(task, outputFile);
        return outputFile.getPath();
    }

    public static Uri convertPathToUri(String pdfString){
        Uri uri=Uri.fromFile(new File(pdfString));
        return uri;
    }

    public static void removePages(String documentPath,ArrayList<Integer> pagesToRemove,Context context,String destPath){
        Uri uri = convertPathToUri(documentPath);

        final Set<Integer> indexToRemove = new HashSet<>(pagesToRemove);
        PdfDocument originalDocument = null;
        try {
            originalDocument = PdfDocumentLoader.openDocument(context,uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        PdfProcessorTask originalDocumentTask =
                PdfProcessorTask.fromDocument(originalDocument).removePages(indexToRemove);
// Process to an output document.
        File outputFile = new File(destPath);
        PdfProcessor.processDocument(originalDocumentTask, outputFile);

    }
}
