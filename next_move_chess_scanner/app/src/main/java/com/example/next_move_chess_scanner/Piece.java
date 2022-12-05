package com.example.next_move_chess_scanner;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class Piece implements Parcelable {
    public Piece(String field, Bitmap fieldPhoto, String recognized, Float confidence){
        this.field = field;
        this.fieldPhoto = fieldPhoto;
        this.recognized = recognized;
        this.confidence = confidence;
    }

    protected Piece(Parcel in) {
        field = in.readString();
        fieldPhoto = Bitmap.CREATOR.createFromParcel(in);
        recognized = in.readString();
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

    public String getRecognized() {
        return recognized;
    }

    public Float getConfidence() {
        return confidence;
    }

    private String field;
    private Bitmap fieldPhoto;
    private String recognized;
    private Float confidence;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(field);
        fieldPhoto.writeToParcel(parcel,0);
        parcel.writeString(recognized);
        parcel.writeFloat(confidence);
    }
}
