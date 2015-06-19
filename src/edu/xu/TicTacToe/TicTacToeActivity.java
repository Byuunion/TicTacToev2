package edu.xu.TicTacToe;

import android.graphics.Color;
import android.os.Bundle;
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

import java.util.Random;

public class TicTacToeActivity extends Activity {

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
                // Create the quit conformation dialog
                /*
                builder.setMessage(R.string.quit_question)
                        .setCancelable(false)
                        .setNegativeButton(R.string.quit_yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                TicTacToeActivity.this.finish();
                            }
                        })
                        .setPositiveButton(R.string.quit_no, null);
                dialog = builder.create();
                */
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
        }
        return false;
    }


    // Set up the game board.
    private void startNewGame() {
        Log.d("TicTacToeActivity", "Running startNewGame Method");
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
                    mComputerMediaPlayer.start();
                }
            }, 50);

            mBoardButtons[location].setTextColor(Color.rgb(200, 0, 0));
        }
    }

    // Handles clicks on the game board buttons
    private class ButtonClickListener implements View.OnClickListener {
        int location;

        public ButtonClickListener(int location) {
            this.location = location;
        }

        public void onClick(View view) {
            Log.d("TicTacToeActivity", "Listening for button clicks mTurn value: " + mTurn);

            if (mTurn == TicTacToeGame.HUMAN_PLAYER) {
                mTurn = TicTacToeGame.COMPUTER_PLAYER;
                if (mBoardButtons[location].isEnabled()) {
                    setMove(TicTacToeGame.HUMAN_PLAYER, location);
                    checkWinner();
                }

                if (mGame.checkForWinner() == 0){
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
                for (Button mBoardButton : mBoardButtons) {
                    mBoardButton.setEnabled(false);
                    mGameOver = true;
                    displayScores();
                }
            }
        }
    }

    private void displayScores() {
        hWinNumTextView.setText(String.valueOf(mHumanWins));
        aWinNumTextView.setText(String.valueOf(mComputerWins));
        tieNumTextView.setText(String.valueOf(mTies));
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // Restore the game's state

        super.onRestoreInstanceState(savedInstanceState);

        mGame.setBoardState(savedInstanceState.getCharArray("board"));
        mGameOver = savedInstanceState.getBoolean("mGameOver");
        mInfoTextView.setText(savedInstanceState.getCharSequence("info"));
        mHumanWins = savedInstanceState.getInt("mHumanWins");
        mComputerWins = savedInstanceState.getInt("mComputerWins");
        mTies = savedInstanceState.getInt("mTies");
        firstPlayer = savedInstanceState.getBoolean("firstPlayer");
        mTurn = savedInstanceState.getChar("mTurn");
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