//
//  DeviceRegistrar.java
//
// Pushwoosh Push Notifications SDK
// www.pushwoosh.com
//
// MIT Licensed

package com.arellomobile.android.push;

import android.content.Context;
import android.util.Log;
import com.arellomobile.android.push.request.RequestHelper;
import com.arellomobile.android.push.utils.NetworkUtils;
import com.arellomobile.android.push.utils.PreferenceUtils;
import com.google.android.gcm.GCMRegistrar;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Register/unregister with the App server.
 */
public class DeviceRegistrar
{
	private static final String TAG = "DeviceRegistrar";

	private static final String REGISTER_PATH = "registerDevice";
	private static final String UNREGISTER_PATH = "unregisterDevice";

	static void registerWithServer(final Context context, final String deviceRegistrationID)
	{
		Log.w(TAG, "Try To Registered for pushes");
		NetworkUtils.NetworkResult res = null;
		Exception exception = new Exception();
		for (int i = 0; i < NetworkUtils.MAX_TRIES; ++i)
		{
			try
			{
				res = makeRequest(context, deviceRegistrationID, REGISTER_PATH);
				if (200 == res.getResultCode() || 103 == res.getResultCode())
				{
					GCMRegistrar.setRegisteredOnServer(context, true);
					PushEventsTransmitter.onRegistered(context, deviceRegistrationID);
					PreferenceUtils.setLastRegistration(context, new Date().getTime());
					Log.w(TAG, "Registered for pushes: " + deviceRegistrationID);
					return;
				}
			}
			catch (Exception e)
			{
				exception = e;
			}
		}

		PushEventsTransmitter.onRegisterError(context, "status code is " + res + "\n error: " + exception.getMessage());
		Log.e(TAG, "Registration error " + exception.getMessage(), exception);
	}

	static void unregisterWithServer(final Context context, final String deviceRegistrationID)
	{
		Log.w(TAG, "Try To Unregistered for pushes");
		GCMRegistrar.setRegisteredOnServer(context, false);

		NetworkUtils.NetworkResult res;
		Exception exception = new Exception();
		for (int i = 0; i < NetworkUtils.MAX_TRIES; ++i)
		{
			try
			{
				res = makeRequest(context, deviceRegistrationID, UNREGISTER_PATH);
				if (200 == res.getResultCode() || 104 == res.getResultCode())
				{
					PushEventsTransmitter.onUnregistered(context, deviceRegistrationID);
					Log.w(TAG, "Unregistered for pushes: " + deviceRegistrationID);
					PreferenceUtils.resetLastRegistration(context);
					return;
				}
			}
			catch (Exception e)
			{
				exception = e;
			}
		}

		PushEventsTransmitter.onUnregisteredError(context, exception.getMessage());
		Log.e(TAG, "Unregistration error " + exception.getMessage(), exception);
	}

	private static NetworkUtils.NetworkResult makeRequest(Context context, String deviceRegistrationID, String urlPath)
			throws Exception
	{
		Map<String, Object> data = new HashMap<String, Object>();

		data.putAll(RequestHelper
				.getRegistrationUnregistrationData(context, deviceRegistrationID, NetworkUtils.PUSH_VERSION));

		return NetworkUtils.makeRequest(data, NetworkUtils.BASE_URL + urlPath);
	}
}
