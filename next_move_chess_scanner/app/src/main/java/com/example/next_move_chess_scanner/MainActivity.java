package com.example.next_move_chess_scanner;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.next_move_chess_scanner.ml.BlackModel;

import org.opencv.android.OpenCVLoader;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MoveListAdapter.AdapterCallback {

    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;

    private RecyclerView recyclerView;
    private MoveListAdapter moveListAdapter;
    private List<Move> moveList = new ArrayList<>();
    private List<Piece> pieceList = new ArrayList<>();
    private ChessDbApi chessDbApi;
    private PieceClassifier pieceClassifier;
    //private String currentPosition = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w";
    private String currentPosition = "r1bqkb1r/p1pp1ppp/2p2n2/4P3/8/8/PPP2PPP/RNBQKB1R b KQkq - 0 6";
    ActivityResultLauncher <String> mGetContent;
    ChessView chessView;
    SharedPreferences sharedPreferences;
    public Button helpButton;
    public Button settingsButton;
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
        Toolbar toolbar = findViewById(R.id.menu_toolbar);
        setSupportActionBar(toolbar);

        sharedPreferences = getSharedPreferences("prefs", MODE_PRIVATE);

        // false - UCI Notation, true - SAN Notation
        boolean sanNotation = sharedPreferences.getBoolean("sanNotation", true);

        // false - english, true - polish
        String language = sharedPreferences.getString("language", getResources().getConfiguration().locale.getLanguage());

        // false - Don't ask everytime about color use defaultColor , true - ask everytime about color
        boolean askColor = sharedPreferences.getBoolean("askColor", true);

        // if above option is false, set default color, false - white, true - black
        boolean defaultColor = sharedPreferences.getBoolean("color", false);

        int maxResults = sharedPreferences.getInt("maxResults",50);

        chessDbApi = new ChessDbApi(this, maxResults);
        helpButton = findViewById(R.id.help);
        settingsButton = findViewById(R.id.settings);

        reverseButton = findViewById(R.id.reverse);
        scanButton = findViewById(R.id.scan);
        textFenButton = findViewById(R.id.text_fen);
        getMovesButton = findViewById(R.id.getMoves);
        scanInfo = findViewById(R.id.scanInfo);
        pieceClassifier = new PieceClassifier(this);

        //moveList.add(new Move(" ", " ",0,0," "," ", false));
        new RequestDbApiTask().execute(currentPosition);
        recyclerView = findViewById(R.id.recyclerView);

        moveListAdapter = new MoveListAdapter(this, moveList, sanNotation);
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

        TextView title = new TextView(this);
        // You Can Customise your Title here
        title.setText(getResources().getString(R.string.choosePhotoSource));
        title.setPadding(10, 10, 10, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.BLACK);
        title.setTextSize(20);

        AlertDialog.Builder photoDialogBuilder = new AlertDialog.Builder(this)
                .setCancelable(true)
                //.setTitle(getResources().getString(R.string.choosePhotoSource))
                .setCustomTitle(title)
                .setPositiveButton(getResources().getString(R.string.camera), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String fileName = "temp.jpg";
                        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                        {
                            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                        }

                        ContentValues values = new ContentValues();
                        values.put(MediaStore.Images.Media.TITLE, fileName);
                        Uri mCapturedImageURI = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
                        startActivityForResult(cameraIntent, CAMERA_REQUEST);


                        //mGetContent.launch("image/*");
                        dialog.cancel();
                        return;
                    }
                })
                .setNegativeButton(getResources().getString(R.string.gallery), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mGetContent.launch("image/*");
                        dialog.cancel();
                        return;
                    }
                })
                .setNeutralButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        return;
                    }
                });
        AlertDialog photoDialog = photoDialogBuilder.create();


        AlertDialog.Builder helpDialogBuilder = new AlertDialog.Builder(this)
                .setCancelable(true)
                .setTitle(getResources().getString(R.string.help))
                .setMessage(getResources().getString(R.string.helpMessage))
                .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.cancel();
                        return;
                    }
                });
        AlertDialog helpDialog = helpDialogBuilder.create();



        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text


        // Set up the buttons

        helpButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                helpDialog.show();
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                Intent intent = new Intent(MainActivity.this, OptionsActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);;
            }
        });

        textFenButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FENdialog.show();
            }
        });


        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               // mGetContent.launch("image/*");
               photoDialog.show();
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
                Bitmap imageOfChessboard = image.copy(image.getConfig(), true);
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

    public String createFEN(List<String> pieces, boolean isWhite) {
        StringBuilder fen = new StringBuilder();

        // If the order of pieces is from position H8 to A1, reverse the order of elements in the pieces list
        if (!isWhite) {
            Collections.reverse(pieces);
        }

        // Iterate through the rows of the board from top to bottom
        for (int i = 7; i >= 0; i--) {
            int emptySquares = 0;
            // Iterate through the columns of the board from left to right
            for (int j = 0; j < 8; j++) {
                // Get the name of the piece at the given position
                String piece = pieces.get(i * 8 + j);
                if (piece.equals("")) {
                    // If there is no piece at the given position, increase the number of empty squares
                    emptySquares++;
                } else {
                    // If there is a piece at the given position, add the appropriate character to the FEN string
                    if (emptySquares > 0) {
                        fen.append(emptySquares);
                        emptySquares = 0;
                    }
                    fen.append(piece);
                }
            }
            // If there are still empty squares at the end of the row, add them to the FEN string
            if (emptySquares > 0) {
                fen.append(emptySquares);
            }
            // If this is not the last row, add a '/' character to the FEN string
            if (i > 0) {
                fen.append('/');
            }
        }

        return fen.toString();
    }


}
