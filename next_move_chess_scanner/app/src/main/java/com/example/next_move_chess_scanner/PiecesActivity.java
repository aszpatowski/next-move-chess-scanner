package com.example.next_move_chess_scanner;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.opencv.android.OpenCVLoader;

import java.util.ArrayList;
import java.util.List;

public class PiecesActivity extends AppCompatActivity implements PieceListAdapter.AdapterCallback {
    private RecyclerView recyclerView;
    private PieceListAdapter pieceListAdapter;
    private List<Piece> pieceList = new ArrayList<>();
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pieces);


        pieceList = getIntent().getParcelableArrayListExtra("PIECES_LIST");

        recyclerView = findViewById(R.id.recyclerView);
        pieceListAdapter = new PieceListAdapter(this, pieceList);
        recyclerView.setAdapter(pieceListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onMethodCallback(String yourValue) {

    }
}
