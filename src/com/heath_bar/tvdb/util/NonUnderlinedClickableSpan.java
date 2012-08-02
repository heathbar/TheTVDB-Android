package com.heath_bar.tvdb.util;

import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

public class NonUnderlinedClickableSpan extends ClickableSpan 
{
	@Override
    public void updateDrawState(TextPaint ds) {
       ds.setColor(ds.linkColor);
       ds.setUnderlineText(false); // set to false to remove underline
    }

	protected String tag;
	public NonUnderlinedClickableSpan(String tag){
		this.tag = tag;
	}
	
	public NonUnderlinedClickableSpan(){
		super();
	}

	
	
	@Override
	public void onClick(View widget) {
		
	}
}