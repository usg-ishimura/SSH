# Mic2Shell
Code for the wearable version of mic2Shell
## Requirements to run
\- Microphone on the wearable device  
\- Wear OS / Android Wear  
\- Google Cloud speech to text APIs credentials  
\- Remote SSH server to access to  
\- Additionally you can install shell_gpt on the remote SSH server to talk to Chat GPT or to make it execute shell commands through mic2Shell
## Usage
1. In order to try out this code, visit the [Cloud Console](https://console.cloud.google.com/), and
navigate to:
`API Manager > Credentials > Create credentials > Service account key > New service account`.
Create a new service account, and download the JSON credentials file. Put the file in the app
resources as `app/src/main/res/raw/credential.json`.
2. Compile it and try it on emulators with Android Studio
3. Sideload APK to your smartwatch
## Features
Under voice aliases section you can assign a shell command to a voice alias, every time you input with voice the alias value the respective shell command is triggered
