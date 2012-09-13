package com.xgil78.twitterexample;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.xgil78.twitterexample.model.Tweet;
import com.xgil78.twitterexample.thread.TwitterSearchTask;
import com.xgil78.twitterexample.thread.TwitterSearchTask.ITwitterSearchTask;


public class Main extends Activity implements ITwitterSearchTask
{
	private static final int QUERY_TIMEOUT = 7000;
	
	private String mQuery = "http://search.twitter.com/search.json?q=xx_search_xx&rpp=50&result=recent";
	
	private String mSearchText;
	private HashMap<String, String> mSpecialChars;
	
	private TwitterSearchTask mTask;
	private Handler mHandler = new Handler();
	
	private EditText mEditText;
	private Button mButton;
	private ListView mListView;
	
	private ProgressDialog mProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView( R.layout.main_layout );
		
		this.getWindow().setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN );		
		
		mSpecialChars = new HashMap<String, String>();
		mSpecialChars.put( "@", "from:@" );
		mSpecialChars.put( "#", "%23" );
		
		mEditText = (EditText) findViewById( R.id.editText1 );
		mEditText.setText( "Google" );
		
		mListView = (ListView) findViewById( R.id.listView1 );
		mButton = (Button) findViewById( R.id.button1 );
		mButton.setOnClickListener( new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				mSearchText = mEditText.getEditableText().toString();

				String value = null;
				for( String key:mSpecialChars.keySet())
				{
					value = mSpecialChars.get( key );
					mSearchText = mSearchText.replace( key, value );
				}
				
				if( mSearchText.length() > 0 )
				{
					InputMethodManager imm = (InputMethodManager)getSystemService( Context.INPUT_METHOD_SERVICE );
					imm.hideSoftInputFromWindow( mEditText.getWindowToken(), 0 );
					
					String query = mQuery.replace( "xx_search_xx", mSearchText );
					readTweets( query );
				}
			}
		});
		
		mProgressDialog = new ProgressDialog( this );
		mProgressDialog.setProgressStyle( ProgressDialog.STYLE_SPINNER );
	}
	
	private void readTweets( String query )
	{
        try
		{
        	Log.d( "", "readTweets" );
        	if( query == null || query.length() == 0 )
        	{
        		Log.e( "", "TwitterFragment.readTweets: la query està buida!" );
        		return;
        	}
        	Log.d( "", "mQuery = " + query );
        	
        	mProgressDialog.show();
        	
    		URL url = new URL( query );
    		mTask = null;
    		mTask = new TwitterSearchTask( this );
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
			mProgressDialog.dismiss();
			Log.i( getClass().getName(), "Timeout | mTask.getStatus() = " + mTask.getStatus() );
			if( mTask.getStatus() == Status.PENDING || mTask.getStatus() == Status.RUNNING )
			{
				mTask.cancel( true );
				mTask = null;
			}
		}
	};

	private void stopTimeout()
	{
		mHandler.removeCallbacksAndMessages( mTimeout );
	}
	
	// ITwitterSearchTask
	
	@Override
	public void onTwitterSearchFinished(ArrayList<Tweet> result)
	{
		mProgressDialog.dismiss();
		
		stopTimeout();
		Log.d( getClass().getName(), "(onTwitterSearchFinished) result.size() = " + result.size() );		
		
		List<Spannable> values = new ArrayList<Spannable>();
		if( result.size() > 0 )
		{
			String searchText = mSearchText;
			for( Tweet tweet:result )
			{
				String value = "";
				for( String key:mSpecialChars.keySet() )
				{
					value = mSpecialChars.get( key );
					searchText = searchText.replace( value, key );
				}
				
				if( tweet.text.contains( searchText ))
				{
					int index = tweet.text.indexOf( searchText, 0 );
					Spannable spannable = new SpannableString( tweet.text );
					spannable.setSpan(new BackgroundColorSpan( getResources().getColor( R.color.Highlight ) ), index, index + searchText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					values.add( spannable );
				}
			}
			
			ArrayAdapter<Spannable> adapter = new ArrayAdapter<Spannable>( this, android.R.layout.simple_list_item_1, android.R.id.text1, values );
			mListView.setAdapter(adapter);
		}
	}
	
}
