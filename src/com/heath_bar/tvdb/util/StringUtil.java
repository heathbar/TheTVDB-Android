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
}
