package com.example.next_move_chess_scanner;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.next_move_chess_scanner.ml.BlackModel;

import org.opencv.android.OpenCVLoader;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MoveListAdapter.AdapterCallback {

    private RecyclerView recyclerView;
    private MoveListAdapter moveListAdapter;
    private List<Move> moveList = new ArrayList<>();
    private List<Piece> pieceList = new ArrayList<>();
    private Bitmap imageOfChessboard;
    private ChessDbApi chessDbApi = new ChessDbApi(this);
    private PieceClassifier pieceClassifier;
    //private String currentPosition = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w";
    private String currentPosition = "r1bqkb1r/p1pp1ppp/2p2n2/4P3/8/8/PPP2PPP/RNBQKB1R b KQkq - 0 6";
    ActivityResultLauncher <String> mGetContent;
    ChessView chessView;
    public Button reverseButton;
    public Button scanButton;
    public Button textFenButton;
    public Button getMovesButton;
    public Button scanInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("OpenCV", "OpenCv Loading status " + OpenCVLoader.initDebug());

        reverseButton = findViewById(R.id.reverse);
        scanButton = findViewById(R.id.scan);
        textFenButton = findViewById(R.id.text_fen);
        getMovesButton = findViewById(R.id.getMoves);
        scanInfo = findViewById(R.id.scanInfo);
        pieceClassifier = new PieceClassifier(this);

        //moveList.add(new Move(" ", " ",0,0," "," ", false));
        new RequestDbApiTask().execute(currentPosition);
        recyclerView = findViewById(R.id.recyclerView);

        moveListAdapter = new MoveListAdapter(this, moveList);
        recyclerView.setAdapter(moveListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Log.d("XD", "OpenCv Loading status " + moveList.indexOf(0));

        chessView = findViewById(R.id.chess_view);
        chessView.setFen(currentPosition);
        new ChangeChessViewTask().execute();

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        AlertDialog.Builder FENDialogBuilder = new AlertDialog.Builder(this)
                .setView(input)
                .setCancelable(false)
                .setTitle(getResources().getString(R.string.enterFenNotation))
                .setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (input.getText().toString().length()>0){
                            currentPosition = input.getText().toString();
                            new RequestDbApiTask().execute(currentPosition);
                            chessView.setFen(currentPosition);
                            new ChangeChessViewTask().execute();
                        }
                        else{
                            Toast.makeText(MainActivity.this, getResources().getString(R.string.empty), Toast.LENGTH_SHORT).show();
                        }
                        dialog.cancel();
                        return;
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this,getResources().getString(R.string.canceled), Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                        return;
                    }
                });
        AlertDialog FENdialog = FENDialogBuilder.create();


        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text


        // Set up the buttons

        textFenButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FENdialog.show();
            }
        });


        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mGetContent.launch("image/*");
            }
        });
        reverseButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                chessView.changeSides();
                new ChangeChessViewTask().execute();
            }
        });

        getMovesButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                moveListAdapter.setMoveList(moveList);
                recyclerView.setAdapter(moveListAdapter);
                chessView.setPointer(moveList.get(0).getUCIMove());
                new ChangeChessViewTask().execute();
                moveListAdapter.notifyDataSetChanged();
            }
        });

        scanInfo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("PIECES_LIST", (ArrayList<? extends Parcelable>) pieceList);
                Intent intent = new Intent(MainActivity.this, PiecesActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        Intent intent = new Intent(MainActivity.this, CropperActivity.class);
                        intent.putExtra("DATA", result.toString());
                        startActivityForResult(intent, 101);
                    }
                });
    }

    @Override
    public void onMethodCallback(String yourValue) {
        chessView.setPointer(yourValue);
        new ChangeChessViewTask().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == -1 && requestCode==101)
        {
            String result = data.getStringExtra("RESULT");
            Uri resultUri = null;
            if(result!=null)
            {
                resultUri = Uri.parse(result);
            }
            try {
                Bitmap image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                imageOfChessboard = image.copy(image.getConfig(), true);
                imageOfChessboard = changeResolution(imageOfChessboard);
                pieceList = createPieceList(imageOfChessboard, true);
                scanInfo.setEnabled(true);

            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d("IMAGE", "IMAGE LOADED");
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.scan)
            Log.d("XD", "OpenCv Loading status " + moveList.indexOf(0));
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private class RequestDbApiTask extends AsyncTask<String, Void, Void>{

        protected void onPreExecute (){
            getMovesButton.setEnabled(false);
        }
        @Override
        protected Void doInBackground(String... fenNotation) {
            chessDbApi.sendRequest(fenNotation[0]);
            return null;
        }
        protected void onPostExecute(Void result) {
            moveList = chessDbApi.getMovesList();
            getMovesButton.setEnabled(true);
        }

    }
    private class ChangeChessViewTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            chessView.refreshChessView();
            return null;
        }


    }
    public Bitmap changeResolution(Bitmap image){
        Log.d("IMAGE", "Height: " + image.getHeight() + "  Width: " + image.getWidth());
        Bitmap scaled = Bitmap.createScaledBitmap(image,256,256, true);
        return scaled;
    }
    public List<Bitmap> divideChessboard(Bitmap chessboard){
        int imageSize = 32;
        List<Bitmap> dividedBitmap = new ArrayList<>();
        for(int row = 0; row<8;row++){
            for(int column = 0; column<8;column++){
                dividedBitmap.add(Bitmap.createBitmap(chessboard,imageSize*row,imageSize*column,imageSize,imageSize));
            }
        }
        return dividedBitmap;
    }
    public List<Piece> createPieceList(Bitmap chessboard, boolean isWhite){
        List<Bitmap> imageList = divideChessboard(chessboard);
        String alphabet = "ABCDEFGH";
        String numbers = "87654321";
        if(!isWhite)
        {
            numbers = "12345678";
        }
        String field;
        List<Piece> tempList = new ArrayList<>();
        int imageNumber = 0;
        float confidence = 1.00f;
        boolean isWhiteField = false;
        for(char number: numbers.toCharArray()){
            isWhiteField = !isWhiteField;
            for(char letter: alphabet.toCharArray()){
                field = letter + Character.toString(number);
                Pair<String, Float> pieceWithProbabilities = pieceClassifier.recognizePiece(imageList.get(imageNumber), isWhiteField);
                tempList.add(new Piece(field, imageList.get(imageNumber), pieceWithProbabilities.first, pieceWithProbabilities.second));
                isWhiteField = !isWhiteField;
                imageNumber++;
            }
        }
        return tempList;
    }

}
