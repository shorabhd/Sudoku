package com.cs442.team_2.sudoku.game.command;

import android.os.Bundle;
import java.util.Stack;

import com.cs442.team_2.sudoku.game.CellCollection;

public class CommandStack {
	private Stack<AbstractCommand> mCommandStack = new Stack<AbstractCommand>();

	private CellCollection mCells;
	public CommandStack(CellCollection cells) {
		mCells = cells;
	}

	public void saveState(Bundle outState) {
		outState.putInt("cmdStack.size", mCommandStack.size());
		for (int i = 0; i < mCommandStack.size(); i++) {
			AbstractCommand command = mCommandStack.get(i);
			Bundle commandState = new Bundle();
			commandState.putString("commandClass", command.getCommandClass());
			command.saveState(commandState);
			outState.putBundle("cmdStack." + i, commandState);
		}
	}

	public void restoreState(Bundle inState) {
		int stackSize = inState.getInt("cmdStack.size");
		for (int i = 0; i < stackSize; i++) {
			Bundle commandState = inState.getBundle("cmdStack." + i);
			AbstractCommand command = AbstractCommand.newInstance(commandState.getString("commandClass"));
			command.restoreState(commandState);
			push(command);
		}
	}

	public void execute(AbstractCommand command) {
		push(command);
		command.execute();
	}

	public void undo() {
		if (!mCommandStack.empty()) {
			AbstractCommand c = pop();
			c.undo();
			validateCells();
		}
	}

	public boolean hasSomethingToUndo() {
		return mCommandStack.size() != 0;
	}

	private void push(AbstractCommand command) {
		if (command instanceof AbstractCellCommand) {
			((AbstractCellCommand) command).setCells(mCells);
		}
		mCommandStack.push(command);
	}

	private AbstractCommand pop() {
		return mCommandStack.pop();
	}

	private void validateCells() {
		mCells.validate();
	}
}
