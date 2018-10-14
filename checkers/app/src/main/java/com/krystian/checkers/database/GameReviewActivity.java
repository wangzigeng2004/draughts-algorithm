package com.krystian.checkers.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.krystian.checkers.R;
import com.krystian.checkers.gameMechanics.GameActivity;
import com.krystian.checkers.gameMechanics.Pawn;
import com.krystian.checkers.gameMechanics.PlayableTile;

import static com.krystian.checkers.R.color.brownPawn;
import static com.krystian.checkers.R.color.whitePawn;
import static com.krystian.checkers.R.id.board;
import static com.krystian.checkers.R.id.game;
import static com.krystian.checkers.gameMechanics.GameActivity.NUMBER_OF_PAWNS;
import static com.krystian.checkers.gameMechanics.GameActivity.NUMBER_OF_PLAYABLE_TILES;
import static com.krystian.checkers.gameMechanics.GameActivity.NUMBER_OF_TILES;

public class GameReviewActivity extends AppCompatActivity implements View.OnClickListener {


    GridLayout board;
    PlayableTile[] playableTile = new PlayableTile[NUMBER_OF_PLAYABLE_TILES];
    View[] playableTileView = new View[NUMBER_OF_PLAYABLE_TILES];
    int gameNumber;
    int currentMoveNumber = 0;
    boolean whiteToMove = true;
    TextView game;
    TextView moveDescription;
    String whiteMoves;
    String brownMoves;
    String boardStates;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_review);

        board = (GridLayout) findViewById(R.id.board);
        game = (TextView) findViewById(R.id.game);
        moveDescription = (TextView) findViewById(R.id.move_description);
        gameNumber = (Integer) getIntent().getExtras().get("GameNumber");

        getGameFromDatabase();
        measureBoard();
    }

    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.left_arrow:
                if(currentMoveNumber != 0) {
                    if(whiteToMove) currentMoveNumber--; //previous brown move
                    whiteToMove = !whiteToMove;
                    setMoveDescription();
                    setNewState();

                }
                break;
            case R.id.right_arrow:
                if(!whiteToMove) currentMoveNumber++;
                whiteToMove = !whiteToMove;
                setMoveDescription();
                setNewState();
                break;
        }
    }

    public void getGameFromDatabase() {
        try {
            GameDatabaseHelper dbHelper = new GameDatabaseHelper(this);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query("GAMES", new String[]{"NAME", "WHITE", "BROWN", "BOARD"},
                    "NUMBER = ?", new String[]{Integer.toString(gameNumber)}, null, null, null);
            game.setText(cursor.getString(0));
            whiteMoves = cursor.getString(1);
            brownMoves = cursor.getString(2);
            boardStates = cursor.getString(3);

            cursor.close();
            db.close();

        } catch(SQLiteException e) {
            Toast.makeText(this, "Nie udało się połączyć z bazą danych", Toast.LENGTH_SHORT).show();
        }

    }

    public void measureBoard() {
        board.post(new Runnable() {
            public void run() {
                int width = board.getWidth();
                int height = board.getHeight();
                drawBoard(width, height);  //to calculate width and height of a single tile
            }
        });
    }

    public void drawBoard(int width, int height) {
        View[] tile = new View[NUMBER_OF_TILES];
        int brownTileCounter = 0; //to set id value for game mechanics and listeners
        for (int i = 0; i < tile.length; i++) {
            tile[i] = new View(this);
            tile[i].setLayoutParams(new LinearLayout.LayoutParams(width / 10, height / 10)); //10 x 10 tiles board
            if ((i % 2 == 0 && (i / 10) % 2 == 0) || (i % 2 != 0 && (i / 10) % 2 != 0))  //which tiles should be white
                tile[i].setBackgroundColor(getResources().getColor(R.color.whiteTile));
            else {
                playableTileView[brownTileCounter] = tile[i];
                playableTileView[brownTileCounter].setId(brownTileCounter + 1);
                brownTileCounter++;
            }

            board.addView(tile[i]);
        }

        createPawns();
    }

    public void createPawns() {
        for(int i=0; i<NUMBER_OF_PLAYABLE_TILES; i++) { //create playableTiles
            if(i>=0 && i < 20) playableTile[i] = new PlayableTile((i+1), -1); //-1 is brown pawn
            else if(i>=30 && i <50) playableTile[i] = new PlayableTile((i+1), 1); //1 is white pawn
            else playableTile[i] = new PlayableTile((i+1), 0); //0 means tile is empty
        }
        drawPawns();
    }

    public void drawPawns() {
        for (int i = 0; i < playableTileView.length; i++) {
            if (playableTile[i].getIsTaken() == 1)
                playableTileView[i].setBackgroundResource(R.drawable.white_pawn);
            else if (playableTile[i].getIsTaken() == -1)
                playableTileView[i].setBackgroundResource(R.drawable.brown_pawn);
            else if (playableTile[i].getIsTaken() == 2)
                playableTileView[i].setBackgroundResource(R.drawable.white_queen);
            else if (playableTile[i].getIsTaken() == -2)
                playableTileView[i].setBackgroundResource(R.drawable.brown_queen);
            else playableTileView[i].setBackgroundResource(0);
        }
    }

    public void setNewState() {
        int moveCounter = 1;
        int playableTileIndex = 0;
        for(int i=0; i<boardStates.length(); i++) {
            if(boardStates.charAt(i) == '#') {
                moveCounter++;
                continue;
            }

            if(moveCounter == currentMoveNumber) {
                playableTile[playableTileIndex].setIsTaken(boardStates.charAt(i));
                playableTileIndex++;
            }
            else if(moveCounter > currentMoveNumber) break;



        }
        drawPawns();
    }

    public void setMoveDescription() {
        int moveCounter = 1;
        String moveDesc = "";

        if(whiteToMove) {
            for(int i=0; i<whiteMoves.length(); i++) {
                if(whiteMoves.charAt(i) == '#') {
                    moveCounter++;
                    continue;
                }

                if(moveCounter == currentMoveNumber) moveDesc += whiteMoves.charAt(i);
                else if(moveCounter > currentMoveNumber) break;
            }
            moveDescription.setText(moveDesc);
        }

        else {
            for(int i=0; i<brownMoves.length(); i++) {
                if(brownMoves.charAt(i) == '#') {
                    moveCounter++;
                    continue;
                }

                if(moveCounter == currentMoveNumber) moveDesc += brownMoves.charAt(i);
                else if(moveCounter > currentMoveNumber) break;
            }
            moveDescription.setText(moveDesc);
        }
    }
}
