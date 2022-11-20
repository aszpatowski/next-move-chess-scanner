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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.opencv.android.OpenCVLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class MainActivity extends AppCompatActivity implements MoveListAdapter.AdapterCallback {

    private RecyclerView recyclerView;
    private MoveListAdapter moveListAdapter;
    private List<Move> moveList = new ArrayList<>();// = chessDbApi.sendRequest("r1bqkbnr/ppppp1pp/2n5/5p2/5P2/5N2/PPPPP1PP/RNBQKB1R w KQkq - 0 1");
    private Uri imageOfChessboard;
    private ChessDbApi chessDbApi = new ChessDbApi(this);
    //private String currentPosition = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w";
    private String currentPosition = "r1bqkb1r/p1pp1ppp/2p2n2/4P3/8/8/PPP2PPP/RNBQKB1R b KQkq - 0 6";
    ActivityResultLauncher <String> mGetContent;
    ChessView chessView;
    public Button switchSidesButton;
    public Button scanButton;
    public Button settingsButton;
    public Button getMovesButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("OpenCV", "OpenCv Loading status " + OpenCVLoader.initDebug());

        switchSidesButton = findViewById(R.id.reverse);
        scanButton = findViewById(R.id.scan);
        settingsButton = findViewById(R.id.settings);
        getMovesButton = findViewById(R.id.getMoves);

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

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final EditText input = new EditText(this);

        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons

        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mGetContent.launch("image/*");
            }
        });
        switchSidesButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                chessView.changeSides();
                new ChangeChessViewTask().execute();
            }
        });
        settingsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        currentPosition = input.getText().toString();
                        Log.d("XD", "OpenCv Loading status " + currentPosition);
                        new RequestDbApiTask().execute(currentPosition);
                        chessView.setFen(currentPosition);
                        new ChangeChessViewTask().execute();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });

        getMovesButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                moveListAdapter.setMoveList(moveList);
                recyclerView.setAdapter(moveListAdapter);
                chessView.setPointer(moveList.get(0).getUCImove());
                new ChangeChessViewTask().execute();
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
            imageOfChessboard = resultUri;
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
}
