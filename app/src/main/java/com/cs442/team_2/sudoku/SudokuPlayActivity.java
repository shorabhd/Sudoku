package com.cs442.team_2.sudoku;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.cs442.team_2.sudoku.db.DatabaseHelper;
import com.cs442.team_2.sudoku.db.SudokuDatabase;
import com.cs442.team_2.sudoku.game.SudokuGame;
import com.cs442.team_2.sudoku.game.SudokuGame.OnPuzzleSolvedListener;

public class SudokuPlayActivity extends ActionBarActivity
{

	public static final String EXTRA_SUDOKU_ID = "sudoku_id";

	private static final int DIALOG_RESTART = 1;
	private static final int DIALOG_WELL_DONE = 2;
	private static final int DIALOG_CLEAR_NOTES = 3;

	private static final int REQUEST_SETTINGS = 1;

	private long mSudokuGameID;
	private SudokuGame mSudokuGame;
	private SudokuDatabase mDatabase;

	private ViewGroup mRootLayout;
	private SudokuBoardView mSudokuBoard;

	private Numpad mNumpad;
	private boolean mShowTime = true;
	private GameTimer mGameTimer;
	private GameTimeFormat mGameTimeFormatter = new GameTimeFormat();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.sudoku_play);

		mRootLayout = (ViewGroup) findViewById(R.id.root_layout);
		mSudokuBoard = (SudokuBoardView) findViewById(R.id.sudoku_board);

		mDatabase = new SudokuDatabase(getApplicationContext());
		mGameTimer = new GameTimer();

		// create sudoku game instance
		if (savedInstanceState == null) {
			// activity runs for the first time, read game from database
			mSudokuGameID = getIntent().getLongExtra(EXTRA_SUDOKU_ID, 0);
			mSudokuGame = mDatabase.getSudoku(mSudokuGameID);
		}
		else
		{
			// activity has been running before, restore its state
			mSudokuGame = new SudokuGame();
			mSudokuGame.restoreState(savedInstanceState);
			mGameTimer.restoreState(savedInstanceState);
		}

		if(getIntent().getBundleExtra("bundle")!=null)
		{
			mSudokuGameID = getIntent().getBundleExtra("bundle").getLong("gameid",0);
			mSudokuGame = mDatabase.getSudoku(mSudokuGameID);
		}


		if (mSudokuGame.getState() == SudokuGame.GAME_STATE_NOT_STARTED) {
			mSudokuGame.start();
		} else if (mSudokuGame.getState() == SudokuGame.GAME_STATE_PLAYING) {
			mSudokuGame.resume();
		}

		if (mSudokuGame.getState() == SudokuGame.GAME_STATE_COMPLETED) {
			mSudokuBoard.setReadOnly(true);
		}

		mSudokuBoard.setGame(mSudokuGame);
		mSudokuGame.setOnPuzzleSolvedListener(onSolvedListener);

		mNumpad = (Numpad) findViewById(R.id.input_methods);
		mNumpad.initialize(mSudokuBoard, mSudokuGame);
		mNumpad.setHighlightCompletedValues(true);
		Music.play(this, R.raw.main);
	}

	@Override
	protected void onResume() {
		super.onResume();

		mShowTime = Prefs.getTimer(this);
		if (mSudokuGame.getState() == SudokuGame.GAME_STATE_PLAYING) {
			mSudokuGame.resume();
			if (mShowTime) {
				mGameTimer.start();
			}
		}
		mNumpad.activate();
		mNumpad.setHighlightCompletedValues(true);
		updateTime();
		Music.play(this, R.raw.main);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mDatabase.updateSudoku(mSudokuGame);
		mGameTimer.stop();
		mNumpad.pause();
		Music.stop(this);
		if(isFinishing())
		{
			SharedPreferences.Editor editor = getSharedPreferences("game", Context.MODE_PRIVATE).edit();
			editor.putBoolean("resume",true);
			editor.putLong("gameid",mSudokuGameID);
			editor.commit();
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mDatabase.close();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mGameTimer.stop();

		if (mSudokuGame.getState() == SudokuGame.GAME_STATE_PLAYING) {
			mSudokuGame.pause();
		}
		mSudokuGame.saveState(outState);
		mGameTimer.saveState(outState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.game_menu, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		if (mSudokuGame.getState() == SudokuGame.GAME_STATE_PLAYING) {
			menu.findItem(R.id.clear).setEnabled(true);
			menu.findItem(R.id.undo).setEnabled(mSudokuGame.hasSomethingToUndo());
		}
		else {
			menu.findItem(R.id.clear).setEnabled(false);
			menu.findItem(R.id.undo).setEnabled(false);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.undo:
				mSudokuGame.undo();
				return true;
			case R.id.restart:
				showDialog(DIALOG_RESTART);
				return true;
			case R.id.clear:
				showDialog(DIALOG_CLEAR_NOTES);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_SETTINGS:
				restartActivity();
				break;
		}
	}

	/**
	 * Restarts whole activity.
	 */
	private void restartActivity() {
		startActivity(getIntent());
		finish();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DIALOG_WELL_DONE:
				return new AlertDialog.Builder(this)
						.setIcon(android.R.drawable.ic_dialog_info)
						.setTitle(R.string.well_done)
						.setMessage(getString(R.string.congrats, mGameTimeFormatter.format(mSudokuGame.getTime())))
						.setPositiveButton(android.R.string.ok, null)
						.create();
			case DIALOG_RESTART:
				return new AlertDialog.Builder(this)
						.setIcon(android.R.drawable.ic_menu_rotate)
						.setTitle(R.string.app_name)
						.setMessage(R.string.restart_confirm)
						.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								// Restart game
								mSudokuGame.reset();
								mSudokuGame.start();
								mSudokuBoard.setReadOnly(false);
								if (mShowTime) {
									mGameTimer.start();
								}
							}
						})
						.setNegativeButton(android.R.string.no, null)
						.create();
			case DIALOG_CLEAR_NOTES:
				return new AlertDialog.Builder(this)
						.setIcon(android.R.drawable.ic_menu_delete)
						.setTitle(R.string.app_name)
						.setMessage(R.string.clear_all_notes_confirm)
						.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								mSudokuGame.clearAllNotes();
							}
						})
						.setNegativeButton(android.R.string.no, null)
						.create();
		}
		return null;
	}

	/**
	 * Occurs when puzzle is solved.
	 */
	private OnPuzzleSolvedListener onSolvedListener = new OnPuzzleSolvedListener() {

		public void onPuzzleSolved() {

			Cursor cursor = mDatabase.getRecordTime(mSudokuGame);
			mSudokuBoard.setReadOnly(true);
			if(cursor.getCount() == 0){
				mDatabase.insertRecord(mSudokuGame);
			}
			else{
				cursor.moveToFirst();
				if(mSudokuGame.getTime() < cursor.getInt(cursor.getColumnIndex(DatabaseHelper.TIME))){
					mDatabase.updateRecordTime(mSudokuGame);
				};
			}
			showDialog(DIALOG_WELL_DONE);
		}

	};

	/**
	 * Update the time of game-play.
	 */
	void updateTime() {
		if (mShowTime) {
			setTitle(mGameTimeFormatter.format(mSudokuGame.getTime()));
		} else {
			setTitle(R.string.app_name);
		}

	}
	// This class implements the game clock.  All it does is update the
	// status each tick.
	private final class GameTimer extends Timer {

		GameTimer() {
			super(1000);
		}

		@Override
		protected boolean step(int count, long time) {
			updateTime();
			// Run until explicitly stopped.
			return false;
		}

	}
}

