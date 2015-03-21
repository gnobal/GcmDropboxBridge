# GcmDropboxBridge
Registers the device in GCM and saves the registration ID in Dropbox, allowing you to send messages to the device from any device that has Dropbox installed

Setup Steps
===========
This repository is missing a file (**app/src/main/java/net/gnobal/gcmdropboxbridge/Secrets.java**) which needs to contain the private keys you get from Google and Dropbox.

The file needs to look like this:
```
package net.gnobal.gcmdropboxbridge;

public class Secrets {
    // See https://developer.android.com/google/gcm/gcm.html#senderid
    final static String GCM_SENDER_ID = "GCM_SENDER_ID";

    // See https://www.dropbox.com/developers/core/start/android
    final static String DB_APP_KEY = "DROPBOX_APP_KEY";
    final static String DB_APP_SECRET = "DROPBOX_APP_SECRET";
}
```

Once you have this file in your repository, the app can work. Start it and click the "Setup" button. Afterwards you can send messages to your device from a script like this:
```
#!/bin/bash

TOKEN_PATH=~/Dropbox/Apps/GcmBridge/regid.txt
TOKEN=$(<$TOKEN_PATH)
MESSAGE="Hello GCM!"

curl -X POST \
-H "Authorization: key=AIzaSyC5MmIIhfYczL2Q5r-I699vDP7qDtE4DVg" \
-H "Content-Type: application/json" \
-d '{
"registration_ids": [ "'"$TOKEN"'" ],
"data": {
"message": "'"$MESSAGE"'" 
} 
}' \
https://android.googleapis.com/gcm/send
```
