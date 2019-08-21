# React Native - Native MQTT

This is an MQTT client library for React Native. It utilizes native MQTT client libraries and exposes them via a unified Javascript interface. There are a few other React Native MQTT libraries, but they did not seem to work as expected, or did not support more advanced TLS configurations.

This library uses the following native MQTT client libraries:

* iOS - https://github.com/emqx/CocoaMQTT
* Android - https://github.com/eclipse/paho.mqtt.java

It supports most of the features supported by these libraries. Tradeoffs have been made to keep the interface unified, so some features are not exposed if not supported in both libraries.

## Getting started

```
$ npm install react-native-native-mqtt --save

-- or -- 

$ yarn add react-native-native-mqtt
```

## Mostly automatic installation

```
$ react-native link react-native-native-mqtt
```

Even though this does the more annoying parts for you, there are still some things that need to be done manually to wire everything up. Those are outlined in the `Additional installation steps` section below.

## Manual installation

### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-native-mqtt` and add `NativeMqtt.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libNativeMqtt.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

### Android

1. Open up `android/app/src/main/java/[...]/MainApplication.java`
  - Add `import com.reactlibrary.NativeMqttPackage;` to the imports at the top of the file
  - Add `new NativeMqttPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-native-mqtt'
  	project(':react-native-native-mqtt').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-native-mqtt/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-native-mqtt')
  	```

## Additional Installation Steps

There are still some manual tasks that need to be done to wire this package up for both iOS and Android. Please perform the steps below to get everything working.

### Android

* Set your `minSdkVersion` in the `android/build.gradle` file to 21 or higher.

### iOS

* Open your project's `ios` folder in Xcode.
* Add a new Swift file to the project. Name it whatever you want. Add a bridging header file when it prompts you to add one automatically.


## Usage
```javascript
import NativeMqtt from 'react-native-native-mqtt';

// TODO: What to do with the module?
NativeMqtt;
```
