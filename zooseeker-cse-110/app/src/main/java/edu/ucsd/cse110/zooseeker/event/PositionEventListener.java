package edu.ucsd.cse110.zooseeker.event;

import com.google.android.gms.maps.model.LatLng;

public interface PositionEventListener {

	/**
	 * Called whenever observers should be updated based on newPos
	 * (Could be called using the same newPos to ping the observer)
	 *
	 * @param newPos
	 */
	public void positionUpdate(LatLng newPos);

}
