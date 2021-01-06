package com.brandactif.scandemo.utils;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.brandactif.scandemo.model.MetaData;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Utils {

    public static String getIso8601Date() {
        String iso8601Date = ZonedDateTime.now(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        return iso8601Date;
    }

    public static String getSerial() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD_MR1) {
            return Build.getSerial();
        } else {
            return "";
        }
    }

    public static MetaData getMetaData(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
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

        return new MetaData(Utils.getSerial(),
                "Android",
                Build.VERSION.RELEASE,
                "Chrome",
                Locale.getDefault().getLanguage(),
                Build.BRAND + " " + Build.MODEL,
                carrierName,
                carrierCountry,
                networkType);
    }
}
