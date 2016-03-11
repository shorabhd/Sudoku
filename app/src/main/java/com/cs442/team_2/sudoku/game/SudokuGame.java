package com.cs442.team_2.sudoku.game;

import android.os.Bundle;
import android.os.SystemClock;

import com.cs442.team_2.sudoku.game.command.AbstractCommand;
import com.cs442.team_2.sudoku.game.command.ClearAllNotesCommand;
import com.cs442.team_2.sudoku.game.command.CommandStack;
import com.cs442.team_2.sudoku.game.command.EditCellNoteCommand;
import com.cs442.team_2.sudoku.game.command.SetCellValueCommand;

public class SudokuGame {

	public static final int GAME_STATE_PLAYING = 0;
	public static final int GAME_STATE_NOT_STARTED = 1;
	public static final int GAME_STATE_COMPLETED = 2;

	private long mId;
	private long mCreated;
	private int mState;
	private int mFId;
	private long mTime;
	private CellCollection mCells;

	private OnPuzzleSolvedListener mOnPuzzleSolvedListener;
	private CommandStack mCommandStack;
	// Time when current activity has become active. 
	private long mActiveFromTime = -1;

	public SudokuGame() {
		mTime = 0;
		mCreated = 0;
		mState = GAME_STATE_NOT_STARTED;
	}

	public void saveState(Bundle outState) {
		outState.putLong("id", mId);
		outState.putLong("created", mCreated);
		outState.putInt("state", mState);
		outState.putLong("time", mTime);
		outState.putString("cells", mCells.serialize());
		mCommandStack.saveState(outState);
	}

	public void restoreState(Bundle inState) {
		mId = inState.getLong("id");
		mCreated = inState.getLong("created");
		mState = inState.getInt("state");
		mTime = inState.getLong("time");
		mCells = CellCollection.deserialize(inState.getString("cells"));

		mCommandStack = new CommandStack(mCells);
		mCommandStack.restoreState(inState);
		validate();
	}


	public void setOnPuzzleSolvedListener(OnPuzzleSolvedListener l) {
		mOnPuzzleSolvedListener = l;
	}

	public void setCreated(long created) {
		mCreated = created;
	}

	public long getCreated() {
		return mCreated;
	}

	public void setState(int state) {
		mState = state;
	}

	public int getState() {
		return mState;
	}

	/**
	 * Sets time of play in milliseconds.
	 */
	public void setTime(long time) {
		mTime = time;
	}

	/**
	 * Gets time of game-play in milliseconds.
	 */
	public long getTime() {
		if (mActiveFromTime != -1) {
			return mTime + SystemClock.uptimeMillis() - mActiveFromTime;
		} else {
			return mTime;
		}
	}

	public void setCells(CellCollection cells) {
		mCells = cells;
		validate();
		mCommandStack = new CommandStack(mCells);
	}

	public CellCollection getCells() {
		return mCells;
	}

	public void setId(long id) {
		mId = id;
	}

	public long getId() {
		return mId;
	}

	public void setFolderId(int fid){
		mFId = fid;
	}

	public int getFolderId(){
		return mFId;
	}

	/**
	 * Sets value for the given cell. 0 means empty cell.
	 */
	public void setCellValue(Cell cell, int value) {
		if (cell == null) {
			throw new IllegalArgumentException("Cell cannot be null.");
		}
		if (value < 0 || value > 9) {
			throw new IllegalArgumentException("Value must be between 0-9.");
		}

		if (cell.isEditable()) {
			executeCommand(new SetCellValueCommand(cell, value));

			validate();
			if (isCompleted()) {
				finish();
				if (mOnPuzzleSolvedListener != null) {
					mOnPuzzleSolvedListener.onPuzzleSolved();
				}
			}
		}
	}

	/**
	 * Sets note attached to the given cell.
	 */
	public void setCellNote(Cell cell, CellNote note) {
		if (cell == null) {
			throw new IllegalArgumentException("Cell cannot be null.");
		}
		if (note == null) {
			throw new IllegalArgumentException("Note cannot be null.");
		}

		if (cell.isEditable()) {
			executeCommand(new EditCellNoteCommand(cell, note));
		}
	}

	private void executeCommand(AbstractCommand c) {
		mCommandStack.execute(c);
	}

	/**
	 * Undo last command.
	 */
	public void undo() {
		mCommandStack.undo();
	}

	public boolean hasSomethingToUndo() {
		return mCommandStack.hasSomethingToUndo();
	}


	/**
	 * Start game-play.
	 */
	public void start() {
		mState = GAME_STATE_PLAYING;
		resume();
	}

	public void resume() {
		mActiveFromTime = SystemClock.uptimeMillis();
	}

	/**
	 * Pauses game-play (for example if activity pauses).
	 */
	public void pause() {
		mTime += SystemClock.uptimeMillis() - mActiveFromTime;
		mActiveFromTime = -1;
	}

	/**
	 * Finishes game-play. Called when puzzle is solved.
	 */
	private void finish() {
		pause();
		mState = GAME_STATE_COMPLETED;
	}

	/**
	 * Resets game.
	 */
	public void reset() {
		for (int r = 0; r < CellCollection.SUDOKU_SIZE; r++) {
			for (int c = 0; c < CellCollection.SUDOKU_SIZE; c++) {
				Cell cell = mCells.getCell(r, c);
				if (cell.isEditable()) {
					cell.setValue(0);
					cell.setNote(new CellNote());
				}
			}
		}
		validate();
		setTime(0);
		mState = GAME_STATE_NOT_STARTED;
	}

	/**
	 * Returns true, if puzzle is solved. In order to know the current state, you have to
	 * call validate first.
	 */
	public boolean isCompleted() {
		return mCells.isCompleted();
	}

	public void clearAllNotes() {
		executeCommand(new ClearAllNotesCommand());
	}

	/**
	 * Fills in possible values which can be entered in each cell.
	 */

	public void validate() {
		mCells.validate();
	}

	public interface OnPuzzleSolvedListener {
		void onPuzzleSolved();
	}

}
