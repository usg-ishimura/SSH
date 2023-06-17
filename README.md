# Mic2Shell
Code for the wearable version of mic2Shell, a simple voice controlled Android SSH client
## Requirements to run
\- Microphone on the wearable device  
\- Wear OS / Android Wear  
\- Google Cloud speech to text APIs credentials  
\- Remote SSH server to access to, you can make a free account at [xshellz](https://www.xshellz.com/)  
\- Additionally you can install [shell_gpt](https://github.com/TheR1D/shell_gpt) on the remote SSH server to talk to Chat GPT or to make it execute shell commands through mic2Shell
## Usage
1. run `git clone --branch wearable https://github.com/usg-ishimura/mic2Shell.git`
2. In order to try out this code, visit the [Cloud Console](https://console.cloud.google.com/), and
navigate to:
`API Manager > Credentials > Create credentials > Service account key > New service account`.
Create a new service account, and download the JSON credentials file. Put the file in the app
resources as `app/src/main/res/raw/credential.json`.
3. Compile it and try it on emulators with Android Studio
4. Sideload APK to your smartwatch
