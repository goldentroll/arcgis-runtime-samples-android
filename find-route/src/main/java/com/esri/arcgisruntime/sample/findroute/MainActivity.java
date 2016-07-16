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

package com.esri.arcgisruntime.sample.findroute;

import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
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

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.ArcGISVectorTiledLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.tasks.route.DirectionManeuver;
import com.esri.arcgisruntime.tasks.route.Route;
import com.esri.arcgisruntime.tasks.route.RouteParameters;
import com.esri.arcgisruntime.tasks.route.RouteResult;
import com.esri.arcgisruntime.tasks.route.RouteTask;
import com.esri.arcgisruntime.tasks.route.Stop;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "FindRouteSample";
    private ProgressDialog mProgressDialog;

    private MapView mMapView;
    private RouteTask mRouteTask;
    private RouteParameters mRouteParams;
    private Point mSourcePoint;
    private Point mDestinationPoint;
    private Route mRoute;
    private SimpleLineSymbol mRouteSymbol;
    private GraphicsOverlay mGraphicsOverlay;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        // inflate MapView from layout
        mMapView = (MapView) findViewById(R.id.mapView);
        // create new Vector Tiled Layer from service url
        ArcGISVectorTiledLayer mVectorTiledLayer = new ArcGISVectorTiledLayer(
                getResources().getString(R.string.navigation_vector));

        // set tiled layer as basemap
        Basemap basemap = new Basemap(mVectorTiledLayer);
        // create a map with the basemap
        ArcGISMap mMap = new ArcGISMap(basemap);
        // create a viewpoint from lat, long, scale
        Viewpoint sanDiegoPoint = new Viewpoint(32.7157, -117.1611, 200000);
        // set initial map extent
        mMap.setInitialViewpoint(sanDiegoPoint);
        // set the map to be displayed in this view
        mMapView.setMap(mMap);

        // inflate navigation drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        FloatingActionButton mDirectionFab = (FloatingActionButton) findViewById(R.id.fab);

        setupDrawer();

        setupSymbols();

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(getString(R.string.progress_title));
        mProgressDialog.setMessage(getString(R.string.progress_message));


        mDirectionFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProgressDialog.show();

                if(getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setHomeButtonEnabled(true);
                    setTitle(getString(R.string.app_name));
                }
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

                // create RouteTask instance
                mRouteTask = new RouteTask(getString(R.string.routing_service));

                final ListenableFuture<RouteParameters> listenableFuture = mRouteTask.generateDefaultParametersAsync();
                listenableFuture.addDoneListener(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (listenableFuture.isDone()) {
                                int i = 0;
                                mRouteParams = listenableFuture.get();

                                // get List of Stops
                                List<Stop> routeStops = mRouteParams.getStops();
                                // set return directions as true to return turn-by-turn directions in the result of getDirectionManeuvers().
                                mRouteParams.setReturnDirections(true);

                                // add your stops to it 32.7254716,-117.1508181 32.7076359,-117.1592837 -117.15557279683529
                                //-13041171, 3860988, SpatialReference(3857) -13041693, 3856006, SpatialReference(3857)
                                routeStops.add(new Stop(new Point(-117.15083257944445, 32.741123367963446, SpatialReferences.getWgs84())));
                                routeStops.add(new Stop(new Point(-117.15557279683529, 32.703360305883045, SpatialReferences.getWgs84())));

                                // solve
                                RouteResult result = mRouteTask.solveAsync(mRouteParams).get();
                                final List routes = result.getRoutes();
                                mRoute = (Route) routes.get(0);
                                // create a mRouteSymbol graphic
                                Graphic routeGraphic = new Graphic(mRoute.getRouteGeometry(), mRouteSymbol);
                                // add mRouteSymbol graphic to the map
                                mGraphicsOverlay.getGraphics().add(routeGraphic);
                                // get directions
                                // NOTE: to get turn-by-turn directions Route Parameters should set returnDirection flag as true
                                final List<DirectionManeuver> directions = mRoute.getDirectionManeuvers();

                                String[] directionsArray = new String[directions.size()];

                                for (DirectionManeuver dm : directions) {
                                    directionsArray[i++] = dm.getDirectionText();
                                }
                                Log.d(TAG,directions.get(0).getGeometry().getExtent().getXMin() + "");
                                Log.d(TAG,directions.get(0).getGeometry().getExtent().getYMin() + "");

                                // Set the adapter for the list view
                                mDrawerList.setAdapter(new ArrayAdapter<>(getApplicationContext(),
                                        R.layout.drawer_layout_text, directionsArray));

                                if (mProgressDialog.isShowing()) {
                                    mProgressDialog.dismiss();
                                }
                                mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        if(mGraphicsOverlay.getGraphics().size() > 3) {
                                            mGraphicsOverlay.getGraphics().remove(mGraphicsOverlay.getGraphics().size() - 1);
                                        }
                                        mDrawerLayout.closeDrawers();
                                        DirectionManeuver dm = directions.get(position);
                                        Geometry gm = dm.getGeometry();
                                        Viewpoint vp = new Viewpoint(gm.getExtent(),20);
                                        mMapView.setViewpointWithDurationAsync(vp,3);
                                        SimpleLineSymbol selectedRouteSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.GREEN, 5);
                                        Graphic selectedRouteGraphic = new Graphic(directions.get(position).getGeometry(), selectedRouteSymbol);
                                        mGraphicsOverlay.getGraphics().add(selectedRouteGraphic);
                                    }
                                });

                            }
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                });
            }
        });


    }

    /**
     * Set up the Source, Destination and mRouteSymbol graphics symbol
     */
    private void setupSymbols() {

        mGraphicsOverlay = new GraphicsOverlay();

        //add the overlay to the map view
        mMapView.getGraphicsOverlays().add(mGraphicsOverlay);

        //[DocRef: Name=Picture Marker Symbol Drawable-android, Category=Fundamentals, Topic=Symbols and Renderers]
        //Create a picture marker symbol from an app resource
        BitmapDrawable startDrawable = (BitmapDrawable) ContextCompat.getDrawable(this, R.drawable.ic_source);
        final PictureMarkerSymbol pinSourceSymbol = new PictureMarkerSymbol(startDrawable);
        pinSourceSymbol.loadAsync();
        //[DocRef: END]
        pinSourceSymbol.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                //add a new graphic as start point
                mSourcePoint = new Point(-117.15083257944445, 32.741123367963446, SpatialReferences.getWgs84());
                Graphic pinSourceGraphic = new Graphic(mSourcePoint, pinSourceSymbol);
                mGraphicsOverlay.getGraphics().add(pinSourceGraphic);
            }
        });
        pinSourceSymbol.setOffsetY(20);
        BitmapDrawable endDrawable = (BitmapDrawable) ContextCompat.getDrawable(this, R.drawable.ic_destination);
        final PictureMarkerSymbol pinDestinationSymbol = new PictureMarkerSymbol(endDrawable);
        pinDestinationSymbol.loadAsync();
        //[DocRef: END]
        pinDestinationSymbol.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                //add a new graphic as end point
                mDestinationPoint = new Point(-117.15557279683529, 32.703360305883045, SpatialReferences.getWgs84());
                Graphic destinationGraphic = new Graphic(mDestinationPoint, pinDestinationSymbol);
                mGraphicsOverlay.getGraphics().add(destinationGraphic);
            }
        });
        pinDestinationSymbol.setOffsetY(20);

        mRouteSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLUE, 5);
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

    /**
     * set up the drawer
     */
    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        super.onConfigurationChanged(newConfig);

        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        // Activate the navigation drawer toggle
        return (mDrawerToggle.onOptionsItemSelected(item)) || super.onOptionsItemSelected(item);
    }
}
