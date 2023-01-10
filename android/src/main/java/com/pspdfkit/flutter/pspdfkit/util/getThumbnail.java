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
        try {
            Uri uri=convertPathToUri(docuPath[0]);
            File fileA=new File(docuPath[1]);
            newDoc = PdfDocumentLoader.openDocument(context,uri);
        final Size pageSize = newDoc.getPageSize(0);
        final int width = 840;
        final int height = (int) (pageSize.height * (width / pageSize.width));
        Bitmap pageBitmap = newDoc.renderPageToBitmap(context, 0, width, height);
        FileOutputStream fileStream = null;
        fileStream = new FileOutputStream(fileA);
        fileStream.write(ImageUtils.convert(pageBitmap));
        return fileA.getPath();
        } catch (Exception e) {
            result.success(null);
            return "";
        }

    }

    protected void onPostExecute(String data) {
        if(data.isEmpty()){
            result.success(null);
        }
        else{
            result.success(data);
        }

    }
}
