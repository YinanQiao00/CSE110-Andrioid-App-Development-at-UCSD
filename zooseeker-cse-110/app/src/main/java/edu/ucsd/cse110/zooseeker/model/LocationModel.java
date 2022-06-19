package edu.ucsd.cse110.zooseeker.model;

import android.annotation.SuppressLint;
import android.app.Application;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class LocationModel extends AndroidViewModel {

	private final MediatorLiveData<LatLng> position;

	private MutableLiveData<LatLng> locationProvider = null;
	private MutableLiveData<LatLng> mockProvider = null;

	public LocationModel(@NonNull Application application) {
		super(application);
		position = new MediatorLiveData<>();

		mockProvider = new MutableLiveData<>();
		position.addSource(mockProvider, position::setValue);
	}

	public LiveData<LatLng> getPositionLiveData() {return position;}

	@SuppressLint("MissingPermission")
	public void setLocationProvider(LocationManager locationManager, String providerType) {
		if (locationProvider != null) {
			removeLocationProvider();
		}

		locationProvider = new MutableLiveData<LatLng>();
		var locationListener = new LocationListener() {
			@Override
			public void onLocationChanged(@NonNull Location location) {
				locationProvider.postValue(new LatLng(location.getLatitude(),location.getLongitude()));
			}
		};
		locationManager.requestLocationUpdates(providerType, 500, 2, locationListener);

		position.addSource(locationProvider, position::setValue);
	}

	void removeLocationProvider() {
		if (locationProvider == null) return;
		position.removeSource(locationProvider);
		locationProvider = null;
	}

	public void mockLocation(LatLng pos)
	{
		mockProvider.postValue(pos);
	}

	public void mockLocations(List<LatLng> route, long delay, TimeUnit timeUnit)
	{
		int i = 0;
		for(LatLng pos : route)
		{
			Log.i("LocationModel", String.format("Model mocking route (%d / %d): %s", i++, route.size(), pos.toString()));
			mockProvider.postValue(pos);

			try {
				Thread.sleep(timeUnit.toMillis(delay));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
