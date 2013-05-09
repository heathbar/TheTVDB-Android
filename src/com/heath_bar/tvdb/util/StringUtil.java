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

public class StringUtil {

	public static String commafy(String pipeDelimitedString){
		if (pipeDelimitedString == null)
			return ""; 
		
		String[] list = pipeDelimitedString.split("[\\|]+");
		
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<list.length; i++){
			if (!list[i].equals("")){
				sb.append(list[i]);
				
				if (i+1 < list.length)
					sb.append(", ");
			}
		}
		return sb.toString();
	}
	
	public static String wordify(int number){
		switch (number){
		case 1: 
			return "ONE";
		case 2:
			return "TWO";
		case 3:
			return "THREE";
		case 4: 
			return "FOUR";
		case 5:
			return "FIVE";
		case 6:
			return "SIX";
		case 7: 
			return "SEVEN";
		case 8:
			return "EIGHT";
		case 9:
			return "NINE";
		case 10:
			return "TEN";
		default:
			return "";
		}
	}
}
