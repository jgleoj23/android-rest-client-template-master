package com.codepath.apps.restclienttemplate.interactor;

import android.content.Context;

import com.codepath.apps.restclienttemplate.R;
import com.codepath.oauth.OAuthBaseClient;
import com.github.scribejava.apis.TwitterApi;
import com.github.scribejava.core.builder.api.BaseApi;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import javax.inject.Inject;

import cz.msebera.android.httpclient.Header;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;

/**
 * Communication with the twitter API happens through here.
 *
 * @author Joseph Gardi
 */
public class TwitterClientInteractor extends OAuthBaseClient {

	public static final BaseApi REST_API_INSTANCE = TwitterApi.instance(); // Change this
	public static final String REST_URL = "https://api.twitter.com/1.1"; // Change this, base API URL
	public static final String REST_CONSUMER_KEY = "RLytuOpmJUxPvpGHqn4Wr5qqE";
	public static final String REST_CONSUMER_SECRET = "97xrCDEQRXCuhGtbLgfg3UyUExlePeeJcADnGmKdbYO6usfsRK";
	// Landing page to indicate the OAuth flow worked in case Chrome for Android 25+ blocks navigation back to the app.
	public static final String FALLBACK_URL = "https://codepath.github.io/android-rest-client-template/success.html";
	// See https://developer.chrome.com/multidevice/android/intents
	public static final String REST_CALLBACK_URL_TEMPLATE = "intent://%s#Intent;action=android.intent.action.VIEW;scheme=%s;package=%s;S.browser_fallback_url=%s;end";
    private final String TAG = getClass().getName();

	@Inject
	public TwitterClientInteractor(Context context) {
		super(context, REST_API_INSTANCE,
				REST_URL,
				REST_CONSUMER_KEY,
				REST_CONSUMER_SECRET,
				String.format(REST_CALLBACK_URL_TEMPLATE, context.getString(R.string.intent_host),
						context.getString(R.string.intent_scheme), context.getPackageName(), FALLBACK_URL));
	}



    private TextHttpResponseHandler makeHandler(final ObservableEmitter<String> emitter) {
        return new TextHttpResponseHandler() {

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                emitter.onError(throwable);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                emitter.onNext(responseString);
            }
        };
    }


    public Observable<String> get(final String path, final RequestParams params) {
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull final ObservableEmitter<String> emitter) throws Exception {
                String url = getApiUrl(path);
                client.get(url, params, makeHandler(emitter));

            }
        });
    }

    public Observable<String> post(final String path, final RequestParams params) {
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull final ObservableEmitter<String> emitter) throws Exception {
                String url = getApiUrl(path);
                client.post(url, params, makeHandler(emitter));
            }
        });
    }

}
