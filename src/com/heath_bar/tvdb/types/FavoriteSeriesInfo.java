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

public class FavoriteSeriesInfo {

	public int _seriesId;
	public String _seriesName;
	public String _lastAired;
	public String _nextAired;
	
	public FavoriteSeriesInfo(int id, String name, String lastAired, String nextAired){
		_seriesId = id;
		_seriesName = name;
		_lastAired = lastAired;
		_nextAired = nextAired;
	}

	public int get_seriesId() {
		return _seriesId;
	}

	public void set_seriesId(int _seriesId) {
		this._seriesId = _seriesId;
	}

	public String get_seriesName() {
		return _seriesName;
	}

	public void set_seriesName(String _seriesName) {
		this._seriesName = _seriesName;
	}

	public String get_lastAired() {
		return _lastAired;
	}

	public void set_lastAired(String _lastAired) {
		this._lastAired = _lastAired;
	}

	public String get_nextAired() {
		return _nextAired;
	}

	public void set_nextAired(String _nextAired) {
		this._nextAired = _nextAired;
	}
	
		
}
