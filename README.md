# IndoorGPS
An android application developed under ACMS (Amazon Campus Mentorship Series).

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
- [ ] Adding Geofencing feature
- [ ] Authentication

### Development Choices
*(Click to expand)*
<details>
  <summary><b>Getting location updates</b></summary>
        
Mainly, there are two different ways to do it:

<b>1. Android Location API</b>
<br />
Android Location API has 3 location providers:<br>
  1.	NETWORK_PROVIDER<br />
          - Calculates location using nearest cell towers and wifi access points.<br />
          - Uses ACCESS_COARSE_LOCATION permission which allows the app to get only an approximated location.<br />
          - It is fast and battery consumption is low.<br /> 
          - But Accuracy is not good.<br />
  2.	GPS_PROVIDER<br />
          - Gets location values using satellites.<br />
          - Uses ACCESS_FINE_LOCATION permission to provide a more precise/accurate location.<br /> 
          - It gives high accuracy of current location.<br /> 
          - But need continuous power supply and takes some time to give results.<br />  
  3.	PASSIVE_PROVIDER<br />
          - Does not request location updates itself.<br />
          - Passively receives location information from other applications that are using location services.<br /> 
          - This is not reliable because if no other app on the phone is getting location updates, our app won't get them either.<br /> 
          - Accuracy is also very low.<br />
 <br />         
As GPS is most accurate so using that would be an obvious choice. But inside buildings, sometimes GPS is not available and in that case we might want to switch to Network provider until GPS becomes available again. But it causes huge battery drain to switch to exact location provider and also takes long to give results.<br />
<br />
<b>2. FusedLocationProviderClient by Google Play Services</b><br />
<br />
This is built on top of Android’s API and automatically chooses what underlying provider to use on the basis of accuracy, battery usage, performance improvement etc.<br />
<br />
According to the docs:<br />
<blockquote>
The Google Play services location APIs are preferred over the Android framework location APIs (android.location) as a way of adding location awareness to your app. If you are currently using the Android framework location APIs, you are strongly encouraged to switch to the Google Play services location APIs as soon as possible.<br/>
…<br />
The Google Location Services API, part of Google Play Services, provides a more powerful, high-level framework that automatically handles location providers, user movement, and location accuracy. It also handles location update scheduling based on power consumption parameters you provide. In most cases, you'll get better battery performance, as well as more appropriate accuracy, by using the Location Services API.
</blockquote>
<br /><br />
It's drawback is that app will only be able to run on devices with google play services installed in it.<br />
<br />
<b><u>Proposed solution:</u></b><br />
- Check if the user’s device has the play services installed.<br />
- If yes, then use FusedLocationProviderClient. <br />
- Otherwise, use Android's Location API.<br />
<br />
</details>

## References
