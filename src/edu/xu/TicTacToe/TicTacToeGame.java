package edu.xu.TicTacToe;

import java.util.Random;


public class TicTacToeGame {

    // The computer's difficulty levels
    public enum DifficultyLevel {Easy, Harder, Expert};

    // Current difficulty level
    private DifficultyLevel mDifficultyLevel = DifficultyLevel.Expert;
    public static final int BOARD_SIZE = 9;

    private char mBoard[];

    // Characters used to represent the human, computer, and open spots
    public static final char HUMAN_PLAYER = 'X';
    public static final char COMPUTER_PLAYER = 'O';
    public static final char OPEN_SPOT = ' ';

    // Random number generator
    private Random mRand;

    public TicTacToeGame()
    {
        mBoard = new char[BOARD_SIZE];
        mRand = new Random();
    }

    /** Clear the board of all X's and O's. */
    public void clearBoard()
    {
        for(int i=0;i<BOARD_SIZE;i++)
        {
            mBoard[i]=OPEN_SPOT;
        }
    }

    /** Set the given player at the given location on the game board.
     *  The location must be available, or the board will not be changed.
     *
     * @param player - The human or computer player
     * @param location - The location (0-8) to place the move
     */
    public void setMove(char player, int location)
    {
        // Game doesn't let the player click on an existing button
        mBoard[location] = player;
    }

    /** Return the best move for the computer to make. You must call setMove() to
     * actually make the computer move to that location.
     * @return The best move for the computer to make.
     */
    public int getComputerMove() {
        int move = -1;
        if (mDifficultyLevel == DifficultyLevel.Easy)
            move = getRandomMove();
        else if (mDifficultyLevel == DifficultyLevel.Harder) {
            move = getWinningMove();
            if (move == -1)
                move = getRandomMove();
        } else if (mDifficultyLevel == DifficultyLevel.Expert) {
// Try to win, but if that's not possible, block.
// If that's not possible, move anywhere.
            move = getWinningMove();
            if (move == -1)
                move = getBlockingMove();
            if (move == -1)
                move = getRandomMove();
        }
        return move;
    }

    // First see if there's a move O can make to win
    private int getWinningMove() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            if (mBoard[i] != HUMAN_PLAYER && mBoard[i] != COMPUTER_PLAYER) {
                char curr = mBoard[i];
                mBoard[i] = COMPUTER_PLAYER;
                if (checkForWinner() == 3) {
                    return i;
                } else
                    mBoard[i] = curr;
            }
        }
        return -1;
    }

    // See if there's a move O can make to block X from winning
    private int getBlockingMove() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            if (mBoard[i] != HUMAN_PLAYER && mBoard[i] != COMPUTER_PLAYER) {
                char curr = mBoard[i];   // Save the current number
                mBoard[i] = HUMAN_PLAYER;
                if (checkForWinner() == 2) {
                    mBoard[i] = COMPUTER_PLAYER;
                    return i;
                } else
                    mBoard[i] = curr;
            }
        }
        return -1;
    }

    // Generate random move
    private int getRandomMove()
    {
        int move;
        do
        {
            move = mRand.nextInt(BOARD_SIZE);
        } while (mBoard[move] == HUMAN_PLAYER || mBoard[move] == COMPUTER_PLAYER);

        mBoard[move] = COMPUTER_PLAYER;
        return move;
    }

    /** Check for a winner.  Return
     * 	0 if no winner or tie yet
     *	1 if it's a tie
     *	2 if X won
     *	3 if O won
     */
    public int checkForWinner() {

        // Check horizontal wins
        for (int i = 0; i <= 6; i += 3)	{
            if (mBoard[i] == HUMAN_PLAYER &&
                    mBoard[i+1] == HUMAN_PLAYER &&
                    mBoard[i+2]== HUMAN_PLAYER)
                return 2;
            if (mBoard[i] == COMPUTER_PLAYER &&
                    mBoard[i+1]== COMPUTER_PLAYER &&
                    mBoard[i+2] == COMPUTER_PLAYER)
                return 3;
        }

        // Check vertical wins
        for (int i = 0; i <= 2; i++) {
            if (mBoard[i] == HUMAN_PLAYER &&
                    mBoard[i+3] == HUMAN_PLAYER &&
                    mBoard[i+6]== HUMAN_PLAYER)
                return 2;
            if (mBoard[i] == COMPUTER_PLAYER &&
                    mBoard[i+3] == COMPUTER_PLAYER &&
                    mBoard[i+6]== COMPUTER_PLAYER)
                return 3;
        }

        // Check for diagonal wins
        if ((mBoard[0] == HUMAN_PLAYER &&
                mBoard[4] == HUMAN_PLAYER &&
                mBoard[8] == HUMAN_PLAYER) ||
                (mBoard[2] == HUMAN_PLAYER &&
                        mBoard[4] == HUMAN_PLAYER &&
                        mBoard[6] == HUMAN_PLAYER))
            return 2;
        if ((mBoard[0] == COMPUTER_PLAYER &&
                mBoard[4] == COMPUTER_PLAYER &&
                mBoard[8] == COMPUTER_PLAYER) ||
                (mBoard[2] == COMPUTER_PLAYER &&
                        mBoard[4] == COMPUTER_PLAYER &&
                        mBoard[6] == COMPUTER_PLAYER))
            return 3;

        // Check for tie
        for (int i = 0; i < BOARD_SIZE; i++) {
            // If we find a number, then no one has won yet
            if (mBoard[i] != HUMAN_PLAYER && mBoard[i] != COMPUTER_PLAYER)
                return 0;
        }

        // If we make it through the previous loop, all places are taken, so it's a tie
        return 1;
    }

    public DifficultyLevel getDifficultyLevel() {

        return mDifficultyLevel;
    }

    public void setDifficultyLevel(DifficultyLevel difficultyLevel){
        mDifficultyLevel = difficultyLevel;
    }

    public char[] getBoardState(){
        return mBoard;
    }

    public void setBoardState(char[] board){
        mBoard = board.clone();
    }

    /**
     * Return the board occupant (HUMAN_PLAYER, COMPUTER_PLAYER,
     * or OPEN_SPOT) for the given location or '?' if an invalid
     * location is given.
     *
     * @param location
    -
    A value between 0 and 8
     * @return The board occupant
     */
    public char getBoardOccupant(int location) {
        if (location >= 0 && location < BOARD_SIZE)
            return mBoard[location];
        return '?';
    }

    @Override
    public String toString() {
        return mBoard[0] + "|" + mBoard[1] + "|" + mBoard[2] + "\n" +
                mBoard[3] + "|" + mBoard[4] + "|" + mBoard[5] + "\n" +
                mBoard[6] + "|" + mBoard[7] + "|" + mBoard[8];
    }

}