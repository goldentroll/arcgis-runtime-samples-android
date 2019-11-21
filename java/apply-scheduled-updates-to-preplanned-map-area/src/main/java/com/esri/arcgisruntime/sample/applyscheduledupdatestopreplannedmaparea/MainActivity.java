/*
 *  Copyright 2019 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.esri.arcgisruntime.sample.applyscheduledupdatestopreplannedmaparea;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.Job;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.MobileMapPackage;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.tasks.offlinemap.OfflineMapSyncJob;
import com.esri.arcgisruntime.tasks.offlinemap.OfflineMapSyncParameters;
import com.esri.arcgisruntime.tasks.offlinemap.OfflineMapSyncResult;
import com.esri.arcgisruntime.tasks.offlinemap.OfflineMapSyncTask;
import com.esri.arcgisruntime.tasks.offlinemap.OfflineMapUpdatesInfo;
import com.esri.arcgisruntime.tasks.offlinemap.OfflineUpdateAvailability;
import com.esri.arcgisruntime.tasks.offlinemap.PreplannedScheduledUpdatesOption;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;
  private File mCopyOfMmpk;
  private TextView mUpdateAvailableTextView;
  private TextView mUpdateSizeTextView;
  private Button mApplyScheduledUpdatesButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // create a map view
    mMapView = findViewById(R.id.mapView);

    // get a reference to the UI views
    mUpdateAvailableTextView = findViewById(R.id.updateAvailableTextView);
    mUpdateSizeTextView = findViewById(R.id.updateSizeTextView);
    mApplyScheduledUpdatesButton = findViewById(R.id.applyScheduledUpdatesButton);

    requestReadPermission();
  }

  private void applyScheduledUpdate() {

    // this is the original mmpk, not updated by the scheduled update
    File originalMmpk = new File(Environment.getExternalStorageDirectory() + getString(R.string.canyonlands_mmpk_path));
    // copy of the mmpk file which will have the update applied to it
    mCopyOfMmpk = new File(getCacheDir() + getString(R.string.canyonlands_folder));

    try {
      // copy the original mmpk into the cache, overwriting any copy of the mmpk already there
      copyDirectory(originalMmpk, mCopyOfMmpk);
    } catch (IOException e) {
      Log.e(TAG, "Error copying MMPK file: " + e.getMessage());
    }

    // load the offline map as a mobile map package
    MobileMapPackage mobileMapPackage = new MobileMapPackage(mCopyOfMmpk.getPath());
    mobileMapPackage.loadAsync();
    mobileMapPackage.addDoneLoadingListener(() -> {
      if (mobileMapPackage.getLoadStatus() == LoadStatus.LOADED && !mobileMapPackage.getMaps().isEmpty()) {
        // add the map from the mobile map package to the map view
        ArcGISMap offlineMap = mobileMapPackage.getMaps().get(0);
        mMapView.setMap(offlineMap);
        // create an offline map sync task with the preplanned area
        OfflineMapSyncTask offlineMapSyncTask = new OfflineMapSyncTask(offlineMap);
        // check for updates to the offline map
        ListenableFuture<OfflineMapUpdatesInfo> offlineMapUpdatesInfoFuture = offlineMapSyncTask.checkForUpdatesAsync();
        offlineMapUpdatesInfoFuture.addDoneListener(() -> {
          try {
            // get and check the results
            OfflineMapUpdatesInfo offlineMapUpdatesInfo = offlineMapUpdatesInfoFuture.get();
            // update the UI with update info
            setUiUpdateInfo(offlineMapUpdatesInfo);
            // if there are available updates
            if (offlineMapUpdatesInfo.getDownloadAvailability() == OfflineUpdateAvailability.AVAILABLE) {
              mUpdateAvailableTextView
                  .setText(getString(R.string.update_status, OfflineUpdateAvailability.AVAILABLE.name()));
              // check and show update size
              mUpdateSizeTextView
                  .setText(getString(R.string.update_size, offlineMapUpdatesInfo.getScheduledUpdatesDownloadSize()));
              // enable the 'Apply Scheduled Updates' button
              mApplyScheduledUpdatesButton.setEnabled(true);
              // when the button is clicked, synchronize the mobile map package
              mApplyScheduledUpdatesButton.setOnClickListener(v -> {
                // create default parameters for the sync task
                ListenableFuture<OfflineMapSyncParameters> offlineMapSyncParametersFuture = offlineMapSyncTask
                    .createDefaultOfflineMapSyncParametersAsync();
                offlineMapSyncParametersFuture.addDoneListener(() -> {
                  try {
                    // get the offline sync parameters from the listenable future
                    OfflineMapSyncParameters offlineMapSyncParameters = offlineMapSyncParametersFuture.get();
                    // set the parameters to download all updates for the mobile map packages
                    offlineMapSyncParameters
                        .setPreplannedScheduledUpdatesOption(PreplannedScheduledUpdatesOption.DOWNLOAD_ALL_UPDATES);
                    // create a sync job using the parameters
                    OfflineMapSyncJob offlineMapSyncJob = offlineMapSyncTask.syncOfflineMap(offlineMapSyncParameters);
                    // start the job and get the results
                    offlineMapSyncJob.start();
                    offlineMapSyncJob.addJobDoneListener(() -> {
                      if (offlineMapSyncJob.getStatus() == Job.Status.SUCCEEDED) {
                        OfflineMapSyncResult offlineMapSyncResult = offlineMapSyncJob.getResult();
                        // if mobile map package reopen is required
                        if (offlineMapSyncResult.isMobileMapPackageReopenRequired()) {
                          // release the mobile map package maps from the map view
                          mMapView.setMap(null);
                          // close the old mobile map package
                          mobileMapPackage.close();
                          // create a new instance of the now updated mobile map package
                          MobileMapPackage updatedMobileMapPackage = new MobileMapPackage(mCopyOfMmpk.getPath());
                          updatedMobileMapPackage.loadAsync();
                          // wait for the new instance of the mobile map package to load
                          updatedMobileMapPackage.addDoneLoadingListener(() -> {
                            if (updatedMobileMapPackage.getLoadStatus() == LoadStatus.LOADED && !updatedMobileMapPackage
                                .getMaps().isEmpty()) {
                              // add the map from the mobile map package to the map view
                              mMapView.setMap(updatedMobileMapPackage.getMaps().get(0));
                            } else {
                              String error =
                                  "Failed to load mobile map package: " + mobileMapPackage.getLoadError().getMessage();
                              Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                              Log.e(TAG, error);
                            }
                          });
                        }
                        // check if the map is up to date against the server. This is not required, since in most cases,
                        // you'll be confident the update was applied because the offline map sync job completed successfully
                        // check for updates to the offline map
                        ListenableFuture<OfflineMapUpdatesInfo> offlineMapUpdatesInfoAfterUpdateFuture = offlineMapSyncTask
                            .checkForUpdatesAsync();
                        offlineMapUpdatesInfoAfterUpdateFuture.addDoneListener(() -> {
                          try {
                            // get the update results
                            OfflineMapUpdatesInfo offlineMapUpdatesInfoAfterUpdate = offlineMapUpdatesInfoAfterUpdateFuture.get();
                            // update the UI with update info
                            setUiUpdateInfo(offlineMapUpdatesInfoAfterUpdate);
                            Log.d(TAG, "Update: " + offlineMapUpdatesInfoAfterUpdate.getDownloadAvailability().name());
                          } catch (Exception e) {
                            String error = "Error checking for Scheduled Updates Availability: " + e.getMessage();
                            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                            Log.e(TAG, error);
                          }
                        });
                      } else {
                        String error = "Error syncing the offline map: " + offlineMapSyncJob.getError().getMessage();
                        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                        Log.e(TAG, error);
                      }
                      // disable the 'Apply Scheduled Updates' button
                      mApplyScheduledUpdatesButton.setEnabled(false);
                    });
                  } catch (InterruptedException | ExecutionException ex) {
                    String error = "Error creating DefaultOfflineMapSyncParameters" + ex.getMessage();
                    Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                    Log.e(TAG, error);
                  }
                });
              });
            } else {
              mUpdateAvailableTextView
                  .setText(getString(R.string.update_status, offlineMapUpdatesInfo.getDownloadAvailability()));
            }
          } catch (Exception e) {
            String error = "Error checking for Scheduled Updates Availability: " + e.getMessage();
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            Log.e(TAG, error);
          }
        });
      } else {
        String error = "Failed to load the mobile map package: " + mobileMapPackage.getLoadError().getMessage();
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
      }
    });
  }

  /**
   * Update UI with update status.
   */
  private void setUiUpdateInfo(OfflineMapUpdatesInfo offlineMapUpdatesInfo) {
    // set the download availability status to the text view
    mUpdateAvailableTextView
        .setText(getString(R.string.update_status, offlineMapUpdatesInfo.getDownloadAvailability().name()));
    if (offlineMapUpdatesInfo.getDownloadAvailability() != OfflineUpdateAvailability.NONE) {
      // server still reports that updates are available
      mUpdateSizeTextView
          .setText(getString(R.string.update_size, offlineMapUpdatesInfo.getScheduledUpdatesDownloadSize()));
      mApplyScheduledUpdatesButton.setEnabled(true);
    } else {
      // server reports that no updates are available
      mUpdateSizeTextView.setText(getString(R.string.update_size_na));
      mApplyScheduledUpdatesButton.setEnabled(false);
    }
  }

  /**
   * Request read external storage for API level 23+.
   */
  private void requestReadPermission() {
    // define permission to request
    String[] reqPermission = { Manifest.permission.READ_EXTERNAL_STORAGE };
    int requestCode = 2;
    if (ContextCompat.checkSelfPermission(this, reqPermission[0]) == PackageManager.PERMISSION_GRANTED) {
      applyScheduledUpdate();
    } else {
      // request permission
      ActivityCompat.requestPermissions(this, reqPermission, requestCode);
    }
  }

  /**
   * Handle the permissions request response.
   */
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      applyScheduledUpdate();
    } else {
      Toast.makeText(this, getString(R.string.canyonlands_mmpk_read_permission_denied), Toast.LENGTH_SHORT).show();
    }
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  @Override
  protected void onPause() {
    super.onPause();
    mMapView.pause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mMapView.resume();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mMapView.dispose();
  }

  /**
   * Copy the given directory to the target directory.
   *
   * @param sourceLocation from which to copy
   * @param targetLocation to copy to
   * @throws IOException
   */
  private static void copyDirectory(File sourceLocation, File targetLocation) throws IOException {
    if (sourceLocation.isDirectory()) {
      if (!targetLocation.exists()) {
        targetLocation.mkdirs();
      }
      String[] children = sourceLocation.list();
      for (String child : children) {
        copyDirectory(new File(sourceLocation, child), new File(
            targetLocation, child));
      }
    } else {
      try (InputStream in = new FileInputStream(sourceLocation)) {
        try (OutputStream out = new FileOutputStream(targetLocation)) {
          byte[] buf = new byte[1024];
          int len;
          while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
          }
        }
      }
    }
  }
}
