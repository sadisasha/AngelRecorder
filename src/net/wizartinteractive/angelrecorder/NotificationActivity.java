package net.wizartinteractive.angelrecorder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextThemeWrapper;

public class NotificationActivity extends Activity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		String message = getIntent().getExtras().getString("message");

		ContextThemeWrapper themedContext;
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			themedContext = new ContextThemeWrapper(this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar);
		}
		else
		{
			themedContext = new ContextThemeWrapper(this, android.R.style.Theme_Light_NoTitleBar);
		}

		final AlertDialog alertDialog = new AlertDialog.Builder(themedContext).create();
		alertDialog.setTitle("Angel Recorder");
		alertDialog.setMessage(message);
		alertDialog.setIcon(R.drawable.ic_launcher);
		alertDialog.setCancelable(true);

		alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				alertDialog.dismiss();
				finish();
			}
		});

		alertDialog.show();
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
