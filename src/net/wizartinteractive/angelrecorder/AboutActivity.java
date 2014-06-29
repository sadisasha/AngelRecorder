package net.wizartinteractive.angelrecorder;

import net.wizartinteractive.common.Utilities;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class AboutActivity extends Activity
{

	public static final String FRAGMENT_NAME = "ABOUT";

	public AboutActivity()
	{
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		getWindow().requestFeature(Window.FEATURE_PROGRESS);

		// setContentView(R.layout.activity_about);

		// WebView webView = (WebView) findViewById(R.id.about_webView);
		WebView webView = new WebView(this);
		setContentView(webView);

		webView.getSettings().setJavaScriptEnabled(true);

		final Activity activity = this;

		webView.setWebChromeClient(new WebChromeClient()
		{
			public void onProgressChanged(WebView view, int progress)
			{
				activity.setProgress(progress * 100);
				Utilities.logDebugMessage(FRAGMENT_NAME, String.format("Progress: %d", progress));
			}
		});

		webView.setWebViewClient(new WebViewClient()
		{
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
			{
				Toast.makeText(activity, String.format("ERROR: %s", description), Toast.LENGTH_SHORT).show();
			}
		});

		webView.loadUrl("http://diseno-web-df.com/angelRecord/");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		return false;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		return false;
	}

	@Override
	public void onStart()
	{
		super.onStart();
	}

	@Override
	public void onResume()
	{
		super.onResume();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
	}

	@Override
	public void onStop()
	{
		super.onStop();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}
}
