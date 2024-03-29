package yonsei_church.yonsei.center.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class Util {
    public static String urlDecode(String str) {
        String result = "";
        try {
            result = URLDecoder.decode(str, "UTF-8");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return result;
    }

    public static String urlEncode(String str) {
        String result = "";
        try {
            result = URLEncoder.encode(str, "UTF-8");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return result;
    }

    public static String checkNull(Object obj,String defaultVal){
        String strRtn = "" ;
        if(defaultVal==null) defaultVal = "" ;
        if(obj==null){
            strRtn = defaultVal ;
        }else{
            String tempStr = String.valueOf(obj).trim();
            if(tempStr.equals(""))
                strRtn = defaultVal ;
            else
                strRtn = tempStr ;
        }
        return strRtn ;
    }

    public Bitmap getBitmapFromURL(String src) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(src);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace(); return null;
        } finally {
            if(connection!=null) connection.disconnect();
        }
    }

    public static String getAppVersion(Context context) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            return pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "0";
        }
    }
}
