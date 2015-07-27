/* Copyright 2014 ESRI
 *
 * All rights reserved under the copyright laws of the United States
 * and applicable international laws, treaties, and conventions.
 *
 * You may freely redistribute and use this sample code, with or
 * without modification, provided you include the original copyright
 * notice and use restrictions.
 *
 * See the sample code usage restrictions document for further information.
 *
 */

package com.esri.arcgis.android.samples.addcsv2graphic;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.map.Callout;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.core.geometry.Point;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleMarkerSymbol;

public class AddCSVActivity extends Activity {

  // ArcGIS Android elements
  MapView mMapView = null;
  ArcGISTiledMapServiceLayer basemapTileLayer;
  GraphicsLayer graphicsLayer = null;

  // When the Date Picker dialog appears, set the default date to be the current date.
  static final int DATE_DIALOG_ID = 0;
  Calendar rightNow = Calendar.getInstance();
  int mYear = rightNow.get(Calendar.YEAR);
  int mMonth = rightNow.get(Calendar.MONTH);
  int mDay = rightNow.get(Calendar.DAY_OF_MONTH);

  ProgressDialog dialog;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    // Retrieve the map and initial extent from XML layout
    mMapView = (MapView) findViewById(R.id.map);
    /* create a @ArcGISTiledMapServiceLayer */
    basemapTileLayer = new ArcGISTiledMapServiceLayer(
        "http://server.arcgisonline.com/ArcGIS/rest/services/ESRI_StreetMap_World_2D/MapServer");
    // add tiled basemap to Map View
    mMapView.addLayer(basemapTileLayer);
    // add graphics layer
    graphicsLayer = new GraphicsLayer();
    mMapView.addLayer(graphicsLayer);

    // enable wrap around
    mMapView.enableWrapAround(true);
    // attribute map
    mMapView.setEsriLogoVisible(true);

    MyOnSingleTapListener listener = new MyOnSingleTapListener(this);
    mMapView.setOnSingleTapListener(listener);

    Button datePicker = (Button) findViewById(R.id.datePicker1);

