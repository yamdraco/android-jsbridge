package com.jsbridge.android;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.bridge.js.R;


public class MainActivity extends Activity {

  private static final String TAG = "MainActivity";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    JSBridge.getInstance(getApplicationContext());

    Button aysncBtn = (Button) findViewById(R.id.async);
    aysncBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        JSBridge.getInstance(getApplicationContext()).runAsyncJs("simAsync", new JSBridgeRequest.BridgeCallback() {
          @Override
          public void onSuccess(String result) {
            Log.d(TAG, "SUCCESS CALLBACK IN JAVA, RESULT: " + result);
          }
        });
      }
    });

    Button insertBtn = (Button) findViewById(R.id.db_insert);
    insertBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        JSBridge.getInstance(getApplicationContext()).runAsyncJs("insert", new JSBridgeRequest.BridgeCallback() {
          @Override
          public void onSuccess(String s) {
          }
        });
      }
    });

    Button selectBtn = (Button) findViewById(R.id.db_select);
    selectBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        JSBridge.getInstance(getApplicationContext()).runAsyncJs("select", new JSBridgeRequest.BridgeCallback() {
          @Override
          public void onSuccess(String s) {
          }
        });
      }
    });
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();
    if (id == R.id.action_settings) {
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
