package net.wizartinteractive.angelrecorder;

import java.lang.reflect.Field;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.view.Window;

public class MainActivity extends ActionBarActivity
{

	private static MainActivity mainActivity = null;

	private FragmentManager fragmentManager = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		this.mainActivity = this;

		try
		{
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");

			if (menuKeyField != null)
			{
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		setContentView(R.layout.activity_main);

		CallsListFragment callListFragment = new CallsListFragment();

		this.setMainFragmentContent(callListFragment);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{

		case R.id.action_settings:

			Intent preferencesIntent = new Intent(MainActivity.this, PreferencesActivity.class);
			this.startActivity(preferencesIntent);

			break;

		case R.id.action_about:

			Intent aboutIntent = new Intent(MainActivity.this, AboutActivity.class);
			this.startActivity(aboutIntent);

			break;

		default:

			return false;

		}

		return super.onOptionsItemSelected(item);
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

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();

		if (this.fragmentManager.getBackStackEntryCount() == 0)
		{
			this.finish();
		}
	}

	public static MainActivity getInstance()
	{
		return mainActivity;
	}

	private void initializeFragmentManager()
	{
		if (this.fragmentManager == null)
		{
			this.fragmentManager = this.getSupportFragmentManager();
		}
	}

	private void setMainFragmentContent(Fragment fragment)
	{
		this.initializeFragmentManager();

		FragmentTransaction transaction = this.fragmentManager.beginTransaction();
		transaction.replace(R.id.main_content_frame, fragment);
		transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		transaction.addToBackStack(fragment.getClass().getSimpleName());
		transaction.commit();
	}
}
