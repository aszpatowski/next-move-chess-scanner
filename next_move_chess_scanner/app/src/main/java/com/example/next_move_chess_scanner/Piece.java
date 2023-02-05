package com.example.next_move_chess_scanner;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

public class Piece implements Parcelable {
    // Create a static map to store the mapping of FEN notations to chess piece names
    public static final Map<String, String> FEN_TO_NAMES = new HashMap<>();
    public static Context mContext;


    public Piece(Context context){
        mContext = context;
            // Put the mappings for white pieces into the map
    
            FEN_TO_NAMES.put("K", mContext.getString(R.string.whiteKing));
            FEN_TO_NAMES.put("Q", mContext.getResources().getString(R.string.whiteQueen));
            FEN_TO_NAMES.put("R", mContext.getResources().getString(R.string.whiteRook));
            FEN_TO_NAMES.put("B", mContext.getResources().getString(R.string.whiteBishop));
            FEN_TO_NAMES.put("N", mContext.getResources().getString(R.string.whiteKnight));
            FEN_TO_NAMES.put("P", mContext.getResources().getString(R.string.whitePawn));
    
            // Put the mappings for black pieces into the map
            FEN_TO_NAMES.put("k", mContext.getResources().getString(R.string.blackKing));
            FEN_TO_NAMES.put("q", mContext.getResources().getString(R.string.blackQueen));
            FEN_TO_NAMES.put("r", mContext.getResources().getString(R.string.blackRook));
            FEN_TO_NAMES.put("b", mContext.getResources().getString(R.string.blackBishop));
            FEN_TO_NAMES.put("n", mContext.getResources().getString(R.string.blackKnight));
            FEN_TO_NAMES.put("p", mContext.getResources().getString(R.string.blackPawn));
    
            FEN_TO_NAMES.put("", mContext.getResources().getString(R.string.blank));
    }
    public Piece(String field, Bitmap fieldPhoto, String recognizedFEN, Float confidence){
        this.field = field;
        this.fieldPhoto = fieldPhoto;
        this.recognizedFEN = recognizedFEN;
        this.recognizedName = FEN_TO_NAMES.get(recognizedFEN);
        this.confidence = confidence;
    }

    protected Piece(Parcel in) {
        field = in.readString();
        fieldPhoto = Bitmap.CREATOR.createFromParcel(in);
        recognizedFEN = in.readString();
        recognizedName = in.readString();
        confidence = in.readFloat();
    }

    public static final Creator<Piece> CREATOR = new Creator<Piece>() {
        @Override
        public Piece createFromParcel(Parcel in) {
            return new Piece(in);
        }

        @Override
        public Piece[] newArray(int size) {
            return new Piece[size];
        }
    };

    public String getField() {
        return field;
    }

    public Bitmap getFieldPhoto() {
        return fieldPhoto;
    }

    public String getRecognizedName() {
        return recognizedName;
    }
    public String getRecognizedFEN() {
        return recognizedFEN;
    }

    public Float getConfidence() {
        return confidence;
    }

    private String field;
    private Bitmap fieldPhoto;
    private String recognizedFEN;
    private String recognizedName;
    private Float confidence;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(field);
        fieldPhoto.writeToParcel(parcel,0);
        parcel.writeString(recognizedFEN);
        parcel.writeString(recognizedName);
        parcel.writeFloat(confidence);
    }
}
