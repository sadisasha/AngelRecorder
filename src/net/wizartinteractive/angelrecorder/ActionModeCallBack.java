package net.wizartinteractive.angelrecorder;

import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

public class ActionModeCallBack implements ActionMode.Callback
{

	@Override
	public boolean onActionItemClicked(ActionMode arg0, MenuItem arg1)
	{
		return false;
	}

	@Override
	public boolean onCreateActionMode(ActionMode arg0, Menu arg1)
	{
		return false;
	}

	@Override
	public void onDestroyActionMode(ActionMode arg0)
	{

	}

	@Override
	public boolean onPrepareActionMode(ActionMode arg0, Menu arg1)
	{
		return false;
	}

}
