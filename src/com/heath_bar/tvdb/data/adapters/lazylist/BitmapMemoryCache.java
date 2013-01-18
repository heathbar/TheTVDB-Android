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
package com.heath_bar.tvdb.data.adapters.lazylist;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.graphics.Bitmap;

public class BitmapMemoryCache {

	private Map<String, Bitmap> cache = Collections.synchronizedMap(new LinkedHashMap<String, Bitmap>(25, 1.5f, true));
    private long current_size = 0;
    private long maxSize = 1; 

    /** Constructor: By default limit the cache to 25% of the heap */
    public BitmapMemoryCache(){
        maxSize = Runtime.getRuntime().maxMemory()/4;
    }
    
    /** Set the max size of the cache 
     * Note: This can be in bytes/bits/inches/light years. It all depends on what your getSize functions returns 
     * By default the maxSize is set to 25% of the heap (in bytes) */
    public void setMaxSize(long maxSize){
        this.maxSize = maxSize;
    }

    /** Retrieve a item from the cache */
    public Bitmap get(String id){
        try{
            if(!cache.containsKey(id))
                return null;
            else
            	return cache.get(id);
        }catch(NullPointerException e){
            e.printStackTrace();
            return null;
        }
    }

    /** Add an item to the cache */
    public void put(String id, Bitmap item){
        try{
        	// If the cache contains the key already, remove it's size, then re-add it
            if(cache.containsKey(id))
            	current_size -= getSize(cache.get(id));
                        
            cache.put(id, item);
            current_size += getSize(item);
            trimCache();
        }catch(Throwable th){
            th.printStackTrace();
        }
    }
    
    /** Trim the cache to maxSize */
    private void trimCache() {
        if(current_size > maxSize){

        	// Iterate over the cache; order by oldest accessed items first
            Iterator<Entry<String, Bitmap>> iter = cache.entrySet().iterator();  
            while(iter.hasNext()){
                Entry<String, Bitmap> entry = iter.next();
                current_size -= getSize(entry.getValue());
                iter.remove();
                if(current_size <= maxSize)
                    break;
            }
        }
    }
    
    protected long getSize(Bitmap item){
    	if (item != null)
    		return item.getRowBytes() * item.getHeight();
		else
			return 0;
    }

    /** Get the current size of the cache in MB. This is mostly for debugging */
    public long getSizeMB() {
		return current_size / 1024 / 1024;
	}
    
    /** Empty the cache */
    public void clear() {
        try{
            cache.clear();
            current_size = 0;
        }catch(NullPointerException ex){
            ex.printStackTrace();
        }
    }
   
}