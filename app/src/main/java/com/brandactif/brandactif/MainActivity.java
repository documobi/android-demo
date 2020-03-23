package com.brandactif.brandactif;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.brandactif.brandactif.model.MediaScanResponse;
import com.brandactif.brandactif.model.RadioScanDetail;
import com.brandactif.brandactif.model.ScanDetail;
import com.brandactif.brandactif.model.ScanResponse;
import com.brandactif.brandactif.model.TvScanDetail;
import com.brandactif.brandactif.network.APIClient;
import com.brandactif.brandactif.network.APIInterface;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity implements LocationListener {

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

    protected LocationManager locationManager;
    protected LocationListener locationListener;

    private String tvUuid = "";
    private String radioUuid = "";
    private String videoUuid = "";
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
            Log.e(TAG, "No location permissions!");
            return;
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
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Latitude:" + location.getLatitude() + ", Longitude:" + location.getLongitude());
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG,"Location disabled");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG,"Location enabled");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG,"Location status changed to " + status);
    }

    void mainButtonTapped() {
        switch (mainButtonType) {
            case SCANNER:
                Log.d(TAG, "Doing image scan!");
                //createScan();
                showingImagePicker = true;
                break;
            case RADIO:
                Log.d(TAG, "Doing radio scan!");
                //RadioScanDetail detail = new RadioScanDetail(String radioUuid,
                 //       String time, double latitude, double longitude, MetaData metaData);

                //createRadioScan(detail);
                break;
            case TV:
                Log.d(TAG, "Doing TV scan!");
                //RadioScanDetail detail = new RadioScanDetail(String radioUuid,
                   //     String time, double latitude, double longitude, MetaData metaData);

                //createTvScan(detail);
                break;
            case VIDEO:
                Log.d(TAG, "Doing video scan!");
                //self.createVideoScan(uuid: settings.videoUuid, time: Date().iso8601 )
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

    void createRadioScan(RadioScanDetail detail) {
        Call<MediaScanResponse> call = apiInterface.createRadioScan(API_KEY, CONTENT_TYPE, detail);
        call.enqueue(new Callback<MediaScanResponse>() {
            @Override
            public void onResponse(Call<MediaScanResponse> call, Response<MediaScanResponse> response) {
                Log.d("TAG", response.code() + "");

                String displayResponse = "";

                MediaScanResponse resource = response.body();
                String redirectUrl = resource.getRedirectUrl();
                Map<String, String> details = resource.getResponseDetails();

            }

            @Override
            public void onFailure(Call<MediaScanResponse> call, Throwable t) {
                call.cancel();
            }
        });
    }

    void createTvScan(TvScanDetail detail) {
        Call<MediaScanResponse> call = apiInterface.createTvScan(API_KEY, CONTENT_TYPE, detail);
        call.enqueue(new Callback<MediaScanResponse>() {
            @Override
            public void onResponse(Call<MediaScanResponse> call, Response<MediaScanResponse> response) {
                Log.d("TAG", response.code() + "");

                String displayResponse = "";

                MediaScanResponse resource = response.body();
                String redirectUrl = resource.getRedirectUrl();
                Map<String, String> details = resource.getResponseDetails();

            }

            @Override
            public void onFailure(Call<MediaScanResponse> call, Throwable t) {
                call.cancel();
            }
        });
    }

}
