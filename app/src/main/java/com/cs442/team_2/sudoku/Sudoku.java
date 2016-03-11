package com.cs442.team_2.sudoku;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.opengl.Visibility;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.cs442.team_2.sudoku.db.DatabaseHelper;
import com.cs442.team_2.sudoku.game.SudokuGame;

/**
 * Created by $hruthi on 10/29/15.
 */
public class Sudoku extends Activity implements OnClickListener {

	private static final String TAG = "Sudoku";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		// Set up click listeners for all the buttons
		View tutorialButton = findViewById(R.id.tutorial);
		tutorialButton.setOnClickListener(this);
		View newButton = findViewById(R.id.new_button);
		newButton.setOnClickListener(this);
		View aboutButton = findViewById(R.id.about_button);
		aboutButton.setOnClickListener(this);
		View exitButton = findViewById(R.id.exit_button);
		exitButton.setOnClickListener(this);
		View resumeButton = findViewById(R.id.resume);
		resumeButton.setOnClickListener(this);
		View upgradeButton = findViewById(R.id.upgrade);
		upgradeButton.setOnClickListener(this);
		View highButton = findViewById(R.id.highscores);
		highButton.setOnClickListener(this);
	}

	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.tutorial:
				startActivity(new Intent(this, TutorialActivity.class));
				break;
			case R.id.about_button:
				Intent i = new Intent(this, About.class);
				startActivity(i);
				break;
			case R.id.new_button:
				//startActivity(new Intent(this, FolderListActivity.class));
				showDialog();
				break;
			case R.id.exit_button:
				finish();
				break;
			case R.id.highscores:
				Intent intent = new Intent(this, SudokuRecords.class);
				startActivity(intent);
				break;
			case R.id.resume:
				SharedPreferences sp = getSharedPreferences("game", Context.MODE_PRIVATE);
				Boolean resume = sp.getBoolean("resume", false);
				if(resume) {
					Long gameId = sp.getLong("gameid", 0);
					Intent in = new Intent(this, SudokuPlayActivity.class);
					Bundle bundle = new Bundle();
					bundle.putLong("gameid", gameId);
					in.putExtra("bundle", bundle);
					startActivity(in);
				}
				else
					Toast.makeText(this, "No Game to Resume", Toast.LENGTH_LONG).show();
				break;
			case R.id.upgrade:
				SharedPreferences sharedPreferences = getSharedPreferences("upgrade", Context.MODE_PRIVATE);
				Boolean upgrade = sharedPreferences.getBoolean("upgrade",true);
				if(upgrade) {
					DatabaseHelper dh = new DatabaseHelper(this);
					if (dh.upgradePuzzles()) {
						Toast.makeText(this, "New Puzzles Loaded", Toast.LENGTH_LONG).show();
					}
					SharedPreferences.Editor editor = getSharedPreferences("upgrade", Context.MODE_PRIVATE).edit();
					editor.putBoolean("upgrade",false);
					editor.commit();
				}
				else
					Toast.makeText(this, "New Puzzles has Already been Loaded", Toast.LENGTH_LONG).show();
				break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.settings:
			startActivity(new Intent(this, Prefs.class));
			return true;
		}
		return false;
	}

	@Override
	protected void onPause() {
		super.onPause();
		Music.stop(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		/*View v = (View)findViewById(R.id.resume);
		v.setVisibility(View.GONE);*/
		Music.play(this, R.raw.main);
	}

	public void showDialog()
	{
		new AlertDialog.Builder(this)
				.setTitle(R.string.select_difficulty)
				.setItems(R.array.difficulty, new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialoginterface, int which) {
						Intent i = new Intent(getApplicationContext(), SudokuListActivity.class);
						Long id = new Long(which + 1);
						i.putExtra(SudokuListActivity.EXTRA_FOLDER_ID, id);
						startActivity(i);
					}
					}).show();
	}
}