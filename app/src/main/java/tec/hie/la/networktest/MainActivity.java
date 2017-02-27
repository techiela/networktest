package tec.hie.la.networktest;

import android.content.Context;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static tec.hie.la.networktest.R.id.webview;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUrl();
        setListener();
    }

    private void setListener() {
        findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
                resetWebView();
                sendRequest();
                render();
            }
        });
    }

    private void resetWebView() {
        WebView webView = ((WebView) findViewById(webview));
        webView.loadUrl("about:blank");
    }

    private void sendRequest() {

        final View progressbar = findViewById(R.id.progress);
        final View result = findViewById(R.id.result);
        String url = ((EditText) findViewById(R.id.url)).getText().toString();
        progressbar.setVisibility(View.VISIBLE);

        saveUrl();

        OkHttpClient client = new OkHttpClient()
                .newBuilder()
                .followRedirects(false)
                .followSslRedirects(false)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            Handler handler = new Handler(getApplicationContext().getMainLooper());

            @Override
            public void onFailure(Call call, final IOException e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressbar.setVisibility(View.INVISIBLE);
                        result.setVisibility(View.VISIBLE);

                        StringBuilder builder = new StringBuilder();
                        builder.append(String.format("Overview:\n%s\n\n", e.toString()));
                        builder.append(String.format("Stacktrace:\n%s", ExceptionUtils.getStackTrace(e.fillInStackTrace())));
                        ((TextView) findViewById(R.id.response)).setText(builder.toString());
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String body = response.body().string();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressbar.setVisibility(View.INVISIBLE);
                        result.setVisibility(View.VISIBLE);

                        StringBuilder builder = new StringBuilder();
                        builder.append(String.format("Status: %d\n", response.code()))
                                .append(response.headers().toString())
                        ;
                        ((TextView) findViewById(R.id.response)).setText(builder.toString());
                    }
                });
            }
        });
    }

    private void render() {

        String url = ((EditText) findViewById(R.id.url)).getText().toString();
        WebView webView = ((WebView) findViewById(webview));
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return false;
            }

            @Override
            public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
                view.loadData(error.toString(), "text/html", CharEncoding.UTF_8);
            }
        });
        webView.loadUrl(url);
    }

    private void initUrl() {
        String url = getApplicationContext().getSharedPreferences("prefs", MODE_PRIVATE).getString("url", "http://www.rakuten.co.jp/");
        ((EditText) findViewById(R.id.url)).setText(url);
    }

    private void saveUrl() {
        String url = ((EditText) findViewById(R.id.url)).getText().toString();
        getApplicationContext().getSharedPreferences("prefs", MODE_PRIVATE).edit().putString("url", url).commit();
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
