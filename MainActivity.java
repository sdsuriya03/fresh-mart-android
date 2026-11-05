package com.freshmart.app;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.net.URISyntaxException;

public class MainActivity extends Activity {

    private WebView webView;

    private static final String SITE_URL =
            "https://heroic-dragon-8d99d1.netlify.app";

    private static final int FILE_CHOOSER_REQUEST = 1001;
    private static final int LOCATION_REQUEST = 2001;

    private android.webkit.ValueCallback<Uri[]> filePathCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setGeolocationEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSupportMultipleWindows(false);

        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onShowFileChooser(
                    WebView webView,
                    android.webkit.ValueCallback<Uri[]> filePathCallback,
                    FileChooserParams fileChooserParams) {

                if (MainActivity.this.filePathCallback != null) {
                    MainActivity.this.filePathCallback.onReceiveValue(null);
                }

                MainActivity.this.filePathCallback =
                        filePathCallback;

                Intent intent =
                        fileChooserParams.createIntent();

                try {
                    startActivityForResult(
                            intent,
                            FILE_CHOOSER_REQUEST
                    );
                } catch (ActivityNotFoundException e) {

                    MainActivity.this.filePathCallback = null;

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
                    android.webkit.GeolocationPermissions.Callback callback) {

                if (checkSelfPermission(
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED
                        &&
                        checkSelfPermission(
                                Manifest.permission.ACCESS_COARSE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED) {

                    requestPermissions(
                            new String[]{
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                            },
                            LOCATION_REQUEST
                    );
                }

                callback.invoke(origin, true, false);
            }
        });

        // HTML -> Android bridge
        webView.addJavascriptInterface(
                new WebAppInterface(),
                "Android"
        );

        webView.setWebViewClient(
                new WebViewClient() {

                    @Override
                    public boolean shouldOverrideUrlLoading(
                            WebView view,
                            WebResourceRequest request) {

                        Uri uri = request.getUrl();

                        if (isExternalScheme(uri)) {
                            handleExternalUrl(uri);
                            return true;
                        }

                        return false;
                    }

                    @Override
                    public boolean shouldOverrideUrlLoading(
                            WebView view,
                            String url) {

                        Uri uri = Uri.parse(url);

                        if (isExternalScheme(uri)) {
                            handleExternalUrl(uri);
                            return true;
                        }

                        return false;
                    }
                }
        );

        webView.loadUrl(SITE_URL);
    }

    private boolean isExternalScheme(Uri uri) {

        if (uri == null) {
            return false;
        }

        String scheme = uri.getScheme();

        if (scheme == null) {
            return false;
        }

        scheme = scheme.toLowerCase();

        return scheme.equals("upi")
                || scheme.equals("intent")
                || scheme.equals("whatsapp")
                || scheme.equals("tel")
                || scheme.equals("mailto")
                || scheme.equals("market");
    }

    private void handleExternalUrl(Uri uri) {

        if (uri == null) {
            return;
        }

        String scheme =
                uri.getScheme() == null
                        ? ""
                        : uri.getScheme().toLowerCase();

        if (scheme.equals("upi")) {

            openUpi(uri);

        } else if (scheme.equals("intent")) {

            openIntentUri(uri);

        } else {

            openExternal(uri);
        }
    }

    private void openUpi(Uri uri) {

        try {

            Intent intent =
                    new Intent(Intent.ACTION_VIEW);

            intent.setData(uri);
            intent.addCategory(Intent.CATEGORY_DEFAULT);

            startActivity(intent);

        } catch (ActivityNotFoundException e) {

            Toast.makeText(
                    this,
                    "GPay / PhonePe / Paytm போன்ற UPI app இல்லை",
                    Toast.LENGTH_LONG
            ).show();

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "UPI app open panna mudiyala",
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

            try {

                startActivity(intent);

            } catch (ActivityNotFoundException e) {

                String fallbackUrl =
                        intent.getStringExtra(
                                "browser_fallback_url"
                        );

                if (fallbackUrl != null
                        && !fallbackUrl.isEmpty()) {

                    openExternal(
                            Uri.parse(fallbackUrl)
                    );

                } else {

                    Toast.makeText(
                            this,
                            "தேவையான app கிடைக்கவில்லை",
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

        } catch (
                URISyntaxException e) {

            Toast.makeText(
                    this,
                    "Invalid app link",
                    Toast.LENGTH_SHORT
            ).show();

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "App open panna mudiyala",
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

        } catch (ActivityNotFoundException e) {

            Toast.makeText(
                    this,
                    "இந்த app-ஐ திறக்க முடியவில்லை",
                    Toast.LENGTH_LONG
            ).show();

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Link open panna mudiyala",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    public class WebAppInterface {

        @JavascriptInterface
        public void openExternal(String url) {

            if (url == null || url.isEmpty()) {
                return;
            }

            runOnUiThread(() -> {

                try {

                    Uri uri =
                            Uri.parse(url);

                    if (isExternalScheme(uri)) {
                        handleExternalUrl(uri);
                    } else {

                        Toast.makeText(
                                MainActivity.this,
                                "Invalid external link",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                } catch (Exception e) {

                    Toast.makeText(
                            MainActivity.this,
                            "Link open panna mudiyala",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            });
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
                            .parseResult(
                                    resultCode,
                                    data
                            );

            filePathCallback.onReceiveValue(results);

            filePathCallback = null;
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
