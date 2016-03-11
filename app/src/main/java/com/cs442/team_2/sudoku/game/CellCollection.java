package com.cs442.team_2.sudoku.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * Collection of sudoku cells. This class in fact represents one sudoku board (9x9).
 */
public class CellCollection {

	public static final int SUDOKU_SIZE = 9;

	private Cell[][] mCells;

	// Helper arrays, contains references to the groups of cells, which should contain unique
	// numbers.
	private CellGroup[] mSectors;
	private CellGroup[] mRows;
	private CellGroup[] mColumns;

	private boolean mOnChangeEnabled = true;

	private final List<OnChangeListener> mChangeListeners = new ArrayList<OnChangeListener>();

	/**
	 * Creates empty sudoku.
	 */
	public static CellCollection createEmpty() {
		Cell[][] cells = new Cell[SUDOKU_SIZE][SUDOKU_SIZE];

		for (int r = 0; r < SUDOKU_SIZE; r++) {

			for (int c = 0; c < SUDOKU_SIZE; c++) {
				cells[r][c] = new Cell();
			}
		}
		return new CellCollection(cells);
	}

	/**
	 * Wraps given array in this object.
	 */
	private CellCollection(Cell[][] cells) {
		mCells = cells;
		initCollection();
	}

	/**
	 * Gets cell at given position.
	 */
	public Cell getCell(int rowIndex, int colIndex) {
		return mCells[rowIndex][colIndex];
	}

	public void markAllCellsAsValid() {
		mOnChangeEnabled = false;
		for (int r = 0; r < SUDOKU_SIZE; r++) {
			for (int c = 0; c < SUDOKU_SIZE; c++) {
				mCells[r][c].setValid(true);
			}
		}
		mOnChangeEnabled = true;
		onChange();
	}

	/**
	 * Validates numbers in collection according to the sudoku rules. Cells with invalid
	 * values are marked - you can use getInvalid method of cell to find out whether cell
	 * contains valid value.
	 */
	public boolean validate() {

		boolean valid = true;

		// first set all cells as valid
		markAllCellsAsValid();

		mOnChangeEnabled = false;
		// run validation in groups
		for (CellGroup row : mRows) {
			if (!row.validate()) {
				valid = false;
			}
		}
		for (CellGroup column : mColumns) {
			if (!column.validate()) {
				valid = false;
			}
		}
		for (CellGroup sector : mSectors) {
			if (!sector.validate()) {
				valid = false;
			}
		}

		mOnChangeEnabled = true;
		onChange();

		return valid;
	}

