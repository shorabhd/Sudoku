package com.cs442.team_2.sudoku.game;

import java.util.HashMap;
import java.util.Map;


/**
 * Represents group of cells which must each contain unique number.
 */

public class CellGroup {
	private Cell[] mCells = new Cell[CellCollection.SUDOKU_SIZE];
	private int mPos = 0;

	public void addCell(Cell cell) {
		mCells[mPos] = cell;
		mPos++;
	}

	/**
	 * Validates numbers in given sudoku group - numbers must be unique.
	 */
	protected boolean validate() {
		boolean valid = true;

		Map<Integer, Cell> cellsByValue = new HashMap<Integer, Cell>();
		for (int i = 0; i < mCells.length; i++) {
			Cell cell = mCells[i];
			int value = cell.getValue();
			if (cellsByValue.get(value) != null) {
				mCells[i].setValid(false);
				cellsByValue.get(value).setValid(false);
				valid = false;
			} else {
				cellsByValue.put(value, cell);
			}
		}
		return valid;
	}
}
