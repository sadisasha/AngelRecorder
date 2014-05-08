package net.wizartinteractive.angelrecorder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

public class SplashScreenActivity extends Activity
{
	protected int time = 3000;

	private Thread splashScreenThread;

	private RelativeLayout splashImage;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_splash_screen);

		this.splashScreenThread = new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					synchronized (this)
					{
						wait(time);
					}
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				finally
				{
					launchActivity();
				}
			}
		};

		splashScreenThread.start();
	}

	private void launchActivity()
	{
		Intent mainActivityIntent = new Intent();

		mainActivityIntent.setClass(SplashScreenActivity.this, MainActivity.class);
		startActivity(mainActivityIntent);

		this.finish();
	}

	@Override
	protected void onStart()
	{
		super.onStart();
	}

	@Override
	protected void onRestart()
	{
		super.onRestart();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
	}

	@Override
	protected void onStop()
	{
		super.onStop();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}
}
