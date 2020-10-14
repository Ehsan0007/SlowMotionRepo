package com.slowmotionbridging;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.trinity.OnRecordingListener;
import com.trinity.camera.CameraCallback;
import com.trinity.camera.TrinityPreviewView;
import com.trinity.listener.OnRenderListener;
import com.trinity.record.PreviewResolution;
import com.trinity.record.Speed;
import com.trinity.record.TrinityRecord;
import com.trinity.record.exception.InitRecorderFailException;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class VideoRecorderActivity extends AppCompatActivity
		implements OnRecordingListener, OnRenderListener, CameraCallback {

	private TrinityRecord mRecord;
	File newFile;
	VideoView videoView;
	TextView timer;
	ProgressBar progress;
	RelativeLayout previewContainer;
	RelativeLayout videoViewContainer;
	MediaController mediaController;
	ImageView start;
	ImageView stop;
	ImageView tick;
	ImageView cross;
	ImageView back;
	TrinityPreviewView preview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main2);
		videoView = findViewById(R.id.videoView);
		timer = findViewById(R.id.timer);
		progress = findViewById(R.id.progressBar);
		previewContainer = findViewById(R.id.previewContainer);
		videoViewContainer = findViewById(R.id.videoViewContainer);
		start = findViewById(R.id.start);
		stop = findViewById(R.id.stop);
		tick = findViewById(R.id.accept);
		cross = findViewById(R.id.cancel);
		back = findViewById(R.id.back);

		findViewById(R.id.accept).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//                Toast.makeText(getApplicationContext(), "" + newFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
				Intent returnIntent = new Intent();
				returnIntent.putExtra("result", newFile.getAbsolutePath());
				setResult(Activity.RESULT_OK, returnIntent);
				finish();
			}
		});

		findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				restartRecording();
			}
		});

		findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent returnIntent = new Intent();
				setResult(Activity.RESULT_CANCELED, returnIntent);
				finish();
			}
		});

		findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				start.setVisibility(View.GONE);
				stop.setVisibility(View.VISIBLE);
				back.setVisibility(View.GONE);
				String date = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
				String path = getExternalCacheDir().getAbsolutePath() + "/VID_" + date + ".mp4";

				try {
					File storageDir = getApplicationContext().getExternalFilesDir(
							Environment.DIRECTORY_MOVIES);
					// newFile = File.createTempFile("videocapture", ".3gp", storageDir);
					newFile = File.createTempFile("videocapture", ".mp4", storageDir);
					try {
						Log.e("FilePath==> ", newFile.getAbsolutePath());
						mRecord.setSpeed(Speed.VERY_SLOW);
						mRecord.startRecording(newFile.getAbsolutePath(), 720, 1280, 2000, 30,
								false, 44100, 1, 128, 60000);
					}
					catch (InitRecorderFailException e) {
						Log.e("Excetion => ", e.toString());
						e.printStackTrace();
					}
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mRecord.stopRecording();
				progress.setVisibility(View.VISIBLE);
				videoViewContainer.setVisibility(View.VISIBLE);
				videoView.setVisibility(View.VISIBLE);
				previewContainer.setVisibility(View.GONE);
				preview.setVisibility(View.GONE);
				new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
					@Override
					public void run() {
						tick.setVisibility(View.VISIBLE);
						cross.setVisibility(View.VISIBLE);
						videoView.setVideoURI(Uri.parse(newFile.getAbsolutePath()));
						mediaController.setAnchorView(videoView);
						videoView.setMediaController(mediaController);
						videoView.seekTo(1);
						progress.setVisibility(View.GONE);
						videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
							@Override
							public void onPrepared(MediaPlayer mp) {
								mediaController.show(0);
							}
						});
					}
				}, 4000);

			}
		});
	}

	private void restartRecording() {
		videoView.stopPlayback();
		timer.setText("");
		videoViewContainer.setVisibility(View.GONE);
		videoView.setVisibility(View.GONE);
		previewContainer.setVisibility(View.VISIBLE);
		preview.setVisibility(View.VISIBLE);
		tick.setVisibility(View.GONE);
		cross.setVisibility(View.GONE);
		start.setVisibility(View.VISIBLE);
		stop.setVisibility(View.GONE);
		back.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onResume() {
		super.onResume();
		checkPermission();
	}

	private void checkPermission() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
					PackageManager.PERMISSION_GRANTED && checkSelfPermission(
					Manifest.permission.READ_EXTERNAL_STORAGE) ==
					PackageManager.PERMISSION_GRANTED && checkSelfPermission(
					Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
					checkSelfPermission(Manifest.permission.RECORD_AUDIO) ==
							PackageManager.PERMISSION_GRANTED) {
				initializeCamera();

			}
			else {
				ActivityCompat.requestPermissions(this,
						new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
								Manifest.permission.READ_EXTERNAL_STORAGE,
								Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
						1992);
			}
		}
	}

	private void initializeCamera() {
		mediaController = new MediaController(this);
		preview = findViewById(R.id.preview);
		mRecord = new TrinityRecord(this, preview);
		mRecord.setOnRenderListener(this);
		mRecord.setOnRecordingListener(this);
		mRecord.setCameraCallback(this);
		mRecord.startPreview(PreviewResolution.RESOLUTION_1280x720);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
			@NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			initializeCamera();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mRecord.release();
		mRecord.stopPreview();
	}

	@Override
	public void onRecording(final int i, int i1) {
		Log.e("onRecording", i + "onRecording " + i1);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				long seconds = i / 1000 % 60;
				long minutes = i / 60000;
				timer.setText(getTimerWithZero(minutes) + ":" + getTimerWithZero(seconds));
			}
		});

	}

	public String getTimerWithZero(long time) {
		String value;
		if (time < 10) {
			value = "0" + time;
		}
		else {
			value = String.valueOf(time);
		}

		return value;
	}

	@Override
	public void dispatchOnFocusEnd(boolean b, PointF pointF) {

	}

	@Override
	public void dispatchOnFocusStart(PointF pointF) {

	}

	@Override
	public void dispatchOnPreviewCallback(byte[] bytes, int i, int i1, int i2) {

	}

	@Override
	public int onDrawFrame(int i, int i1, int i2, float[] floats) {
		return 0;
	}

	@Override
	public void onSurfaceCreated() {
		Log.e("onSurfaceCreated", "onSurfaceCreated");
	}

	@Override
	public void onSurfaceDestroy() {
		Log.e("onSurfaceDestroy", "onSurfaceDestroy");
	}
}