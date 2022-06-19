package edu.ucsd.cse110.zooseeker.event;

public interface SettingsUpdateListener {

	/**
	 * Called when the settings of the app changed for <b>key</b> to <b>value</b>
	 *
	 * @param key
	 * @param value
	 */
	public void onSettingsUpdate(String key, String value);

}
