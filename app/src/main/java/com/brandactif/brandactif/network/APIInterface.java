package com.brandactif.brandactif.network;

import com.brandactif.brandactif.model.MediaScanResponse;
import com.brandactif.brandactif.model.PresignedUrl;
import com.brandactif.brandactif.model.PresignedUrlResponse;
import com.brandactif.brandactif.model.RadioScan;
import com.brandactif.brandactif.model.Scan;
import com.brandactif.brandactif.model.ScanResponse;
import com.brandactif.brandactif.model.TvScan;
import com.brandactif.brandactif.model.VideoScan;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface APIInterface {

    @POST("api/v2/presigned_url")
    Call<PresignedUrlResponse> getPresignedUrl(@Header("api-key") String apiKey,
                                               @Header("content-type") String contentType,
                                               @Body PresignedUrl presignedUrl);

    @POST("api/v2/scans")
    Call<String> createScan(@Header("api-key") String apiKey,
                            @Header("content-type") String contentType,
                            @Body Scan scan);

    @GET("api/v2/scans/{uuid}")
    Call<ScanResponse> getScan(@Header("api-key") String apiKey,
                               @Header("content-type") String contentType,
                               @Path("uuid") String uuid);

    @POST("api/v2/radio_scans")
    Call<MediaScanResponse> createRadioScan(@Header("api-key") String apiKey,
                                            @Header("content-type") String contentType,
                                            @Body RadioScan radioScan);

    @POST("api/v2/tv_scans")
    Call<MediaScanResponse> createTvScan(@Header("api-key") String apiKey,
                                         @Header("content-type") String contentType,
                                         @Body TvScan tvScan);

    @POST("api/v2/video_scans")
    Call<MediaScanResponse> createVideoScan(@Header("api-key") String apiKey,
                                            @Header("content-type") String contentType,
                                            @Body VideoScan videoScan);

    @GET("api/v2/video_time_ranges?")
    Call<ScanResponse> getVideoTimeRanges(@Header("api-key") String apiKey,
                                          @Header("content-type") String contentType,
                                          @Query("video_uuid") String videoUuid);

}

