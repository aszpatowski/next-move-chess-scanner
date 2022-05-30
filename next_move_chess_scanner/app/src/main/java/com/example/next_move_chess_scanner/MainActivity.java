package com.example.next_move_chess_scanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import org.opencv.android.OpenCVLoader;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MoveListAdapter moveListAdapter;
    private ChessDbApi chessDbApi = new ChessDbApi(this);
    private List<Move> moveList = new ArrayList<>();// = chessDbApi.sendRequest("r1bqkbnr/ppppp1pp/2n5/5p2/5P2/5N2/PPPPP1PP/RNBQKB1R w KQkq - 0 1");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("OpenCV", "OpenCv Loading status " + OpenCVLoader.initDebug());
        moveList.add(new Move(" ", " ",0,0," "," ", false));
        moveList = chessDbApi.sendRequest("r1bqkbnr/ppppp1pp/2n5/5p2/5P2/5N2/PPPPP1PP/RNBQKB1R w KQkq - 0 1");
        recyclerView = findViewById(R.id.recyclerView);
        moveListAdapter = new MoveListAdapter(this, moveList);
        recyclerView.setAdapter(moveListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //Log.d("XD", "OpenCv Loading status " + moveList.indexOf(0));
        Log.d("XD", "OpenCv Loading status " + moveList.indexOf(0));
        final Button button = findViewById(R.id.scan);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                moveList = chessDbApi.getMovesList();
                moveListAdapter.notifyDataSetChanged();
            }
        });
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