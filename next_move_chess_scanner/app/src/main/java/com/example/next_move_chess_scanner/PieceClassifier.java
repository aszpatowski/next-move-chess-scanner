package com.example.next_move_chess_scanner;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.Pair;

// import com.example.next_move_chess_scanner.ml.BlackModel;
// import com.example.next_move_chess_scanner.ml.WhiteModel;
// import com.example.next_move_chess_scanner.ml.WhitePiecesModelAll;
// import com.example.next_move_chess_scanner.ml.BlackPiecesModelAll;
import com.example.next_move_chess_scanner.ml.WhiteBlankOrOccupiedModelAll;
import com.example.next_move_chess_scanner.ml.BlackBlankOrOccupiedModelAll;

import com.example.next_move_chess_scanner.ml.WhiteBlackOrWhiteModelRgb;
import com.example.next_move_chess_scanner.ml.BlackBlackOrWhiteModelRgb;


import com.example.next_move_chess_scanner.ml.WhitePiecesModelWhite;
import com.example.next_move_chess_scanner.ml.WhitePiecesModelBlack;
import com.example.next_move_chess_scanner.ml.BlackPiecesModelWhite;
import com.example.next_move_chess_scanner.ml.BlackPiecesModelBlack;


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
    private static String[] WHITE_PIECES_NAMES;
    private static String[] BLACK_PIECES_NAMES;
    private final Context context;

    PieceClassifier(Context ctx) {
        context = ctx;
        WHITE_PIECES_NAMES = new String[]{
                "B",
                "K",
                "N",
                "P",
                "Q",
                "R",
        };
        BLACK_PIECES_NAMES = new String[]{
                "b",
                "k",
                "n",
                "p",
                "q",
                "r",
        };
    }



    public Pair<String, Float> recognizePiece(Bitmap piece, Boolean isWhite){

        ByteBuffer convertedBitmap = convertBitmapToByteBuffer(piece);
        ByteBuffer convertedBitmapRGB = convertBitmapToByteBufferRGB(piece);

        float[] arrayWithProbabilities;
        int index;

        if(isWhite)
        {
            Pair <Boolean,Float> isOccupied = isOccupied(recognizeIsOccupiedWhiteField(convertedBitmap));
            if(isOccupied.first){
                Pair <Boolean,Float> isBlackOrWhite = isBlackOrWhite(recognizeWhiteOrBlackWhiteField(convertedBitmapRGB));
                if(isBlackOrWhite.first)
                {
                    arrayWithProbabilities = recognizeWhiteFieldWhitePiece(convertedBitmap);
                    index = getMax(arrayWithProbabilities);
                    return new Pair<>(WHITE_PIECES_NAMES[index], isOccupied.second*isBlackOrWhite.second*arrayWithProbabilities[index]);
                }
                else{
                    arrayWithProbabilities = recognizeWhiteFieldBlackPiece(convertedBitmap);
                    index = getMax(arrayWithProbabilities);
                    return new Pair<>(BLACK_PIECES_NAMES[index], isOccupied.second*isBlackOrWhite.second*arrayWithProbabilities[index]);
                }
            }
            else{
                return new Pair<>("", isOccupied.second);
            }
        }
        else{
            Pair <Boolean,Float> isOccupied = isOccupied(recognizeIsOccupiedBlackField(convertedBitmap));
            if(isOccupied.first){
                Pair <Boolean,Float> isBlackOrWhite = isBlackOrWhite(recognizeWhiteOrBlackBlackField(convertedBitmapRGB));
                if(isBlackOrWhite.first)
                {
                    arrayWithProbabilities = recognizeBlackFieldWhitePiece(convertedBitmap);
                    index = getMax(arrayWithProbabilities);
                    return new Pair<>(WHITE_PIECES_NAMES[index], isOccupied.second*isBlackOrWhite.second*arrayWithProbabilities[index]);
                }
                else{
                    arrayWithProbabilities = recognizeBlackFieldBlackPiece(convertedBitmap);
                    index = getMax(arrayWithProbabilities);
                    return new Pair<>(BLACK_PIECES_NAMES[index], isOccupied.second*isBlackOrWhite.second*arrayWithProbabilities[index]);
                }
            }
            else{
                return new Pair<>("", isOccupied.second);
            }
        }


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

    private float[] recognizeWhiteFieldWhitePiece(ByteBuffer field) {
        try {
            WhitePiecesModelWhite model = WhitePiecesModelWhite.newInstance(context.getApplicationContext());
            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 32, 32, 1}, DataType.FLOAT32);
            inputFeature0.loadBuffer(field);
            // Runs model inference and gets result.
            WhitePiecesModelWhite.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            // Releases model resources if no longer used.
            model.close();
            return outputFeature0.getFloatArray();
        } catch (IOException e) {
            // TODO Handle the exception
        }
        return new float[0];
    }

    private float[] recognizeBlackFieldWhitePiece(ByteBuffer field) {
        try {
            BlackPiecesModelWhite model = BlackPiecesModelWhite.newInstance(context.getApplicationContext());
            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 32, 32, 1}, DataType.FLOAT32);
            inputFeature0.loadBuffer(field);
            // Runs model inference and gets result.
            BlackPiecesModelWhite.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            // Releases model resources if no longer used.
            model.close();
            return outputFeature0.getFloatArray();
        } catch (IOException e) {
            // TODO Handle the exception
        }
        return new float[0];
    }

    private float[] recognizeWhiteFieldBlackPiece(ByteBuffer field) {
        try {
            WhitePiecesModelBlack model = WhitePiecesModelBlack.newInstance(context.getApplicationContext());
            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 32, 32, 1}, DataType.FLOAT32);
            inputFeature0.loadBuffer(field);
            // Runs model inference and gets result.
            WhitePiecesModelBlack.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            // Releases model resources if no longer used.
            model.close();
            return outputFeature0.getFloatArray();
        } catch (IOException e) {
            // TODO Handle the exception
        }
        return new float[0];
    }

    private float[] recognizeBlackFieldBlackPiece(ByteBuffer field) {
        try {
            BlackPiecesModelBlack model = BlackPiecesModelBlack.newInstance(context.getApplicationContext());
            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 32, 32, 1}, DataType.FLOAT32);
            inputFeature0.loadBuffer(field);
            // Runs model inference and gets result.
            BlackPiecesModelBlack.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            // Releases model resources if no longer used.
            model.close();
            return outputFeature0.getFloatArray();
        } catch (IOException e) {
            // TODO Handle the exception
        }
        return new float[0];
    }


    private float[] recognizeWhiteOrBlackWhiteField(ByteBuffer field) {
        try {
            WhiteBlackOrWhiteModelRgb model = WhiteBlackOrWhiteModelRgb.newInstance(context.getApplicationContext());

            // Creates inputs for reference.

            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 32, 32,3}, DataType.FLOAT32);

            inputFeature0.loadBuffer(field);

            // Runs model inference and gets result.
            WhiteBlackOrWhiteModelRgb.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            // Releases model resources if no longer used.
            model.close();
            return outputFeature0.getFloatArray();
        } catch (IOException e) {
            // TODO Handle the exception
        }
        return new float[0];
    }

    private float[] recognizeWhiteOrBlackBlackField(ByteBuffer field) {
        try {
            BlackBlackOrWhiteModelRgb model = BlackBlackOrWhiteModelRgb.newInstance(context.getApplicationContext());

            // Creates inputs for reference.

            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 32, 32,3}, DataType.FLOAT32);

            inputFeature0.loadBuffer(field);

            // Runs model inference and gets result.
            BlackBlackOrWhiteModelRgb.Outputs outputs = model.process(inputFeature0);
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
            Log.d("pieceClassiferPiece", "Chance of " + BLACK_PIECES_NAMES[i]+" is "+ arr[i]);
            if(arr[i] > arr[max])
            {
                max=i;
            }
        }
        return max;
    }
    Pair <Boolean,Float> isOccupied(float[] arr){
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
        return new Pair<>(max != 0, arr[max]);
    }
    Pair <Boolean,Float> isBlackOrWhite(float[] arr){
        String[] color = new String[]{
                "Black",
                "White"
        };
        int max = 0;

        for (int i=0; i<arr.length; i++){
            Log.d("pieceClassiferColor", "Chance of " + color[i]+" is "+ arr[i]);
            if(arr[i] > arr[max])
            {
                max=i;
            }
        }
        return new Pair<>(max != 0, arr[max]);
    }

}
