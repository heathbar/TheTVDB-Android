/*
│──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────│
│                                                  TERMS OF USE: MIT License                                                   │
│                                                  Copyright © 2012 Heath Paddock                                              │
├──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
│Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation    │ 
│files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,    │
│modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software│
│is furnished to do so, subject to the following conditions:                                                                   │
│                                                                                                                              │
│The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.│
│                                                                                                                              │
│THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE          │
│WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR         │
│COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,   │
│ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                         │
├──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
 */

package com.heath_bar.tvdb.types;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseArray;

public class TaskManagementFragment extends Fragment {

	SparseArray<ManageableTask> mTasks = new SparseArray<ManageableTask>();
	SparseArray<Object> mResultData = new SparseArray<Object>();
	boolean isCreated = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		
		for (int i=0; i<mTasks.size(); i++){
			ManageableTask t = mTasks.valueAt(i);
			if (t != null){
				t.execute(this);
			}
		}
		isCreated = true;
	}

	public void addTask(int id, ManageableTask task) throws IllegalArgumentException{
		if (mTasks.get(id) != null){
			//throw new IllegalArgumentException("There is already a task defined for that ID");
		}else{
			task.setTaskId(id);
			mTasks.put(id, task);
			
			if (isCreated){
				task.execute(this);
			}
		}
	}
	
    // This is also called by the AsyncTask.
    public void taskFinished(int id, Object resultData)
    {
        mTasks.delete(id);
        mResultData.put(id, resultData); 

        // Tell the activity that we are done.
        if (mListener != null)
        	mListener.onTaskFinished(id, resultData);
	}
    
    public Object getResultData(int id){
    	return mResultData.get(id);
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
