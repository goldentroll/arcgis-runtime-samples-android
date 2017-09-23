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

package com.esri.arcgisruntime.sample.openexistingmap;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalItem;

public class MainActivity extends AppCompatActivity {

    private MapView mMapView;
    private ArcGISMap mMap;
    private Portal mPortal;
    private PortalItem mPortalItem;

    private ListView mDrawerList;

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawerList = (ListView)findViewById(R.id.navList);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();

        // inflate MapView from layout
        mMapView = (MapView) findViewById(R.id.mapView);

        // get the portal url for ArcGIS Online
        mPortal = new Portal(getResources().getString(R.string.portal_url));
        // get the pre-defined portal id and portal url
        mPortalItem = new PortalItem(mPortal, getResources().getString(R.string.webmap_houses_with_mortgages_id));
        // create a map from a PortalItem
        mMap = new ArcGISMap(mPortalItem);
        // set the map to be displayed in this view
        mMapView.setMap(mMap);

        // add the webmap titles to the drawer
        addDrawerItems();
        setupDrawer();

        // set icons on action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void addDrawerItems(){
        String[] webmapTitles = {getResources().getString(R.string.webmap_houses_with_mortgages_title),
                            getResources().getString(R.string.webmap_usa_tapestry_segmentation_title),
                            getResources().getString(R.string.webmap_geology_us_title)};
        ArrayAdapter<String> mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, webmapTitles);
        mDrawerList.setAdapter(mAdapter);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull AdapterView<?> adapterView, @NonNull View view, int position, long id) {
                if(position == 0){
                    mPortalItem = new PortalItem(mPortal, getResources().getString(R.string.webmap_houses_with_mortgages_id));
                    // create a map from a PortalItem
                    mMap = new ArcGISMap(mPortalItem);
                    // set the map to be displayed in this view
                    mMapView.setMap(mMap);
                    // close the drawer
                    mDrawerLayout.closeDrawer(adapterView);
                }else if(position == 1){
                    mPortalItem = new PortalItem(mPortal, getResources().getString(R.string.webmap_usa_tapestry_segmentation_id));
                    // create a map from a PortalItem
                    mMap = new ArcGISMap(mPortalItem);
                    // set the map to be displayed in this view
                    mMapView.setMap(mMap);
                    // close the drawer
                    mDrawerLayout.closeDrawer(adapterView);
                }else if(position == 2){
                    mPortalItem = new PortalItem(mPortal, getResources().getString(R.string.webmap_geology_us));
                    // create a map from a PortalItem
                    mMap = new ArcGISMap(mPortalItem);
                    // set the map to be displayed in this view
                    mMapView.setMap(mMap);
                    // close the drawer
                    mDrawerLayout.closeDrawer(adapterView);
                }
            }
        });
    }

    private void setupDrawer(){
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close){

            // called when drawer has settled in an open state
            public void onDrawerOpened(View drawerView){
                super.onDrawerOpened(drawerView);
                // change the title to the nav bar
                getSupportActionBar().setTitle(getResources().getString(R.string.navbar_title));
                // invalidate options menu
                invalidateOptionsMenu();
            }

            // called when drawer has settled in a closed state
            public void onDrawerClosed(View view){
                super.onDrawerClosed(view);
                // set title to the app
                getSupportActionBar().setTitle(mActivityTitle);
                // invalidate options menu
                invalidateOptionsMenu();
            }
        };
        // enable draw indicator
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        // attach toggle to drawer layout
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    protected void onPause(){
        super.onPause();
        mMapView.pause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        mMapView.resume();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState){
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // activate the navigation drawer toggle
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);

    }
}
