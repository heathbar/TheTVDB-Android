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

package com.heath_bar.tvdb;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;



public class RatingFragment extends DialogFragment {
	
	protected int initialRating = 5;
	protected int rating;
	protected String title;
	
	public void setTitle(String title){
		this.title = title;
	}
		
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    LayoutInflater inflater = getActivity().getLayoutInflater();
	    
	    // Inflate and set the layout for the dialog
	    // Pass null as the parent view because its going in the dialog layout
	    builder.setView(inflater.inflate(R.layout.rating_dialog, null));
    	builder.setTitle(title);
    	builder.setPositiveButton(R.string.rate, new DialogInterface.OnClickListener() {
        	@Override
             public void onClick(DialogInterface dialog, int id) {
                 mListener.onDialogPositiveClick(RatingFragment.this);
             }
         });
         builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mListener.onDialogNegativeClick(RatingFragment.this);
			}
		});

	    return builder.create();
	}
	
	
	@Override
	public void onStart() {
	    super.onStart();

		// set default value
	    rating = initialRating;
	    TextView value = (TextView)getDialog().findViewById(R.id.value);
		value.setText(String.valueOf(rating));
		
		Button minus = (Button)getDialog().findViewById(R.id.btn_minus);
		minus.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (rating > 0){
					rating--;
					TextView value = (TextView)getDialog().findViewById(R.id.value);
					value.setText(String.valueOf(rating));
				}
			}
		});
		
		Button plus = (Button)getDialog().findViewById(R.id.btn_plus);
		plus.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				if (rating < 10){
					rating++;
					TextView value = (TextView)getDialog().findViewById(R.id.value);
					value.setText(String.valueOf(rating));
				}
			}
		});
		
	   
	}

	/* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface NoticeDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }
    
    // Use this instance of the interface to deliver action events
    NoticeDialogListener mListener;
    
    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString() + " must implement NoticeDialogListener");
        }
    }

	public void setInitialValue(int rating) {
		initialRating = rating;		
	}
}
