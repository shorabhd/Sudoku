package com.cs442.team_2.sudoku.game.command;

import com.cs442.team_2.sudoku.game.CellCollection;

public abstract class AbstractCellCommand extends AbstractCommand {

	private CellCollection mCells;

	protected CellCollection getCells() {
		return mCells;
	}

	protected void setCells(CellCollection mCells) {
		this.mCells = mCells;
	}

}
