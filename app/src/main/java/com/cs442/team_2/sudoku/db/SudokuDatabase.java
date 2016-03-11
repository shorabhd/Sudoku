package com.cs442.team_2.sudoku.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.cs442.team_2.sudoku.game.CellCollection;
import com.cs442.team_2.sudoku.game.SudokuGame;

public class SudokuDatabase {
	public static final String DATABASE_NAME = "opensudoku";


	public static final String SUDOKU_TABLE_NAME = "sudoku";
	public static final String RECORD_TABLE_NAME = "record";
	public static final String FOLDER_TABLE_NAME = "folder";

	private DatabaseHelper mOpenHelper;

	public SudokuDatabase(Context context) {
		mOpenHelper = new DatabaseHelper(context);
	}

	/**
	 * Returns list of puzzles in the given folder.
	 */
	public Cursor getSudokuList(long folderID) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		qb.setTables(SUDOKU_TABLE_NAME);
		qb.appendWhere(DatabaseHelper.FOLDER_ID + "=" + folderID);

		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		return qb.query(db, null, null, null, null, null, "created DESC");
	}

	public Cursor getSudokuList(long folderID, int state) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		qb.setTables(SUDOKU_TABLE_NAME);
		qb.appendWhere(DatabaseHelper.FOLDER_ID + "=" + folderID + " and " + DatabaseHelper.STATE + "=" + state);

		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		return qb.query(db, null, null, null, null, null, "created DESC");
	}

	/**
	 * Returns Sudoku game object.
	 */
	public SudokuGame getSudoku(long sudokuID) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		qb.setTables(SUDOKU_TABLE_NAME);
		qb.appendWhere(DatabaseHelper.SUDOKU_ID + "=" + sudokuID);

		// Get the database and run the query

		SQLiteDatabase db = null;
		Cursor c = null;
		SudokuGame s = null;
		try {
			db = mOpenHelper.getReadableDatabase();
			c = qb.query(db, null, null, null, null, null, null);

			if (c.moveToFirst()) {
				long id = c.getLong(c.getColumnIndex(DatabaseHelper.SUDOKU_ID));
				long created = c.getLong(c.getColumnIndex(DatabaseHelper.CREATED));
				String data = c.getString(c.getColumnIndex(DatabaseHelper.DATA));
				int state = c.getInt(c.getColumnIndex(DatabaseHelper.STATE));
				long time = c.getLong(c.getColumnIndex(DatabaseHelper.TIME));
				int folderId = c.getInt(c.getColumnIndex(DatabaseHelper.FOLDER_ID));

				s = new SudokuGame();
				s.setId(id);
				s.setCreated(created);
				s.setCells(CellCollection.deserialize(data));
				s.setState(state);
				s.setTime(time);
				s.setFolderId(folderId);
			}
		} finally {
			if (c != null) c.close();
		}
		return s;
	}

	/**
	 * Updates Sudoku Game in the Database.
	 */
	public void updateSudoku(SudokuGame sudoku) {
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.DATA, sudoku.getCells().serialize());
		values.put(DatabaseHelper.STATE, sudoku.getState());
		values.put(DatabaseHelper.TIME, sudoku.getTime());
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		db.update(SUDOKU_TABLE_NAME, values, DatabaseHelper.SUDOKU_ID + "=" + sudoku.getId(), null);
	}

	/**
	 * update and insert data in Record table
	 */

	public void insertRecord(SudokuGame sudoku){
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.SUDOKU_ID, sudoku.getId());
		values.put(DatabaseHelper.TIME,sudoku.getTime());
		values.put(DatabaseHelper.FOLDER_ID,sudoku.getFolderId());

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		db.insert(RECORD_TABLE_NAME, null, values);
	}

	public void updateRecordTime(SudokuGame sudoku){
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.TIME,sudoku.getTime());
		db.update(RECORD_TABLE_NAME, values, DatabaseHelper.SUDOKU_ID + " =? ", new String[]{Long.toString(sudoku.getId())});
	}

	public Cursor getRecordTime(SudokuGame sudoku){
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Cursor cursor = db.query(RECORD_TABLE_NAME,null,DatabaseHelper.SUDOKU_ID + "= ?",new String[]{Long.toString(sudoku.getId())},null,null,null);
		return cursor;
	}

	public Cursor getRecordList(Long folderID){
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		qb.setTables(RECORD_TABLE_NAME);
		qb.appendWhere(DatabaseHelper.FOLDER_ID + " = " + folderID);

		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		return qb.query(db, null, null, null, null, null, null);
	}



	public void close()
	{
		mOpenHelper.close();
	}
}
