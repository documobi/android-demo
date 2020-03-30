package com.brandactif.scandemo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.os.Handler;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
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
import com.brandactif.scandemo.model.MetaData;
import com.brandactif.scandemo.model.PresignedUrl;
import com.brandactif.scandemo.model.PresignedUrlResponse;
import com.brandactif.scandemo.model.RadioScan;
import com.brandactif.scandemo.model.Scan;
import com.brandactif.scandemo.model.ScanResponse;
import com.brandactif.scandemo.model.TvScan;
import com.brandactif.scandemo.model.VideoScan;
import com.brandactif.scandemo.network.APIClient;
import com.brandactif.scandemo.network.APIInterface;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
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
    private String tvUuid = "b5823bd3-aaf3-4031-a6a3-a7331c835e52";
    private String radioUuid = "b5823bd3-aaf3-4031-a6a3-a7331c835e52";
    private String videoUuid = "b5823bd3-aaf3-4031-a6a3-a7331c835e52";
    private boolean isSettingsEnabled = false;
    private ScanType defaultScanType = ScanType.SCANNER;

    private final String CONTENT_TYPE = "application/json";
    private final int MAX_IMAGE_WIDTH = 600;
    private final int MAX_IMAGE_HEIGHT = 600;
    private final int JPEG_QUALITY = 90;
    private final long GET_SCAN_DELAY = 1500; // msec

    private Color backgroundColor;
    private String logo = "";
    private String currentPhotoPath = "";

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
    private final int REQUEST_PERMISSIONS_CODE = 1000;
    private final int REQUEST_FROM_CAMERA = 1001;
    private final int REQUEST_FROM_GALLERY = 1002;
    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvUuid = getString(R.string.tv_uuid);
        radioUuid = getString(R.string.radio_uuid);
        videoUuid = getString(R.string.video_uuid);

        switch (getString(R.string.default_scan_type)) {
            case "tv":
                defaultScanType = ScanType.TV;
                mainButtonType = defaultScanType;
                leftButtonType = ScanType.RADIO;
                rightButtonType = ScanType.SCANNER;
                break;
            case "video":
                defaultScanType = ScanType.VIDEO;
                mainButtonType = defaultScanType;
                leftButtonType = ScanType.TV;
                rightButtonType = ScanType.RADIO;
                break;
            case "radio":
                defaultScanType = ScanType.RADIO;
                mainButtonType = defaultScanType;
                leftButtonType = ScanType.TV;
                rightButtonType = ScanType.SCANNER;
                break;
            case "scanner":
            default:
                defaultScanType = ScanType.SCANNER;
                mainButtonType = defaultScanType;
                leftButtonType = ScanType.TV;
                rightButtonType = ScanType.RADIO;
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
        btnLeft = findViewById(R.id.btnLeft);
        btnRight = findViewById(R.id.btnRight);
        btnSettings = findViewById(R.id.btnSettings);

        btnMain.setImageResource(R.mipmap.button);
        btnLeft.setText(R.string.switch_to_radio);
        btnRight.setText(R.string.switch_to_tv);

        apiInterface = APIClient.getClient().create(APIInterface.class);

        if (isSettingsEnabled) {
            btnSettings.setVisibility(View.VISIBLE);
        } else {
            btnSettings.setVisibility(View.INVISIBLE);
        }

        updateButtons();

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
            case SCANNER:
                Log.d(TAG, "Doing image scan!");
                getPresignedUrl(new PresignedUrl("scan", IMAGE_FILENAME));
                selectImageInput();
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

    private void leftButtonTapped() {
        ScanType prevType = mainButtonType;
        mainButtonType = leftButtonType;
        leftButtonType = prevType;
        updateButtons();
    }

    private void rightButtonTapped() {
        ScanType prevType = mainButtonType;
        mainButtonType = rightButtonType;
        rightButtonType = prevType;
        updateButtons();
    }

    private void settingsButtonTapped() {

    }

    private void updateButtons() {
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

    private void selectImageInput() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // String array for alert dialog multi choice items
        String[] items = new String[] {"Camera", "Gallery"};

        builder.setTitle("Select image source");
        builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                int selectedPosition = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
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

    String getIso8601Date() {
        String iso8601Date = ZonedDateTime.now(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        Log.d(TAG, "Current time = " + iso8601Date);
        return iso8601Date;
    }

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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.READ_PHONE_STATE},
                    REQUEST_PERMISSIONS_CODE);
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

                String metaDataJson = new Gson().toJson(getMetaData());
                createScan(new Scan(IMAGE_FILENAME, scanUuid, latitude, longitude, metaDataJson));
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                call.cancel();
            }
        });

    }

    void createScan(Scan scan) {
        Log.d(TAG, "Create scan: " + scan.toString());
        Call<CreateScanResponse> call = apiInterface.createScan(API_KEY, CONTENT_TYPE, scan);
        call.enqueue(new Callback<CreateScanResponse>() {
            @Override
            public void onResponse(Call<CreateScanResponse> call, Response<CreateScanResponse> response) {
                Log.d(TAG, "createScan returned response " + response.code());

                CreateScanResponse resource = response.body();
                Log.d(TAG, "createScan body = " + resource.toString());
                final String uuid = resource.getUuid();

                // Get scan after min 1 second delay
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getScan(uuid);
                    }
                }, GET_SCAN_DELAY);
            }

            @Override
            public void onFailure(Call<CreateScanResponse> call, Throwable t) {
                Log.e(TAG, "createScan failed");
                call.cancel();
            }
        });
    }

    void getScan(String uuid) {
        Log.d(TAG, "Get scan: " + uuid);
        Call<ScanResponse> call = apiInterface.getScan(API_KEY, CONTENT_TYPE, uuid);
        call.enqueue(new Callback<ScanResponse>() {
            @Override
            public void onResponse(Call<ScanResponse> call, Response<ScanResponse> response) {
                Log.d(TAG, "getScan returned response " + response.code());

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
                }
            }

            @Override
            public void onFailure(Call<ScanResponse> call, Throwable t) {
                Log.e(TAG, "getScan failed");
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
