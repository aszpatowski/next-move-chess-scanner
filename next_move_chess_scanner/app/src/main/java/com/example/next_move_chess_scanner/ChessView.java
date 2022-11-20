package com.example.next_move_chess_scanner;

import static java.lang.Integer.min;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.graphics.Canvas;
import android.graphics.Paint;

import androidx.core.content.ContextCompat;

import java.util.HashMap;
import java.util.Map;

class ChessView extends View {
    Map<String, Integer> pieces = new HashMap<String, Integer>();
    Map<String, Bitmap> bitmaps = new HashMap<String, Bitmap>();
    Map<Character, String> fenToNames = new HashMap<Character, String>();
    Map<Character, Integer> UCItoNumbers = new HashMap<Character, Integer>();
    int lightColor;
    int darkColor;
    int markedColor;
    int pointerColor;
    Paint paint = new Paint();
    String currentFen;
    String pointer = "";
    boolean isPointer = false;
    boolean white = true;

    private float scaleFactor = 1.0f;
    private float originX = 20f;
    private float originY = 20f;
    private float cellSide = 130f;

    public ChessView(Context context, AttributeSet attrs) {
        super(context, attrs);
        pieces.put("bishop_black", R.drawable.ic_bb);
        pieces.put("king_black", R.drawable.ic_bk);
        pieces.put("knight_black", R.drawable.ic_bn);
        pieces.put("pawn_black", R.drawable.ic_bp);
        pieces.put("queen_black", R.drawable.ic_bq);
        pieces.put("rook_black", R.drawable.ic_br);
        pieces.put("bishop_white", R.drawable.ic_wb);
        pieces.put("king_white", R.drawable.ic_wk);
        pieces.put("knight_white", R.drawable.ic_wn);
        pieces.put("pawn_white", R.drawable.ic_wp);
        pieces.put("queen_white", R.drawable.ic_wq);
        pieces.put("rook_white", R.drawable.ic_wr);
        lightColor = ContextCompat.getColor(context, R.color.modern_ivory);
        darkColor = ContextCompat.getColor(context, R.color.juniper_berries);
        markedColor = ContextCompat.getColor(context, R.color.marked);
        pointerColor = ContextCompat.getColor(context, R.color.black);
        loadBitmaps();
        fenToNames.put('k', "king_black");
        fenToNames.put('q', "queen_black");
        fenToNames.put('n', "knight_black");
        fenToNames.put('b', "bishop_black");
        fenToNames.put('r', "rook_black");
        fenToNames.put('p', "pawn_black");
        fenToNames.put('K', "king_white");
        fenToNames.put('Q', "queen_white");
        fenToNames.put('N', "knight_white");
        fenToNames.put('B', "bishop_white");
        fenToNames.put('R', "rook_white");
        fenToNames.put('P', "pawn_white");
        UCItoNumbers.put('a', 0);
        UCItoNumbers.put('b', 1);
        UCItoNumbers.put('c', 2);
        UCItoNumbers.put('d', 3);
        UCItoNumbers.put('e', 4);
        UCItoNumbers.put('f', 5);
        UCItoNumbers.put('g', 6);
        UCItoNumbers.put('h', 7);
        UCItoNumbers.put('8', 0);
        UCItoNumbers.put('7', 1);
        UCItoNumbers.put('6', 2);
        UCItoNumbers.put('5', 3);
        UCItoNumbers.put('4', 4);
        UCItoNumbers.put('3', 5);
        UCItoNumbers.put('2', 6);
        UCItoNumbers.put('1', 7);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float chessBoardSide = min(getWidth(), getHeight()) * scaleFactor;
        cellSide = chessBoardSide / 8f;
        originX = (getWidth() - chessBoardSide) / 2f;
        originY = (getHeight() - chessBoardSide) / 2f;
        //canvas.drawColor(Color.WHITE, PorterDuff.Mode.CLEAR);
        drawChessboard(canvas);
        if (isPointer) {
            highlightSquareAt(canvas, UCItoNumbers.get(pointer.charAt(0)), UCItoNumbers.get(pointer.charAt(1)), markedColor);
            highlightSquareAt(canvas, UCItoNumbers.get(pointer.charAt(2)), UCItoNumbers.get(pointer.charAt(3)), markedColor);
        }
        drawFromFen(canvas);

    }

