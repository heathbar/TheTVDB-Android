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
	
	public static long parseTime(String timeString) {
		try{
			SimpleDateFormat format = new SimpleDateFormat("hh:mm aa Z");
			Date d = format.parse(timeString + " +0000");						// calculate from UTC so 8:00 PM renders 72000 seconds, regardless of timezone
			return d.getTime()/1000L;
		}catch (ParseException pe){
			return 0;
		}	
	}
	
	public static String toNiceString(String dateString){
		
		String today = DateUtil.toString(System.currentTimeMillis()/1000L);
		String tomorrow = DateUtil.toString(System.currentTimeMillis()/1000L + 86400);
		String yesterday = DateUtil.toString(System.currentTimeMillis()/1000L - 86400);

		if (dateString.equals(today))
			return "Today";
		else if (dateString.equals(tomorrow))
			return "Tomorrow";
		else if (dateString.equals(yesterday))
			return "Yesterday";
		else
			return dateString;
	
	}
}
