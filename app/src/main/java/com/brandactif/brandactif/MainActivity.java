package com.brandactif.brandactif;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.brandactif.brandactif.model.MediaScanResponse;
import com.brandactif.brandactif.model.MetaData;
import com.brandactif.brandactif.model.RadioScan;
import com.brandactif.brandactif.model.ScanDetail;
import com.brandactif.brandactif.model.TvScan;
import com.brandactif.brandactif.model.VideoScan;
import com.brandactif.brandactif.network.APIClient;
import com.brandactif.brandactif.network.APIInterface;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    enum ScanType {
        SCANNER, TV, RADIO, VIDEO
    }

    private final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_CODE = 200;

    private final String API_KEY = "6c7e04489c2ce3ddebc062c992a1b0802b3be18c7bc4ce950ac430e5e2420c09";
    private final String CONTENT_TYPE = "application/json";
    private final String imageFilename = "image.jpg";
    private final double imageWidth = 400.0;
    private final double jpegQuality = 90.0;
    private final double redirectDelay = 1.5; // seconds

    private String tvUuid = "b5823bd3-aaf3-4031-a6a3-a7331c835e52";
    private String radioUuid = "b5823bd3-aaf3-4031-a6a3-a7331c835e52";
    private String videoUuid = "b5823bd3-aaf3-4031-a6a3-a7331c835e52";
    private Color backgroundColor;
    private String logo = "";
    private boolean settingsEnabled = false;

    private ScanType mainButtonType = ScanType.SCANNER;
    private ScanType leftButtonType = ScanType.RADIO;
    private ScanType rightButtonType = ScanType.TV;

    private String scanUuid = "";
    private String presignedUrl = "";
    private boolean showingImagePicker = false;
    private Image inputImage = null;

    private ImageView imgLogo;
    private ImageButton btnMain;
    private Button btnLeft;
    private Button btnRight;
    private ImageButton btnSettings;

    private APIInterface apiInterface;

    private FusedLocationProviderClient fusedLocationClient;
    private final int locationRequestCode = 1000;
    private final int cameraRequestCode = 2000;
    private final int phoneStateRequestCode = 1000;
    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CAMERA, Manifest.permission.READ_PHONE_STATE},
                    cameraRequestCode);
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


        imgLogo = findViewById(R.id.imgLogo);
        btnMain = findViewById(R.id.btnMain);
        btnLeft = findViewById(R.id.btnLeft);
        btnRight = findViewById(R.id.btnRight);
        btnSettings = findViewById(R.id.btnSettings);

        btnMain.setImageResource(R.mipmap.button);
        btnLeft.setText(R.string.switch_to_radio);
        btnRight.setText(R.string.switch_to_tv);

        apiInterface = APIClient.getClient().create(APIInterface.class);

        btnMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainButtonTapped();
            }
        });

        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leftButtonTapped();
            }
        });

        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rightButtonTapped();
            }
        });

        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsButtonTapped();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case locationRequestCode: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                    fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                        if (location != null) {
                            // Logic to handle location object
                            Log.d(TAG, "Current location = lat:" + location.getLatitude() + ". lon:" + location.getLongitude());
                            currentLocation = location;
                        }
                    });
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    void mainButtonTapped() {
        double latitude = currentLocation != null ? currentLocation.getLatitude() : 0.0;
        double longitude = currentLocation != null ? currentLocation.getLongitude() : 0.0;

        switch (mainButtonType) {
            case SCANNER:
                Log.d(TAG, "Doing image scan!");
                // Get image from camera
                //createScan();
                break;
            case RADIO:
                Log.d(TAG, "Doing radio scan!");
                RadioScan radioScan = new RadioScan(radioUuid,
                        getIso8601Date(),
                        latitude,
                        longitude,
                        getMetaData());
                createRadioScan(radioScan);
                break;
            case TV:
                Log.d(TAG, "Doing TV scan!");
                TvScan tvScan = new TvScan(tvUuid,
                        getIso8601Date(),
                        latitude,
                        longitude,
                        getMetaData());
                createTvScan(tvScan);
                break;
            case VIDEO:
                Log.d(TAG, "Doing video scan!");
                /*
                VideoScan videoScan = new VideoScan(videoUuid,
                        getIso8601Time(),
                        latitude,
                        longitude,
                        getMetaData());
                createVideoScan(videoScan);
                 */
                break;
        }
    }

    void leftButtonTapped() {
        ScanType prevType = mainButtonType;
        mainButtonType = leftButtonType;
        leftButtonType = prevType;
        updateButtons();
    }

    void rightButtonTapped() {
        ScanType prevType = mainButtonType;
        mainButtonType = rightButtonType;
        rightButtonType = prevType;
        updateButtons();
    }

    void settingsButtonTapped() {

    }

    void updateButtons() {
        switch (mainButtonType) {
            case SCANNER:
                btnMain.setImageResource(R.mipmap.button);
                break;
            case RADIO:
                btnMain.setImageResource(R.mipmap.radio);
                break;
            case TV:
                btnMain.setImageResource(R.mipmap.tv);
                break;
            case VIDEO:
                btnMain.setImageResource(R.mipmap.button);
                break;
        }

        switch (leftButtonType) {
            case SCANNER:
                btnLeft.setText(R.string.switch_to_scanner);
                break;
            case RADIO:
                btnLeft.setText(R.string.switch_to_radio);
                break;
            case TV:
                btnLeft.setText(R.string.switch_to_tv);
                break;
            case VIDEO:
                btnLeft.setText(R.string.switch_to_video);
                break;
        }


        switch (rightButtonType) {
            case SCANNER:
                btnRight.setText(R.string.switch_to_scanner);
                break;
            case RADIO:
                btnRight.setText(R.string.switch_to_radio);
                break;
            case TV:
                btnRight.setText(R.string.switch_to_tv);
                break;
            case VIDEO:
                btnRight.setText(R.string.switch_to_video);
                break;
        }
    }

    String getIso8601Date() {
        String iso8601Date = ZonedDateTime.now(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        Log.d(TAG, "Current time = " + iso8601Date);
        return iso8601Date;
    }

    MetaData getMetaData() {
        return new MetaData(Build.getSerial(),
                "Android",
                Build.VERSION.RELEASE,
                "Chrome",
                Locale.getDefault().getLanguage(),
                Build.BRAND + " " + Build.MODEL);
    }

    void createScan(ScanDetail scanDetail) {
        /*
        Call<String> call = apiInterface.createScan(apiKey, contentType, scanDetail);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<MultipleResource> call, Response<MultipleResource> response) {


                Log.d("TAG", response.code() + "");

                String displayResponse = "";

                MultipleResource resource = response.body();
                Integer text = resource.page;
                Integer total = resource.total;
                Integer totalPages = resource.totalPages;
                List<MultipleResource.Datum> datumList = resource.data;

                displayResponse += text + " Page\n" + total + " Total\n" + totalPages + " Total Pages\n";

                for (MultipleResource.Datum datum : datumList) {
                    displayResponse += datum.id + " " + datum.name + " " + datum.pantoneValue + " " + datum.year + "\n";
                }

                responseText.setText(displayResponse);

            }

            @Override
            public void onFailure(Call<MultipleResource> call, Throwable t) {
                call.cancel();
            }
        });
         */
    }

    void createRadioScan(RadioScan radioScan) {
        Call<MediaScanResponse> call = apiInterface.createRadioScan(API_KEY, CONTENT_TYPE, radioScan);
        call.enqueue(new Callback<MediaScanResponse>() {
            @Override
            public void onResponse(Call<MediaScanResponse> call, Response<MediaScanResponse> response) {
                Log.d(TAG, response.code() + "");

                String displayResponse = "";

                MediaScanResponse resource = response.body();
                if (resource != null) {
                    String redirectUrl = resource.getRedirectUrl();
                    Map<String, String> details = resource.getResponseDetails();

                    Log.d(TAG, "Redirect URL = " + redirectUrl);
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(redirectUrl));
                    startActivity(browserIntent);
                }
            }

            @Override
            public void onFailure(Call<MediaScanResponse> call, Throwable t) {
                call.cancel();
            }
        });
    }

    void createTvScan(TvScan tvScan) {
        Call<MediaScanResponse> call = apiInterface.createTvScan(API_KEY, CONTENT_TYPE, tvScan);
        call.enqueue(new Callback<MediaScanResponse>() {
            @Override
            public void onResponse(Call<MediaScanResponse> call, Response<MediaScanResponse> response) {
                Log.d(TAG, response.code() + "");

                String displayResponse = "";

                MediaScanResponse resource = response.body();
                if (resource != null) {
                    String redirectUrl = resource.getRedirectUrl();
                    Map<String, String> details = resource.getResponseDetails();

                    Log.d(TAG, "Redirect URL = " + redirectUrl);
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(redirectUrl));
                    startActivity(browserIntent);
                }
            }

            @Override
            public void onFailure(Call<MediaScanResponse> call, Throwable t) {
                call.cancel();
            }
        });
    }

    void createVideoScan(VideoScan videoScan) {
        Call<MediaScanResponse> call = apiInterface.createVideoScan(API_KEY, CONTENT_TYPE, videoScan);
        call.enqueue(new Callback<MediaScanResponse>() {
            @Override
            public void onResponse(Call<MediaScanResponse> call, Response<MediaScanResponse> response) {
                Log.d(TAG, response.code() + "");

                String displayResponse = "";

                MediaScanResponse resource = response.body();
                if (resource != null) {
                    String redirectUrl = resource.getRedirectUrl();
                    Map<String, String> details = resource.getResponseDetails();

                    Log.d(TAG, "Redirect URL = " + redirectUrl);
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(redirectUrl));
                    startActivity(browserIntent);
                }
            }

            @Override
            public void onFailure(Call<MediaScanResponse> call, Throwable t) {
                call.cancel();
            }
        });
    }

}
