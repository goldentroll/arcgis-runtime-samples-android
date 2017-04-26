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

package com.esri.arcgisruntime.sample.authenticationprofile;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalInfo;
import com.esri.arcgisruntime.portal.PortalUser;
import com.esri.arcgisruntime.security.AuthenticationManager;
import com.esri.arcgisruntime.security.DefaultAuthenticationChallengeHandler;

public class MainActivity extends AppCompatActivity {

    private TextView userText;
    private TextView emailText;
    private TextView portalNameText;
    private TextView createDate;
    private ImageView userImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set the DefaultAuthenticationChallengeHandler to allow authentication with the portal.
        DefaultAuthenticationChallengeHandler handler = new DefaultAuthenticationChallengeHandler(this);
        AuthenticationManager.setAuthenticationChallengeHandler(handler);
        // Set loginRequired to true always prompt for credential,
        // When set to false to only login if required by the portal
        final Portal portal = new Portal("https://www.arcgis.com", true);
        portal.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                if (portal.getLoadStatus() == LoadStatus.LOADED) {
                    // Get the portal information
                    PortalInfo portalInformation = portal.getPortalInfo();
                    String portalName = portalInformation.getPortalName();
                    portalNameText = (TextView) findViewById(R.id.portal);
                    portalNameText.setText(portalName);

                    // this portal does not require authentication, if null send toast message
                    if(portal.getUser() != null){
                        // Get the authenticated portal user
                        PortalUser user = portal.getUser();
                        // get the users full name
                        String userName = user.getFullName();
                        // update the textview
                        userText = (TextView) findViewById(R.id.userName);
                        userText.setText(userName);
                        // get the users email
                        String email = user.getEmail();
                        // update the textview
                        emailText = (TextView) findViewById(R.id.email);
                        emailText.setText(email);
                        // get the created date
                        Calendar startDate = user.getCreated();
                        // format date
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
                        // get string format
                        String formatDate = simpleDateFormat.format(startDate.getTime());
                        // update textview
                        createDate = (TextView) findViewById(R.id.create_date);
                        createDate.setText(formatDate);
                        // check if user profile thumbnail exists
                        if (user.getThumbnailFileName() == null) {
                            return;
                        }
                        // fetch the thumbnail
                        final ListenableFuture<byte[]> thumbnailFuture = user.fetchThumbnailAsync();
                        thumbnailFuture.addDoneListener(new Runnable() {
                            @Override
                            public void run() {
                                // get the thumbnail image data
                                byte[] itemThumbnailData;
                                try {
                                    itemThumbnailData = thumbnailFuture.get();

                                    if ((itemThumbnailData != null) && (itemThumbnailData.length > 0)) {
                                        // create a Bitmap to use as required
                                        Bitmap itemThumbnail = BitmapFactory.decodeByteArray(itemThumbnailData, 0, itemThumbnailData.length);
                                        // set the Bitmap onto the ImageView
                                        userImage = (ImageView) findViewById(R.id.userImage);
                                        userImage.setImageBitmap(itemThumbnail);
                                    }
                                } catch (InterruptedException | ExecutionException e) {
                                    Log.d("TEST", e.getMessage());
                                }
                            }
                        });
                    } else {
                        // send message that user did not authenticate
                        Toast.makeText(getApplicationContext(), "User did not authenticate against " + portalName, Toast.LENGTH_LONG).show();
                    }

                }
            }
        });
        portal.loadAsync();
    }
}

