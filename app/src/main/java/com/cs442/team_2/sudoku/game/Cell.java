package com.cs442.team_2.sudoku.game;

import java.util.StringTokenizer;

/**
 * Sudoku cell. Every cell has value, some notes attached to it and some basic
 * state (whether it is editable and valid).
 */
public class Cell
{
	private CellCollection mCellCollection;
	private final Object mCellCollectionLock = new Object();
	private int mRowIndex = -1;
	private int mColumnIndex = -1;
	private CellGroup mSector; // sector containing this cell
	private CellGroup mRow; // row containing this cell
	private CellGroup mColumn; // column containing this cell

	private int mValue;
	private CellNote mNote;
	private boolean mEditable;
	private boolean mValid;

	/**
	 * Creates empty editable cell.
	 */
	public Cell() {
		this(0, new CellNote(), true, true);
	}

	/**
	 * Creates empty editable cell containing given value.
	 */
	public Cell(int value) {
		this(value, new CellNote(), true, true);
	}

	private Cell(int value, CellNote note, boolean editable, boolean valid) {
		if (value < 0 || value > 9) {
			throw new IllegalArgumentException("Value must be between 0-9.");
		}

		mValue = value;
		mNote = note;
		mEditable = editable;
		mValid = valid;
	}

	/**
	 * Gets cell's row index within CellCollection.
	 */
	public int getRowIndex() {
		return mRowIndex;
	}

	/**
	 * Gets cell's column index within CellCollection.
	 */
	public int getColumnIndex() {
		return mColumnIndex;
	}

	/**
	 * Called when Cell is added to CellCollection.
	 */
	protected void initCollection(CellCollection cellCollection, int rowIndex, int colIndex,
								  CellGroup sector, CellGroup row, CellGroup column) {
		synchronized (mCellCollectionLock) {
			mCellCollection = cellCollection;
		}

		mRowIndex = rowIndex;
		mColumnIndex = colIndex;
		mSector = sector;
		mRow = row;
		mColumn = column;
		sector.addCell(this);
		row.addCell(this);
		column.addCell(this);
	}

	/**
	 * Sets cell's value. Value can be 1-9 or 0 if cell should be empty.
	 */
	public void setValue(int value) {
		if (value < 0 || value > 9) {
			throw new IllegalArgumentException("Value must be between 0-9.");
		}
		mValue = value;
		onChange();
	}

	/**
	 * Gets cell's value. Value can be 1-9 or 0 if cell is empty.
	 */
	public int getValue() {
		return mValue;
	}


	/**
	 * Gets note attached to the cell.
	 */
	public CellNote getNote() {
		return mNote;
	}

	/**
	 * Sets note attached to the cell
	 */
	public void setNote(CellNote note) {
		mNote = note;
		onChange();
	}

	/**
	 * Returns whether cell can be edited.
	 */
	public boolean isEditable() {
		return mEditable;
	}

	/**
	 * Sets whether cell can be edited.
	 */
	public void setEditable(Boolean editable) {
		mEditable = editable;
		onChange();
	}

	/**
	 * Sets whether cell contains valid value according to sudoku rules.
	 */
	public void setValid(Boolean valid) {
		mValid = valid;
		onChange();
	}

	/**
	 * Returns true, if cell contains valid value according to sudoku rules.
	 */
	public boolean isValid() {
		return mValid;
	}


	/**
	 * Creates instance from given StringTokenizer.
	 */
	public static Cell deserialize(StringTokenizer data) {
		Cell cell = new Cell();
		cell.setValue(Integer.parseInt(data.nextToken()));
		cell.setNote(CellNote.deserialize(data.nextToken()));
		cell.setEditable(data.nextToken().equals("1"));

		return cell;
	}


	/**
	 * Appends string representation of this object to the given StringBuilder.
	 */
	public void serialize(StringBuilder data) {
		data.append(mValue).append("|");
		if (mNote == null || mNote.isEmpty()) {
			data.append("-").append("|");
		} else {
			mNote.serialize(data);
			data.append("|");
		}
		data.append(mEditable ? "1" : "0").append("|");
	}

	/**
	 * Notify CellCollection that something has changed.
	 */
	private void onChange() {
		synchronized (mCellCollectionLock) {
			if (mCellCollection != null) {
				mCellCollection.onChange();
			}

		}
	}
}
