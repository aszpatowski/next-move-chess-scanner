package com.example.next_move_chess_scanner;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

public class OptionsActivity extends AppCompatActivity {
    /*
    This class is an Activity that allows users to customize their settings. 
    It contains several UI elements such as a Button, Switches and a SeekBar. 
    The user can choose the language, notation, color and limit the number of moves. 
    The class also contains two Buttons for saving or cancelling the changes.
    */
    public Button chooseLanguageButton;
    public Switch chooseNotationSwitch;
    public Switch askWhatColorSwitch;
    public Switch defaultColorSwitch;
    public TextView limitMovesText;
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
        limitMovesText = findViewById(R.id.limitMovesText);
        limitMovesSeek = findViewById(R.id.limitMovesSeek);

        saveButton = findViewById(R.id.saveOptions);
        cancelButton = findViewById(R.id.cancelOptions);

        // Set earlier defined values on controls

        chooseNotationSwitch.setChecked(sanNotation);
        askWhatColorSwitch.setChecked(askColor);
        defaultColorSwitch.setChecked(defaultColor);
        limitMovesText.setText(Integer.toString(maxResults));
        limitMovesSeek.setProgress(maxResults);

        // Set up the controls

        chooseLanguageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ;
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent data = new Intent();
                sanNotation = chooseNotationSwitch.isChecked();
                askColor = askWhatColorSwitch.isChecked();
                defaultColor = defaultColorSwitch.isChecked();
                maxResults = limitMovesSeek.getProgress();
                data.putExtra("sanNotation",sanNotation);
                data.putExtra("language",askColor);
                data.putExtra("askColor",askColor);
                data.putExtra("color",defaultColor);
                data.putExtra("maxResults",maxResults);
                setResult(OptionsActivity.RESULT_OK, data);
                finish();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent data = new Intent();
                data.putExtra("sanNotation",sanNotation);
                data.putExtra("language",askColor);
                data.putExtra("askColor",askColor);
                data.putExtra("color",defaultColor);
                data.putExtra("maxResults",maxResults);
                setResult(OptionsActivity.RESULT_OK, data);
                finish();
            }
        });
        limitMovesSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {


            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {

                limitMovesText.setText(Integer.toString(progress));


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }
}
