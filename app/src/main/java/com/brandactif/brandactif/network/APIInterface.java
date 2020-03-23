package com.brandactif.brandactif.network;

import com.brandactif.brandactif.model.MediaScanResponse;
import com.brandactif.brandactif.model.PresignedUrlBody;
import com.brandactif.brandactif.model.PresignedUrlResponse;
import com.brandactif.brandactif.model.RadioScanDetail;
import com.brandactif.brandactif.model.ScanDetail;
import com.brandactif.brandactif.model.ScanResponse;
import com.brandactif.brandactif.model.TvScanDetail;
import com.brandactif.brandactif.model.VideoScanDetail;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface APIInterface {

    @POST("/presigned_url")
    Call<PresignedUrlResponse> uploadImageToS3(@Header("api-key") String apiKey,
                                               @Header("content-type") String contentType,
                                               @Body PresignedUrlBody presignedUrlBody);

    @POST("/scans")
    Call<String> createScan(@Header("api-key") String apiKey,
                            @Header("content-type") String contentType,
                            @Body ScanDetail scanDetail);

    @GET("/scans/{uuid}")
    Call<ScanResponse> getScan(@Header("api-key") String apiKey,
                               @Header("content-type") String contentType,
                               @Path("uuid") String uuid);

    @POST("/radio_scans")
    Call<MediaScanResponse> createRadioScan(@Header("api-key") String apiKey,
                                            @Header("content-type") String contentType,
                                            @Body RadioScanDetail radioScanDetail);

    @POST("/tv_scans")
    Call<MediaScanResponse> createTvScan(@Header("api-key") String apiKey,
                                         @Header("content-type") String contentType,
                                         @Body TvScanDetail tvScanDetail);

    @POST("/video_scans")
    Call<MediaScanResponse> createVideoScan(@Header("api-key") String apiKey,
                                            @Header("content-type") String contentType,
                                            @Body VideoScanDetail videoScanDetail);

    @GET("/video_time_ranges?")
    Call<ScanResponse> getVideoTimeRanges(@Header("api-key") String apiKey,
                                          @Header("content-type") String contentType,
                                          @Query("video_uuid") String videoUuid);

}

