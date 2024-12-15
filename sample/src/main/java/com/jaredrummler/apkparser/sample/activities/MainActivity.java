/*
 * Copyright (C) 2015. Jared Rummler <jared.rummler@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.jaredrummler.apkparser.sample.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.jaredrummler.apkparser.ApkParser;
import com.jaredrummler.apkparser.ApkParserExt;
import com.jaredrummler.apkparser.model.DexInfo;
import com.jaredrummler.apkparser.sample.dialogs.XmlListDialog;
import com.jaredrummler.apkparser.sample.fragments.AppListFragment;
import com.jaredrummler.apkparser.sample.interfaces.ApkParserSample;
import com.jaredrummler.apkparser.sample.util.Helper;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ApkParserSample {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (savedInstanceState == null) {
      getFragmentManager()
          .beginTransaction()
          .add(android.R.id.content, new AppListFragment())
          .commit();
    }
  }

  @Override public void openXmlFile(PackageInfo app, String xml) {
    Intent intent = new Intent(this, XmlSourceViewerActivity.class);
    intent.putExtra("app", app);
    intent.putExtra("xml", xml);
    startActivity(intent);
  }

  @Override public void listXmlFiles(final PackageInfo app) {
    final ProgressDialog pd = new ProgressDialog(this);
    pd.setMessage("Please wait...");
    pd.show();

    new AsyncTask<Void, Void, String[]>() {

      @Override protected String[] doInBackground(Void... params) {
        return Helper.getXmlFiles(app.applicationInfo.sourceDir);
      }

      @Override protected void onPostExecute(String[] items) {
        pd.dismiss();
        if (!isFinishing()) {
          XmlListDialog.show(MainActivity.this, app, items);
        }
      }
    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
  }

  @Override public void showMethodCount(final PackageInfo app) {
    new Thread(new Runnable() {

      @Override public void run() {
        ApkParser parser = ApkParserExt.create(app);
        try {
          List<DexInfo> dexInfos = parser.getDexInfos();
          int methodCount = 0;
          for (DexInfo dexInfo : dexInfos) {
            methodCount += dexInfo.header.methodIdsSize;
          }
          String message = NumberFormat.getNumberInstance().format(methodCount);
          toast(message, Toast.LENGTH_SHORT);
        } catch (IOException e) {
          toast(e.getMessage(), Toast.LENGTH_LONG);
        } finally {
          parser.close();
        }
      }
    }).start();
  }

  private void toast(final String message, final int length) {
    if (Looper.myLooper() == Looper.getMainLooper()) {
      Toast.makeText(getApplicationContext(), message, length).show();
    } else {
      runOnUiThread(new Runnable() {

        @Override public void run() {
          Toast.makeText(getApplicationContext(), message, length).show();
        }
      });
    }
  }
}
