package com.heath_bar.tvdb.types;

import android.os.AsyncTask;

/** This class defines the type of tasks that our TaskFragment can run */

public abstract class TaskFragmentTask extends AsyncTask<TaskFragment, Void, Object> {
	protected abstract Object doInBackground(TaskFragment... taskFragment);
	protected abstract void onPostExecute(Object o);
}