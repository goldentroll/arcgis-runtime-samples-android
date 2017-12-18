/* Copyright 2017 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.esri.arcgisruntime.sample.rgbrenderer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import com.esri.arcgisruntime.layers.RasterLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.raster.MinMaxStretchParameters;
import com.esri.arcgisruntime.raster.PercentClipStretchParameters;
import com.esri.arcgisruntime.raster.RGBRenderer;
import com.esri.arcgisruntime.raster.Raster;
import com.esri.arcgisruntime.raster.StandardDeviationStretchParameters;
import com.esri.arcgisruntime.raster.StretchParameters;

import java.io.File;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements ParametersDialogFragment.ParametersListener {

  private FragmentManager mFragmentManager;
  private MapView mMapView;
  private RasterLayer mRasterLayer;

  private int mMinR;
  private int mMaxR;
  private int mMinG;
  private int mMaxG;
  private int mMinB;
  private int mMaxB;
  private int mPercentClipMin;
  private int mPercentClipMax;
  private int mStdDevFactor;
  private StretchType mStretchType;

  @Override
  public void returnParameters(int minR, int maxR, int minG, int maxG, int minB, int maxB, int percentClipMin,
      int percentClipMax, int stdDevFactor, StretchType stretchType) {
    //gets dialog box parameters and calls updateRenderer
    mMinR = minR;
    mMaxR = maxR;
    mMinG = minG;
    mMaxG = maxG;
    mMinB = minB;
    mMaxB = maxB;
    mPercentClipMin = percentClipMin;
    mPercentClipMax = percentClipMax;
    mStdDevFactor = stdDevFactor;
    mStretchType = stretchType;
    updateRenderer();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    //set default values for rgb parameters
    mMinR = 0;
    mMaxR = 255;
    mMinG = 0;
    mMaxG = 255;
    mMinB = 0;
    mMaxB = 255;
    mPercentClipMin = 0;
    mPercentClipMax = 99;
    mStdDevFactor = 1;
    mStretchType = StretchType.MIN_MAX;
    // retrieve the MapView from layout
    mMapView = (MapView) findViewById(R.id.mapView);
    mFragmentManager = getSupportFragmentManager();
    // define permission to request
    String[] reqPermission = new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE };
    int requestCode = 2;
    // For API level 23+ request permission at runtime
    if (ContextCompat.checkSelfPermission(MainActivity.this,
        reqPermission[0]) == PackageManager.PERMISSION_GRANTED) {
      initialize();
    } else {
      // request permission
      ActivityCompat.requestPermissions(MainActivity.this, reqPermission, requestCode);
    }
  }

  /**
   * Using values stored in strings.xml, builds path to raster.
   *
   * @return the path to raster file
   */
  private String buildRasterPath(String filename) {
    // get sdcard resource name
    File extStorDir = Environment.getExternalStorageDirectory();
    // get the directory
    String extSDCardDirName = this.getResources().getString(R.string.data_sdcard_offline_dir);
    // create the full path to the raster file
    return extStorDir.getAbsolutePath()
        + File.separator
        + extSDCardDirName
        + File.separator
        + filename
        + ".tif";
  }

  /**
   * Creates new imagery raster based on a given path, creates an ArcGISMap, sets it to a MapView and
   * calls updateRenderer().
   */
  private void initialize() {
    // create raster
    Raster raster = new Raster(
        new File(buildRasterPath(this.getString(R.string.raster_name))).getAbsolutePath());
    mRasterLayer = new RasterLayer(raster);
    // create a basemap from the raster layer
    Basemap basemap = new Basemap(mRasterLayer);
    ArcGISMap map = new ArcGISMap(basemap);
    // add the map to a map view
    mMapView.setMap(map);
    updateRenderer();
  }

  /**
   * Creates StretchRenderer of the chosen type: MinMax, PercentClip or StandardDeviation.
   */
  private void updateRenderer() {
    StretchParameters stretchParameters;
    switch (mStretchType) {
      default:
        stretchParameters = new MinMaxStretchParameters(
            Arrays.asList((double) mMinR, (double) mMinG, (double) mMinB),
            Arrays.asList((double) mMaxR, (double) mMaxG, (double) mMaxB));
        break;
      case PERCENT_CLIP:
        stretchParameters = new PercentClipStretchParameters(mPercentClipMin, mPercentClipMax);
        break;
      case STANDARD_DEVIATION:
        stretchParameters = new StandardDeviationStretchParameters(mStdDevFactor);
    }
    RGBRenderer rgbRenderer = new RGBRenderer(stretchParameters, Arrays.asList(0, 1, 2), null, true);
    mRasterLayer.setRasterRenderer(rgbRenderer);
  }

  /**
   * Handle the permissions request response.
   */
  public void onRequestPermissionsResult(int requestCode,
      @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      initialize();
    } else {
      // report to user that permission was denied
      Toast.makeText(MainActivity.this,
          getResources().getString(R.string.location_permission_denied),
          Toast.LENGTH_SHORT).show();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.rgb_parameters, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    //send parameters to fragment
    ParametersDialogFragment paramDialog = new ParametersDialogFragment();
    Bundle rgbParameters = new Bundle();
    rgbParameters.putInt("minR", mMinR);
    rgbParameters.putInt("maxR", mMaxR);
    rgbParameters.putInt("minG", mMinG);
    rgbParameters.putInt("maxG", mMaxG);
    rgbParameters.putInt("minB", mMinB);
    rgbParameters.putInt("maxB", mMaxB);
    rgbParameters.putInt("percent_clip_min", mPercentClipMin);
    rgbParameters.putInt("percent_clip_max", mPercentClipMax);
    rgbParameters.putInt("std_dev_factor", mStdDevFactor);
    rgbParameters.putSerializable("stretch_type", mStretchType);
    paramDialog.setArguments(rgbParameters);
    paramDialog.show(mFragmentManager, "param_dialog");
    return super.onOptionsItemSelected(item);
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

  enum StretchType {
    MIN_MAX("Min Max"),
    PERCENT_CLIP("Percent Clip"),
    STANDARD_DEVIATION("Standard Deviation");

    private final String stringValue;

    StretchType(String toString) {
      stringValue = toString;
    }

    @Override public String toString() {
      return stringValue;
    }
  }
}