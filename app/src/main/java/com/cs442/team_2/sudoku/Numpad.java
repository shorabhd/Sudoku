package com.cs442.team_2.sudoku;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.cs442.team_2.sudoku.game.Cell;
import com.cs442.team_2.sudoku.game.CellCollection;
import com.cs442.team_2.sudoku.game.CellCollection.OnChangeListener;
import com.cs442.team_2.sudoku.game.CellNote;
import com.cs442.team_2.sudoku.game.SudokuGame;
import com.cs442.team_2.sudoku.SudokuBoardView.OnCellSelectedListener;
import com.cs442.team_2.sudoku.SudokuBoardView.OnCellTappedListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Shorabh on 10-11-2015.
 */
public class Numpad extends LinearLayout
{
    private Context mContext;
    private SudokuBoardView mBoard;
    private SudokuGame mGame;

    public void pause()
    {
        onPause();
    }

    public void onPause()
    {

    }

    public Numpad(Context context)
    {
        super(context);
        mContext = context;
    }

    public Numpad(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    private boolean moveCellSelectionOnPress = true;
    private boolean mHighlightCompletedValues = true;

    private static final int MODE_EDIT_VALUE = 0;
    private static final int MODE_EDIT_NOTE = 1;

    private Cell mSelectedCell;
    private ImageButton mSwitchNumNoteButton;

    private int mEditMode = MODE_EDIT_VALUE;

    private Map<Integer, Button> mNumberButtons;

    public void initialize(SudokuBoardView board, SudokuGame game)
    {
        mBoard = board;
        mBoard.setOnCellTappedListener(mOnCellTapListener);
        mBoard.setOnCellSelectedListener(mOnCellSelected);
        mGame = game;
        mGame.getCells().addOnChangeListener(mOnCellsChangeListener);
    }

    protected void activate() {
        View controlPanel = createView();
        this.addView(controlPanel, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        controlPanel.setVisibility(View.VISIBLE);
        update();
        mSelectedCell = mBoard.getSelectedCell();
    }

    private OnChangeListener mOnCellsChangeListener = new OnChangeListener()
    {
        public void onChange()
        {
                update();
        }
    };

    private void update()
    {
        switch (mEditMode)
        {
            case MODE_EDIT_NOTE:
                mSwitchNumNoteButton.setImageResource(R.drawable.pencil);
                break;
            case MODE_EDIT_VALUE:
                mSwitchNumNoteButton.setImageResource(R.drawable.pencil_disabled);
                break;
        }

        Map<Integer, Integer> valuesUseCount = null;
        if (mHighlightCompletedValues)
        {
            valuesUseCount = mGame.getCells().getValuesUseCount();
            for (Map.Entry<Integer, Integer> entry : valuesUseCount.entrySet())
            {
                boolean highlightValue = entry.getValue() >= CellCollection.SUDOKU_SIZE;
                Button b = mNumberButtons.get(entry.getKey());
                if (highlightValue) {
                    b.setBackgroundResource(R.drawable.btn_completed_bg);
                } else {
                    b.setBackgroundResource(R.drawable.btn_default_bg);
                }
            }
        }
    }

    public boolean isMoveCellSelectionOnPress()
    {
        return moveCellSelectionOnPress;
    }

    public void setMoveCellSelectionOnPress(boolean moveCellSelectionOnPress)
    {
        this.moveCellSelectionOnPress = moveCellSelectionOnPress;
    }

    public void setHighlightCompletedValues(boolean highlightCompletedValues)
    {
        mHighlightCompletedValues = highlightCompletedValues;
    }

    protected View createView()
    {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View controlPanel = inflater.inflate(R.layout.im_numpad, null);

        mNumberButtons = new HashMap<Integer, Button>();
        mNumberButtons.put(1, (Button) controlPanel.findViewById(R.id.button_1));
        mNumberButtons.put(2, (Button) controlPanel.findViewById(R.id.button_2));
        mNumberButtons.put(3, (Button) controlPanel.findViewById(R.id.button_3));
        mNumberButtons.put(4, (Button) controlPanel.findViewById(R.id.button_4));
        mNumberButtons.put(5, (Button) controlPanel.findViewById(R.id.button_5));
        mNumberButtons.put(6, (Button) controlPanel.findViewById(R.id.button_6));
        mNumberButtons.put(7, (Button) controlPanel.findViewById(R.id.button_7));
        mNumberButtons.put(8, (Button) controlPanel.findViewById(R.id.button_8));
        mNumberButtons.put(9, (Button) controlPanel.findViewById(R.id.button_9));
        mNumberButtons.put(0, (Button) controlPanel.findViewById(R.id.button_clear));

        for (Integer num : mNumberButtons.keySet()) {
            Button b = mNumberButtons.get(num);
            b.setTag(num);
            b.setOnClickListener(mNumberButtonClick);
        }

        mSwitchNumNoteButton = (ImageButton) controlPanel.findViewById(R.id.switch_num_note);
        mSwitchNumNoteButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                mEditMode = mEditMode == MODE_EDIT_VALUE ? MODE_EDIT_NOTE : MODE_EDIT_VALUE;
                update();
            }

        });

        return controlPanel;
    }

    private OnClickListener mNumberButtonClick = new OnClickListener() {

        public void onClick(View v) {
            int selNumber = (Integer) v.getTag();
            Cell selCell = mSelectedCell;

            if (selCell != null) {
                switch (mEditMode) {
                    case MODE_EDIT_NOTE:
                        if (selNumber == 0) {
                            mGame.setCellNote(selCell, CellNote.EMPTY);
                        } else if (selNumber > 0 && selNumber <= 9) {
                            mGame.setCellNote(selCell, selCell.getNote().toggleNumber(selNumber));
                        }
                        break;
                    case MODE_EDIT_VALUE:
                        if (selNumber >= 0 && selNumber <= 9) {
                            mGame.setCellValue(selCell, selNumber);
                            if (isMoveCellSelectionOnPress()) {
                                mBoard.moveCellSelectionRight();
                            }
                        }
                        break;
                }
            }
        }
    };
    private OnCellSelectedListener mOnCellSelected = new OnCellSelectedListener()
    {
        public void onCellSelected(Cell cell) {
            mSelectedCell = cell;
        }
    };

    private OnCellTappedListener mOnCellTapListener = new OnCellTappedListener()
    {
        public void onCellTapped(Cell cell) {
            mSelectedCell = cell;
        }
    };
}
