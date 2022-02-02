# React Native - Native MQTT

**Minimum deployment target updated to 13.0 for ios.**

This is an MQTT client library for React Native. It utilizes native MQTT client libraries and exposes them via a unified Javascript interface. There are a few other React Native MQTT libraries, but they did not seem to work as expected, or did not support more advanced TLS configurations.

NOTE: This is currently tested only on React Native 0.60 and above using the automatic linking functionality.

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

## Installation

If you are not on React Native 0.60+ and not using auto-linking, you may need to run the usual link command as below:

```
$ react-native link react-native-native-mqtt
```

This module has only been tested on 0.60+, so at this point you are a bit on your own, but should be standard stuff.

## Additional Installation Steps

There are still some manual tasks that need to be done to wire this package up for both iOS and Android. Please perform the steps below to get everything working.

### Android

* Set your `minSdkVersion` in the `android/build.gradle` file to 21 or higher.

### iOS

We need to add a bridging header file to your Xcode project because this module was written in Swift.

* Open your project's `ios` folder in Xcode.
* Add a new Swift file to the project. Name it whatever you want. Add a bridging header file when it prompts you to add one automatically.

Now you need to run a `pod install` for your project.

* Navigate to the `ios` folder in your project and run `pod install`.

## Usage

This is a quick example written in Typescript.

```javascript
import * as Mqtt from 'react-native-native-mqtt';

const client = new Mqtt.Client('[SCHEME]://[URL]:[PORT]');

client.connect({
	clientId: 'CLIENT_ID',
	...
}, err => {});

client.on(Mqtt.Event.Message, (topic: string, message: Buffer) => {
	console.log('Mqtt Message:', topic, message.toString());
});

client.on(Mqtt.Event.Connect, () => {
	console.log('MQTT Connect');
	client.subscribe(['#'], [0]);
});

client.on(Mqtt.Event.Error, (error: string) => {
	console.log('MQTT Error:', error);
});

client.on(Mqtt.Event.Disconnect, (cause: string) => {
	console.log('MQTT Disconnect:', cause);
});
```
