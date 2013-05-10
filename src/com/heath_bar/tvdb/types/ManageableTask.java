package com.heath_bar.tvdb.types;

import android.os.AsyncTask;

/** This class defines the type of tasks that our TaskFragment can run */

public abstract class ManageableTask extends AsyncTask<TaskManagementFragment, Void, Object> {
	private int taskId;

	public int getTaskId() {
		return taskId;
	}
	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}
	
	protected abstract Object doInBackground(TaskManagementFragment... taskFragment);
	protected abstract void onPostExecute(Object o);
}