package com.brandactif.scandemo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.assist.AssistContent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
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
import androidx.core.content.FileProvider;

import com.brandactif.scandemo.model.CreateScanResponse;
import com.brandactif.scandemo.model.MediaScanResponse;
import com.brandactif.scandemo.model.PresignedUrl;
import com.brandactif.scandemo.model.PresignedUrlResponse;
import com.brandactif.scandemo.model.RadioScan;
import com.brandactif.scandemo.model.Scan;
import com.brandactif.scandemo.model.ScanResponse;
import com.brandactif.scandemo.model.TvScan;
import com.brandactif.scandemo.network.APIClient;
import com.brandactif.scandemo.network.APIInterface;
import com.brandactif.scandemo.utils.Utils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
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
        SCAN, TV, RADIO, VIDEO
    }

    private final String TAG = "MainActivity";
    private final int PERMISSION_REQUEST_CODE = 200;
    private final String IMAGE_FILENAME = "image.jpg";
    private final String KEY_SCAN_UUID = "scan_uuid";
    private final String KEY_PRESIGNED_URL = "presigned_url";

    private String apiKey = "6c7e04489c2ce3ddebc062c992a1b0802b3be18c7bc4ce950ac430e5e2420c09";
    private String tvName = "b5823bd3-aaf3-4031-a6a3-a7331c835e52";
    private String radioName = "b5823bd3-aaf3-4031-a6a3-a7331c835e52";
    private boolean isSettingsEnabled = false;
    private ScanType defaultScanType = ScanType.SCAN;

    private final String CONTENT_TYPE = "application/json";
    private final int MAX_IMAGE_WIDTH = 600;
    private final int MAX_IMAGE_HEIGHT = 600;
    private final int JPEG_QUALITY = 90;
    private final long GET_SCAN_DELAY = 1500; // msec

    private Color backgroundColor;
    private String logo = "";
    private String currentPhotoPath = "";

    private ScanType mainButtonType = ScanType.SCAN;

    private ImageView imgLogo;
    private ImageButton btnMain;
    private ImageButton btnTv;
    private ImageButton btnRadio;
    private ImageButton btnScan;
    private ImageButton btnVideo;
    private ImageButton btnSettings;

    private APIInterface apiInterface;

    private FusedLocationProviderClient fusedLocationClient;
    private final int REQUEST_PERMISSIONS_CODE = 1000;
    private final int REQUEST_FROM_CAMERA = 1001;
    private final int REQUEST_FROM_GALLERY = 1002;
    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        apiKey = getString(R.string.api_key);
        tvName = getString(R.string.tv_uuid);
        radioName = getString(R.string.radio_uuid);

        switch (getString(R.string.default_scan_type)) {
            case "tv":
                defaultScanType = ScanType.TV;
                mainButtonType = defaultScanType;
                break;
            case "video":
                defaultScanType = ScanType.VIDEO;
                mainButtonType = defaultScanType;
                break;
            case "radio":
                defaultScanType = ScanType.RADIO;
                mainButtonType = defaultScanType;
                break;
            case "scanner":
            default:
                defaultScanType = ScanType.SCAN;
                mainButtonType = defaultScanType;
                break;
        }

        if (getString(R.string.settings_enabled).equals("true")) {
            isSettingsEnabled = true;
        } else {
            isSettingsEnabled = false;
        }

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

        imgLogo = findViewById(R.id.imgLogo);
        btnMain = findViewById(R.id.btnMain);
        btnTv = findViewById(R.id.btnTv);
        btnRadio = findViewById(R.id.btnRadio);
        btnVideo = findViewById(R.id.btnVideo);
        btnScan = findViewById(R.id.btnScan);
        btnSettings = findViewById(R.id.btnSettings);

        btnMain.setImageResource(R.mipmap.button);

        apiInterface = APIClient.getClient(getString(R.string.base_url)).create(APIInterface.class);

        if (isSettingsEnabled) {
            btnSettings.setVisibility(View.VISIBLE);
        } else {
            btnSettings.setVisibility(View.GONE);
        }

        updateButtons();

        btnMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainButtonTapped();
            }
        });

        btnTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modeButtonTapped(ScanType.TV);
            }
        });

        btnRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modeButtonTapped(ScanType.RADIO);
            }
        });

        btnVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modeButtonTapped(ScanType.VIDEO);
            }
        });

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modeButtonTapped(ScanType.SCAN);
            }
        });

        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsButtonTapped();
            }
        });

        // Handle intents
        Intent intent = getIntent();
        if (intent != null) {
            handleIntent(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Handle intents
        Intent intent = getIntent();
        if (intent != null) {
            handleIntent(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSIONS_CODE: {
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
        Bitmap originalImage = null;
        Bitmap resizedImage = null;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) {
            Log.e(TAG, "onActivityResult returned " + resultCode);
            return;
        }

        if (requestCode == REQUEST_FROM_CAMERA) {
            Log.d(TAG, "Current photo path = " + this.currentPhotoPath);
            File file = new File(currentPhotoPath);
            try {
                originalImage = MediaStore.Images.Media
                        .getBitmap(getContentResolver(), Uri.fromFile(file));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == REQUEST_FROM_GALLERY) {
            Uri selectedImageUri = data.getData();
            try {
                originalImage = MediaStore.Images.Media
                        .getBitmap(getContentResolver(), selectedImageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "Invalid request code " + requestCode);
            return;
        }

        if (originalImage != null) {
            resizedImage = resizeImage(originalImage, MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT);
            resizedImage.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, bytes);
        } else {
            Log.d(TAG, "Cannot get original image!");
            return;
        }

        File destination = new File(Environment.getExternalStorageDirectory(), "temp.jpg");
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "Intent action = " + action);
        if (action.equals(Intent.ACTION_VIEW)) {
            Log.d(TAG, "Intent data string = " + intent.getDataString());
            if (intent.getDataString().contains("https://www.brandactif.com/scan")) {
                double latitude = currentLocation != null ? currentLocation.getLatitude() : 0.0;
                double longitude = currentLocation != null ? currentLocation.getLongitude() : 0.0;

                Log.d(TAG, "Doing TV scan!");
                TvScan tvScan = new TvScan(tvName,
                        Utils.getIso8601Date(),
                        latitude,
                        longitude,
                        Utils.getMetaData(MainActivity.this));
                createTvScan(tvScan);


/*
                Uri data = intent.getData();
                String feature = data.getQueryParameter("appFeature");
                Log.d(TAG, "Feature = " + feature);
                switch (feature) {
                    case "TV":
                        Log.d(TAG, "Doing TV scan!");
                        TvScan tvScan = new TvScan(tvName,
                                Utils.getIso8601Date(),
                                latitude,
                                longitude,
                                Utils.getMetaData(MainActivity.this));
                        createTvScan(tvScan);
                        break;
                    case "RADIO":
                        Log.d(TAG, "Doing Radio scan!");
                        RadioScan radioScan = new RadioScan(radioName,
                                Utils.getIso8601Date(),
                                latitude,
                                longitude,
                                Utils.getMetaData(MainActivity.this));
                        createRadioScan(radioScan);
                        break;
                    default:
                        break;
                }
                */
            }
        }
    }

    private Bitmap resizeImage(Bitmap image, int maxWidth, int maxHeight) {
        if (maxHeight > 0 && maxWidth > 0) {
            int width = image.getWidth();
            int height = image.getHeight();
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) maxWidth / (float) maxHeight;

            int finalWidth = maxWidth;
            int finalHeight = maxHeight;
            if (ratioMax > ratioBitmap) {
                finalWidth = (int) ((float) maxHeight * ratioBitmap);
            } else {
                finalHeight = (int) ((float) maxWidth / ratioBitmap);
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
            return image;
        } else {
            return image;
        }
    }

    private void mainButtonTapped() {
        double latitude = currentLocation != null ? currentLocation.getLatitude() : 0.0;
        double longitude = currentLocation != null ? currentLocation.getLongitude() : 0.0;

        switch (mainButtonType) {
            case SCAN:
                Log.d(TAG, "Doing image scan!");
                getPresignedUrl(new PresignedUrl("scan", IMAGE_FILENAME));
                selectImageInput();
                break;
            case RADIO:
                Log.d(TAG, "Doing radio scan!");
                RadioScan radioScan = new RadioScan(radioName,
                        Utils.getIso8601Date(),
                        latitude,
                        longitude,
                        Utils.getMetaData(MainActivity.this));
                createRadioScan(radioScan);
                break;
            case TV:
                Log.d(TAG, "Doing TV scan!");
                TvScan tvScan = new TvScan(tvName,
                        Utils.getIso8601Date(),
                        latitude,
                        longitude,
                        Utils.getMetaData(MainActivity.this));
                createTvScan(tvScan);
                break;
        }
    }

    private void modeButtonTapped(ScanType scanType) {
        switch (scanType) {
            case VIDEO:
                Intent videoActivity = new Intent(getApplicationContext(), VideoListActivity.class);
                startActivity(videoActivity);
                break;
            default:
                mainButtonType = scanType;
                updateButtons();
        }
    }

    private void settingsButtonTapped() {

    }

    private void updateButtons() {
        switch (mainButtonType) {
            case SCAN:
                btnMain.setImageResource(R.mipmap.scan);
                break;
            case RADIO:
                btnMain.setImageResource(R.mipmap.radio);
                break;
            case TV:
                btnMain.setImageResource(R.mipmap.tv);
                break;
        }
    }

    private void selectImageInput() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // String array for alert dialog multi choice items
        String[] items = new String[]{"Camera", "Gallery"};

        builder.setTitle("Select image source");
        builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                if (selectedPosition == 0) {
                    pickFromCamera();
                } else {
                    pickFromGallery();
                }
            }
        }).show();
    }

    private void pickFromCamera() {
        // Get image from camera
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (intent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        getApplicationContext().getPackageName() + ".provider",
                        photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(intent, REQUEST_FROM_CAMERA);
            }
        }
    }

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        String[] mimeTypes = {"image/jpeg"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(intent, REQUEST_FROM_GALLERY);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    float getVideoTime() {
        // Get current video playback time
        return 0.0f;
    }

    void getPresignedUrl(PresignedUrl presignedUrl) {
        Call<PresignedUrlResponse> call = apiInterface.getPresignedUrl(apiKey, CONTENT_TYPE, presignedUrl);
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
                if (response.code() != 200) {
                    Log.d(TAG, "uploadToS3 returned response " + response.code());
                    return;
                }

                String metaDataJson = new Gson().toJson(Utils.getMetaData(MainActivity.this));
                createScan(new Scan(IMAGE_FILENAME, scanUuid, latitude, longitude, metaDataJson));
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(MainActivity.this, "API call failed", Toast.LENGTH_SHORT).show();
                call.cancel();
            }
        });

    }

    void createScan(Scan scan) {
        Log.d(TAG, "Create scan: " + scan.toString());
        Call<CreateScanResponse> call = apiInterface.createScan(apiKey, CONTENT_TYPE, scan);
        call.enqueue(new Callback<CreateScanResponse>() {
            @Override
            public void onResponse(Call<CreateScanResponse> call, Response<CreateScanResponse> response) {
                if (response.code() != 202) {
                    Log.d(TAG, "createScan returned response " + response.code());
                    return;
                }

                CreateScanResponse resource = response.body();
                Log.d(TAG, "createScan body = " + resource.toString());
                final String uuid = resource.getUuid();

                // Get scan after min 1 second delay
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String encodedUuid = URLEncoder.encode(uuid, StandardCharsets.UTF_8.toString());
                            getScan(encodedUuid);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                }, GET_SCAN_DELAY);
            }

            @Override
            public void onFailure(Call<CreateScanResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "API call failed", Toast.LENGTH_SHORT).show();
                call.cancel();
            }
        });
    }

    void getScan(String uuid) {
        Log.d(TAG, "Get scan: " + uuid);
        Call<ScanResponse> call = apiInterface.getScan(apiKey, CONTENT_TYPE, uuid);
        call.enqueue(new Callback<ScanResponse>() {
            @Override
            public void onResponse(Call<ScanResponse> call, Response<ScanResponse> response) {
                if (response.code() != 200) {
                    Log.d(TAG, "getScan returned response " + response.code());
                    return;
                }

                ScanResponse resource = response.body();
                Log.d(TAG, "getScan body = " + resource.toString());

                String redirectUrl = resource.getRedirectUrl();
                String fallbackUrl = resource.getFallbackUrl();
                if (redirectUrl != null && !redirectUrl.isEmpty()) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(redirectUrl));
                    startActivity(browserIntent);
                } else if (fallbackUrl != null && !fallbackUrl.isEmpty()) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl));
                    startActivity(browserIntent);
                } else {
                    Toast.makeText(MainActivity.this, "Image scan status " + resource.getStatus(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ScanResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "API call failed", Toast.LENGTH_SHORT).show();
                call.cancel();
            }
        });

    }

    void createRadioScan(RadioScan radioScan) {
        Call<MediaScanResponse> call = apiInterface.createRadioScan(apiKey, CONTENT_TYPE, radioScan);
        call.enqueue(new Callback<MediaScanResponse>() {
            @Override
            public void onResponse(Call<MediaScanResponse> call, Response<MediaScanResponse> response) {
                if (response.code() != 201) {
                    Log.d(TAG, "createRadioScan returned response " + response.code());
                    return;
                }

                MediaScanResponse resource = response.body();
                if (resource != null) {
                    String redirectUrl = resource.getRedirectUrl();
                    if (redirectUrl != null && !redirectUrl.isEmpty()) {
                        Map<String, String> details = resource.getResponseDetails();

                        Log.d(TAG, "Redirect URL = " + redirectUrl);
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(redirectUrl));
                        startActivity(browserIntent);
                    } else {
                        Toast.makeText(MainActivity.this, "No redirect URL", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<MediaScanResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "API call failed", Toast.LENGTH_SHORT).show();
                call.cancel();
            }
        });
    }

    void createTvScan(TvScan tvScan) {
        Call<MediaScanResponse> call = apiInterface.createTvScan(apiKey, CONTENT_TYPE, tvScan);
        call.enqueue(new Callback<MediaScanResponse>() {
            @Override
            public void onResponse(Call<MediaScanResponse> call, Response<MediaScanResponse> response) {
                if (response.code() != 201) {
                    Log.d(TAG, "createTvScan returned response " + response.code());
                    return;
                }

                MediaScanResponse resource = response.body();
                if (resource != null) {
                    String redirectUrl = resource.getRedirectUrl();
                    if (redirectUrl != null && !redirectUrl.isEmpty()) {
                        Map<String, String> details = resource.getResponseDetails();

                        Log.d(TAG, "Redirect URL = " + redirectUrl);
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(redirectUrl));
                        startActivity(browserIntent);
                    } else {
                        Toast.makeText(MainActivity.this, "No redirect URL", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<MediaScanResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "API call failed", Toast.LENGTH_SHORT).show();
                call.cancel();
            }
        });
    }

}
