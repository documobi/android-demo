package com.brandactif.scandemo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
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
import com.brandactif.scandemo.model.MetaData;
import com.brandactif.scandemo.model.VideoScan;
import com.brandactif.scandemo.network.APIClient;
import com.brandactif.scandemo.network.APIInterface;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VideoActivity extends AppCompatActivity {

    private final String API_KEY = "6c7e04489c2ce3ddebc062c992a1b0802b3be18c7bc4ce950ac430e5e2420c09";
    private final String CONTENT_TYPE = "application/json";
    private String videoName = "b5823bd3-aaf3-4031-a6a3-a7331c835e52";

    private final int REQUEST_PERMISSIONS_CODE = 1000;
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;

    private final String TAG = "VideoPlayer";
    private VideoView mVideoView;
    private Button mBtnTapToBuy;
    private ListView mListView;
    private ArrayList<String> mTimestamps;
    private ArrayAdapter<String> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        videoName = getString(R.string.video_uuid);

        mVideoView = findViewById(R.id.videoView);
        mBtnTapToBuy = findViewById(R.id.btnTapToBuy);
        mListView = findViewById(R.id.listView);
        mTimestamps = new ArrayList<>();

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
                mTimestamps);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedTimestamp = mTimestamps.get(position);
                VideoScan videoScan = new VideoScan(videoName,
                        Float.parseFloat(selectedTimestamp),
                        currentLocation.getLatitude(),
                        currentLocation.getLongitude(),
                        getMetaData());
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
                addTimestamp(mVideoView.getCurrentPosition());
            }
        });
    }

    private void addTimestamp(long currentPosition) {
        float timeStamp = mVideoView.getCurrentPosition()/1000.0f;
        mTimestamps.add(Float.toString(timeStamp));
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStart() {
        super.onStart();
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

        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.the_wolf_of_wall_street);
        mVideoView.setVideoURI(videoUri);
        mVideoView.start();
    }

    private void releasePlayer() {
        mVideoView.stopPlayback();
    }

    // TODO: Move method to common static Utils class
    MetaData getMetaData() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String carrierName = telephonyManager.getNetworkOperatorName();
        String carrierCountry = telephonyManager.getNetworkCountryIso();

        int dataNetworkType = telephonyManager.getDataNetworkType();
        String networkType = "";

        switch (dataNetworkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                networkType = "2G";
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                networkType = "3G";
            case TelephonyManager.NETWORK_TYPE_LTE:
                networkType = "4G";
            case TelephonyManager.NETWORK_TYPE_NR:
                networkType = "5G";
            default:
                networkType = "Unknown";
        }

        return new MetaData(Build.getSerial(),
                "Android",
                Build.VERSION.RELEASE,
                "Chrome",
                Locale.getDefault().getLanguage(),
                Build.BRAND + " " + Build.MODEL,
                carrierName,
                carrierCountry,
                networkType);
    }

    private void createVideoScan(VideoScan videoScan) {
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<MediaScanResponse> call = apiInterface.createVideoScan(API_KEY, CONTENT_TYPE, videoScan);
        call.enqueue(new Callback<MediaScanResponse>() {
            @Override
            public void onResponse(Call<MediaScanResponse> call, Response<MediaScanResponse> response) {
                Log.d(TAG, response.code() + "");

                String displayResponse = "";

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