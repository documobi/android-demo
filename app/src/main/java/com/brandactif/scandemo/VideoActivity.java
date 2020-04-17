package com.brandactif.scandemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.brandactif.scandemo.model.MediaScanResponse;
import com.brandactif.scandemo.model.VideoScan;
import com.brandactif.scandemo.model.VideoTimeRange;
import com.brandactif.scandemo.network.APIClient;
import com.brandactif.scandemo.network.APIInterface;
import com.brandactif.scandemo.utils.Utils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VideoActivity extends AppCompatActivity {

    private final String TAG = "VideoActivity";
    private final String CONTENT_TYPE = "application/json";

    private String apiKey = "6c7e04489c2ce3ddebc062c992a1b0802b3be18c7bc4ce950ac430e5e2420c09";
    private String videoName = "b5823bd3-aaf3-4031-a6a3-a7331c835e52";
    private int videoId;

    private APIInterface apiInterface;

    private final int REQUEST_PERMISSIONS_CODE = 1000;
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;

    private VideoView mVideoView;
    private Button mBtnTapToBuy;
    private ListView mListView;
    private ArrayList<VideoTimeRange> mTimeRanges;
    private ArrayList<String> mClickedTimestamps;
    private ArrayAdapter<String> mAdapter;

    private Handler mHandler = new Handler();
    private Runnable mRunnable;
    private final int DELAY = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        apiInterface = APIClient.getClient(getString(R.string.base_url)).create(APIInterface.class);
        apiKey = getString(R.string.api_key);

        Intent intent = getIntent();
        videoName = intent.getStringExtra("videoName");
        videoId = intent.getIntExtra("videoId", R.raw.mediacorp);
        String videoTitle = intent.getStringExtra("videoTitle");

        setTitle(videoTitle);

        mVideoView = findViewById(R.id.videoView);
        mBtnTapToBuy = findViewById(R.id.btnTapToBuy);
        mListView = findViewById(R.id.listView);
        mTimeRanges = new ArrayList<>();
        mClickedTimestamps = new ArrayList<>();

        getVideoTimeRanges(videoName);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_PERMISSIONS_CODE);
        } else {
            // already permission granted
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                // Logic to handle location object
                                Log.d(TAG, "Current location = lat:" + location.getLatitude() + ". lon:" + location.getLongitude());
                                currentLocation = location;
                            }
                        }
                    });
        }

        mAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1,
                mClickedTimestamps);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedTimestamp = mClickedTimestamps.get(position);
                VideoScan videoScan = new VideoScan(videoName,
                        Float.parseFloat(selectedTimestamp),
                        currentLocation.getLatitude(),
                        currentLocation.getLongitude(),
                        Utils.getMetaData(VideoActivity.this));
                createVideoScan(videoScan);
            }
        });

        // Set up the media controller widget and attach it to the video view.
        MediaController controller = new MediaController(this);
        controller.setMediaPlayer(mVideoView);
        mVideoView.setMediaController(controller);

        mBtnTapToBuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTimeRanges.size() > 1) {
                    // Add to timestamp list
                    addTimestamp(mVideoView.getCurrentPosition());
                } else {
                    // Redirect to URL immediately if only one time range
                    if (mVideoView.isPlaying()) {
                        float currPos = mVideoView.getCurrentPosition() / 1000.0f;
                        String selectedTimestamp = Float.toString(currPos);
                        VideoScan videoScan = new VideoScan(videoName,
                                Float.parseFloat(selectedTimestamp),
                                currentLocation.getLatitude(),
                                currentLocation.getLongitude(),
                                Utils.getMetaData(VideoActivity.this));
                        createVideoScan(videoScan);
                    }
                }
            }
        });
    }

    private void addTimestamp(long currentPosition) {
        float timeStamp = mVideoView.getCurrentPosition()/1000.0f;
        mClickedTimestamps.add(Float.toString(timeStamp));
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mBtnTapToBuy.setVisibility(View.INVISIBLE);
        initializePlayer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        releasePlayer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVideoView.pause();
    }

    private void initializePlayer() {
        MediaController mediaController = new MediaController(this);
        mVideoView.setMediaController(mediaController);

        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + videoId);
        mVideoView.setVideoURI(videoUri);
        mVideoView.start();

        // Track video ppsition and hide/show red button according to timestamps
        mHandler.postDelayed(mRunnable = new Runnable() {
            @Override
            public void run() {
                mHandler.postDelayed(this, DELAY);
                if (mVideoView.isPlaying()) {
                    float currPos = mVideoView.getCurrentPosition() / 1000.0f;
                    Log.d(TAG, "Video current position = " + currPos);
                    if (isShowRedButton(currPos)) {
                        mBtnTapToBuy.setVisibility(View.VISIBLE);
                    } else {
                        mBtnTapToBuy.setVisibility(View.INVISIBLE);
                    }
                } else {
                    mHandler.removeCallbacks(mRunnable);
                }
            }
        }, DELAY);
    }

    private void releasePlayer() {
        mVideoView.stopPlayback();
    }

    private boolean isShowRedButton(float currentPosition) {
        for (int i=0; i<mTimeRanges.size(); i++) {
            VideoTimeRange range = mTimeRanges.get(i);
            if (currentPosition >= range.getStartAt() &&
                    currentPosition <= range.getEndAt()) {
                return true;
            }
        }
        return false;
    }

    private void getVideoTimeRanges(String uuid) {
        Log.d(TAG, "Get timestamps: " + uuid);
        Call<VideoTimeRange[]> call = apiInterface.getVideoTimeRanges(apiKey, CONTENT_TYPE, uuid);
        call.enqueue(new Callback<VideoTimeRange[]>() {
            @Override
            public void onResponse(Call<VideoTimeRange[]> call, Response<VideoTimeRange[]> response) {
                if (response.code() != 200) {
                    Log.d(TAG, "getVideoTimeRanges returned response " + response.code());
                    return;
                }

                VideoTimeRange resource[] = response.body();
                Log.d(TAG, "getVideoTimeRanges body = " + resource.toString());

                for (int i=0; i<resource.length; i++) {
                    VideoTimeRange tr = resource[i];
                    mTimeRanges.add(tr);
                    Log.d(TAG, "startAt=" + tr.getStartAt() + ", endAt=" + tr.getEndAt());
                }
            }

            @Override
            public void onFailure(Call<VideoTimeRange[]> call, Throwable t) {
                Toast.makeText(VideoActivity.this, "API call failed", Toast.LENGTH_SHORT).show();
                call.cancel();
            }
        });

    }

    private void createVideoScan(VideoScan videoScan) {
        Call<MediaScanResponse> call = apiInterface.createVideoScan(apiKey, CONTENT_TYPE, videoScan);
        call.enqueue(new Callback<MediaScanResponse>() {
            @Override
            public void onResponse(Call<MediaScanResponse> call, Response<MediaScanResponse> response) {
                if (response.code() != 201) {
                    Log.d(TAG, "createVideoScan returned response " + response.code());
                    return;
                }

                MediaScanResponse resource = response.body();
                Log.d(TAG, "createVideoScan body = " + resource.toString());

                if (resource != null) {
                    String redirectUrl = resource.getRedirectUrl();
                    if (redirectUrl != null && !redirectUrl.isEmpty()) {
                        Map<String, String> details = resource.getResponseDetails();

                        Log.d(TAG, "Redirect URL = " + redirectUrl);
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(redirectUrl));
                        startActivity(browserIntent);
                    } else {
                        Toast.makeText(VideoActivity.this, "No redirect URL", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<MediaScanResponse> call, Throwable t) {
                Toast.makeText(VideoActivity.this, "API call failed", Toast.LENGTH_SHORT).show();
                call.cancel();
            }
        });
    }
}