package com.jsbridge.android;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @authors  Draco Yam, Marvin Lam
 * @version  0.0.1
 * @since    2014-08-07
 */
public class JSBridge {

  private static final String TAG = "JSBridge";
  private static JSBridge instance = null;

  private Integer idCounter = 1;                                                                 // identifier for each async request
  private HashMap<Integer, JSBridgeRequest> requests = new HashMap<Integer, JSBridgeRequest>();  // storing requests currently using the bridge
  private ArrayList<JSBridgeRequest> queue = new ArrayList<JSBridgeRequest>();                   // queue for storing request before the html is ready
  private ArrayList<String> scripts = new ArrayList<String>();                                   // script to run before the queue is run
  private WebView webview;
  private boolean isReady = false;

  /**
   * make the bridge becomes singleton
   * @param context, application context
   * @return an instance of JSBridge
   */
  public static JSBridge getInstance(Context context) {
    if (instance == null) {
      instance = new JSBridge(context);
    }
    return instance;
  }

  /**
   * Constructor for JSBridge
   * @param context, application context
   */
  public JSBridge(Context context) {
    webview = new WebView(context);
    webview.getSettings().setJavaScriptEnabled(true);
    webview.getSettings().setDatabaseEnabled(true);
    webview.setWebViewClient(new WebViewClient() {
      @Override
      public void onPageFinished(WebView view, String url) {
        Log.v(TAG, "onPageFinished - url: " + url);
        isReady = true;
        runAsyncJs();
      }

      // TODO: Error kill app
    });
    /**
     * onExceededDatabaseQuota and setDatabasePath are deprecated in KITKAT,
     * previous versions need to call them to activate Web SQL in webview
     */
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
      String databasePath = context.getDir("jsbridge", Context.MODE_PRIVATE).getPath();
      webview.getSettings().setDatabasePath(databasePath);
      webview.setWebChromeClient(new WebChromeClient() {
        @Override
        public void onExceededDatabaseQuota(String url, String databaseIdentifier, long currentQuota, long estimatedSize, long totalUsedQuota, WebStorage.QuotaUpdater quotaUpdater) {
          quotaUpdater.updateQuota(estimatedSize * 2);
        }
      });
    }
    webview.addJavascriptInterface(this, "Android");
    webview.loadUrl("file:///android_asset/empty.html");
  }

  /**
   * Load all scripts from the scripts queue before loading the methods in javascript
   */
  public void loadScript() {
    if (scripts.size() > 0) {
      webview.loadUrl("javascript:" + scripts.remove(0));
    }
    if (scripts.size() > 0) {
      loadScript();
    }
  }

  /**
   * Run the javascript function without arguments
   * @param func name of the javascript function
   * @param callback for success callback
   */
  public void runAsyncJs(String func, JSBridgeRequest.BridgeCallback callback) {
    runAsyncJs(func, null, callback);
  }

  /**
   * Run the javascript function with arguments
   *   a key is inserted in the parameter to keep track of the javascript return to a specific object instance
   * @param func function name to call
   * @param params parameters to pass to the functions
   * @param callback success callback from javascript
   */
  public void runAsyncJs(String func, String params, JSBridgeRequest.BridgeCallback callback) {
    Integer key = idCounter++;
    JSBridgeRequest req = new JSBridgeRequest(key, func, params, callback);
    requests.put(key, req);
    queue.add(req);
    runAsyncJs();
  }

  /**
   * Run code under 2 conditions
   *   HTML is already loaded and
   *   queue size > 0
   */
  private void runAsyncJs() {
    if (isReady && queue.size() > 0) {
      JSBridgeRequest req = queue.remove(0);

      if (req.getParams() == null || req.getParams().isEmpty()) {
        req.setParams("''");
      }
      webview.loadUrl("javascript:" + req.getFunc() + "(" + req.getKey() + ", " + req.getParams() + ")");
      runAsyncJs();
    }
  }

  @JavascriptInterface
  public void bridgeResponse(String key, String result) {
    Integer intKey = null;
    try {
      intKey = Integer.parseInt(key);
    } catch (Exception e) {
    }
    if (intKey != null && requests.containsKey(intKey)) {
      JSBridgeRequest req = requests.get(intKey);
      (req.getCallback()).onSuccess(result);
      requests.remove(req);
    } else if (key.equals("log")) {
      Log.v(TAG, result);
    } else {
      // Notification
    }
  }

}
