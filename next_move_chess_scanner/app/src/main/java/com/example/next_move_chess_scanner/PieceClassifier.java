package com.example.next_move_chess_scanner;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.Pair;

// import com.example.next_move_chess_scanner.ml.BlackModel;
// import com.example.next_move_chess_scanner.ml.WhiteModel;
import com.example.next_move_chess_scanner.ml.WhitePiecesModelAll;
import com.example.next_move_chess_scanner.ml.BlackPiecesModelAll;
import com.example.next_move_chess_scanner.ml.WhiteBlankOrOccupiedModelAll;
import com.example.next_move_chess_scanner.ml.BlackBlankOrOccupiedModelAll;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.Float;

public final class PieceClassifier {
    private static final int inputImageWidth = 32;
    private static final int inputImageHeight = 32;
    private static final int FLOAT_TYPE_SIZE = 4;
    private static final int PIXEL_SIZE = 1;
    private static final int modelInputSize = FLOAT_TYPE_SIZE * inputImageWidth * inputImageHeight * PIXEL_SIZE;
    private static String[] PIECES_NAMES;
    private final Context context;

    PieceClassifier(Context ctx) {
        context = ctx;
        PIECES_NAMES = new String[]{
                "b",
                "B",
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
                ""
        };
    }


    public Pair<String, Float> recognizePiece(Bitmap piece, Boolean isWhite){

        ByteBuffer convertedBitmap = convertBitmapToByteBuffer(piece);
        //ByteBuffer convertedBitmap = convertBitmapToByteBuffer2(piece);
        //ByteBuffer convertedBitmap = TensorImage.fromBitmap(piece).getBuffer();
        float[] arrayWithProbabilities;
        float isOccupiedProbability;

        if(isWhite)
        {
            arrayWithProbabilities = recognizeIsOccupiedWhiteField(convertedBitmap);
            int index = getMax2(arrayWithProbabilities);
            if(index == 0){
                return new Pair<>(PIECES_NAMES[12], arrayWithProbabilities[index]);
            }
            isOccupiedProbability = arrayWithProbabilities[index];
            arrayWithProbabilities = recognizePieceWhiteField(convertedBitmap);
        }
        else{
            arrayWithProbabilities = recognizeIsOccupiedBlackField(convertedBitmap);
            int index = getMax2(arrayWithProbabilities);
            if(index == 0){
                return new Pair<>(PIECES_NAMES[12], arrayWithProbabilities[index]);
            }
            isOccupiedProbability = arrayWithProbabilities[index];
            arrayWithProbabilities = recognizePieceBlackField(convertedBitmap);
        }
        int index = getMax(arrayWithProbabilities);

        return new Pair<>(PIECES_NAMES[index], isOccupiedProbability*arrayWithProbabilities[index]);
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


    private float[] recognizeIsOccupiedWhiteField(ByteBuffer field) {
        try {
            WhiteBlankOrOccupiedModelAll model = WhiteBlankOrOccupiedModelAll.newInstance(context.getApplicationContext());
            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 32, 32, 1}, DataType.FLOAT32);
            inputFeature0.loadBuffer(field);
            // Runs model inference and gets result.
            WhiteBlankOrOccupiedModelAll.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            // Releases model resources if no longer used.
            model.close();
            return outputFeature0.getFloatArray();
        } catch (IOException e) {
            // TODO Handle the exception
        }
        return new float[0];
    }

    private float[] recognizeIsOccupiedBlackField(ByteBuffer field) {
        try {
            BlackBlankOrOccupiedModelAll model = BlackBlankOrOccupiedModelAll.newInstance(context.getApplicationContext());

            // Creates inputs for reference.

            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 32, 32,1}, DataType.FLOAT32);

            inputFeature0.loadBuffer(field);

            // Runs model inference and gets result.
            BlackBlankOrOccupiedModelAll.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            // Releases model resources if no longer used.
            model.close();
            return outputFeature0.getFloatArray();
        } catch (IOException e) {
            // TODO Handle the exception
        }
        return new float[0];
    }

    private float[] recognizePieceWhiteField(ByteBuffer field) {
        try {
            WhitePiecesModelAll model = WhitePiecesModelAll.newInstance(context.getApplicationContext());
            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 32, 32, 1}, DataType.FLOAT32);
            inputFeature0.loadBuffer(field);
            // Runs model inference and gets result.
            WhitePiecesModelAll.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            // Releases model resources if no longer used.
            model.close();
            return outputFeature0.getFloatArray();
        } catch (IOException e) {
            // TODO Handle the exception
        }
        return new float[0];
    }

    private float[] recognizePieceBlackField(ByteBuffer field) {
        try {
            BlackPiecesModelAll model = BlackPiecesModelAll.newInstance(context.getApplicationContext());

            // Creates inputs for reference.

            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 32, 32,1}, DataType.FLOAT32);

            inputFeature0.loadBuffer(field);

            // Runs model inference and gets result.
            BlackPiecesModelAll.Outputs outputs = model.process(inputFeature0);
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
    int getMax2(float[] arr){
        String[] occupied = new String[]{
                "Blank",
                "Occupied"
        };
        int max = 0;

        for (int i=0; i<arr.length; i++){
            Log.d("pieceClassiferOccupied", "Chance of " + occupied[i]+" is "+ arr[i]);
            if(arr[i] > arr[max])
            {
                max=i;
            }
        }
        return max;
    }

}