    datePicker.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // refactor to use FramentDialog
        showDialog(DATE_DIALOG_ID);
      }
    });

  }

  @Override
  protected Dialog onCreateDialog(int id) {
    switch (id) {
    case DATE_DIALOG_ID:
      return new DatePickerDialog(this, mDateSetListener, mYear, mMonth, mDay);
    }
    return null;
  }

  private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
      mYear = year;
      mMonth = monthOfYear;
      mDay = dayOfMonth;

      String sday;
      String smonth;
      String syear;

      if (mDay < 10) {
        sday = "0" + mDay;
      } else {
        sday = "" + mDay;
      }

      mMonth++;
      if (mMonth < 10) {
        smonth = "0" + mMonth;
      } else {
        smonth = "" + mMonth;
      }

      syear = mYear - 2000 + "";

      String date = syear + smonth + sday;

      FetchEvents aef = new FetchEvents();
      aef.execute(date);

    }
  };

  void getCSVReport(String date) {
    /*
     * This is where the feeds are brought in for tornado reports, hail reports
     * and wind reports. The reports come in with X,Ys and comments. The
     * reported locations are added as features to a GraphicsLayer.
     */
    graphicsLayer.removeAll();
    getWindEvents(date);
    getHailEvents(date);
    getTornadoEvents(date);
  }

  private void getWindEvents(String date) {

    // Find all of the WIND events. Use URLConnection and read in each line
    // of the CSV file.
    try {
      URL wind = new URL("http://www.spc.noaa.gov/climo/reports/" + date + "_rpts_wind.csv");
      URLConnection windc = wind.openConnection();
      BufferedReader reader = new BufferedReader(new InputStreamReader(windc.getInputStream()));
      String inputLine;
      String[] columnNames = {};
      Point mapPnt;

      Graphic graphic;

      Map<String, Object> attr = new HashMap<String, Object>();

      ArrayList<Graphic> windEvents = new ArrayList<Graphic>();
      SimpleMarkerSymbol symbol = new SimpleMarkerSymbol(Color.BLUE, 20, SimpleMarkerSymbol.STYLE.DIAMOND);

      while ((inputLine = reader.readLine()) != null) {
        String[] values = inputLine.split(",");
        if (values[0].equals("Time")) {
          columnNames = values;
          continue;
        }

        double lat = Double.parseDouble(values[5]);
        double lon = Double.parseDouble(values[6]);
        mapPnt = new Point(lon, lat);

        // Create attributes to populate graphic.
        int index = 0;
        for (String key : columnNames) {
          attr.put(key, values[index]);
          index++;
        }

        graphic = new Graphic(mapPnt, symbol, attr);
        windEvents.add(graphic);
      }

      // Call this to add reports to the GraphicsLayer...
      addReports(windEvents);

      // Close the input stream...
      reader.close();

    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  private void getHailEvents(String date) {

    // Find all of the HAIL events. Use URLConnection and read in each line
    // of the CSV file.
    try {
      URL hail = new URL("http://www.spc.noaa.gov/climo/reports/" + date + "_rpts_hail.csv");
      URLConnection hailConnect = hail.openConnection();
      BufferedReader in = new BufferedReader(new InputStreamReader(hailConnect.getInputStream()));
      String inputLine;
      String[] columnNames = {};
      Point pt;
      Graphic graphic;

      Map<String, Object> attr = new HashMap<String, Object>();

      ArrayList<Graphic> hailEvents = new ArrayList<Graphic>();
      SimpleMarkerSymbol symbol = new SimpleMarkerSymbol(Color.GREEN, 20, SimpleMarkerSymbol.STYLE.DIAMOND);

      while ((inputLine = in.readLine()) != null) {
        String[] values = inputLine.split(",");
        if (values[0].equals("Time")) {
          columnNames = values;
          continue;
        }

        double lat = Double.parseDouble(values[5]);
        double lon = Double.parseDouble(values[6]);

        pt = new Point(lon, lat);

        // Create attributes to populate graphic.
        int index = 0;
        for (String key : columnNames) {
          attr.put(key, values[index]);
          index++;
        }

        graphic = new Graphic(pt, symbol, attr);
        hailEvents.add(graphic);
      }

      // Call this to add reports to the GraphicsLayer...
      addReports(hailEvents);

      // Close the input stream...
      in.close();

    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void getTornadoEvents(String date) {

    // Find all of the TORNADO events. Use URLConnection and read in each
    // line of the CSV file.
    try {
      URL tornado = new URL("http://www.spc.noaa.gov/climo/reports/" + date + "_rpts_torn.csv");
      URLConnection tornadoConnection = tornado.openConnection();
      BufferedReader in = new BufferedReader(new InputStreamReader(tornadoConnection.getInputStream()));
      String inputLine;
      String[] columnNames = {};
      Point pt;
      Graphic graphic;

      Map<String, Object> attr = new HashMap<String, Object>();

      ArrayList<Graphic> tornadoEvents = new ArrayList<Graphic>();

      SimpleMarkerSymbol symbol = new SimpleMarkerSymbol(Color.RED, 20, SimpleMarkerSymbol.STYLE.DIAMOND);

      while ((inputLine = in.readLine()) != null) {
        String[] values = inputLine.split(",");
        if (values[0].equals("Time")) {
          columnNames = values;
          continue;
        }

        double lat = Double.parseDouble(values[5]);
        double lon = Double.parseDouble(values[6]);

        pt = new Point(lon, lat);

        // Create attributes to populate graphic.
        int index = 0;
        for (String key : columnNames) {
          attr.put(key, values[index]);
          index++;
        }

        graphic = new Graphic(pt, symbol, attr);
        tornadoEvents.add(graphic);

      }

      addReports(tornadoEvents);
      in.close();

    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void addReports(ArrayList<Graphic> graphics) {
    Graphic graphic;
    // if there is something to add, do it...
    if (graphics.size() > 0) {

      for (int i = 0; i < graphics.size(); i++) {
        graphic = graphics.get(i);
        graphicsLayer.addGraphic(graphic);

      }
    }

  }

  @Override
  protected void onPause() {
    super.onPause();
    mMapView.pause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mMapView.unpause();
  }

  private class MyOnSingleTapListener implements OnSingleTapListener {

    // Here, we use a single tap to popup the attributes for a report...
    Context _ctx;
    private static final long serialVersionUID = 1L;

    public MyOnSingleTapListener(Context ctx) {
      _ctx = ctx;
    }

    @Override
    public void onSingleTap(float x, float y) {
      Callout mapCallout = mMapView.getCallout();
      mapCallout.hide();
      Point pnt = mMapView.toMapPoint(x, y);

      int[] grs = graphicsLayer.getGraphicIDs(x, y, 20);
      Log.d("Test", "Graphics number is " + grs.length);

      if (grs.length > 0) {
        Graphic g = graphicsLayer.getGraphic(grs[0]);
        Map<String, Object> atts = g.getAttributes();
        String text = "";
        for (int i = 0; i < atts.size(); i++) {
          text = text + atts.keySet().toArray()[i] + ": " + atts.values().toArray()[i] + "\n";
        }

        TextView tv = new TextView(_ctx);
        tv.setText(text);

        // Here, we populate the Callout with the attribute information
        // from the report.
        mapCallout.setOffset(0, -3);
        mapCallout.setCoordinates(pnt);
        mapCallout.setMaxHeight(350);
        mapCallout.setMaxWidth(900);
        mapCallout.setStyle(R.xml.mycalloutprefs);
        mapCallout.setContent(tv);

        mapCallout.show();
      }

    }
  }

  private class FetchEvents extends AsyncTask<String, Void, Void> {

    private ProgressDialog mProgDialog;

    // empty constructor
    public FetchEvents() {}

    @Override
    protected void onPreExecute() {
      // remove any previous callouts
      if (mMapView.getCallout().isShowing()) {
        mMapView.getCallout().hide();
      }
      // show progress dialog while searching for events
      mProgDialog = ProgressDialog.show(AddCSVActivity.this, "", "Fetching CVS, Please wait....", true);

    }

    @Override
    protected Void doInBackground(String... dateStrs) {

      if (dateStrs.length > 0) {
        getCSVReport(dateStrs[0]);
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void result) {

      // remove dialog
      if (mProgDialog.isShowing()) {
        mProgDialog.dismiss();
      }
      
      if (graphicsLayer != null && graphicsLayer.getNumberOfGraphics() == 0) {
        // update UI with notice that no results were found
        Toast.makeText(AddCSVActivity.this, "There were no storm reports on this date.", Toast.LENGTH_SHORT).show();
      } else {
        Toast.makeText(AddCSVActivity.this, "tap on an event to see it's information.", Toast.LENGTH_LONG).show();
      }

    }

  }

}