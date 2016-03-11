package com.cs442.team_2.sudoku.game.command;

import android.os.Bundle;

public abstract class AbstractCommand {

	public static AbstractCommand newInstance(String commandClass) {
		if (commandClass.equals(ClearAllNotesCommand.class.getSimpleName())) {
			return new ClearAllNotesCommand();
		} else if (commandClass.equals(EditCellNoteCommand.class.getSimpleName())) {
			return new EditCellNoteCommand();
		} else if (commandClass.equals(SetCellValueCommand.class.getSimpleName())) {
			return new SetCellValueCommand();
		} else {
			throw new IllegalArgumentException(String.format("Unknown command class '%s'.", commandClass));
		}
	}

	void saveState(Bundle outState) {
	}

	void restoreState(Bundle inState) {
	}

	public String getCommandClass() {
		return getClass().getSimpleName();
	}
	abstract void execute();
	abstract void undo();

}
