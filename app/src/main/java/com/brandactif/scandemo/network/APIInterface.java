package com.brandactif.scandemo.network;

import com.brandactif.scandemo.model.CreateScanResponse;
import com.brandactif.scandemo.model.MediaScanResponse;
import com.brandactif.scandemo.model.PresignedUrl;
import com.brandactif.scandemo.model.PresignedUrlResponse;
import com.brandactif.scandemo.model.RadioScan;
import com.brandactif.scandemo.model.Scan;
import com.brandactif.scandemo.model.ScanResponse;
import com.brandactif.scandemo.model.TvScan;
import com.brandactif.scandemo.model.VideoScan;
import com.brandactif.scandemo.model.VideoTimeRange;

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
    Call<CreateScanResponse> createScan(@Header("api-key") String apiKey,
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

    @GET("api/v2/video_time_ranges")
    Call<VideoTimeRange[]> getVideoTimeRanges(@Header("api-key") String apiKey,
                                              @Header("content-type") String contentType,
                                              @Query("video_name") String videoName);

}