    protected void drawPieceAt(Canvas canvas, int row, int col, String namePiece) {
        canvas.drawBitmap(bitmaps.get(namePiece), null, new RectF(originX + col * cellSide, originY + row * cellSide, originX + (col + 1) * cellSide, originY + (row + 1) * cellSide), paint);
    }

    protected void drawChessboard(Canvas canvas) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                drawSquareAt(canvas, col, row, (col + row) % 2 == 1);
            }
        }
    }

    protected void drawSquareAt(Canvas canvas, int col, int row, boolean isDark) {
        paint.setColor(isDark ? darkColor : lightColor);
        canvas.drawRect(originX + col * cellSide, originY + row * cellSide, originX + (col + 1) * cellSide, originY + (row + 1) * cellSide, paint);
    }

    protected void highlightSquareAt(Canvas canvas, int col, int row, int color) {
        paint.setColor(color);
        if(white)
            canvas.drawRect(originX + col * cellSide, originY + row * cellSide, originX + (col + 1) * cellSide, originY + (row + 1) * cellSide, paint);
        else
            canvas.drawRect(originX + col * cellSide, originY + (7-row) * cellSide, originX + (col + 1) * cellSide, originY + (7-row + 1) * cellSide, paint);
    }

    private void loadBitmaps() {
        for (Map.Entry<String, Integer> entry : pieces.entrySet()) {
            bitmaps.put(entry.getKey(), getBitmap(entry.getValue()));
        }
    }

    private Bitmap getBitmap(int drawableRes) {
        Drawable drawable = getResources().getDrawable(drawableRes);
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public void setFen(String fenNotation) {
        currentFen = fenNotation;
        isPointer = false;
    }


    public void setPointer(String pointerLoc) {
        pointer = pointerLoc;
        isPointer = true;
    }
    public void changeSides(){
        if (white==true)
            white= false;
        else
            white= true;
    }
    public void refreshChessView(){
        invalidate();
    }

    private void drawFromFen(Canvas canvas) {

        int i = 0;
        char space = ' ';
        char slash = '/';
        int row = 0;
        int column = 0;
        int addColumns = 1;
        while (currentFen.charAt(i) != space) {
            Log.d("ChessView", " char: " + currentFen.charAt(i));
            if (Character.isDigit(currentFen.charAt(i))) {
                addColumns = Character.getNumericValue(currentFen.charAt(i));
            }
            else if (currentFen.charAt(i) == slash) {
                column = 0;
                row++;
                i += 1;
                continue;
            }
            else {
                if(white)
                    drawPieceAt(canvas, row, column, fenToNames.get(currentFen.charAt(i)));
                else
                    drawPieceAt(canvas, 7-row, column, fenToNames.get(currentFen.charAt(i)));
            }
            i += 1;
            column += addColumns;
            addColumns = 1;

        }

    }

    private void drawPointer(Canvas canvas) {
        paint.setColor(markedColor);
        paint.setStrokeWidth(10f);
        Log.d("ChessView- pointer  ", pointer + " " + (UCItoNumbers.get(pointer.charAt(0))) + " " + (UCItoNumbers.get(pointer.charAt(1))) + " " + (UCItoNumbers.get(pointer.charAt(2))) + " " + (UCItoNumbers.get(pointer.charAt(3))));
        canvas.drawLine(originX + cellSide / 2 + UCItoNumbers.get(pointer.charAt(0)) * cellSide, originY + cellSide / 2 + UCItoNumbers.get(pointer.charAt(1)) * cellSide, originX + cellSide / 2 + UCItoNumbers.get(pointer.charAt(2)) * cellSide, originY + cellSide / 2 + UCItoNumbers.get(pointer.charAt(3)) * cellSide, paint);
    }
}