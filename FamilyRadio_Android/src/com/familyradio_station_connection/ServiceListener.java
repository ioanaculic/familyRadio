package com.familyradio_station_connection;

import android.net.nsd.NsdServiceInfo;

public interface ServiceListener {
	
	public void serviceFound (NsdServiceInfo info);
	
}
