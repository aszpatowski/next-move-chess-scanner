package com.example.next_move_chess_scanner;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.Pair;


//import com.example.next_move_chess_scanner.ml.OneNetOneInput;
import com.example.next_move_chess_scanner.ml.OneNetTwoInputs;


import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.lang.Float;

public final class PieceClassifier {
    private static final int inputFieldWidth = 32;
    private static final int inputFieldHeight = 32;
    private static final int inputBoardWidth = 200;
    private static final int inputBoardHeight = 200;
    private static final int FLOAT_TYPE_SIZE = 4;
    private static final int PIXEL_SIZE_RGB = 3;
    private static final int modelFieldInputSizeRGB = FLOAT_TYPE_SIZE * inputFieldWidth * inputFieldHeight * PIXEL_SIZE_RGB;
    private static final int modelBoardInputSizeRGB = FLOAT_TYPE_SIZE * inputBoardWidth * inputBoardHeight * PIXEL_SIZE_RGB;
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



    public Pair<String, Float> recognizePiece(Bitmap piece, ByteBuffer board){

        ByteBuffer convertedBitmapFieldRGB = convertFieldBitmapToByteBufferRGB(piece);
        float[] arrayWithProbabilities = recognizeField(convertedBitmapFieldRGB, board);
        int index = getMax(arrayWithProbabilities);
        return new Pair<>(PIECES_NAMES[index], arrayWithProbabilities[index]);


    }


    private ByteBuffer convertFieldBitmapToByteBufferRGB(Bitmap bitmap){
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(modelFieldInputSizeRGB);
        byteBuffer.order(ByteOrder.nativeOrder());

        //val pixels = IntArray(inputImageWidth * inputImageHeight);
        int[] pixels = new int[inputFieldWidth * inputFieldHeight];
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



    private float[] recognizeField(ByteBuffer field, ByteBuffer board) {
        try {
            OneNetTwoInputs model = OneNetTwoInputs.newInstance(context.getApplicationContext());

            // Creates inputs for reference.

            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 32, 32,3}, DataType.FLOAT32);
            TensorBuffer inputFeature1 = TensorBuffer.createFixedSize(new int[]{1, 200, 200,3}, DataType.FLOAT32);

            inputFeature0.loadBuffer(field);
            inputFeature1.loadBuffer(board);

            // Runs model inference and gets result.
            OneNetTwoInputs.Outputs outputs = model.process(inputFeature0, inputFeature1);
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
