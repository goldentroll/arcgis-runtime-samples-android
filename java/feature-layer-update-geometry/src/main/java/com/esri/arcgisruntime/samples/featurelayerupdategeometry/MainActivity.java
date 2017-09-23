/* Copyright 2016 ESRI
 *
 * All rights reserved under the copyright laws of the United States
 * and applicable international laws, treaties, and conventions.
 *
 * You may freely redistribute and use this sample code, with or
 * without modification, provided you include the original copyright
 * notice and use restrictions.
 *
 * See the Sample code usage restrictions document for further information.
 *
 */
package com.esri.arcgisruntime.samples.featurelayerupdategeometry;

import java.util.List;
import java.util.concurrent.ExecutionException;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.ArcGISFeature;
import com.esri.arcgisruntime.data.FeatureEditResult;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.GeoElement;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult;
import com.esri.arcgisruntime.mapping.view.MapView;


public class MainActivity extends AppCompatActivity {
  private MapView mMapView;
  private FeatureLayer mFeatureLayer;
  private boolean mFeatureSelected = false;
  private ArcGISFeature mIdentifiedFeature;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    // inflate MapView from layout
    mMapView = (MapView) findViewById(R.id.mapView);
    // create a map with the streets basemap
    ArcGISMap map = new ArcGISMap(Basemap.createStreets());
    //set an initial viewpoint
    map.setInitialViewpoint(new Viewpoint(new Point(-100.343, 34.585, SpatialReferences.getWgs84()), 1E8));
    // set the map to be displayed in the MapView
    mMapView.setMap(map);

    // create feature layer with its service feature table
    // create the service feature table
    final ServiceFeatureTable serviceFeatureTable = new ServiceFeatureTable(getResources().getString(R.string.sample_service_url));
    // create the feature layer using the service feature table
    mFeatureLayer = new FeatureLayer(serviceFeatureTable);
    mFeatureLayer.setSelectionColor(Color.CYAN); 
    mFeatureLayer.setSelectionWidth(3);
    // add the layer to the map
    map.getOperationalLayers().add(mFeatureLayer);
    Toast.makeText(getApplicationContext(), "Tap on a feature to select it", Toast.LENGTH_LONG).show();

    // set an on touch listener to listen for click events
    mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {
      @Override
      public boolean onSingleTapConfirmed(MotionEvent e) {

        if (!mFeatureSelected) {
          android.graphics.Point screenCoordinate = new android.graphics.Point(Math.round(e.getX()), Math.round(e.getY()));
          double tolerance = 20;
          //Identify Layers to find features
          final ListenableFuture<IdentifyLayerResult> identifyFuture = mMapView.identifyLayerAsync(mFeatureLayer, screenCoordinate, tolerance, false, 1);
          identifyFuture.addDoneListener(new Runnable() {
            @Override
            public void run() {
              try {
                // call get on the future to get the result
                IdentifyLayerResult layerResult = identifyFuture.get();
                List<GeoElement> resultGeoElements = layerResult.getElements();

                if(resultGeoElements.size() > 0){
                  if(resultGeoElements.get(0) instanceof ArcGISFeature){
                    mIdentifiedFeature = (ArcGISFeature) resultGeoElements.get(0);
                    //Select the identified feature
                    mFeatureLayer.selectFeature(mIdentifiedFeature);
                    mFeatureSelected = true;
                    Toast.makeText(getApplicationContext(), "Feature Selected. Tap on map to update its geometry " , Toast.LENGTH_LONG).show();
                  }else{
                    Toast.makeText(getApplicationContext(), "No Features Selected. Tap on a feature" , Toast.LENGTH_LONG).show();
                  }
                }
              } catch (InterruptedException | ExecutionException e) {
                Log.e(getResources().getString(R.string.app_name), "Update feature failed: " + e.getMessage());
              }
            }
          });
        } else {
          Point movedPoint = mMapView.screenToLocation(new android.graphics.Point(Math.round(e.getX()), Math.round(e.getY())));
          final Point normalizedPoint = (Point) GeometryEngine.normalizeCentralMeridian(movedPoint);
          mIdentifiedFeature.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
              mIdentifiedFeature.setGeometry(normalizedPoint);
              final ListenableFuture<Void> updateFuture = mFeatureLayer.getFeatureTable().updateFeatureAsync(mIdentifiedFeature);
              updateFuture.addDoneListener(new Runnable() {
                @Override
                public void run() {
                  try {
                    // track the update
                    updateFuture.get();
                    // apply edits once the update has completed
                    if (updateFuture.isDone()) {
                      applyEditsToServer();
                      mFeatureLayer.clearSelection();
                      mFeatureSelected = false;
                    } else {
                      Log.e(getResources().getString(R.string.app_name), "Update feature failed");
                    }
                  } catch (InterruptedException | ExecutionException e) {
                    Log.e(getResources().getString(R.string.app_name), "Update feature failed: " + e.getMessage());
                  }
                }
              });
            }
          });
          mIdentifiedFeature.loadAsync();
        }
        return super.onSingleTapConfirmed(e);
      }
    });
  }

  /**
   * Applies edits to the FeatureService
   */
  private void applyEditsToServer() {
    final ListenableFuture<List<FeatureEditResult>> applyEditsFuture = ((ServiceFeatureTable) mFeatureLayer.getFeatureTable()).applyEditsAsync();
    applyEditsFuture.addDoneListener(new Runnable() {
      @Override
      public void run() {
        try {
          // get results of edit
          List<FeatureEditResult> featureEditResultsList = applyEditsFuture.get();
          if (!featureEditResultsList.get(0).hasCompletedWithErrors()) {
            Toast.makeText(getApplicationContext(), "Applied Geometry Edits to Server. ObjectID: " + featureEditResultsList.get(0).getObjectId(), Toast.LENGTH_SHORT).show();
          }
        } catch (InterruptedException | ExecutionException e) {
          Log.e(getResources().getString(R.string.app_name), "Update feature failed: " + e.getMessage());
        }
      }
    });
  }

  @Override
  protected void onPause() {
    super.onPause();
    // pause MapView
    mMapView.pause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    // resume MapView
    mMapView.resume();
  }
}
