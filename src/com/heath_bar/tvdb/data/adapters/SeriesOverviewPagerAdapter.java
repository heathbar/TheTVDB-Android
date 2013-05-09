/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.heath_bar.tvdb.data.adapters;

import java.util.ArrayList;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.heath_bar.tvdb.CastFragment;
import com.heath_bar.tvdb.EpisodeListFragment;
import com.heath_bar.tvdb.SummaryFragment;

/**
 * Implementation of {@link android.support.v4.view.PagerAdapter} that
 * uses a {@link Fragment} to manage each page. This class also handles
 * saving and restoring of fragment's state.
 *
 * <p>This version of the pager is more useful when there are a large number
 * of pages, working more like a list view.  When pages are not visible to
 * the user, their entire fragment may be destroyed, only keeping the saved
 * state of that fragment.  This allows the pager to hold on to much less
 * memory associated with each visited page as compared to
 * {@link FragmentPagerAdapter} at the cost of potentially more overhead when
 * switching between pages.
 *
 * <p>When using FragmentPagerAdapter the host ViewPager must have a
 * valid ID set.</p>
 *
 * <p>Subclasses only need to implement {@link #getItem(int)}
 * and {@link #getCount()} to have a working adapter.
 *
 * <p>Here is an example implementation of a pager containing fragments of
 * lists:
 *
 * {@sample development/samples/Support13Demos/src/com/example/android/supportv13/app/FragmentStatePagerSupport.java
 *      complete}
 *
 * <p>The <code>R.layout.fragment_pager</code> resource of the top-level fragment is:
 *
 * {@sample development/samples/Support13Demos/res/layout/fragment_pager.xml
 *      complete}
 *
 * <p>The <code>R.layout.fragment_pager_list</code> resource containing each
 * individual fragment's layout is:
 *
 * {@sample development/samples/Support13Demos/res/layout/fragment_pager_list.xml
 *      complete}
 */

public class SeriesOverviewPagerAdapter extends PagerAdapter implements ViewPager.OnPageChangeListener {

	
    private static final String TAG = "FragmentStatePagerAdapter";
    private static final boolean DEBUG = false;
    protected int NUM_FRAGMENTS = 3;
    
    private final FragmentManager mFragmentManager;
    private FragmentTransaction mCurTransaction = null;

    private ArrayList<Fragment.SavedState> mSavedState = new ArrayList<Fragment.SavedState>(NUM_FRAGMENTS);
    private ArrayList<Fragment> mFragments = new ArrayList<Fragment>(NUM_FRAGMENTS);
    private ArrayList<Boolean> mCommited = new ArrayList<Boolean>(NUM_FRAGMENTS);
    private Fragment mCurrentPrimaryItem = null;

    protected ArrayList<String> mTitles = new ArrayList<String>(NUM_FRAGMENTS);
	
	
	public SeriesOverviewPagerAdapter(FragmentManager fm) {
        mFragmentManager = fm;
        
    	mTitles.add(0, "Cast");
		mTitles.add(1, "Summary");
		mTitles.add(2, "Episodes");
	}
	
	public Fragment getItem(int index) {
	
		Fragment item = null;
		try {
			item = mFragments.get(index);
		} catch(IndexOutOfBoundsException e) { }
		
		if (item == null){
			switch (index){
			case 0:
				item = new CastFragment();
				break;
			case 1:
				item = new SummaryFragment();
				break;
			case 2: 
				item = new EpisodeListFragment();
				break;
			}

			while (mFragments.size() <= index){
	            mFragments.add(null);
	            mCommited.add(false);
			}
	        mFragments.set(index, item);
		}
		
                
		return item;
	}

	@Override
	public int getCount() {
		return NUM_FRAGMENTS;
	}

    public CharSequence getPageTitle(int index) {
    	try {
    		return mTitles.get(index);
    	}catch (IndexOutOfBoundsException e){
    		return "Series Overview";
    	}
    }
	
    public CastFragment getCastFragment(){
		return (CastFragment)getItem(0);
	}
    
    public SummaryFragment getSummaryFragment(){
		return (SummaryFragment)getItem(1);
	}
    
    public EpisodeListFragment getEpisodeListFragment(){
		return (EpisodeListFragment)getItem(2);
	}
	
