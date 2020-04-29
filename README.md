# IndoorsGPS
An android application developed under ACMS (Amazon Campus Mentorship Series) project.

### Problem Statement
Build a web tool which shows a building boundary and all the people moving inside that building. It will capture the movement data using an app which will be installed on the mobile devices of all those people, and that app will keep sending location information to a server periodically. The location accuracy should not be more than 5 meters.

### Built With
* Java programming language
* Android SDK
* Android Studio IDE

## Getting Started

### Prerequisites
Android Studio, Git

### Installation
To import this project into Android Studio, proceed as follows:

1. Click **File** > **New** > **Project from Version Control**.
2. Enter URL: https://github.com/priyankamadhwal/IndoorsGPS.git
3. Click **Clone**.
5. [Build and run the app](https://developer.android.com/studio/run).

## Dev

### To Do
- [x] Setting up Permissions  
- [x] Getting location updates when application is running in foreground
- [x] Creating a notification channel for foreground service
- [x] Getting location updates when application is in background or killed (Service)
- [x] Updating UI from Service using a Broadcast Reciever
- [x] Connecting to server using an HTTP Client
- [ ] Adding Geofencing feature _(currently working on this)_
- [ ] Authentication

### Development Decisions
*(Click to expand)*
<details>
  <summary><b>Getting location updates</b></summary>
        
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
- Check if the user’s device has the play services installed.<br />
- If yes, then use FusedLocationProviderClient. <br />
- Otherwise, use Android's Location API.<br />
<i><b>Note: </b>Currently we are using only FusedLocationProviderClient and will add Android Location API in a later update.</i>
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
<br /><br /><br />
</details>

## References
- https://medium.com/@kevalpatel2106/how-to-handle-background-services-in-android-o-f96783e65268
- https://android.jlelse.eu/local-broadcast-less-overhead-and-secure-in-android-cfa343bb05be
