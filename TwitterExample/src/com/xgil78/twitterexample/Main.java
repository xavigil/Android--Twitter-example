package com.xgil78.twitterexample;

import java.net.URL;
import java.util.ArrayList;

import android.app.Activity;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import com.xgil78.twitterexample.model.Tweet;
import com.xgil78.twitterexample.thread.TwitterSearchTask;
import com.xgil78.twitterexample.thread.TwitterSearchTask.ITwitterSearchTask;


public class Main extends Activity implements ITwitterSearchTask
{
	private static final int QUERY_TIMEOUT = 5000;
	
	private String mQuery = "http://search.twitter.com/search.json?q=from:@equipviari&rpp=50&result=recent";
	private TwitterSearchTask mTask = new TwitterSearchTask( this ) ;
	private Handler mHandler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView( R.layout.main_layout );
		readTweets();
	}
	
	private void readTweets()
	{
        try
		{
        	Log.d( "", "readTweets" );
        	if( mQuery == null || mQuery.length() == 0 )
        	{
        		Log.e( "", "TwitterFragment.readTweets: la query està buida!" );
        		return;
        	}
        	Log.d( "", "mQuery = " + mQuery );
        	
    		URL url = new URL( mQuery );
    		mTask.execute( url );
    		
        	mHandler.removeCallbacksAndMessages( mTimeout );
        	mHandler.postAtTime( mTimeout, mTimeout, SystemClock.uptimeMillis() + QUERY_TIMEOUT );    		
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}		
	}

	private Runnable mTimeout = new Runnable()
	{
		@Override
		public void run()
		{
			if( mTask.getStatus() == Status.PENDING || mTask.getStatus() == Status.RUNNING )
			{
				mTask.cancel( true );
				mTask = null;
			}
		}
	};
	
	// ITwitterSearchTask
	
	@Override
	public void onTwitterSearchFinished(ArrayList<Tweet> result)
	{
		for( Tweet t:result )
		{
			Log.d( "", t.text );
		}
	}
	
}
