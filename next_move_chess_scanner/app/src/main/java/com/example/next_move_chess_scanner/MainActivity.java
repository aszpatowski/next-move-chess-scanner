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

public class MainActivity extends AppCompatActivity implements MoveListAdapter.AdapterCallback {

    private RecyclerView recyclerView;
    private MoveListAdapter moveListAdapter;
    private ChessDbApi chessDbApi = new ChessDbApi(this);
    private List<Move> moveList = new ArrayList<>();// = chessDbApi.sendRequest("r1bqkbnr/ppppp1pp/2n5/5p2/5P2/5N2/PPPPP1PP/RNBQKB1R w KQkq - 0 1");
    private Uri imageOfChessboard;
    //private String currentPosition = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w";
    private String currentPosition = "r1bqkb1r/p1pp1ppp/2p2n2/4P3/8/8/PPP2PPP/RNBQKB1R b KQkq - 0 6";
    ActivityResultLauncher <String> mGetContent;
    ChessView chessView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("OpenCV", "OpenCv Loading status " + OpenCVLoader.initDebug());

        final Button switchSides = findViewById(R.id.reverse);
        final Button buttonScan = findViewById(R.id.scan);
        final Button settings = findViewById(R.id.settings);

        moveList.add(new Move(" ", " ",0,0," "," ", false));
        moveList = chessDbApi.sendRequest(currentPosition);
        recyclerView = findViewById(R.id.recyclerView);

        moveListAdapter = new MoveListAdapter(this, moveList);
        recyclerView.setAdapter(moveListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Log.d("XD", "OpenCv Loading status " + moveList.indexOf(0));

        chessView = findViewById(R.id.chess_view);
        chessView.setFen(currentPosition);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final EditText input = new EditText(this);

        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons

        buttonScan.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mGetContent.launch("image/*");
            }
        });
        switchSides.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                chessView.changeSides();
            }
        });
        settings.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        currentPosition = input.getText().toString();
                        Log.d("XD", "OpenCv Loading status " + currentPosition);
                        chessDbApi.sendRequest(currentPosition);
                        chessView.setFen(currentPosition);
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

        final Button buttonGetMoves = findViewById(R.id.getMoves);
        buttonGetMoves.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                moveList = chessDbApi.getMovesList();
                moveListAdapter.setMoveList(moveList);
                recyclerView.setAdapter(moveListAdapter);
                moveListAdapter.notifyDataSetChanged();
                chessView.setPointer(moveList.get(0).getUCImove());
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
        moveListAdapter.notifyDataSetChanged();
    }


}
