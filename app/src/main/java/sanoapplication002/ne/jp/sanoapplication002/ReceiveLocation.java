package sanoapplication002.ne.jp.sanoapplication002;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

	public class ReceiveLocation extends BroadcastReceiver {
	    @Override
	    public void onReceive(Context context, Intent intent)
	    {
	    	//Do this when the system sends the intent
	    	if(intent.hasExtra(LocationManager.KEY_LOCATION_CHANGED))
	    	{	    

	    		Toast.makeText(context, "測位完了", Toast.LENGTH_SHORT).show(); 
	    	}
	    }
	}
