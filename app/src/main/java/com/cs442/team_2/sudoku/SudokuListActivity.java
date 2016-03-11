package com.cs442.team_2.sudoku;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;

import com.cs442.team_2.sudoku.db.DatabaseHelper;
import com.cs442.team_2.sudoku.db.SudokuDatabase;
import com.cs442.team_2.sudoku.game.CellCollection;
import com.cs442.team_2.sudoku.game.SudokuGame;

public class SudokuListActivity extends ListActivity {

	public static final String EXTRA_FOLDER_ID = "folder_id";

	private static final String TAG = "SudokuListActivity";

	private long mFolderID;

	private SimpleCursorAdapter mAdapter;
	private Cursor mCursor;
	private SudokuDatabase mDatabase;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.sudoku_list);
		getListView().setOnCreateContextMenuListener(this);
		setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

		mDatabase = new SudokuDatabase(getApplicationContext());

		Intent intent = getIntent();
		if (intent.hasExtra(EXTRA_FOLDER_ID)) {
			mFolderID = intent.getLongExtra(EXTRA_FOLDER_ID, 0);
		}
		else {
			finish();
			return;
		}


		mAdapter = new SimpleCursorAdapter(this, R.layout.sudoku_list_item,
				null, new String[]{DatabaseHelper.DATA,DatabaseHelper.SUDOKU_ID, DatabaseHelper.TIME},
				new int[]{R.id.sudoku_board, R.id.name, R.id.time});
		mAdapter.setViewBinder(new SudokuListViewBinder(this));
		updateList();
		setListAdapter(mAdapter);
		Music.play(this, R.raw.main);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mDatabase.close();
		Music.stop(this);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState(state);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// the puzzle list is naturally refreshed when the window
		// regains focus, so we only need to update the title
		updateTitle();
		Music.play(this, R.raw.main);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// if there is no activity in history and back button was pressed, go
		// to FolderListActivity, which is the root activity.
		if (isTaskRoot() && keyCode == KeyEvent.KEYCODE_BACK) {
			Intent i = new Intent();
			i.setClass(this, Sudoku.class);
			startActivity(i);
			finish();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		playSudoku(id);
	}

	/**
	 * Updates whole list.
	 */
	private void updateList() {
		updateTitle();
		if (mCursor != null) {
			stopManagingCursor(mCursor);
		}
		mCursor = mDatabase.getSudokuList(mFolderID);
		startManagingCursor(mCursor);
		mAdapter.changeCursor(mCursor);
	}

	private void updateTitle() {
		int id = (int) mFolderID;
		switch (id)
		{
			case 1:
				setTitle("Easy");
				break;
			case 2:
				setTitle("Medium");
				break;
			case 3:
				setTitle("Hard");
				break;
		}
	}

	private void playSudoku(long sudokuID) {
		Intent i = new Intent(SudokuListActivity.this, SudokuPlayActivity.class);
		i.putExtra(SudokuPlayActivity.EXTRA_SUDOKU_ID, sudokuID);
		startActivity(i);
	}

	private static class SudokuListViewBinder implements ViewBinder {
		private Context mContext;
		private GameTimeFormat mGameTimeFormatter = new GameTimeFormat();

		public SudokuListViewBinder(Context context) {
			mContext = context;
		}

		public boolean setViewValue(View view, Cursor c, int columnIndex) {

			int state = c.getInt(c.getColumnIndex(DatabaseHelper.STATE));

			TextView label = null;

			switch (view.getId()) {
				case R.id.sudoku_board:
					String data = c.getString(columnIndex);
					CellCollection cells = null;
					try {
						cells = CellCollection.deserialize(data);
					} catch (Exception e) {
						long id = c.getLong(c.getColumnIndex(DatabaseHelper.SUDOKU_ID));
						Log.e(TAG, String.format("Exception occurred when deserializing puzzle with id %s.", id), e);
					}
					SudokuBoardView board = (SudokuBoardView) view;
					board.setReadOnly(true);
					board.setFocusable(false);
					((SudokuBoardView) view).setCells(cells);
					break;
				case R.id.name:
					long id = c.getLong(c.getColumnIndex(DatabaseHelper.SUDOKU_ID));
					label = ((TextView) view);
					String nameString = "Puzzle: "+id;
					label.setVisibility(nameString == null ? View.GONE
							: View.VISIBLE);
					label.setText(nameString);
					if (state == SudokuGame.GAME_STATE_COMPLETED) {
						label.setTextColor(Color.rgb(187, 187, 187));
					} else {
						label.setTextColor(Color.rgb(0, 0, 0));
					}
					break;
				case R.id.time:
					long time = c.getLong(columnIndex);
					label = ((TextView) view);
					String timeString = null;
					if (time != 0) {
						timeString = mGameTimeFormatter.format(time);
					}
					label.setVisibility(timeString == null ? View.GONE
							: View.VISIBLE);
					label.setText(timeString);
					if (state == SudokuGame.GAME_STATE_COMPLETED) {
						label.setTextColor(Color.rgb(187, 187, 187));
					} else {
						label.setTextColor(Color.rgb(0, 0, 0));
					}
					break;
			}
			return true;
		}
	}
}
