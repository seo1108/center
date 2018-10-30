package yonsei_church.yonsei.center.api;

import android.content.Context;
import android.util.Log;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.bind.DateTypeAdapter;
import com.squareup.okhttp.OkHttpClient;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;
import retrofit.converter.SimpleXMLConverter;
import yonsei_church.yonsei.center.app.AppConst;

public class APIService {
    private static OkHttpClient client;
    public static final int CONNECT_TIMEOUT = 120;
    public static final int WRITE_TIMEOUT = 120;
    public static final int READ_TIMEOUT = 120;

    public APIService() {

    }

    public static <S> S createService(Class<S> serviceClass, final String token) {
        RestAdapter.Builder builder = new RestAdapter.Builder()
                .setEndpoint(AppConst.DOMAIN);
        builder.setRequestInterceptor(new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                // TODO Add Header
                //request.addHeader("token", token);
            }
        });
        RestAdapter adapter = builder.build();

        return adapter.create(serviceClass);
    }

    public static <S> S createService(Class<S> serviceClass, final Context context) {
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(Date.class, new DateTypeAdapter())
                .create();


        OkHttpClient client = configureClient(new OkHttpClient());
        client.setConnectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS);
        client.setWriteTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS);
        client.setReadTimeout(READ_TIMEOUT, TimeUnit.SECONDS);

        RestAdapter.Builder builder = new RestAdapter.Builder()
                .setEndpoint(AppConst.DOMAIN)
                .setClient(new OkClient(client));
        builder.setRequestInterceptor(new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                // TODO Add Header

            }
        });
        builder.setLogLevel(RestAdapter.LogLevel.FULL);
        builder.setConverter(new GsonConverter((gson)));
        RestAdapter adapter = builder.build();
        return adapter.create(serviceClass);
    }

    public static <S> S createStringService(Class<S> serviceClass, final Context context) {



        OkHttpClient client  = configureClient(new OkHttpClient());
        client.setConnectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS);
        client.setWriteTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS);
        client.setReadTimeout(READ_TIMEOUT, TimeUnit.SECONDS);

        RestAdapter.Builder builder = new RestAdapter.Builder()
                .setEndpoint(AppConst.DOMAIN)
                .setClient(new OkClient(client))
                ;
        builder.setRequestInterceptor(new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                // TODO Add Header

            }
        });
        builder.setConverter(new StringConverter());
        builder.setLogLevel(RestAdapter.LogLevel.BASIC);
        RestAdapter adapter = builder.build();
        return adapter.create(serviceClass);
    }

    /**
     * UnCertificated 허용
     */
    public static OkHttpClient configureClient(final OkHttpClient builder) {
        final TrustManager[] certs = new TrustManager[]{new X509TrustManager() {

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                X509Certificate[] x509Certificates = new X509Certificate[0];
                return x509Certificates;
            }

            @Override
            public void checkServerTrusted(final X509Certificate[] chain,
                                           final String authType) {
            }

            @Override
            public void checkClientTrusted(final X509Certificate[] chain,
                                           final String authType) {
            }
        }};

        SSLContext ctx = null;
        try {
            ctx = SSLContext.getInstance("TLS");
            ctx.init(null, certs, new SecureRandom());
        } catch (final java.security.GeneralSecurityException ex) {
            ex.printStackTrace();
        }

        try {
            final HostnameVerifier hostnameVerifier = new HostnameVerifier() {
                @Override
                public boolean verify(final String hostname, final SSLSession session) {
                    return true;
                }
            };

            builder.setSslSocketFactory(ctx.getSocketFactory());
            builder.setHostnameVerifier(hostnameVerifier);
        } catch (final Exception e) {
            e.printStackTrace();
        }

        return builder;
    }
}
