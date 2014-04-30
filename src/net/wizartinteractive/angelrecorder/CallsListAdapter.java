package net.wizartinteractive.angelrecorder;

import java.io.File;
import java.util.ArrayList;

import net.wizartinteractive.common.Utilities;
import net.wizartinteractive.dbmodels.Call;
import net.wizartinteractive.dbmodels.CallType;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * the array adapter for the events list
 * 
 * @author Streetwizard
 * 
 */
public class CallsListAdapter extends ArrayAdapter<Call>
{
	// application context
	private Context appContext = null;

	// a layout inflater to inflate the events list
	private LayoutInflater layoutInflater;

	public CallsListAdapter(Context context, int resource, ArrayList<Call> objects)
	{
		super(context, resource, objects);
		this.appContext = context;
		this.layoutInflater = (LayoutInflater) this.appContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder viewHolder;
		
		final Call tempItemList = getItem(position);

		// this check improves performance to only create the elements to fill the device screen
		if (convertView == null)
		{
			convertView = layoutInflater.inflate(R.layout.call_item, parent, false);

			viewHolder = new ViewHolder();
			viewHolder.contact = (ImageView) convertView.findViewById(R.id.contact_imageView);
			viewHolder.phoneNumber = (TextView) convertView.findViewById(R.id.phoneNumber_textView);
			viewHolder.phoneDirection = (ImageView) convertView.findViewById(R.id.phone_imageView);
			viewHolder.details = (TextView) convertView.findViewById(R.id.callDetails_textView);
			viewHolder.play = (ImageButton) convertView.findViewById(R.id.play_imageButton);
			viewHolder.play.setFocusable(false);
			viewHolder.play.setClickable(false);

			convertView.setTag(viewHolder);

			viewHolder.play.setOnClickListener(new OnClickListener()
			{
				public void onClick(View v)
				{
					Intent viewIntent = new Intent(Intent.ACTION_VIEW);
					File file = new File(tempItemList.getFilePath());

					viewIntent.setDataAndType(Uri.fromFile(file), "audio/*");

					appContext.startActivity(Intent.createChooser(viewIntent, null));
				}
			});
		}
		else
		{
			viewHolder = (ViewHolder) convertView.getTag();
		}

		viewHolder.contact.setImageResource(R.drawable.ic_launcher);
		viewHolder.phoneNumber.setText(tempItemList.getIncomingNumber());
		viewHolder.phoneDirection.setImageResource(tempItemList.getType() == CallType.INCOMING ? android.R.drawable.sym_call_incoming : android.R.drawable.sym_call_outgoing);
		viewHolder.details.setText(String.format("%s", Utilities.ConvertMilisecondsToHMS(tempItemList.getDuration())));

		viewHolder.play.setTag(tempItemList);

		return convertView;
	}

	private class ViewHolder
	{
		ImageView contact;
		TextView phoneNumber;
		ImageView phoneDirection;
		TextView details;
		ImageButton play;
	}
}
