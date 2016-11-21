# Authentication Profile
The Authentication Profile sample use the `DefaultAuthenticationChallengeHandler` class to take care of showing an authentication dialog for logging into a `Portal`. Once authenticated against the portal the app displays information about the authenticated user's profile.

![Authentication Profile App](auth-profile.png) 

## Features
* AuthenticationManager
* DefaultAuthenticationChallengeHandler
* PortalInfo
* PortalUser

## Developer Pattern
The `DefaultAuthenticationChallengeHandler` is the default implementation of the `AuthenticationChallengeHandler` interface to handle  all security types that ArcGIS supports (including OAuth).  This sample uses it to authenticate with a portal.  This is set on the `AuthenticationManager`. To authenticate the user you create a `Portal` object, provide the essential credentials and then asynchronously load the portal as shown below:

```java
// Set the DefaultAuthenticationChallegeHandler to allow authentication with the portal.
DefaultAuthenticationChallengeHandler handler = new DefaultAuthenticationChallengeHandler(this);
AuthenticationManager.setAuthenticationChallengeHandler(handler);
// Set loginRequired to true always prompt for credential,
// When set to false to only login if required by the portal
final Portal portal = new Portal("http://www.arcgis.com", true);
```

The `Portal` class follows the loadable pattern and includes listeners to monitor the status of loading the portal.

```java
portal.addDoneLoadingListener(new Runnable() {
    @Override
    public void run() {
        if (portal.getLoadStatus() == LoadStatus.LOADED) {
        // loaded
    }
});
```

Once the portal has loaded you can access information about the portal and authenticated user.  

```java
// Get the authenticated portal user
PortalUser user = portal.getUser();
// get the users full name
String userName = user.getFullName();
```