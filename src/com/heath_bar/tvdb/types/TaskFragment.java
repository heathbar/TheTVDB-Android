package com.heath_bar.tvdb.types;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public class TaskFragment extends Fragment {

	TaskFragmentTask mTask;
	Object mResultData;
	int mId = -1;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		
		if (mTask != null)
			mTask.execute(this);
	}

	public void setTask(int id, TaskFragmentTask task){
		mId = id;
		mTask = task;
	}
	
	// This is called by the AsyncTask.
    public void updateProgress(int percent) { }

    // This is also called by the AsyncTask.
    public void taskFinished(Object resultData)
    {
        mTask = null;
        mResultData = resultData; 

        // Tell the activity that we are done.
        if (mListener != null)
        	mListener.onTaskFinished(mId, resultData);
	}
    
    public Object getResultData(){
    	return mResultData;
    }
    
    
    // Handle the callback to the activity
    public interface TaskFinishedListener
    {
        public void onTaskFinished(int taskId, Object resultData);
    }
    
    private static TaskFinishedListener sDummyListener = new TaskFinishedListener()
    {
        public void onTaskFinished(int taskId, Object resultData) { }
    };
    
    private TaskFinishedListener mListener = sDummyListener;

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        if (!(activity instanceof TaskFinishedListener))
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        mListener = (TaskFinishedListener) activity;
    }
    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = sDummyListener;
    }
    
}
