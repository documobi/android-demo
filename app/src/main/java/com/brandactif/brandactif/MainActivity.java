package com.brandactif.brandactif;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import com.brandactif.brandactif.model.PresignedUrl;
import com.brandactif.brandactif.model.PresignedUrlResponse;
import com.brandactif.brandactif.model.RadioScan;
import com.brandactif.brandactif.model.Scan;
import com.brandactif.brandactif.model.TvScan;
import com.brandactif.brandactif.model.VideoScan;
import com.brandactif.brandactif.network.APIClient;
import com.brandactif.brandactif.network.APIInterface;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.PUT;
import retrofit2.http.Url;

public class MainActivity extends AppCompatActivity {

    interface UpdateImageInterface {
        @PUT
        Call<Void> updateImage(@Url String url, @Body RequestBody image);
    }

    enum ScanType {
        SCANNER, TV, RADIO, VIDEO
    }

    private final String TAG = "MainActivity";
    private final int PERMISSION_REQUEST_CODE = 200;
    private final String IMAGE_FILENAME = "image.jpg";
    private final String KEY_SCAN_UUID = "scan_uuid";
    private final String KEY_PRESIGNED_URL = "presigned_url";

    private final String API_KEY = "6c7e04489c2ce3ddebc062c992a1b0802b3be18c7bc4ce950ac430e5e2420c09";
    private final String CONTENT_TYPE = "application/json";
    private final double IMAGE_WIDTH = 400.0;
    private final int JPEG_QUALITY = 90;
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

    private ImageView imgLogo;
    private ImageButton btnMain;
    private Button btnLeft;
    private Button btnRight;
    private ImageButton btnSettings;

    private APIInterface apiInterface;

    private FusedLocationProviderClient fusedLocationClient;
    private final int REQUEST_CODE = 1000;
    private final int REQUEST_CAMERA = 999;
    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.CAMERA,
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE);
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
            case REQUEST_CODE: {
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CAMERA) {
            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            thumbnail.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, bytes);
            File destination = new File(Environment.getExternalStorageDirectory(),"temp.jpg");
            FileOutputStream fo;
            try {
                fo = new FileOutputStream(destination);
                fo.write(bytes.toByteArray());
                fo.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Upload to S3
            uploadToS3(destination.getAbsolutePath());
        }
    }

    void mainButtonTapped() {
        double latitude = currentLocation != null ? currentLocation.getLatitude() : 0.0;
        double longitude = currentLocation != null ? currentLocation.getLongitude() : 0.0;

        switch (mainButtonType) {
            case SCANNER:
                Log.d(TAG, "Doing image scan!");
                getPresignedUrl(new PresignedUrl("scan", IMAGE_FILENAME));
                // Get image from camera
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, REQUEST_CAMERA);
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

    void getPresignedUrl(PresignedUrl presignedUrl) {
        Call<PresignedUrlResponse> call = apiInterface.getPresignedUrl(API_KEY, CONTENT_TYPE, presignedUrl);
        call.enqueue(new Callback<PresignedUrlResponse>() {
            @Override
            public void onResponse(Call<PresignedUrlResponse> call, Response<PresignedUrlResponse> response) {

                Log.d(TAG, response.code() + "");

                PresignedUrlResponse resource = response.body();
                if (resource != null) {
                    String uuid = resource.getUuid();
                    String presignedUrl = resource.getUploadUrl();
                    Log.d(TAG, "UUID = " + uuid);
                    Log.d(TAG, "Presigned URL = " + presignedUrl);

                    // Save to user defaults first
                    SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(KEY_SCAN_UUID, uuid);
                    editor.putString(KEY_PRESIGNED_URL, presignedUrl);
                    editor.apply();
                }
            }

            @Override
            public void onFailure(Call<PresignedUrlResponse> call, Throwable t) {
                call.cancel();
            }
        });
    }

    void uploadToS3(String imageFilePath) {
        Log.d(TAG, "Uploading image at " + imageFilePath);

        String CONTENT_IMAGE = "image/jpeg";

        File file = new File(imageFilePath);    // create new file on device
        RequestBody requestFile = RequestBody.create(MediaType.parse(CONTENT_IMAGE), file);

        /* since the pre-signed URL from S3 contains a host, this dummy URL will
         * be replaced completely by the pre-signed URL.  (I'm using baseURl(String) here
         * but see baseUrl(okhttp3.HttpUrl) in Javadoc for how base URLs are handled
         */
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String presignedUrl = sharedPref.getString(KEY_PRESIGNED_URL, "");
        String scanUuid = sharedPref.getString(KEY_SCAN_UUID, "");
        double latitude = currentLocation != null ? currentLocation.getLatitude() : 0.0;
        double longitude = currentLocation != null ? currentLocation.getLongitude() : 0.0;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.amazon.com/")
                .build();

        UpdateImageInterface imageInterface = retrofit.create(UpdateImageInterface.class);
        // imageUrl is the String as received from AWS S3
        Call<Void> call = imageInterface.updateImage(presignedUrl, requestFile);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.d(TAG, response.code() + "");

                createScan(new Scan(IMAGE_FILENAME, scanUuid, latitude, longitude, getMetaData().toString()));
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                call.cancel();
            }
        });

    }

    void createScan(Scan scan) {
        Log.d(TAG, "Create scan: " + scan.getScan().toString());
        Call<String> call = apiInterface.createScan(API_KEY, CONTENT_TYPE, scan);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Log.d(TAG, response.code() + "");

                String uuid = response.body();
                Log.d(TAG, "UUID = " + uuid);
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                call.cancel();
            }
        });
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
