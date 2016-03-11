package com.cs442.team_2.sudoku;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;

/**
 * Created by haomei on 10/29/15.
 */

/**
 * This class implements a simple periodic timer.
 */
abstract class Timer
		extends Handler {

	/**
	 * Construct a periodic timer with a given tick interval.
	 */
	public Timer(long ival) {
		mTickInterval = ival;
		mIsRunning = false;
		mAccumTime = 0;
	}


	/**
	 * Start the timer.  step() will be called at regular intervals
	 * until it returns true; then done() will be called.
	 * Subclasses may override this to do their own setup; but they
	 * must then call super.start().
	 */
	public void start() {
		if (mIsRunning)
			return;

		mIsRunning = true;

		long now = SystemClock.uptimeMillis();

		// Start accumulating time again.
		mLastLogTime = now;

		// Schedule the first event at once.
		mNextTime = now;
		postAtTime(runner, mNextTime);
	}


	/**
	 * Stop the timer.  step() will not be called again until it is
	 * restarted.
	 * Subclasses may override this to do their own setup; but they
	 * must then call super.stop().
	 */
	public void stop() {
		if (mIsRunning) {
			mIsRunning = false;
			long now = SystemClock.uptimeMillis();
			mAccumTime += now - mLastLogTime;
			mLastLogTime = now;
		}
	}


	public final long getTime() {
		return mAccumTime;
	}

	/**
	 * Subclasses override this to handle a timer tick.
	 */
	protected abstract boolean step(int count, long time);


	/**
	 * Subclasses may override this to handle completion of a run.
	 */
	protected void done() {
	}


	/**
	 * Handle a step of the animation.
	 */
	private final Runnable runner = new Runnable() {

		public final void run() {
			if (mIsRunning) {
				long now = SystemClock.uptimeMillis();

				// Add up the time since the last step.
				mAccumTime += now - mLastLogTime;
				mLastLogTime = now;

				if (!step(mTickCount++, mAccumTime)) {
					// Schedule the next.  If we've got behind, schedule
					// it for a tick after now.  (Otherwise we'd end
					// up with a zillion events queued.)
					mNextTime += mTickInterval;
					if (mNextTime <= now)
						mNextTime += mTickInterval;
					postAtTime(runner, mNextTime);
				} else {
					mIsRunning = false;
					done();
				}
			}
		}

	};


	/**
	 * Save game state so that the user does not lose anything
	 * if the game process is killed while we are in the
	 * background.
	 */
	void saveState(Bundle outState) {
		// Accumulate all time up to now, so we know where we're saving.
		if (mIsRunning) {
			long now = SystemClock.uptimeMillis();
			mAccumTime += now - mLastLogTime;
			mLastLogTime = now;
		}

		outState.putLong("tickInterval", mTickInterval);
		outState.putBoolean("isRunning", mIsRunning);
		outState.putInt("tickCount", mTickCount);
		outState.putLong("accumTime", mAccumTime);
	}


	/**
	 * Restore our game state from the given Bundle.  If the saved
	 * state was running, we will continue running.
	 */
	boolean restoreState(Bundle map) {
		return restoreState(map, true);
	}


	/**
	 * Restore our game state from the given Bundle.
	 */
	boolean restoreState(Bundle map, boolean run) {
		mTickInterval = map.getLong("tickInterval");
		mIsRunning = map.getBoolean("isRunning");
		mTickCount = map.getInt("tickCount");
		mAccumTime = map.getLong("accumTime");
		mLastLogTime = SystemClock.uptimeMillis();

		// If we were running, restart if requested, else stop.
		if (mIsRunning) {
			if (run)
				start();
			else
				mIsRunning = false;
		}

		return true;
	}


	// The tick interval in ms.
	private long mTickInterval = 0;

	// true iff the timer is running.
	private boolean mIsRunning = false;

	// Number of times step() has been called.
	private int mTickCount;

	// Time at which to execute the next step.  We schedule each
	// step at this plus x ms; this gives us an even execution rate.
	private long mNextTime;

	// The accumulated time in ms for which this timer has been running.
	// Increments between start() and stop(); start(true) resets it.
	private long mAccumTime;

	// The time at which we last added to accumTime.
	private long mLastLogTime;
}

