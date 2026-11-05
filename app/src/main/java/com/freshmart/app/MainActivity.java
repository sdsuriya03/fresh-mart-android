package com.freshmart.app;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private WebView webView;

    private static final String SITE_URL =
            "https://elaborate-mousse-b703f9.netlify.app";

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
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSupportMultipleWindows(false);

        webView.setWebChromeClient(new WebChromeClient());

        // HTML -> Android bridge
        webView.addJavascriptInterface(new WebAppInterface(), "Android");

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    android.webkit.WebResourceRequest request) {

                String url = request.getUrl().toString();

                if (isExternalUrl(url)) {
                    openExternal(url);
                    return true;
                }

                return false;
            }

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    String url) {

                if (isExternalUrl(url)) {
                    openExternal(url);
                    return true;
                }

                return false;
            }
        });

        webView.loadUrl(SITE_URL);
    }

    private boolean isExternalUrl(String url) {
        if (url == null) return false;

        String lower = url.toLowerCase();

        return lower.startsWith("upi://")
                || lower.startsWith("intent://")
                || lower.startsWith("whatsapp://")
                || lower.startsWith("https://wa.me/")
                || lower.startsWith("https://api.whatsapp.com/")
                || lower.startsWith("tel:")
                || lower.startsWith("mailto:")
                || lower.startsWith("market:");
    }

    private void openExternal(String url) {
        try {

            Intent intent;

            if (url.toLowerCase().startsWith("intent://")) {

                intent = Intent.parseUri(
                        url,
                        Intent.URI_INTENT_SCHEME
                );

            } else {

                intent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(url)
                );
            }

            startActivity(intent);

        } catch (ActivityNotFoundException e) {

            Toast.makeText(
                    MainActivity.this,
                    "இந்த phone-ல் தேவையான App கிடைக்கவில்லை",
                    Toast.LENGTH_LONG
            ).show();

        } catch (Exception e) {

            Toast.makeText(
                    MainActivity.this,
                    "App திறக்க முடியவில்லை",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    public class WebAppInterface {

        @JavascriptInterface
        public void openExternal(String url) {

            runOnUiThread(() -> {

                if (url != null && isExternalUrl(url)) {
                    openExternal(url);
                } else {
                    Toast.makeText(
                            MainActivity.this,
                            "Invalid link",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {

        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
