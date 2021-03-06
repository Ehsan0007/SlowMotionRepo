package com.slowmotionbridging;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;
import com.facebook.react.modules.core.PermissionAwareActivity;
import com.facebook.react.modules.core.PermissionListener;

import android.app.Activity;
import android.content.Intent;

import java.util.Map;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

public class ToastModule extends ReactContextBaseJavaModule implements PermissionListener {

	private static ReactApplicationContext reactContext;
	private PermissionListener mPermissionListener;
	private static final String DURATION_SHORT_KEY = "SHORT";
	private static final String DURATION_LONG_KEY = "LONG";

	private static final int VIDEO_PICKER_REQUEST = 9876;
	private static final String E_ACTIVITY_DOES_NOT_EXIST = "E_ACTIVITY_DOES_NOT_EXIST";
	private static final String E_PICKER_CANCELLED = "E_PICKER_CANCELLED";
	private static final String E_FAILED_TO_SHOW_PICKER = "E_FAILED_TO_SHOW_PICKER";
	private static final String E_NO_IMAGE_DATA_FOUND = "E_NO_IMAGE_DATA_FOUND";
	private Promise mPickerPromise;

	ToastModule(ReactApplicationContext context) {
		super(context);
		reactContext = context;
		reactContext.addActivityEventListener(mActivityEventListener);
		reactContext.addActivityEventListener(mActivityEventListener);
	}

	@Override
	public String getName() {
		return "ToastExample";
	}

	@Override
	public Map<String, Object> getConstants() {
		final Map<String, Object> constants = new HashMap<>();
		constants.put(DURATION_SHORT_KEY, Toast.LENGTH_SHORT);
		constants.put(DURATION_LONG_KEY, Toast.LENGTH_LONG);
		return constants;
	}

	private final ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {

		@Override
		public void onActivityResult(Activity activity, int requestCode, int resultCode,
				Intent intent) {
			if (requestCode == VIDEO_PICKER_REQUEST) {
				if (mPickerPromise != null) {
					if (resultCode == Activity.RESULT_CANCELED) {
						mPickerPromise.reject(E_PICKER_CANCELLED, "Image picker was cancelled");
					}
					else if (resultCode == Activity.RESULT_OK) {
						String result = intent.getStringExtra("result");
						Uri uri = intent.getData();

						if (result == null) {
							mPickerPromise.reject(E_NO_IMAGE_DATA_FOUND, "No image data found");
						}
						else {
							mPickerPromise.resolve(result);
						}
					}

					mPickerPromise = null;
				}
			}
		}

	};

	@ReactMethod
	public void pickVideo(Promise promise) {
		PermissionAwareActivity activity = (PermissionAwareActivity) getCurrentActivity();
		if (activity == null) {
			promise.reject(E_ACTIVITY_DOES_NOT_EXIST, "Activity doesn't exist");
			return;
		}
		mPickerPromise = promise;

		if (activity != null) {
			try {
				checkPermission(activity);
			}
			catch (Exception e) {
				mPickerPromise.reject(E_FAILED_TO_SHOW_PICKER, e);
				mPickerPromise = null;
			}
			// Toast.makeText(getReactApplicationContext(), message, duration).show();
		}
	}

	String[] PERMISSIONS = {
			Manifest.permission.WRITE_EXTERNAL_STORAGE,
			Manifest.permission.READ_EXTERNAL_STORAGE,
			Manifest.permission.CAMERA,
			Manifest.permission.RECORD_AUDIO,
	};

	private void checkPermission(PermissionAwareActivity activity) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
					PackageManager.PERMISSION_GRANTED && activity.checkSelfPermission(
					Manifest.permission.READ_EXTERNAL_STORAGE) ==
					PackageManager.PERMISSION_GRANTED && activity.checkSelfPermission(
					Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
					activity.checkSelfPermission(Manifest.permission.RECORD_AUDIO) ==
							PackageManager.PERMISSION_GRANTED) {
				openActivity(getCurrentActivity());
			}
			else {
				activity.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
								Manifest.permission.READ_EXTERNAL_STORAGE,
								Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
						1992,this);
			}
		}
	}

	private void openActivity(Activity activity) {
		Intent intent = new Intent(activity, VideoRecorderActivity.class);
		activity.startActivityForResult(intent, VIDEO_PICKER_REQUEST);
	}

	@Override
	public boolean onRequestPermissionsResult(int requestCode, String[] permissions,
			int[] grantResults) {
		if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			pickVideo(mPickerPromise);
		}
		return true;
	}
}