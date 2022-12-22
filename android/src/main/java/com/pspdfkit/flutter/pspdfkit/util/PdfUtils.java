package com.pspdfkit.flutter.pspdfkit.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import com.pspdfkit.document.PdfDocument;
import com.pspdfkit.document.PdfDocumentLoader;
import com.pspdfkit.document.processor.NewPage;
import com.pspdfkit.document.processor.PdfProcessor;
import com.pspdfkit.document.processor.PdfProcessorTask;
import com.pspdfkit.utils.Size;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
}
