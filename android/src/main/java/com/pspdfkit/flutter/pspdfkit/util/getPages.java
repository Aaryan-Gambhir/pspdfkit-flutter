package com.pspdfkit.flutter.pspdfkit.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;

import com.pspdfkit.document.PdfDocument;
import com.pspdfkit.document.PdfDocumentLoader;
import com.pspdfkit.utils.Size;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.flutter.plugin.common.MethodChannel;

public class getPages extends AsyncTask<String,Void,ArrayList<byte[]>> {
    final Context context;
    final MethodChannel.Result result;
    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2, 2, 0,
            TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

    public getPages(Context context,MethodChannel.Result result) {
        this.context = context;
        this.result=result;
    }
    private Runnable getRunnable(final int pageIndex, final  PdfDocument newDoc,  final ArrayList<byte[]> pageList,CountDownLatch latch) {
        Runnable runnable = () -> {
            final Size pageSize = newDoc.getPageSize(pageIndex);
            final int width = 840;
            final int height = (int) (pageSize.height * (width / pageSize.width));
            Bitmap pageBitmap = newDoc.renderPageToBitmap(context, pageIndex, width, height);
            pageList.add(ImageUtils.convert(pageBitmap));
            latch.countDown();
        };
        return runnable;
    }
    @Override
    protected ArrayList<byte[]> doInBackground(String... docuPath) {
        PdfDocument newDoc = null;
        final ArrayList<byte[]> pageList = new ArrayList<>();
        int pageCount =0;
        try {
            Uri uri=Uri.fromFile(new File(docuPath[0]));
            newDoc = PdfDocumentLoader.openDocument(context,uri);
        } catch (IOException e) {
            return pageList;
        }
        try {
            assert newDoc != null;
            pageCount = newDoc.getPageCount();
        }
        catch (Exception e){
            return pageList;
        }
            CountDownLatch countDownLatch = new CountDownLatch(pageCount);
        for(int pageIndex = 0;pageIndex < pageCount;pageIndex++)
        // Page size is in PDF points (not pixels).
        {
           threadPoolExecutor.execute(getRunnable(pageIndex,newDoc,pageList,countDownLatch));
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            return pageList;
        }
        return pageList;
    }

    protected void onPostExecute(ArrayList<byte[]> data) {
        result.success(data);
    }
}
