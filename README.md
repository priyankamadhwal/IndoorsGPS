# iExplore-app
An android application developed under ACMS (Amazon Campus Mentorship Series) project.

### Problem Statement
Build a web tool which shows a building boundary and all the people moving inside that building. It will capture the movement data using an app which will be installed on the mobile devices of all those people, and that app will keep sending location information to a server periodically. The location accuracy should not be more than 5 meters.

### Built With
* Java programming language
* Android SDK
* Android Studio IDE

## Getting Started

### Prerequisites
Android Studio, Git, Submodules: iExplore-server and iExplore-webapp

### Installation
To import this project into Android Studio, proceed as follows:

1. Click **File** > **New** > **Project from Version Control** > **Git**.
2. Enter URL: https://github.com/priyankamadhwal/iExplore-app.git
3. Click **Clone**.
4. Configure [Google API Console project](https://developers.google.com/identity/sign-in/android/start-integrating#configure_a_project).
5. Download and add *credentials.json* to **app** folder.
6. Get the **Web application** type client id from [Credentials Page](https://console.developers.google.com/apis/credentials) in the API console project and add it to SERVER_CLIENT_ID in /app/src/main/java/com/acms/iexplore/data/**Constants.java**.
7. Edit IP_ADDRESS, PORT_SERVER and PORT_WEB in /app/src/main/java/com/acms/iexplore/data/**Constants.java**.
6. [Build the app](https://developer.android.com/studio/run#reference).

### Project Setup
1. Install [iExplore-server](https://github.com/shubhangi-ghosh/ACMS_server).
2. Run server ```node app.js```
3. Install [iExplore-webapp](https://github.com/Shrutikatyal/iExplore-web).
4. Run webapp: ```ng serve --host <ip address>```
5. [Build and run the app](https://developer.android.com/studio/run).

**Note:** 
1. Make sure that the server, webapp and android app are connected to the same network.
2. To run the app on emulator use ip address: 10.0.2.2
3. If you are runnig the app on emulator, then run the webapp using ```ng serve``` only.

## Dev

### To Do
- [x] Setting up Permissions  
- [x] Getting location updates when application is running in foreground
- [x] Creating a notification channel for foreground service
- [x] Getting location updates when application is in background or killed (Service)
- [x] Updating UI from Service using a Broadcast Reciever
- [x] Connecting to server using an HTTP Client
- [x] Adding Geofence for a single building
- [x] Adding Geofences for multiple buildings (stored in DB)
- [x] Authentication
- [x] UI Design
- [ ] Unit testing
- [ ] Integration testing

### Development Decisions
*(Click to expand)*
<details>
  <summary><b>Runtime permissions</b></summary>
  <br />
  Android M (API 23) introduced runtime permissions, letting user to allow or deny any permission at runtime.<br />
  To use location services, this application uses ACCESS_FINE_LOCATION permission and ACCESS_BACKGROUND_LOCATION permission (Android Q and above).<br/><br />
  <b><u>Our solution:</u></b><br />
  For granting each and every permission, long and tedious code is required. So, to overcome this, we are using <b>Dexter</b> library which simplifies the process of requesting runtime permissions.
  <br /><br />
</details>

<details>
  <summary><b>Getting location updates</b></summary>
      <br />  
Mainly, there are two different ways to do it:

<b>1. Android Framework Location API</b>
<br />
It has 3 location providers:<br>
  1.	NETWORK_PROVIDER<br />
          - Calculates location using nearest cell towers and wifi access points.<br />
          - Uses ACCESS_COARSE_LOCATION permission which allows the app to get only an approximated location.<br />
          - It is fast and battery consumption is low.<br /> 
          - Accuracy is not very good.<br />
  2.	GPS_PROVIDER<br />
          - Gets location values using satellites.<br />
          - Uses ACCESS_FINE_LOCATION permission to provide a more precise/accurate location.<br /> 
          - Gives high accuracy of current location.<br /> 
          - Needs continuous power supply.
          - Might be slow sometimes.<br />  
  3.	PASSIVE_PROVIDER<br />
          - Does not request location updates itself.<br />
          - Passively receives location information from other applications that are using location services.<br /> 
          - Not reliable because if no other app on the phone is getting location updates, our app won't get them either.<br /> 
          - Accuracy is also very low.<br />
 <br />         
As GPS is most accurate, so using that would be an obvious choice. But inside buildings, sometimes GPS is not available and in that case we might want to switch to Network provider until GPS becomes available again. But it causes huge battery drain to switch to exact location provider and may take a little longer to give the result.<br />
<br />
<b>2. FusedLocationProviderClient by Google Play Services</b><br />
<br />
This is built on top of Android’s API and automatically chooses what underlying provider to use on the basis of accuracy, battery usage, speed etc.
<br /><br />
According to the docs:<br />
<blockquote>
The Google Play services location APIs are preferred over the Android framework location APIs (android.location) as a way of adding location awareness to your app. If you are currently using the Android framework location APIs, you are strongly encouraged to switch to the Google Play services location APIs as soon as possible.<br/>
…<br />
The Google Location Services API, part of Google Play Services, provides a more powerful, high-level framework that automatically handles location providers, user movement, and location accuracy. It also handles location update scheduling based on power consumption parameters you provide. In most cases, you'll get better battery performance, as well as more appropriate accuracy, by using the Location Services API.
</blockquote>
<br />
It's drawback is that app will only be able to run on devices with Google Play services installed in it.<br />
<br />
<b><u>Our solution:</u></b><br />
We will be using the FusedLocationProviderClient along with other Google Play Services APIs: Geofencing and Google sign-in.
<br />
<br />
</details>

<details>
  <summary><b>Services in Android</b></summary>
  <br />
  <b>Background services: </b><br /><br />        
<p>Whenever an application runs in the background using services, it consumes memory and battery which are very limited resources. So, Android O onwards, the application is allowed to create and run background services only for a few minutes after which they are killed by the system. </p>
<p>Some periodic task can be created using a scheduler that will start service again after some given interval, service will do its work and then stop itself again. By this, the application will not be considered battery draining. But there are some limitations in the number of times an app can request location update in background. Also the doze mode and app standby delays the execution by some amount of time if the phone is idle.</p>
  <br />
  <b>Foreground services: </b><br /><br /> 
<p>A foreground service will keep the user aware that application is performing some background tasks by displaying a persistent notification and the system will consider it to be something the user is actively aware of and thus not a candidate for killing when low on memory or power.</p>
  <p>But as this notification couldn't be dismissed, users may find this behavior annoying.</p>
  <br />
  <p><b><u>Our solution:</u></b><br />
    We will be using a Foreground Service for Android versions O and above as it makes it possible to get uninterrupted continuous location updates which is very essential for this app.
  </p>
  <br />
</details>

<details>
  <summary><b>Android Broadcasts</b></summary>
  <br />
  <b>Global vs. Local Broadcasts</b>
  <br/>
  <p>
    Using a <i>global broadcast</i>, any other application can also send and receive broadcast messages to and from our application. This can be a serious security threat for the application. Also global broadcast is sent system-wide, so it is not performance efficient.
  </p>
  <p>
    <br />
    Android provides <i>local broadcasts</i> with the <b>LocalBroadcastManager</b> class which provides following benifits:<br />
<ul>
  <li>Broadcast data won’t leave your app, so don’t need to worry about leaking private data.</li>
<li>It is not possible for other applications to send these broadcasts to your app, so you don’t need to worry about having security holes they can exploit.</li>
<li>It is more efficient than sending a global broadcast through the system.</li>
<li>No overhead of system-wide broadcast.</li>
  </ul>
<p>
<br />
<b><u>Our solution:</u></b><br />
  We are using a BroadcastReceiver to receive the updated location in <i>MainActivity</i> that is going to be broadcast locally from the Service and then update the UI.
<br /><br />
</details>

<details>
  <summary><b>Connecting the app with Server</b></summary><br />
  There are a lot of networking libraries that can be used for this purpose- OkHttp, AndroidAsync, Retrofit, Volley, Robospice etc.<br /><br /> 
  <b><u>Our solution:</u></b><br />
  We are using <b>Retrofit</b> in this project because of following reasons:<br />
  <ul>
    <li>Easy to understand and use</li>
    <li>Treats the Api calls as simple java method calls</li>
    <li>Handles the Json/Xml parsing itself</li>
    <li>We do not have too many custom requirements in terms of caching and request prioritization</li>
    <li>Good community support</li>
  </ul>
<br />
</details>

<details>
  <summary><b>Geofencing</b></summary>
  <br />
  <p>
Geofencing combines awareness of the user's current location with awareness of the user's proximity to locations that may be of interest. To mark a location of interest, you specify its latitude and longitude. To adjust the proximity for the location, you add a radius. The latitude, longitude, and radius define a geofence, creating a circular area, or fence, around the location of interest.
<p>
  <b>Points to consider:</b><br />
  <ul>
    <li>You can have multiple active geofences, with a limit of 100 per app, per device user.</li>
    <li>For best results, the minimium radius of the geofence should be set between 100 - 150 meters.</li>
    <li>When Wi-Fi is available location accuracy is usually between 20 - 50 meters. When indoor location is available, the accuracy range can be as small as 5 meters. Unless you know indoor location is available inside the geofence, assume that Wi-Fi location accuracy is about 50 meters. When Wi-Fi location isn't available (for example, rural areas) the location accuracy degrades further.</li>
    <li>If there is no reliable data connection, alerts might not be generated. This is because the geofence service depends on the network location provider which in turn requires a data connection.</li>
    <li>The geofence service doesn't continuously query for location, so expect some latency when receiving alerts. Usually the latency is less than 2 minutes, even less when the device has been moving. If Background Location Limits are in effect, the latency is about 2-3 minutes on average. If the device has been stationary for a significant period of time, the latency may increase (up to 6 minutes).</li>
  </ul>
<br />
<b><u>Our solution:</u></b><br />
  In this app, we are using geofences to determine when a user enters or exits a building. The app will start sending the location updates to server as soon as the ENTER event is triggered and stop the moment the EXIT event is triggered. Also, geofences are being added to monitor multiple buildings so that we can know in which building the user currently is.
<br /><br />
</details>

<details>
  <summary><b>Authentication</b></summary>
  <br />
  <p>
    We should verify a user's identity before giving him access to the app.
  </p>
<br />
<b><u>Our solution:</u></b><br />
  Currently, we are just using the <b>Google sign-in</b> option to let the user quickly and easily register/sign-in to our app with their existing Google account.
<br /><br />
</details>

## Workflow
   
![workflow](https://i.postimg.cc/MpGbDbbX/app-work-flow-1.png)

## Screenshots
<details>
  <summary>Click to view</summary>
  <br />
  <p align="center">
    <img src="https://i.postimg.cc/9QNHL5Kp/Screenshot-20200509-122208-i-Explore.jpg" width="150" />
    <img src="https://i.postimg.cc/7Y2zHwMC/Screenshot-20200509-122215-i-Explore.jpg" width="150" />
    <img src="https://i.postimg.cc/kg7WfLKB/Screenshot-20200509-122351-Google-Play-services.jpg" width="150" />
    <img src="https://i.postimg.cc/sgnGKx3N/Screenshot-20200509-122535-Package-installer.jpg" width="150" />
    <img src="https://i.postimg.cc/RCk0Sf0m/Screenshot-20200509-130459-i-Explore.jpg" width="150" />
    <img src="https://i.postimg.cc/jj3sGn1G/Screenshot-20200509-123115-i-Explore.jpg" width="150" />
    <img src="https://i.postimg.cc/k5GCFX5w/Screenshot-20200509-130511-i-Explore.jpg" width="150" />
    <img src="https://i.postimg.cc/8C78fKy0/Screenshot-20200509-130517-i-Explore.jpg" width="150" />
    <img src="https://i.postimg.cc/FHn8vb9g/Screenshot-20200509-130525-i-Explore.jpg" width="150" />
    <img src="https://i.postimg.cc/P5c39t4S/Screenshot-20200509-130634-i-Explore.jpg" width="150" />
<!--     <img src="/screenshots/Screenshot_11_iExplore.jpg" width="150" /> -->
  </p>
</details>

## References
- https://github.com/Karumi/Dexter
- https://www.youtube.com/watch?v=ycja50TzjoU
- https://stackoverflow.com/a/42964535
- https://medium.com/@maheshikapiumi/android-location-services-7894cea13878
- https://developer.android.com/training/location/request-updates
- https://medium.com/@kevalpatel2106/how-to-handle-background-services-in-android-o-f96783e65268
- https://android.jlelse.eu/local-broadcast-less-overhead-and-secure-in-android-cfa343bb05be
- https://androidwave.com/foreground-service-android-example/
- https://www.youtube.com/watch?v=rNYaEFl6Fms
- https://www.youtube.com/watch?v=nmAtMqljH9M
- https://developer.android.com/training/location/geofencing
- https://developers.google.com/identity/sign-in/android/start-integrating
