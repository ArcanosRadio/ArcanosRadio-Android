package de.developercity.arcanosradio.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v8.renderscript.*;

public class ImageHelper {

    public static Bitmap scaleBitmap(int scaleFactor, byte[] byteArray) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, bmOptions);
    }

    public static int findScaleFactor(int targetW, int targetH, byte[] byteArray) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, bmOptions);
        int actualW = bmOptions.outWidth;
        int actualH = bmOptions.outHeight;

        return Math.min(actualW / targetW, actualH / targetH);
    }

    public static Bitmap decodeAndRescaleBitmap(byte[] byteArray, int width, int height) {
        int scaleFactor = findScaleFactor(width, height, byteArray);

        return scaleBitmap(scaleFactor, byteArray);
    }

    // https://futurestud.io/tutorials/how-to-blur-images-efficiently-with-androids-renderscript
    public static Bitmap blur(Context context, Bitmap image, float radius) {
        int width = image.getWidth();
        int height = image.getHeight();

        Bitmap inputBitmap = Bitmap.createScaledBitmap(image, width, height, false);
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);

        RenderScript rs = RenderScript.create(context);
        ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
        theIntrinsic.setRadius(radius);
        theIntrinsic.setInput(tmpIn);
        theIntrinsic.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);

        return outputBitmap;
    }

}
