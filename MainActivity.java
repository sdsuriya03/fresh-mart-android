package com.freshmart.app;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.GeolocationPermissions;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

public class MainActivity extends Activity {

    private static final int FILE_CHOOSER_REQUEST = 1001;
    private static final int LOCATION_PERMISSION_REQUEST = 1002;

    private WebView webView;
    private ValueCallback<Uri[]> filePathCallback;
    private GeolocationPermissions.Callback geoCallback;
    private String geoOrigin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSupportMultipleWindows(true);
        settings.setGeolocationEnabled(true);

        settings.setUserAgentString(
                settings.getUserAgentString() + " FreshMartAndroid"
        );

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    WebResourceRequest request) {

                return handleExternalUrl(request.getUrl());
            }

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    String url) {

                return handleExternalUrl(Uri.parse(url));
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onShowFileChooser(
                    WebView view,
                    ValueCallback<Uri[]> callback,
                    FileChooserParams params) {

                if (filePathCallback != null) {
                    filePathCallback.onReceiveValue(null);
                }

                filePathCallback = callback;

                Intent intent = params.createIntent();
                intent.addCategory(Intent.CATEGORY_OPENABLE);

                try {
                    startActivityForResult(
                            intent,
                            FILE_CHOOSER_REQUEST
                    );
                } catch (ActivityNotFoundException e) {

                    filePathCallback = null;

                    Toast.makeText(
                            MainActivity.this,
                            "Gallery open panna mudiyala",
                            Toast.LENGTH_SHORT
                    ).show();

                    return false;
                }

                return true;
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(
                    String origin,
                    GeolocationPermissions.Callback callback) {

                geoOrigin = origin;
                geoCallback = callback;

                if (ActivityCompat.checkSelfPermission(
                        MainActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                        ||
                    ActivityCompat.checkSelfPermission(
                        MainActivity.this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED) {

                    callback.invoke(origin, true, false);

                } else {

                    ActivityCompat.requestPermissions(
                            MainActivity.this,
                            new String[]{
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                            },
                            LOCATION_PERMISSION_REQUEST
                    );
                }
            }

            @Override
            public boolean onCreateWindow(
                    WebView view,
                    boolean isDialog,
                    boolean isUserGesture,
                    android.os.Message resultMsg) {

                WebView.HitTestResult hit =
                        view.getHitTestResult();

                String url =
                        hit != null ? hit.getExtra() : null;

                if (url != null) {
                    handleExternalUrl(Uri.parse(url));
                }

                return false;
            }
        });

        // UPDATED FRESH MART WEBSITE
        webView.loadUrl(
                "https://elaborate-mousse-b703f9.netlify.app"
        );
    }

    private boolean handleExternalUrl(Uri uri) {

        if (uri == null) {
            return true;
        }

        String scheme =
                uri.getScheme() == null
                        ? ""
                        : uri.getScheme().toLowerCase();

        String host =
                uri.getHost() == null
                        ? ""
                        : uri.getHost().toLowerCase();

        // Normal website links
        if (scheme.equals("http")
                || scheme.equals("https")) {

            if (host.contains("wa.me")
                    || host.contains("whatsapp.com")
                    || host.contains("api.whatsapp.com")
                    || host.contains("accounts.google.com")) {

                openExternal(uri);
                return true;
            }

            return false;
        }

        // Phone / Email
        if (scheme.equals("tel")
                || scheme.equals("mailto")) {

            openExternal(uri);
            return true;
        }

        // UPI payment
        if (scheme.equals("upi")) {

            openUpi(uri);
            return true;
        }

        // Android intent links
        if (scheme.equals("intent")) {

            openIntentUri(uri);
            return true;
        }

        // Play Store / market links
        if (scheme.equals("market")) {

            openExternal(uri);
            return true;
        }

        return true;
    }

    private void openUpi(Uri uri) {

        try {

            Intent intent =
                    new Intent(Intent.ACTION_VIEW);

            intent.setData(uri);

            startActivity(intent);

        } catch (ActivityNotFoundException e) {

            Toast.makeText(
                    this,
                    "UPI app install pannirukkingala check pannunga",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void openIntentUri(Uri uri) {

        try {

            Intent intent =
                    Intent.parseUri(
                            uri.toString(),
                            Intent.URI_INTENT_SCHEME
                    );

            startActivity(intent);

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Payment app open panna mudiyala",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void openExternal(Uri uri) {

        try {

            Intent intent =
                    new Intent(
                            Intent.ACTION_VIEW,
                            uri
                    );

            startActivity(intent);

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "இந்த link-ஐ திறக்க முடியவில்லை",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (requestCode == FILE_CHOOSER_REQUEST
                && filePathCallback != null) {

            Uri[] results =
                    WebChromeClient.FileChooserParams
                            .parseResult(resultCode, data);

            filePathCallback.onReceiveValue(results);
            filePathCallback = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == LOCATION_PERMISSION_REQUEST
                && geoCallback != null) {

            boolean granted =
                    grantResults.length > 0
                            && grantResults[0]
                            == PackageManager.PERMISSION_GRANTED;

            geoCallback.invoke(
                    geoOrigin,
                    granted,
                    false
            );

            geoCallback = null;
            geoOrigin = null;
        }
    }

    @Override
    public void onBackPressed() {

        if (webView != null
                && webView.canGoBack()) {

            webView.goBack();

        } else {

            super.onBackPressed();
        }
    }
}
