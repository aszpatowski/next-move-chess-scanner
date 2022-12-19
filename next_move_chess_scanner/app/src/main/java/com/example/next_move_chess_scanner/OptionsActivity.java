package com.example.next_move_chess_scanner;


import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

/*
What options:
- UCI - SAN NOTATION
- Language
- what color u are (disabled if true below)
- ask everytime what color i am
- turn on/turn off automatic detect chessboard
-
 */
public class OptionsActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

    }
}
