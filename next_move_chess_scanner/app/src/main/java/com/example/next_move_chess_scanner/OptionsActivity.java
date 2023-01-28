package com.example.next_move_chess_scanner;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;

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

    public Button chooseLanguageButton;
    public Switch chooseNotationSwitch;
    public Switch askWhatColorSwitch;
    public Switch defaultColorSwitch;
    public SeekBar limitMovesSeek;

    public Button saveButton;
    public Button cancelButton;

    public boolean sanNotation;

    public String language;

    public boolean askColor;

    public boolean defaultColor;

    public int maxResults;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        setContentView(R.layout.activity_options);

        sanNotation = bundle.getBoolean("sanNotation", true);

        // false - english, true - polish
        language = bundle.getString("language", getResources().getConfiguration().locale.getLanguage());

        // false - Don't ask everytime about color use defaultColor , true - ask everytime about color
        askColor = bundle.getBoolean("askColor", true);

        // if above option is false, set default color, false - white, true - black
        defaultColor = bundle.getBoolean("color", false);

        maxResults = bundle.getInt("maxResults",50);

        chooseLanguageButton = findViewById(R.id.chooseLanguage);
        chooseNotationSwitch = findViewById(R.id.chooseNotation);
        askWhatColorSwitch = findViewById(R.id.askWhatColor);
        defaultColorSwitch = findViewById(R.id.defaultColor);
        limitMovesSeek = findViewById(R.id.limitMovesSeek);

        saveButton = findViewById(R.id.saveOptions);
        cancelButton = findViewById(R.id.cancelOptions);

        // Set earlier defined values on controls

        chooseNotationSwitch.setChecked(sanNotation);
        askWhatColorSwitch.setChecked(askColor);
        defaultColorSwitch.setChecked(defaultColor);
        limitMovesSeek.setProgress(maxResults);

        // Set up the controls

        chooseLanguageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ;
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                setResult(OptionsActivity.RESULT_OK, intent);
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                setResult(OptionsActivity.RESULT_OK, intent);
            }
        });

    }
}
