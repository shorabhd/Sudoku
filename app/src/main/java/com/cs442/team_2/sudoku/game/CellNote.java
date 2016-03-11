package com.cs442.team_2.sudoku.game;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
/**
 * Created by Shorabh on 10-11-2015.
 */
public class CellNote {
	private final Set<Integer> mNotedNumbers;

	public static final CellNote EMPTY = new CellNote();

	public CellNote() {
		mNotedNumbers = Collections.unmodifiableSet(new HashSet<Integer>());
	}

	private CellNote(Set<Integer> notedNumbers) {
		mNotedNumbers = Collections.unmodifiableSet(notedNumbers);
	}

	/**
	 * Creates instance from given string (string which has been
	 * created by #serialize(StringBuilder) or #serialize() method).
	 */
	public static CellNote deserialize(String note) {

		Set<Integer> notedNumbers = new HashSet<Integer>();
		if (note != null && !note.equals("")) {
			StringTokenizer tokenizer = new StringTokenizer(note, ",");
			while (tokenizer.hasMoreTokens()) {
				String value = tokenizer.nextToken();
				if (!value.equals("-")) {
					notedNumbers.add(Integer.parseInt(value));
				}
			}
		}

		return new CellNote(notedNumbers);
	}


	/**
	 * Appends string representation of this object to the given StringBuilder.
	 */
	public void serialize(StringBuilder data) {
		if (mNotedNumbers.size() == 0) {
			data.append("-");
		} else {
			for (Integer num : mNotedNumbers) {
				data.append(num).append(",");
			}
		}
	}

	public String serialize() {
		StringBuilder sb = new StringBuilder();
		serialize(sb);
		return sb.toString();
	}

	/**
	 * Returns numbers currently noted in cell.
	 */
	public Set<Integer> getNotedNumbers() {
		return mNotedNumbers;
	}

	/**
	 * Toggles noted number: if number is already noted, it will be removed otherwise it will be added.
	 */
	public CellNote toggleNumber(int number) {
		if (number < 1 || number > 9)
			throw new IllegalArgumentException("Number must be between 1-9.");

		Set<Integer> notedNumbers = new HashSet<Integer>(getNotedNumbers());
		if (notedNumbers.contains(number)) {
			notedNumbers.remove(number);
		} else {
			notedNumbers.add(number);
		}

		return new CellNote(notedNumbers);
	}

	public boolean isEmpty() {
		return mNotedNumbers.size() == 0;
	}

}