	public boolean isCompleted() {
		for (int r = 0; r < SUDOKU_SIZE; r++) {
			for (int c = 0; c < SUDOKU_SIZE; c++) {
				Cell cell = mCells[r][c];
				if (cell.getValue() == 0 || !cell.isValid()) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Marks all filled cells (cells with value other than 0) as not editable.
	 */
	public void markFilledCellsAsNotEditable() {
		for (int r = 0; r < SUDOKU_SIZE; r++) {
			for (int c = 0; c < SUDOKU_SIZE; c++) {
				Cell cell = mCells[r][c];
				cell.setEditable(cell.getValue() == 0);
			}
		}
	}


	/**
	 * Returns how many times each value is used in CellCollection.
	 * Returns map with entry for each value.
	 */
	public Map<Integer, Integer> getValuesUseCount() {
		Map<Integer, Integer> valuesUseCount = new HashMap<Integer, Integer>();
		for (int value = 1; value <= CellCollection.SUDOKU_SIZE; value++) {
			valuesUseCount.put(value, 0);
		}

		for (int r = 0; r < CellCollection.SUDOKU_SIZE; r++) {
			for (int c = 0; c < CellCollection.SUDOKU_SIZE; c++) {
				int value = getCell(r, c).getValue();
				if (value != 0) {
					valuesUseCount.put(value, valuesUseCount.get(value) + 1);
				}
			}
		}

		return valuesUseCount;
	}

	/**
	 * Initializes collection, initialization has two steps:
	 * 1) Groups of cells which must contain unique numbers are created.
	 * 2) Row and column index for each cell is set.
	 */
	private void initCollection() {
		mRows = new CellGroup[SUDOKU_SIZE];
		mColumns = new CellGroup[SUDOKU_SIZE];
		mSectors = new CellGroup[SUDOKU_SIZE];

		for (int i = 0; i < SUDOKU_SIZE; i++) {
			mRows[i] = new CellGroup();
			mColumns[i] = new CellGroup();
			mSectors[i] = new CellGroup();
		}

		for (int r = 0; r < SUDOKU_SIZE; r++) {
			for (int c = 0; c < SUDOKU_SIZE; c++) {
				Cell cell = mCells[r][c];

				cell.initCollection(this, r, c,
						mSectors[((c / 3) * 3) + (r / 3)],
						mRows[c],
						mColumns[r]
				);
			}
		}
	}

	/**
	 * Creates instance from given StringTokenizer
	 */
	public static CellCollection deserialize(StringTokenizer data) {
		Cell[][] cells = new Cell[SUDOKU_SIZE][SUDOKU_SIZE];

		int r = 0, c = 0;
		while (data.hasMoreTokens() && r < 9) {
			cells[r][c] = Cell.deserialize(data);
			c++;

			if (c == 9) {
				r++;
				c = 0;
			}
		}
		return new CellCollection(cells);
	}

	/**
	 * Creates instance from given string (string which has been
	 * created by #serialize(StringBuilder) or #serialize() method).
	 */
	public static CellCollection deserialize(String data) {
		String[] lines = data.split("\n");
		if (lines.length == 0) {
			throw new IllegalArgumentException("Cannot deserialize Sudoku, data corrupted.");
		}

		if (lines[0].equals("version: 1")) {
			StringTokenizer st = new StringTokenizer(lines[1], "|");
			return deserialize(st);
		} else {
			return fromString(data);
		}
	}

	/**
	 * Creates collection instance from given string.
	 */
	public static CellCollection fromString(String data) {

		Cell[][] cells = new Cell[SUDOKU_SIZE][SUDOKU_SIZE];

		int pos = 0;
		for (int r = 0; r < CellCollection.SUDOKU_SIZE; r++) {
			for (int c = 0; c < CellCollection.SUDOKU_SIZE; c++) {
				int value = 0;
				while (pos < data.length()) {
					pos++;
					if (data.charAt(pos - 1) >= '0' && data.charAt(pos - 1) <= '9')
					{
						value = data.charAt(pos - 1) - '0';
						break;
					}
				}
				Cell cell = new Cell();
				cell.setValue(value);
				cell.setEditable(value == 0);
				cells[r][c] = cell;
			}
		}

		return new CellCollection(cells);
	}

	public String serialize() {
		StringBuilder sb = new StringBuilder();
		serialize(sb);
		return sb.toString();
	}

	/**
	 * Writes collection to given StringBuilder. You can later recreate the object instance
	 * by calling #deserialize(String) method.
	 */
	public void serialize(StringBuilder data) {
		data.append("version: 1\n");

		for (int r = 0; r < SUDOKU_SIZE; r++) {
			for (int c = 0; c < SUDOKU_SIZE; c++) {
				Cell cell = mCells[r][c];
				cell.serialize(data);
			}
		}
	}

	public void addOnChangeListener(OnChangeListener listener) {
		if (listener == null) {
			throw new IllegalArgumentException("The listener is null.");
		}
		synchronized (mChangeListeners) {
			if (mChangeListeners.contains(listener)) {
				throw new IllegalStateException("Listener " + listener + "is already registered.");
			}
			mChangeListeners.add(listener);
		}
	}

	/**
	 * Notify all registered listeners that something has changed.
	 */
	protected void onChange() {
		if (mOnChangeEnabled) {
			synchronized (mChangeListeners) {
				for (OnChangeListener l : mChangeListeners) {
					l.onChange();
				}
			}
		}
	}

	public interface OnChangeListener {
		/**
		 * Called when anything in the collection changes (cell's value, note, etc.)
		 */
		void onChange();
	}
}
