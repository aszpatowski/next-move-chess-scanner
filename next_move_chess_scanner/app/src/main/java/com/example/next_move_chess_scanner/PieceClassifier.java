package com.example.next_move_chess_scanner;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.Pair;


import com.example.next_move_chess_scanner.ml.OneNetOneInput;


import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.lang.Float;

public final class PieceClassifier {
    private static final int inputImageWidth = 32;
    private static final int inputImageHeight = 32;
    private static final int FLOAT_TYPE_SIZE = 4;
    private static final int PIXEL_SIZE_GRAY = 1;
    private static final int PIXEL_SIZE_RGB = 3;
    private static final int modelInputSize = FLOAT_TYPE_SIZE * inputImageWidth * inputImageHeight * PIXEL_SIZE_GRAY;
    private static final int modelInputSizeRGB = FLOAT_TYPE_SIZE * inputImageWidth * inputImageHeight * PIXEL_SIZE_RGB;
    private static String[] PIECES_NAMES;
    private final Context context;

    PieceClassifier(Context ctx) {
        context = ctx;
        PIECES_NAMES = new String[]{
                "b",
                "B",
                "",
                "k",
                "K",
                "n",
                "N",
                "p",
                "P",
                "q",
                "Q",
                "r",
                "R",
        };
    }



    public Pair<String, Float> recognizePiece(Bitmap piece){

        ByteBuffer convertedBitmapRGB = convertBitmapToByteBufferRGB(piece);
        float[] arrayWithProbabilities = recognizeField(convertedBitmapRGB);
        int index = getMax(arrayWithProbabilities);
        return new Pair<>(PIECES_NAMES[index], arrayWithProbabilities[index]);


    }


    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap){
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(modelInputSize);
        byteBuffer.order(ByteOrder.nativeOrder());

        //val pixels = IntArray(inputImageWidth * inputImageHeight);
        int[] pixels = new int[inputImageWidth * inputImageHeight];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        for (int pixelValue: pixels) {
            int r = (pixelValue >> 16 & 255 );
            int g = (pixelValue >> 8 & 255);
            int b = (pixelValue & 255);

            // conversion from rgb to grayscale and normalization 0 to 1
            float normalizedPixelValue = (r + g + b ) / 3.0f / 255.0f;
            byteBuffer.putFloat(normalizedPixelValue);
        }

        return byteBuffer;
    }

    private ByteBuffer convertBitmapToByteBufferRGB(Bitmap bitmap){
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(modelInputSizeRGB);
        byteBuffer.order(ByteOrder.nativeOrder());

        //val pixels = IntArray(inputImageWidth * inputImageHeight);
        int[] pixels = new int[inputImageWidth * inputImageHeight];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        for (int pixelValue: pixels) {
            //Log.d("BufferR", Integer.toString(pixelValue >> 16 & 255 ));
            //Log.d("BufferG", Integer.toString(pixelValue >> 8 & 255 ));
            //Log.d("BufferB", Integer.toString(pixelValue & 255 ));
            float r = (pixelValue >> 16 & 255 ) / 255.0f;
            float g = (pixelValue >> 8 & 255) / 255.0f;
            float b = (pixelValue & 255) / 255.0f;

            // conversion from rgb to grayscale and normalization 0 to 1
            byteBuffer.putFloat(r);
            byteBuffer.putFloat(g);
            byteBuffer.putFloat(b);
        }

        return byteBuffer;
    }


    private float[] recognizeField(ByteBuffer field) {
        try {
            OneNetOneInput model = OneNetOneInput.newInstance(context.getApplicationContext());

            // Creates inputs for reference.

            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 32, 32,3}, DataType.FLOAT32);

            inputFeature0.loadBuffer(field);

            // Runs model inference and gets result.
            OneNetOneInput.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            // Releases model resources if no longer used.
            model.close();
            return outputFeature0.getFloatArray();
        } catch (IOException e) {
            // TODO Handle the exception
        }
        return new float[0];
    }


    int getMax(float[] arr){
        int max = 0;

        for (int i=0; i<arr.length; i++){
            Log.d("pieceClassiferPiece", "Chance of " + PIECES_NAMES[i]+" is "+ arr[i]);
            if(arr[i] > arr[max])
            {
                max=i;
            }
        }
        return max;
    }

}
