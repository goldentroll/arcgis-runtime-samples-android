/* Copyright 2016 Esri
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

package com.esri.arcgisruntime.sample.authoringamap;

import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.ArcGISMapImageLayer;
import com.esri.arcgisruntime.layers.ArcGISTiledLayer;
import com.esri.arcgisruntime.layers.Layer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.LayerViewStateChangedEvent;
import com.esri.arcgisruntime.mapping.view.LayerViewStateChangedListener;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.security.OAuthLoginManager;

public class MainActivity extends AppCompatActivity {

    private static final int MIN_SCALE = 40000000;
    private static final int SCALE = 50000000;
    static ArcGISMap mMap;
    private static OAuthLoginManager oauthLoginManager;
    private ProgressDialog progressDialog;
    private Viewpoint viewpoint;
    private MapView mMapView;
    private String[] mBasemapTiles;
    private DrawerLayout mDrawerLayout;
    private ListView mBasemapListView;
    private ListView mLayerListView;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private ActionBarDrawerToggle mDrawerToggle;
    private Layer[] layer_array = new Layer[2];

    public static OAuthLoginManager getOAuthLoginManagerInstance() {
        return oauthLoginManager;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        mTitle = mDrawerTitle = getTitle();
        // inflate MapView from layout
        mMapView = (MapView) findViewById(R.id.mapView);
        // create a map with Topographic Basemap
        mMap = new ArcGISMap(Basemap.createStreets());
        // set the map to be displayed in this view
        mMapView.setMap(mMap);

        // create spatial reference for all points
        SpatialReference spatialReference = SpatialReferences.getWebMercator();

        // create the esri location point
        Point londonPoint = new Point(-14093, 6711377, spatialReference);
        // create the viewpoint with the London point and scale
        viewpoint = new Viewpoint(londonPoint, SCALE);

        final ArcGISTiledLayer tiledLayer = new ArcGISTiledLayer(getApplication().getString(R.string.world_elevation));

        final ArcGISMapImageLayer imageLayer = new ArcGISMapImageLayer(getApplication().getString(R.string.world_census));
        // setting the scales at which this layer can be viewed
        imageLayer.setMinScale(MIN_SCALE);
        imageLayer.setMaxScale(MIN_SCALE / 10);

        layer_array[0] = tiledLayer;
        layer_array[1] = imageLayer;

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getApplication().getString(R.string.author_map_message));
        progressDialog.setMessage(getApplication().getString(R.string.wait));

        // create arrays from String arrays
        mBasemapTiles = getResources().getStringArray(R.array.basemap_array);
        String[] mLayerTiles = getResources().getStringArray(R.array.operational_layer_array);

        // inflate the Basemap and Layer list views
        mBasemapListView = (ListView) findViewById(R.id.basemap_list);
        mLayerListView = (ListView) findViewById(R.id.layer_list);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // Set the adapter for the Basemap list view
        mBasemapListView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, mBasemapTiles));
        mBasemapListView.setItemChecked(0, true);

        // Set the adapter for the Operational Layer list view
        mLayerListView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, mLayerTiles));

        mBasemapListView.setOnItemClickListener(new BasemapClickListener());

        // set actions for drawer state - close/open
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.app_name, R.string.app_name) {
            // if the drawer is closed, get the checked items from LayerListView and add the checked layer
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                mMap.addDoneLoadingListener(new Runnable() {
                    @Override
                    public void run() {
                        if (mMap.getLoadStatus().name().equalsIgnoreCase(LoadStatus.LOADED.name())) {
                            // if both items are checked, add them
                            if (mLayerListView.getCheckedItemCount() > 1) {
                                progressDialog.show();
                                removeLayers();
                                mMap.getOperationalLayers().add(layer_array[0]);
                                mMap.getOperationalLayers().add(layer_array[1]);
                            } else { // if any one item is checked, add as layer
                                if (mLayerListView.isItemChecked(0)) {
                                    progressDialog.show();
                                    removeLayers();
                                    mMap.getOperationalLayers().add(layer_array[0]);
                                } else if (mLayerListView.isItemChecked(1)) {
                                    progressDialog.show();
                                    removeLayers();
                                    mMap.getOperationalLayers().add(layer_array[1]);
                                } else {
                                    removeLayers();
                                }
                            }
                        }
                    }
                });
                // if the progrees dialog is showing, dismiss it
                mMapView.addLayerViewStateChangedListener(new LayerViewStateChangedListener() {
                    @Override
                    public void layerViewStateChanged(LayerViewStateChangedEvent layerViewStateChangedEvent) {
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }

                    }
                });
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);
                // calling onPrepareOptionsMenu() to hide action bar icons
                invalidateOptionsMenu();

            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // toggle nav drawer on selecting action bar app icon/title
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {
            oAuthBrowser();
        }

        // Activate the navigation drawer toggle
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);

    }

    /**
     * launch the OAuth browser page to get credentials
     */
    private void oAuthBrowser() {

        try {
            // create a OAuthLoginManager object with portalURL, clientID, redirectUri and expiration
            String[] portalSettings = getResources().getStringArray(R.array.portal);
            oauthLoginManager = new OAuthLoginManager(portalSettings[1], portalSettings[2], portalSettings[3], 0);
            // launch the browser to get the credentials
            oauthLoginManager.launchOAuthBrowserPage(getApplicationContext());

        } catch (Exception e) {
            Log.e("error-", e.getMessage() + "");
        }


    }

    /***
     * Called when invalidateOptionsMenu() is triggered
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // if nav drawer is opened, hide the action items
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            menu.findItem(R.id.action_save).setVisible(false);
        } else {
            menu.findItem(R.id.action_save).setVisible(true);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    /**
     * sync the state of the drawer toggle
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggle
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * create a ArcGISMap for the selected position
     *
     * @param choice chosen Base-map
     */
    private void setBasemap(int choice) {

        removeLayers();
        switch (choice) {
            case 0:
                mMap = new ArcGISMap(Basemap.createStreets());
                // set the map to be displayed in this view
                mMapView.setMap(mMap);
                break;
            case 1:
                mMap = new ArcGISMap(Basemap.createImagery());
                // set the map to be displayed in this view
                mMapView.setMap(mMap);
                break;
            case 2:
                mMap = new ArcGISMap(Basemap.createTopographic());
                // set the map to be displayed in this view
                mMapView.setMap(mMap);
                break;
            case 3:
                mMap = new ArcGISMap(Basemap.createOceans());
                // set the map to be displayed in this view
                mMapView.setMap(mMap);
                break;
        }

        mMapView.setViewpointWithDurationAsync(viewpoint, 7);

    }

    /**
     * Remove the operational layers from the Map
     */
    private void removeLayers() {
        if (mMap.getOperationalLayers().size() == 2) {
            for (int i = 0; i < mMap.getOperationalLayers().size(); i++) {
                mMap.getOperationalLayers().remove(0);
            }
        }
        if (mMap.getOperationalLayers().size() == 1) {
            mMap.getOperationalLayers().remove(0);
        }
    }

    /**
     * Class BasemapClickListener listens for item click and sets the Basemap for selected choice.
     */
    private class BasemapClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // display view for selected nav drawer item
            mBasemapListView.setSelection(position);
            // set the basemap
            setBasemap(position);
            Toast.makeText(getApplicationContext(), "Selected " + mBasemapTiles[position], Toast.LENGTH_SHORT).show();
        }
    }


}
