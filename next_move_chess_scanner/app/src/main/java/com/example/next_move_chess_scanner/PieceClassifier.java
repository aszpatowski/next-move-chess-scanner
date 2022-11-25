package com.example.next_move_chess_scanner;

import android.graphics.Bitmap;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class PieceClassifier {
    private int inputImageWidth;
    private int inputImageHeight;
    private int modelInputSize;
    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap){
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(modelInputSize);
        byteBuffer.order(ByteOrder.nativeOrder());

        //val pixels = IntArray(inputImageWidth * inputImageHeight);
        int[] pixels = new int[inputImageWidth * inputImageHeight];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        for (int pixelValue: pixels) {
            int r = (pixelValue >> 16 & 0xFF);
            int g = (pixelValue >> 8 & 0xFF);
            int b = (pixelValue & 0xFF);

            // conversion from rgb to grayscale and normalization 0 to 1
            float normalizedPixelValue = (r + g + b) / 3.0f / 255.0f;
            byteBuffer.putFloat(normalizedPixelValue);
        }

        return byteBuffer;
    }
}
