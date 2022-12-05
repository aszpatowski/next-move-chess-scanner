package com.example.next_move_chess_scanner;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.Pair;

import com.example.next_move_chess_scanner.ml.BlackModel;
import com.example.next_move_chess_scanner.ml.WhiteModel;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.lang.Float;

public final class PieceClassifier {
    private static final int inputImageWidth = 32;
    private static final int inputImageHeight = 32;
    private static final int FLOAT_TYPE_SIZE = 4;
    private static final int PIXEL_SIZE = 1;
    private static final int modelInputSize = FLOAT_TYPE_SIZE * inputImageWidth * inputImageHeight * PIXEL_SIZE;
    private static final String[] PIECES_NAMES = {
            "Black bishop",
            "White bishop",
            "Blank",
            "Black king",
            "White king",
            "Black knight",
            "White knight",
            "Black pawn",
            "White pawn",
            "Black queen",
            "White queen",
            "Black rook",
            "White rook"
        };
    private static Context context;

    PieceClassifier(Context ctx) {
        context = ctx;
    }


    public Pair<String, Float> recognizePiece(Bitmap piece, Boolean isWhite){

        ByteBuffer convertedBitmap = convertBitmapToByteBuffer(piece);
        //ByteBuffer convertedBitmap = convertBitmapToByteBuffer2(piece);
        //ByteBuffer convertedBitmap = TensorImage.fromBitmap(piece).getBuffer();
        float[] arrayWithProbabilities;

        if(isWhite)
        {
            arrayWithProbabilities = recognizeWhiteField(convertedBitmap);
        }
        else{
            arrayWithProbabilities = recognizeBlackField(convertedBitmap);
        }
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


    private float[] recognizeWhiteField(ByteBuffer field) {
        try {
            WhiteModel model = WhiteModel.newInstance(context.getApplicationContext());
            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 32, 32, 1}, DataType.FLOAT32);
            inputFeature0.loadBuffer(field);
            // Runs model inference and gets result.
            WhiteModel.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            // Releases model resources if no longer used.
            model.close();
            return outputFeature0.getFloatArray();
        } catch (IOException e) {
            // TODO Handle the exception
        }
        return new float[0];
    }

    private float[] recognizeBlackField(ByteBuffer field) {
        try {
            BlackModel model = BlackModel.newInstance(context.getApplicationContext());

            // Creates inputs for reference.

            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 32, 32,1}, DataType.FLOAT32);

            inputFeature0.loadBuffer(field);

            // Runs model inference and gets result.
            BlackModel.Outputs outputs = model.process(inputFeature0);
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
            Log.d("pieceClassifer", "Chance of " + PIECES_NAMES[i]+" is "+ arr[i]);
            if(arr[i] > arr[max])
            {
                max=i;
            }
        }
        return max;
    }

}
