package edu.xu.TicTacToe;

import android.app.ActionBar;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.widget.Toast;

import android.content.SharedPreferences;

import android.media.MediaPlayer;

import android.os.Environment;

import java.io.File;
import java.io.FileWriter;

import java.io.IOException;
import java.util.Random;

public class TicTacToeActivity extends Activity {
    //Declaring FileWriter
    public FileWriter outputStream;

    // Buttons making up the board
    private Button mBoardButtons[];

    // Represents the internal state of the game
    private TicTacToeGame mGame;

    // Various text displayed
    private TextView mInfoTextView;
    private TextView hWinNumTextView;
    private TextView tieNumTextView;
    private TextView aWinNumTextView;

    private Random mRand = new Random();

    MediaPlayer mHumanMediaPlayer;
    MediaPlayer mComputerMediaPlayer;

    char mTurn;

    boolean firstPlayer;
    boolean mGameOver = false;

    int mHumanWins = 0;
    int mComputerWins = 0;
    int mTies = 0;

    static final int DIALOG_DIFFICULTY_ID = 0;
    static final int DIALOG_RESET_ID = 1;
    static final int DIALOG_ABOUT_ID = 2;
    static final int DIALOG_QUIT_ID = 3;

    private SharedPreferences mPrefs;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("TicTacToeActivity", "Running onCreate method");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mPrefs = getSharedPreferences("ttt_prefs", MODE_PRIVATE);

        // Restore the scores
        mHumanWins = mPrefs.getInt("mHumanWins", 0);
        mComputerWins = mPrefs.getInt("mComputerWins", 0);
        mTies = mPrefs.getInt("mTies", 0);

        //gets the activity's default ActionBar
        ActionBar actionBar = getActionBar();
        actionBar.show();

        // Open file for debugging info on the SD card
        String outFile = Environment.getExternalStorageDirectory() + File.separator + "Activity.txt";
        Log.d("TicTacToeActivity", "outFile is: " + outFile);
        Log.d("TicTacToeActivity", "Environment Storage Dir: " + Environment.getExternalStorageDirectory());


        // Display the file path
        Toast.makeText(getApplicationContext(), outFile, Toast.LENGTH_SHORT).show();

        // Create the file writer to be used in methods
        try {
            outputStream = new FileWriter(outFile);
            Log.d("TicTacToeActivity", "Successfully instatiated outputStream");
        } catch (IOException ex) {}

        //Write restored data to debug file
        try{
            outputStream.write("TicTacToeActivity : Restoring Saved Scores" + "\n");
            outputStream.write("Human Wins: " + mHumanWins + "\n");
            outputStream.write("Computer Wins: " + mComputerWins + "\n");
            outputStream.write("Ties : " + mTies + "\n");
            Log.d("TicTacToeActivity", "Successfully completed outputStream.write");
        }
        catch(java.io.IOException ex){}

        mBoardButtons = new Button[TicTacToeGame.BOARD_SIZE];
        mBoardButtons[0] = (Button) findViewById(R.id.one);
        mBoardButtons[1] = (Button) findViewById(R.id.two);
        mBoardButtons[2] = (Button) findViewById(R.id.three);
        mBoardButtons[3] = (Button) findViewById(R.id.four);
        mBoardButtons[4] = (Button) findViewById(R.id.five);
        mBoardButtons[5] = (Button) findViewById(R.id.six);
        mBoardButtons[6] = (Button) findViewById(R.id.seven);
        mBoardButtons[7] = (Button) findViewById(R.id.eight);
        mBoardButtons[8] = (Button) findViewById(R.id.nine);

        mInfoTextView = (TextView) findViewById(R.id.information);
        hWinNumTextView = (TextView) findViewById(R.id.hWinNum);
        tieNumTextView = (TextView) findViewById(R.id.tieNum);
        aWinNumTextView = (TextView) findViewById(R.id.aWinNum);

        mGame = new TicTacToeGame();

        // Choose who goes first
        int x = mRand.nextInt(2);
        if (x == 0) {
            firstPlayer = true;
            mInfoTextView.setText(R.string.turn_human);
        } else {
            firstPlayer = false;
            mInfoTextView.setText(R.string.first_computer);
        }

        Log.d("TicTacToeActivity", "Random Value: " + x);

        if(savedInstanceState == null){
            startNewGame();
        }

