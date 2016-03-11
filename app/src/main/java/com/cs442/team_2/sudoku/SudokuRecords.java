package com.cs442.team_2.sudoku;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.cs442.team_2.sudoku.db.DatabaseHelper;
import com.cs442.team_2.sudoku.db.SudokuDatabase;
import com.cs442.team_2.sudoku.game.SudokuGame;


public class SudokuRecords extends ActionBarActivity {

    private TextView easyTextView;
    private TextView mediumTextView;
    private TextView hardTextView;
    private ListView easyListView;
    private ListView hardListView;
    private ListView mediumListView;
    private SimpleCursorAdapter easyAdapter;
    private SimpleCursorAdapter mediumAdapter;
    private SimpleCursorAdapter hardAdapter;

    private Cursor easyCursor;
    private Cursor mediumCursor;
    private Cursor hardCursor;

    private SudokuDatabase mDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sudoku_records);
        setTitle("Records");

        mDatabase = new SudokuDatabase(getApplicationContext());

        easyTextView = (TextView)findViewById(R.id.easy_title);
        mediumTextView = (TextView)findViewById(R.id.medium_title);
        hardTextView = (TextView)findViewById(R.id.hard_title);

        easyTextView.setTextSize(16);
        mediumTextView.setTextSize(16);
        hardTextView.setTextSize(16);

        easyTextView.setBackgroundColor(Color.DKGRAY);
        mediumTextView.setBackgroundColor(Color.DKGRAY);
        hardTextView.setBackgroundColor(Color.DKGRAY);


        easyListView = (ListView)findViewById(R.id.easy_records_list);
        hardListView = (ListView)findViewById(R.id.hard_records_list);
        mediumListView = (ListView)findViewById(R.id.medium_records_list);


        easyAdapter = new SimpleCursorAdapter(this, R.layout.record_list_item,
                null, new String[]{DatabaseHelper.SUDOKU_ID, DatabaseHelper.TIME},
                new int[]{ R.id.puzzle_id, R.id.complete_time});

        mediumAdapter = new SimpleCursorAdapter(this, R.layout.record_list_item,
                null, new String[]{DatabaseHelper.SUDOKU_ID, DatabaseHelper.TIME},
                new int[]{ R.id.puzzle_id, R.id.complete_time});

        hardAdapter = new SimpleCursorAdapter(this, R.layout.record_list_item,
                null, new String[]{DatabaseHelper.SUDOKU_ID, DatabaseHelper.TIME},
                new int[]{ R.id.puzzle_id, R.id.complete_time});

        easyAdapter.setViewBinder(new RecordListViewBinder(this));
        mediumAdapter.setViewBinder(new RecordListViewBinder(this));
        hardAdapter.setViewBinder(new RecordListViewBinder(this));

        updateList(easyCursor, easyAdapter, Long.valueOf("1"));
        updateList(mediumCursor,mediumAdapter,Long.valueOf("2"));
        updateList(hardCursor, hardAdapter, Long.valueOf("3"));

        easyListView.setAdapter(easyAdapter);
        mediumListView.setAdapter(mediumAdapter);
        hardListView.setAdapter(hardAdapter);

        easyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), SudokuPlayActivity.class);
                intent.putExtra(SudokuPlayActivity.EXTRA_SUDOKU_ID, id);
                startActivity(intent);
            }
        });

        mediumListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), SudokuPlayActivity.class);
                intent.putExtra(SudokuPlayActivity.EXTRA_SUDOKU_ID,id);
                startActivity(intent);
            }
        });

        hardListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), SudokuPlayActivity.class);
                intent.putExtra(SudokuPlayActivity.EXTRA_SUDOKU_ID,id);
                startActivity(intent);
            }
        });

        ListUtils.setDynamicHeight(easyListView);
        ListUtils.setDynamicHeight(mediumListView);
        ListUtils.setDynamicHeight(hardListView);



    }

    private void updateList(Cursor mCursor, SimpleCursorAdapter mAdapter,Long mFolderID) {
        if (mCursor != null) {
            stopManagingCursor(mCursor);
        }
        mCursor = mDatabase.getRecordList(mFolderID);
        startManagingCursor(mCursor);
        mAdapter.changeCursor(mCursor);
    }


    private static class RecordListViewBinder implements SimpleCursorAdapter.ViewBinder {
        private Context mContext;
        private GameTimeFormat mGameTimeFormatter = new GameTimeFormat();

        public RecordListViewBinder(Context context) {
            mContext = context;
        }

        public boolean setViewValue(View view, Cursor c, int columnIndex) {

            TextView label = null;

            switch (view.getId()) {

                case R.id.puzzle_id:
                    long id = c.getLong(c.getColumnIndex(DatabaseHelper.SUDOKU_ID));
                    label = ((TextView) view);
                    String nameString = "Puzzle: "+id;
                    label.setVisibility(nameString == null ? View.GONE
                            : View.VISIBLE);
                    label.setText(nameString);
                    label.setTextColor(Color.rgb(0, 0, 0));
                    break;
                case R.id.complete_time:
                    long time = c.getLong(columnIndex);
                    label = ((TextView) view);
                    String timeString = null;
                    if (time != 0) {
                        timeString = mGameTimeFormatter.format(time);
                    }
                    label.setVisibility(timeString == null ? View.GONE
                            : View.VISIBLE);
                    label.setText(timeString);
                    label.setTextColor(Color.rgb(0, 0, 0));
                    break;
            }
            return true;
        }
    }

    public static class ListUtils {
        public static void setDynamicHeight(ListView mListView) {
            ListAdapter mListAdapter = mListView.getAdapter();
            if (mListAdapter == null) {
                // when adapter is null
                return;
            }
            int height = 0;
            int desiredWidth = View.MeasureSpec.makeMeasureSpec(mListView.getWidth(), View.MeasureSpec.UNSPECIFIED);
            for (int i = 0; i < mListAdapter.getCount(); i++) {
                View listItem = mListAdapter.getView(i, null, mListView);
                listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
                height += listItem.getMeasuredHeight();
            }
            ViewGroup.LayoutParams params = mListView.getLayoutParams();
            params.height = height + (mListView.getDividerHeight() * (mListAdapter.getCount()+4));
            mListView.setLayoutParams(params);
            mListView.requestLayout();
        }
    }

}
