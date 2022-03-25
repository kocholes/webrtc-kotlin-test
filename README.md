# WebRTC Kotlin Test

## Requirements
Firebase credentials to connect to Firestore. Create an android app from the Firebase
[console](https://console.firebase.google.com/) then copy the `google-services.json` file to `/app`

## How to use the app
- Press `Create Offer` button to create a connection hash
- Use the [web app](https://github.com/kocholes/webrtc-web-test) as the other peer, paste the 
  generated connection hash an press `Connect`. Both peers must be on the same local network
- If the connection is successful:
  - Write a message and press `Send` to send it to the other peer. 
  - Press `Ping` to calculate the transmission time from one peer to another
  - Press `Send Roll` to start sending rotation data in realtime to the other peer
