package com.xgil78.twitterexample.thread;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.xgil78.twitterexample.model.Tweet;
import com.xgil78.twitterexample.model.TweetSearchResponse;

public class TwitterSearchTask extends AsyncTask<URL, Void, Void>
{
	public interface ITwitterSearchTask{
		void onTwitterSearchFinished( ArrayList<Tweet> result );
	}
	
	private ITwitterSearchTask mListenter;
	private ArrayList<Tweet> mResult;

	private SimpleDateFormat mDF = new SimpleDateFormat( "EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH ); // Thu, 26 Jul 2012 09:26:16 +0000 
	
	public TwitterSearchTask( ITwitterSearchTask listener )
	{
		mListenter = listener;
	}
	
	@Override
	protected Void doInBackground( URL... params )
	{
		try
		{
			readTwitter( params[0].toString() );
		}
		catch (Exception e) { e.printStackTrace(); }
		
		return null;
	}

	@Override
	protected void onPostExecute(Void result)
	{
		super.onPostExecute(result);
		if( mListenter != null )
			mListenter.onTwitterSearchFinished( mResult );
	}
	
	
	private void readTwitter( String url )
	{
		InputStream source = retrieveStream( url );
		
		Gson gson = new Gson();
		Reader reader = new InputStreamReader(source);
		TweetSearchResponse response = gson.fromJson(reader, TweetSearchResponse.class);
		
		mResult = (ArrayList<Tweet>) response.results;
	
		try
		{
			// Fem parseig de les dates 
			for( Tweet tweet : mResult )
			{
				tweet.tweetDate = mDF.parse( tweet.createdAt );
//				Log.d("", "tweet = " + tweet.text );
			}
		}
		catch (Exception e){ e.printStackTrace(); }		
	}
	
	private InputStream retrieveStream(String url) 
	{
		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet getRequest = new HttpGet(url);
		
		try 
		{
			HttpResponse getResponse = client.execute(getRequest);
			final int statusCode = getResponse.getStatusLine().getStatusCode();
			
			if (statusCode != HttpStatus.SC_OK) 
			{
					Log.w(getClass().getSimpleName(),
							"Error " + statusCode + " for URL " + url);
					return null;
			}
			HttpEntity getResponseEntity = getResponse.getEntity();
			return getResponseEntity.getContent();
		}
		catch (IOException e) 
		{
				getRequest.abort();
				Log.w(getClass().getSimpleName(), "Error for URL " + url, e);
		}
		return null;
	}
	
}