    @Override
    public void startUpdate(ViewGroup container) {
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        // If we already have this item instantiated, there is nothing
        // to do.  This can happen when we are restoring the entire pager
        // from its saved state, where the fragment manager has already
        // taken care of restoring the fragments we previously had instantiated.
    	if (mCommited.size() > position && mCommited.get(position)){
    		Fragment f = mFragments.get(position); 
    		if (f != null){
    			return f;
    		}
    	}
//        if (mFragments.size() > position) {
//            Fragment f = mFragments.get(position);
//            if (f != null && mCommited.get(position)) {
//                return f;
//            }
//        }

        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction();
        }

        Fragment fragment = getItem(position);
        if (mSavedState.size() > position) {
            Fragment.SavedState fss = mSavedState.get(position);
            if (fss != null) {
                fragment.setInitialSavedState(fss);
            }
        }
        fragment.setMenuVisibility(false);
        fragment.setUserVisibleHint(false);
        mCurTransaction.add(container.getId(), fragment);
        mCommited.set(position, true);

        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        Fragment fragment = (Fragment)object;

        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction();
        }
        if (DEBUG) Log.v(TAG, "Removing item #" + position + ": f=" + object
                + " v=" + ((Fragment)object).getView());
        while (mSavedState.size() <= position) {
            mSavedState.add(null);
        }
        mSavedState.set(position, mFragmentManager.saveFragmentInstanceState(fragment));
        mFragments.set(position, null);

        mCurTransaction.remove(fragment);
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        Fragment fragment = (Fragment)object;
        if (fragment != mCurrentPrimaryItem) {
            if (mCurrentPrimaryItem != null) {
                mCurrentPrimaryItem.setMenuVisibility(false);
                mCurrentPrimaryItem.setUserVisibleHint(false);
            }
            if (fragment != null) {
                fragment.setMenuVisibility(true);
                fragment.setUserVisibleHint(true);
            }
            mCurrentPrimaryItem = fragment;
        }
    }

    @Override
    public void finishUpdate(ViewGroup container) {
        if (mCurTransaction != null) {
            mCurTransaction.commitAllowingStateLoss();
            mCurTransaction = null;
            mFragmentManager.executePendingTransactions();
        }
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return ((Fragment)object).getView() == view;
    }

    @Override
    public Parcelable saveState() {
        Bundle state = null;
        if (mSavedState.size() > 0) {
            state = new Bundle();
            Fragment.SavedState[] fss = new Fragment.SavedState[mSavedState.size()];
            mSavedState.toArray(fss);
            state.putParcelableArray("states", fss);
        }
        if (mCommited.size() > 0){
        	state = (state == null) ? new Bundle(): state;
        	boolean[] css = new boolean[mCommited.size()];
            for (int i=0; i<mCommited.size(); i++)
            	css[i] = (boolean)mCommited.get(i);
            state.putBooleanArray("commited", css);
        }
        for (int i=0; i<mFragments.size(); i++) {
            Fragment f = mFragments.get(i);
            if (f != null) {
            	state = (state == null) ? new Bundle(): state;
                String key = "f" + i;
                mFragmentManager.putFragment(state, key, f);
            }
        }
        return state;
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
        if (state != null) {
            Bundle bundle = (Bundle)state;
            bundle.setClassLoader(loader);
            Parcelable[] fss = bundle.getParcelableArray("states");
            mSavedState.clear();
            mCommited.clear();
            mFragments.clear();
            if (fss != null) {
                for (int i=0; i<fss.length; i++) {
                    mSavedState.add((Fragment.SavedState)fss[i]);
                }
            }
            boolean[] css = bundle.getBooleanArray("commited");
            if (css != null) {
            	for (int i=0; i<css.length; i++) {
            		mCommited.add(css[i]);
            	}
            }
            
            Iterable<String> keys = bundle.keySet();
            for (String key: keys) {
                if (key.startsWith("f")) {
                    int index = Integer.parseInt(key.substring(1));
                    Fragment f = mFragmentManager.getFragment(bundle, key);
                    if (f != null) {
                        while (mFragments.size() <= index) {
                            mFragments.add(null);
                        }
                        f.setMenuVisibility(false);
                        mFragments.set(index, f);
                    } else {
                        Log.w(TAG, "Bad fragment at key " + key);
                    }
                }
            }
        }
    }
    
    @Override  
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

    @Override
    public void onPageSelected(int position) { }

    @Override
    public void onPageScrollStateChanged(int state) { }
	
}
