package org.goods.living.tech.health.device.utils;


import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import cz.msebera.android.httpclient.entity.StringEntity;

public class ServerRestClient {
    private final String baseUrl;

    public ServerRestClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    private AsyncHttpClient clientAsync = new AsyncHttpClient();
    private SyncHttpClient clientSync = new SyncHttpClient();

    public void getAsync(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {//RequestParams
        clientAsync.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public void postAsync(String url, StringEntity params, AsyncHttpResponseHandler responseHandler) {
        clientAsync.post(null, getAbsoluteUrl(url), params, "application/json", responseHandler);
    }

    public void getSync(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        clientSync.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public void postSync(String url, StringEntity params, AsyncHttpResponseHandler responseHandler) {

        clientSync.post(null, getAbsoluteUrl(url), params, "application/json", responseHandler);
    }

    private String getAbsoluteUrl(String relativeUrl) {
        return baseUrl + relativeUrl;
    }
}
