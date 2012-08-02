package com.heath_bar.tvdb.util;

public class StringUtil {

	public static String commafy(String actorListString){
		if (actorListString == null)
			return ""; 
		
		String[] list = actorListString.split("[\\|]+");
		
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
