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
