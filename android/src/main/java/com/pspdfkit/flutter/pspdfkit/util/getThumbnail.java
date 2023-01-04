package com.pspdfkit.flutter.pspdfkit.util;

import static com.pspdfkit.flutter.pspdfkit.util.PdfUtils.convertPathToUri;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;

import com.pspdfkit.document.PdfDocument;
import com.pspdfkit.document.PdfDocumentLoader;
import com.pspdfkit.utils.Size;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.flutter.plugin.common.MethodChannel;

public class getThumbnail extends AsyncTask<String,Void,String> {
    final Context context;
    final MethodChannel.Result result;

    public getThumbnail(Context context,MethodChannel.Result result) {
        this.context = context;
        this.result=result;
    }


    @Override
    protected String doInBackground(String... docuPath) {
        PdfDocument newDoc = null;
        Uri uri=convertPathToUri(docuPath[0]);
        try {
            newDoc = PdfDocumentLoader.openDocument(context,uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        final Size pageSize = newDoc.getPageSize(0);
        final int width = 840;
        final int height = (int) (pageSize.height * (width / pageSize.width));
        Bitmap pageBitmap = newDoc.renderPageToBitmap(context, 0, width, height);
        File fileA=new File(docuPath[1]);
        FileOutputStream fileStream = null;
        try {
            fileStream = new FileOutputStream(fileA);
        } catch (FileNotFoundException e) {
            result.success(null);
        }
        try {

            fileStream.write(ImageUtils.convert(pageBitmap));
        } catch (FileNotFoundException e) {
            result.success(null);
            return "";
        } catch (IOException e) {
            result.success(null);
            return "";
        }finally {
            try {
                fileStream.close();
            } catch (IOException e) {
                result.success(null);
            }
        }
       return fileA.getPath();
    }

    protected void onPostExecute(String data) {
        result.success(data);
    }
}
