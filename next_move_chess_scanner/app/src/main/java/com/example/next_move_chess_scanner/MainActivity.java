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

import org.opencv.android.OpenCVLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity implements MoveListAdapter.AdapterCallback {

    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;

    private RecyclerView recyclerView;
    private MoveListAdapter moveListAdapter;
    private List<Move> moveList = new ArrayList<>();
    private List<Piece> pieceList = new ArrayList<>();
    private ChessDbApi chessDbApi;
    private PieceClassifier pieceClassifier;
    private String currentPosition = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w";
    //private String currentPosition = "r1bqkb1r/p1pp1ppp/2p2n2/4P3/8/8/PPP2PPP/RNBQKB1R b KQkq - 0 6";
    ActivityResultLauncher <String> mGetContent;
    ChessView chessView;
    SharedPreferences sharedPreferences;
    Bitmap imageOfChessboard;
    public boolean playerColor;
    
    public Button helpButton;
    public Button settingsButton;
    public Button reverseButton;
    public Button scanButton;
    public Button textFenButton;
    public Button getMovesButton;
    public Button scanInfoButton;

    public boolean sanNotation;

    public String language;

    public boolean askColor;

    public boolean defaultColor;

    public int maxResults;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("OpenCV", "OpenCv Loading status " + OpenCVLoader.initDebug());
        Toolbar toolbar = findViewById(R.id.menu_toolbar);
        setSupportActionBar(toolbar);
        Piece tempPiece = new Piece(this);

        sharedPreferences = getSharedPreferences("prefs", MODE_PRIVATE);

        // false - UCI Notation, true - SAN Notation
        sanNotation = sharedPreferences.getBoolean("sanNotation", true);

        // false - english, true - polish
        language = sharedPreferences.getString("language", getResources().getConfiguration().locale.getLanguage());

        // false - Don't ask everytime about color use defaultColor , true - ask everytime about color
        askColor = sharedPreferences.getBoolean("askColor", true);

        // if below option is false, set default color, false - white, true - black
        defaultColor = sharedPreferences.getBoolean("color", false);


        maxResults = sharedPreferences.getInt("maxResults",50);
        // define ChessDbApi object and amount of results that will be returned to
        chessDbApi = new ChessDbApi(this, maxResults);

        // define buttons in toolbar
        helpButton = findViewById(R.id.help);
        settingsButton = findViewById(R.id.settings);

        // define views
        chessView = findViewById(R.id.chess_view);
        recyclerView = findViewById(R.id.recyclerView);

        // define other layout buttons
        reverseButton = findViewById(R.id.reverse);
        scanButton = findViewById(R.id.scan);
        textFenButton = findViewById(R.id.text_fen);
        getMovesButton = findViewById(R.id.getMoves);




        scanInfoButton = findViewById(R.id.scanInfo);
        pieceClassifier = new PieceClassifier(this);

        new RequestDbApiTask().execute(currentPosition);

        moveListAdapter = new MoveListAdapter(this, moveList, sanNotation);
        recyclerView.setAdapter(moveListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Log.d("XD", "OpenCv Loading status " + moveList.indexOf(0));

        chessView.setFen(currentPosition);
        new ChangeChessViewTask().execute();

        // Set dialogs what appear when click: textFenButton, scanButton, helpButton

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

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(currentPosition);
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

        TextView photoDialogTitle = new TextView(this);
        photoDialogTitle.setText(getResources().getString(R.string.choosePhotoSource));
        photoDialogTitle.setPadding(10, 10, 10, 10);
        photoDialogTitle.setGravity(Gravity.CENTER);
        photoDialogTitle.setTextColor(Color.BLACK);
        photoDialogTitle.setTextSize(20);

        AlertDialog.Builder photoDialogBuilder = new AlertDialog.Builder(this)
                .setCancelable(true)
                .setCustomTitle(photoDialogTitle)
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
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.canceled), Toast.LENGTH_SHORT).show();
                        return;
                    }
                });
        AlertDialog photoDialog = photoDialogBuilder.create();

        TextView whatColorDialogTitle = new TextView(this);
        whatColorDialogTitle.setText(getResources().getString(R.string.whatColor));
        whatColorDialogTitle.setPadding(10, 10, 10, 10);
        whatColorDialogTitle.setGravity(Gravity.CENTER);
        whatColorDialogTitle.setTextColor(Color.BLACK);
        whatColorDialogTitle.setTextSize(20);

        AlertDialog.Builder whatColorDialogBuilder = new AlertDialog.Builder(this)
                .setCancelable(true)
                .setCustomTitle(whatColorDialogTitle)
                .setPositiveButton(getResources().getString(R.string.white), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        playerColor = false;
                        chessView.setSide(playerColor);
                        setChessboard();
                        dialog.cancel();
                        return;
                    }
                })
                .setNegativeButton(getResources().getString(R.string.black), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        playerColor = true;
                        chessView.setSide(playerColor);
                        setChessboard();
                        dialog.cancel();
                        return;
                    }
                })
                .setNeutralButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        playerColor = defaultColor;
                        chessView.setSide(playerColor);
                        setChessboard();
                        dialog.cancel();
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.setDefaultColor), Toast.LENGTH_SHORT).show();
                        return;
                    }
                });
        AlertDialog whatColorDialog = whatColorDialogBuilder.create();




        // Set up the buttons

        helpButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                helpDialog.show();
                
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putBoolean("sanNotation", sanNotation);
                bundle.putString("language", language);
                bundle.putBoolean("askColor", askColor);
                bundle.putBoolean("defaultColor", defaultColor);
                bundle.putInt("maxResults", maxResults);
                Intent intent = new Intent(MainActivity.this, OptionsActivity.class);
                intent.putExtras(bundle);
                startActivityForResult(intent, 1);
            }
        });

        textFenButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FENdialog.show();
            }
        });


        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(askColor){
                    whatColorDialog.show();
                }
                else {
                    playerColor = defaultColor;
                    chessView.setSide(playerColor);
                    setChessboard();
                }
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

        scanInfoButton.setOnClickListener(new View.OnClickListener() {
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
                Bitmap tempImageOfChessboard = image.copy(image.getConfig(), true);
                imageOfChessboard = changeResolution(tempImageOfChessboard);

            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d("IMAGE", "IMAGE LOADED");
        }
        if (requestCode == 1) {
            if(resultCode == OptionsActivity.RESULT_OK && data != null){
                // false - UCI Notation, true - SAN Notation
                sanNotation = data.getBooleanExtra("sanNotation", sanNotation);

                // false - english, true - polish
                language = data.getStringExtra("language");

                // false - Don't ask everytime about color use defaultColor , true - ask everytime about color
                askColor = data.getBooleanExtra("askColor", askColor);

                // if below option is false, set default color, false - white, true - black
                defaultColor = data.getBooleanExtra("defaultColor", defaultColor);


                maxResults = data.getIntExtra("maxResults", maxResults);

                sharedPreferences.edit().putBoolean("sanNotation", sanNotation).commit();
                sharedPreferences.edit().putString("language", language).commit();
                sharedPreferences.edit().putBoolean("askColor", askColor).commit();
                sharedPreferences.edit().putBoolean("defaultColor", defaultColor).commit();
                sharedPreferences.edit().putInt("maxResults", maxResults).commit();
            }

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
        // Divide the chessboard into individual images
        List<Bitmap> imageList = divideChessboard(chessboard);
        // Create strings for the letters and numbers of the board
        String alphabet = "HGFEDCBA";
        String numbers = "12345678";
        // If it is not white, switch the order of the numbers string
        if(!isWhite) {
            numbers = "87654321";
            alphabet = "ABCDEFGH";
        }
        // Create a string to store the field name and an empty list to store pieces in 
        String field; 
        List<Piece> tempList = new ArrayList<>(); 
    
        // Keep track of which image number we are on 
        int imageNumber = 0; 
    
        // Boolean to keep track of whether or not it is a white field 
        boolean isWhiteField = false;
    
        // Loop through each character in the numbers string 
        for(char letter: alphabet.toCharArray()){
    
            // Switch between white and black fields for each row 
            isWhiteField = !isWhiteField;
    
            // Loop through each character in the alphabet string  
            for(char number: numbers.toCharArray()){
    
                // Combine letter and number characters to create a field name  
                field = letter + Character.toString(number);
    
                // Recognize piece with pieceClassifier, get back pair with first being piece name and second being probability  
                Pair<String, Float> pieceWithProbabilities = pieceClassifier.recognizePiece(imageList.get(imageNumber), isWhiteField);
    
                // Create new Piece object with field name, image, piece FENname, and probability
                tempList.add(new Piece(field, imageList.get(imageNumber), pieceWithProbabilities.first ,pieceWithProbabilities.second));
    
                // Switch between white and black fields   
                isWhiteField = !isWhiteField;
    
                // Increment image number   
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
            for (int j = 7; j >= 0; j--) {
                // Get the name of the piece at the given position
                String piece = pieces.get(j * 8 + i);
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
        fen.append(" ");
        if (isWhite)
        {
            fen.append("w");
        }
        else {
            fen.append("b");
        }
        return fen.toString();
    }
    public void setChessboard(){
        pieceList = createPieceList(imageOfChessboard, playerColor);
        scanInfoButton.setEnabled(true);
        for (Piece piece : pieceList) {
            Log.d("Piece ", piece.getRecognizedName());
        }
        List<String> pieceFENList = pieceList.stream()
                .map(piece -> piece.getRecognizedFEN())
                .collect(Collectors.toList());

        currentPosition = createFEN(pieceFENList, playerColor);
        Toast.makeText(MainActivity.this, getResources().getString(R.string.setFen) + currentPosition, Toast.LENGTH_LONG).show();
        Log.d("setChessboard", "currentPosition is " + currentPosition);
        new RequestDbApiTask().execute(currentPosition);
        chessView.setFen(currentPosition);

        new ChangeChessViewTask().execute();
    }

}
