package com.heath_bar.tvdb.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

	
	public static Date parseDate(String dateString){
		try {
			if (dateString.equals(""))
				return new Date(0);
			else
				return parseDate(dateString, "yyyy-MM-dd");
		} catch (IllegalArgumentException e){
			return new Date(0);
		}
		
	}
	public static Date parseDate(String dateString, String dateFormat){
		try {
		    SimpleDateFormat format = new SimpleDateFormat(dateFormat);
		    return format.parse(dateString);
		}
		catch(ParseException pe) {
		    throw new IllegalArgumentException();
		}
	}
	
	public static String toString(long date){
		Date d = new Date(date*1000L);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		return format.format(d);
	}
}
