package com.sostvcn.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * Created by Administrator on 2017/5/20.
 */
public class ImageUtils {

    public static int IO_BUFFER_SIZE = 2 * 1024;


    public static Bitmap GetUrlBitmap(String url, int scaleRatio) {


        int blurRadius = 8;//通常设置为8就行。
        if (scaleRatio <= 0) {
            scaleRatio = 10;
        }


        Bitmap originBitmap = null;
        InputStream in = null;
        BufferedOutputStream out = null;
        try {
            in = new BufferedInputStream(new URL(url).openStream(), IO_BUFFER_SIZE);
            final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
            out = new BufferedOutputStream(dataStream, IO_BUFFER_SIZE);
            copy(in, out);
            out.flush();
            byte[] data = dataStream.toByteArray();
            originBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);


            Bitmap scaledBitmap = Bitmap.createScaledBitmap(originBitmap,
                    originBitmap.getWidth() / scaleRatio,
                    originBitmap.getHeight() / scaleRatio,
                    false);
            Bitmap blurBitmap = doBlur(scaledBitmap, blurRadius, true);
            return blurBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void copy(InputStream in, OutputStream out)
            throws IOException {
        byte[] b = new byte[IO_BUFFER_SIZE];
        int read;
        while ((read = in.read(b)) != -1) {
            out.write(b, 0, read);
        }
    }

    public static Bitmap doBlur(Bitmap sentBitmap, int radius, boolean canReuseInBitmap) {
        Bitmap bitmap;
        if (canReuseInBitmap) {
            bitmap = sentBitmap;
        } else {
            bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);
        }


        if (radius < 1) {
            return (null);
        }


        int w = bitmap.getWidth();
        int h = bitmap.getHeight();


        int[] pix = new int[w * h];
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);


        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;


        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];


        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }


        yw = yi = 0;


        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;


        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;


            for (x = 0; x < w; x++) {


                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];


                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;


                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];


                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];


                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];


                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);


                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];


                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;


                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];


                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];


                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];


                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;


                sir = stack[i + radius];


                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];


                rbs = r1 - Math.abs(i);


                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;


                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }


                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];


                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;


                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];


                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];


                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];


                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];


                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];


                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;


                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];


                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];


                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];


                yi += w;
            }
        }


        bitmap.setPixels(pix, 0, w, 0, 0, w, h);


        return (bitmap);
    }
}
