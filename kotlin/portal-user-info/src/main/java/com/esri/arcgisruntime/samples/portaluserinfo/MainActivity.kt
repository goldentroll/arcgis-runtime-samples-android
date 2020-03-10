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

package com.esri.arcgisruntime.samples.portaluserinfo

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.security.AuthenticationManager
import com.esri.arcgisruntime.security.DefaultAuthenticationChallengeHandler
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_profile.*
import org.jetbrains.anko.toast
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // Set the DefaultAuthenticationChallengeHandler to allow authentication with the portal.
    val handler = DefaultAuthenticationChallengeHandler(this)
    AuthenticationManager.setAuthenticationChallengeHandler(handler)
    // Set loginRequired to true always prompt for credential,
    // When set to false to only login if required by the portal
    val portal = Portal("http://www.arcgis.com", true)

    portal.addDoneLoadingListener {

      when (portal.loadStatus) {

        LoadStatus.LOADED -> {
          val portalInformation = portal.portalInfo
          val portalInfoName = portalInformation.portalName
          portalName.text = portalInfoName
          // this portal does not require authentication, if null send toast message
          if (portal.user != null) {
            // Get the authenticated portal user
            val user = portal.user
            // get the users full name
            val fullname = user.fullName
            userName.text = fullname
            // get the users email
            val userEmail = user.email
            email.text = userEmail
            // get the created date
            val startDate = user.created
            val simpleDateFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.US)
            val formatDate = simpleDateFormat.format(startDate.time)
            createDate.text = formatDate
            // check if user profile thumbnail exists
            if (user.thumbnailFileName != null) {
              // fetch the thumbnail
              val thumbnailFuture = user.fetchThumbnailAsync()
              thumbnailFuture.addDoneListener {
                val itemThumbnailData = thumbnailFuture.get()
                if (itemThumbnailData != null && itemThumbnailData.isNotEmpty()) {
                  // create Bitmap to use as required
                  val itemThumbnail =
                    BitmapFactory.decodeByteArray(itemThumbnailData, 0, itemThumbnailData.size)
                  // set the Bitmap to the ImageView
                  userImage.setImageBitmap(itemThumbnail)
                }
              }
            } else {
              toast("No thumbnail associated with $fullname")
            }
          } else {
            toast("User did not authenticate against $portalInfoName")
          }
        }

        LoadStatus.FAILED_TO_LOAD -> {
          toast("Portal failed to load")
        }
      }

    }
    portal.loadAsync()
  }

}
