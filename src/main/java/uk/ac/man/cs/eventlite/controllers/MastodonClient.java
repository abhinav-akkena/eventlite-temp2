//https://github.com/sys1yagi/mastodon4j/blob/master/mastodon4j/src/main/java/com/sys1yagi/mastodon4j/MastodonClient.kt
//Translation of the Kotlin code to java 

package uk.ac.man.cs.eventlite.controllers;

import com.google.gson.Gson;
import com.sys1yagi.mastodon4j.Parameter;
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;

import okhttp3.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class MastodonClient {
    private final String instanceName;
    private final OkHttpClient client;
    private final Gson gson;
    private boolean debug = false;

    private MastodonClient(String instanceName, OkHttpClient client, Gson gson) {
        this.instanceName = instanceName;
        this.client = client;
        this.gson = gson;
    }

    public static class Builder {
        private final String instanceName;
        private final OkHttpClient.Builder okHttpClientBuilder;
        private final Gson gson;
        private String accessToken;
        private boolean debug = false;

        public Builder(String instanceName, OkHttpClient.Builder okHttpClientBuilder, Gson gson) {
            this.instanceName = instanceName;
            this.okHttpClientBuilder = okHttpClientBuilder;
            this.gson = gson;
        }

        public Builder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public Builder useStreamingApi() {
            this.okHttpClientBuilder.readTimeout(60, TimeUnit.SECONDS);
            return this;
        }

        public Builder debug() {
            this.debug = true;
            return this;
        }

        public MastodonClient build() {
            OkHttpClient client = okHttpClientBuilder
                    .addNetworkInterceptor(new AuthorizationInterceptor(accessToken))
                    .build();
            MastodonClient mastodonClient = new MastodonClient(instanceName, client, gson);
            mastodonClient.debug = debug;
            return mastodonClient;
        }
    }

    private void debugPrint(String log) {
        if (debug) {
            System.out.println(log);
        }
    }

    private static class AuthorizationInterceptor implements Interceptor {
        private final String accessToken;

        public AuthorizationInterceptor(String accessToken) {
            this.accessToken = accessToken;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();
            Request.Builder builder = originalRequest.newBuilder()
                    .headers(originalRequest.headers())
                    .method(originalRequest.method(), originalRequest.body());
            if (accessToken != null) {
                builder.header("Authorization", String.format("Bearer %s", accessToken));
            }
            Request compressedRequest = builder.build();
            return chain.proceed(compressedRequest);
        }
    }

    private String baseUrl() {
    	return "https://" + this.instanceName + "/api/v1";
    }

    public Gson getSerializer() {
        return gson;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public Response get(String path, Parameter parameter) throws Mastodon4jRequestException {
        try {
            String url = baseUrl() + "/" + path;
            debugPrint(url);
            String urlWithParams = (parameter != null) ? url + "?" + parameter.build() : url;
            Request request = new Request.Builder().url(urlWithParams).get().build();
            Call call = client.newCall(request);
            return call.execute();
        } catch (IOException e) {
            throw new Mastodon4jRequestException(e);
        }
    }

    public Response postUrl(String url, RequestBody body) throws Mastodon4jRequestException {
        try {
            debugPrint(url);
            Request request = new Request.Builder().url(url).post(body).build();
            Call call = client.newCall(request);
            return call.execute();
        } catch (IllegalArgumentException | IOException e) {
            throw new Mastodon4jRequestException(e);
        }
    }

    public Response post(String path, RequestBody body) throws Mastodon4jRequestException {
        return postUrl(baseUrl() + "/" + path, body);
    }

    public Response patch(String path, RequestBody body) throws Mastodon4jRequestException {
        try {
            String url = baseUrl() + "/" + path;
            debugPrint(url);
            Request request = new Request.Builder().url(url).patch(body).build();
            Call call = client.newCall(request);
            return call.execute();
        } catch (IOException e) {
            throw new Mastodon4jRequestException(e);
        }
    }

    public Response delete(String path) throws Mastodon4jRequestException {
        try {
            String url = baseUrl() + "/" + path;
            debugPrint(url);
            Request request = new Request.Builder().url(url).delete().build();
            Call call = client.newCall(request);
            return call.execute();
        } catch (IOException e) {
            throw new Mastodon4jRequestException(e);
        }
    }
}