        displayScores();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d("TicTacToeActivity", "Running onCreateOptionsMenu Method");
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Log.d("TicTacToeActivity", "Running onCreateDialog method with a value of: " + id);
        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        switch (id) {
            case DIALOG_DIFFICULTY_ID:
                builder.setTitle(R.string.difficulty_choose);
                final CharSequence[] levels =
                        {getResources().getString(R.string.difficulty_easy),
                                getResources().getString(R.string.difficulty_harder),
                                getResources().getString(R.string.difficulty_expert)};

                int selected = -1;
                if (mGame.getDifficultyLevel() == TicTacToeGame.DifficultyLevel.Easy)
                    selected = 0;
                if (mGame.getDifficultyLevel() == TicTacToeGame.DifficultyLevel.Harder)
                    selected = 1;
                if (mGame.getDifficultyLevel() == TicTacToeGame.DifficultyLevel.Expert)
                    selected = 2;

                builder.setSingleChoiceItems(levels, selected,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                dialog.dismiss();// Close dialog

                                if (item == 0)
                                    mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Easy);
                                if (item == 1)
                                    mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Harder);
                                if (item == 2)
                                    mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Expert);

                                // Display the selected difficultylevel
                                Toast.makeText(getApplicationContext(), levels[item],
                                        Toast.LENGTH_SHORT).show();

                            }

                        });
                dialog = builder.create();

                break;

            case DIALOG_ABOUT_ID:
                Context context = getApplicationContext();
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.about_dialog, null);
                builder.setView(layout);
                builder.setPositiveButton("OK", null);
                dialog = builder.create();

                break;

            case DIALOG_RESET_ID:
                // Reset logic

                mHumanWins = 0;
                mComputerWins = 0;
                mTies = 0;
                displayScores();
                break;

        case DIALOG_QUIT_ID:
            // Create the quit conformation dialog

            builder.setMessage(R.string.quit_question)
                .setCancelable(false)
                .setNegativeButton(R.string.quit_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Write game status to outfile
                        try{
                            outputStream.write("TicTacToeActivity" + "I am closing the debug file" + "\n");
                            outputStream.write("TicTacToeActivity" + "I am exiting the game" + "\n");
                        }
                        catch(java.io.IOException ex){}

                        //Close the outfile when we quit the app
                        try{
                            outputStream.close();
                        } catch(java.io.IOException ex) {}

                        TicTacToeActivity.this.finish();
                    }
                })
                .setPositiveButton(R.string.quit_no, null);
        dialog = builder.create();


        break;
    }

        Log.d("TicTacToeActivity", "Completed onCreateDialog Method");
        return dialog;
    }

    @Override
    protected void onResume() {
        Log.d("TicTacToeActivity", "Running onResume Method");
        super.onResume();

        mHumanMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.sword);
        mComputerMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.swish);
    }

    @Override
    protected void onPause() {
        super.onPause();

        mHumanMediaPlayer.release();
        mComputerMediaPlayer.release();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Save the current scores
        SharedPreferences.Editor ed = mPrefs.edit();
        ed.putInt("mHumanWins", mHumanWins);
        ed.putInt("mComputerWins", mComputerWins);
        ed.putInt("mTies", mTies);
        ed.commit();
    }

    // Handles menu item selections
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("TicTacToeActivity", "Running onOptionItemSelected with a option of: " + item);
        switch (item.getItemId()) {
            case R.id.new_game:
                startNewGame();
                return true;
            case R.id.ai_difficulty:
                showDialog(DIALOG_DIFFICULTY_ID);
                return true;
            case R.id.about_dialog:
                showDialog(DIALOG_ABOUT_ID);
                return true;
            case R.id.reset:
                showDialog(DIALOG_RESET_ID);
                return true;
            case R.id.quit:
                showDialog(DIALOG_QUIT_ID);
                return true;
        }
        return false;
    }


    // Set up the game board.
    private void startNewGame() {
        Log.d("TicTacToeActivity", "Running startNewGame Method");

        try{
            outputStream.write("TicTacToeActivity : Starting a new game." + "\n");
        }
        catch(java.io.IOException ex){}

        mGameOver = false;
        //Alternate the starting turns
        if (firstPlayer == true) {
            Log.d("TicTacToeActivity", "Changing first player to false");
            firstPlayer = false;
            mInfoTextView.setText(R.string.first_computer);
        } else {
            Log.d("TicTacToeActivity", "Changing first player to true");
            firstPlayer = true;
            mInfoTextView.setText(R.string.first_human);
        }

        mGame.clearBoard();

        // Reset all buttons
        for (int i = 0; i < mBoardButtons.length; i++) {
            mBoardButtons[i].setText("");
            mBoardButtons[i].setEnabled(true);
            mBoardButtons[i].setOnClickListener(new ButtonClickListener(i));
        }

        if(firstPlayer == false){
            mTurn = 'O';
            int location = mGame.getComputerMove();
            setMove(mTurn, location);
        }

        if(mTurn == 'O')
            mInfoTextView.setText(R.string.first_computer);

        else
            mInfoTextView.setText(R.string.first_human);

        mTurn = 'X';

        Log.d("TicTacToeActivity", "Completed startNewGame method");
    }

    private void setMove(char mTurn, int location) {
        Log.d("TicTacToeActivity", "Running setMove method with values of player: " + mTurn + " and a move of " + location);
        try{
            outputStream.write("TicTacToeActivity : I am in setMove(), about to move for " + mTurn + "\n");
        }
        catch(java.io.IOException ex){}

        mGame.setMove(mTurn, location);
        mBoardButtons[location].setEnabled(false);
        mBoardButtons[location].setText(String.valueOf(mTurn));

        if (mTurn == TicTacToeGame.HUMAN_PLAYER) {
            mBoardButtons[location].setTextColor(Color.rgb(0, 200, 0));
            mHumanMediaPlayer.start(); // Play the sound effect
        } else {
            mInfoTextView.setText(R.string.turn_computer);
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    try {
                        mComputerMediaPlayer.start();
                    }
                    catch(IllegalStateException ex){
                        Log.d("TicTacToeActivity", "Entered Catch Block");
                    }

                }
            }, 50);

            mBoardButtons[location].setTextColor(Color.rgb(200, 0, 0));
        }
        try{
            outputStream.write("TicTacToeActivity : \n");
            outputStream.write("Here is the board after the move for " + mTurn + "\n");
            outputStream.write(mGame.toString() + "\n");
        }
        catch(java.io.IOException ex){}
    }

    // Handles clicks on the game board buttons
    private class ButtonClickListener implements View.OnClickListener {
        int location;

        public ButtonClickListener(int location) {
            this.location = location;
        }

        public void onClick(View view) {
            Log.d("TicTacToeActivity", "Listening for button clicks mTurn value: " + mTurn);
            try{
                int colNum = -1,rowNum = -1;

                if(location == 0 || location == 3 || location == 6){
                    colNum = 0;
                }
                if(location == 1 || location == 4 || location == 7){
                    colNum = 1;
                }
                if(location == 1 || location == 5 || location == 8){
                    colNum = 2;
                }
                if(location == 0 || location == 1 || location == 2){
                    rowNum = 0;
                }
                if(location == 3 || location == 4 || location == 5){
                    rowNum = 1;
                }
                if(location == 6 || location == 7 || location == 8){
                    rowNum = 2;
                }
                outputStream.write("TicTacToeActivity : Cell that was touched:" +
                        " Col = " + colNum + " Row = " + rowNum + " Position = " + location + ".\n");
            }
            catch(java.io.IOException ex){}

            if (mTurn == TicTacToeGame.HUMAN_PLAYER) {
                mTurn = TicTacToeGame.COMPUTER_PLAYER;
                if (mBoardButtons[location].isEnabled()) {
                    setMove(TicTacToeGame.HUMAN_PLAYER, location);
                    checkWinner();
                }

                if (mGame.checkForWinner() == 0) {
                    mInfoTextView.setText(R.string.turn_computer);
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            int move = mGame.getComputerMove();
                            setMove(TicTacToeGame.COMPUTER_PLAYER, move);
                            checkWinner();
                            mTurn = 'X';
                        }
                    }, 1000);
                }
            }

            Log.d("TicTacToeActivity", "Completed onClick Method");
        }
    }

    private void checkWinner() {
        //Disable Buttons on winner
        int winner = mGame.checkForWinner();
        Log.d("TicTacToeActivity","Entered check Winner value: " + winner);



        if (winner == 0)
            mInfoTextView.setText(R.string.turn_human);

        else if (winner == 1) {
            mInfoTextView.setText(R.string.result_tie);
            mTies += 1;
        } else if (winner == 2) {
            mInfoTextView.setText(R.string.result_human_wins);
            mHumanWins += 1;
        } else {
            mInfoTextView.setText(R.string.result_computer_wins);
            mComputerWins += 1;
        }

        if (winner != 0) {
            try {
                outputStream.write("TicTacToeActivity : There was a winner after the last move for " + mTurn + "\n");
            }
            catch(java.io.IOException ex){}

            for (Button mBoardButton : mBoardButtons) {
                mBoardButton.setEnabled(false);
                mGameOver = true;
                displayScores();
            }
        }
    }

    private void displayScores() {
        hWinNumTextView.setText(String.valueOf(mHumanWins));
        aWinNumTextView.setText(String.valueOf(mComputerWins));
        tieNumTextView.setText(String.valueOf(mTies));
    }

    // Save Instance
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharArray("board", mGame.getBoardState());
        outState.putBoolean("mGameOver", mGameOver);
        outState.putInt("mHumanWins", Integer.valueOf(mHumanWins));
        outState.putInt("mComputerWins", Integer.valueOf(mComputerWins));
        outState.putInt("mTies", Integer.valueOf(mTies));
        outState.putCharSequence("info", mInfoTextView.getText());
        outState.putBoolean("firstPlayer", firstPlayer);
        outState.putChar("mTurn", mTurn);

        int selected = -1;
        if (mGame.getDifficultyLevel() == TicTacToeGame.DifficultyLevel.Easy)
            selected = 0;
        if (mGame.getDifficultyLevel() == TicTacToeGame.DifficultyLevel.Harder)
            selected = 1;
        if (mGame.getDifficultyLevel() == TicTacToeGame.DifficultyLevel.Expert)
            selected = 2;
        // Save difficulty level as a Int
        outState.putInt("mDifficulty", selected);
    }

    // Restore Instance
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // Restore the game's state
        super.onRestoreInstanceState(savedInstanceState);

        try{
            outputStream.write("TicTacToeActivity : The orientation of the device changed, restoring game state \n");
            outputStream.write("Board State: \n");
            outputStream.write(mGame.toString() + "\n");
        }
        catch(java.io.IOException ex){}

        mGame.setBoardState(savedInstanceState.getCharArray("board"));
        mGameOver = savedInstanceState.getBoolean("mGameOver");
        mInfoTextView.setText(savedInstanceState.getCharSequence("info"));
        mHumanWins = savedInstanceState.getInt("mHumanWins");
        mComputerWins = savedInstanceState.getInt("mComputerWins");
        mTies = savedInstanceState.getInt("mTies");
        firstPlayer = savedInstanceState.getBoolean("firstPlayer");
        mTurn = savedInstanceState.getChar("mTurn");

        // restore difficulty level
        int select = savedInstanceState.getInt("mDifficulty");
        if (select == 0) mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Easy);
        if (select == 1) mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Harder);
        if (select == 2) mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Expert);

        if(mGameOver == false){
            if(mTurn == mGame.COMPUTER_PLAYER) {
                Log.d("TicTacToeActivity", "Entered Computer Move Restore");
                setMove(mTurn, mGame.getComputerMove());
                checkWinner();
                mTurn = mGame.HUMAN_PLAYER;
            }

        }
        restoreBoard();

    }


    private void restoreBoard() {
        // Reset and enable all buttons and attach listeners

        for (int i = 0; i < mBoardButtons.length; i++) {
            mBoardButtons[i].setText(" ");
            mBoardButtons[i].setEnabled(true);
            mBoardButtons[i].setOnClickListener(new ButtonClickListener(i));
        }
        // Draw all the X and O moves and disable buttons where a move has been made
        for (int i = 0; i < TicTacToeGame.BOARD_SIZE; i++) {
            if (mGame.getBoardOccupant(i) == TicTacToeGame.HUMAN_PLAYER) {
                // Restore X moves
                mBoardButtons[i].setText(String.valueOf(mGame.getBoardOccupant(i)));
                mBoardButtons[i].setTextColor(Color.rgb(0, 200, 0));
                mBoardButtons[i].setEnabled(false);
            } else if (mGame.getBoardOccupant(i) == TicTacToeGame.COMPUTER_PLAYER) {
                // Restore Y moves
                mBoardButtons[i].setText(String.valueOf(mGame.getBoardOccupant(i)));
                mBoardButtons[i].setTextColor(Color.rgb(200, 0, 0));
                mBoardButtons[i].setEnabled(false);
            } else {
                //Restore open spaces
                mBoardButtons[i].setText(String.valueOf(mGame.getBoardOccupant(i)));
                if(mGameOver == true){
                    mBoardButtons[i].setEnabled(false);
                }
            }
        }
    }
}

