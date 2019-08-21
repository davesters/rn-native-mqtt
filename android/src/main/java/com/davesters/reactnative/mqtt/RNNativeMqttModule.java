package com.davesters.reactnative.mqtt;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import java.util.concurrent.ConcurrentHashMap;

public class RNNativeMqttModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;

    private ConcurrentHashMap<String, MqttClient> clients;

    public RNNativeMqttModule(ReactApplicationContext reactContext) {
        super(reactContext);

        this.reactContext = reactContext;
        clients = new ConcurrentHashMap<>();
    }

    @Override
    public String getName() {
        return "RNNativeMqtt";
    }

    @ReactMethod
    public void newClient(final String id) {
        clients.put(id, new MqttClient(reactContext, id));
    }

    @ReactMethod
    public void connect(final String id, final String host, final ReadableMap options, final Callback callback) {
        if (!clients.containsKey(id)) {
            return;
        }

        clients.get(id).connect(host, options, callback);
    }

    @ReactMethod
    public void subscribe(final String id, final ReadableArray topicList, final ReadableArray qosList) {
        if (!clients.containsKey(id)) {
            return;
        }

        clients.get(id).subscribe(topicList, qosList);
    }

    @ReactMethod
    public void unsubscribe(final String id, final ReadableArray topicList) {
        if (!clients.containsKey(id)) {
            return;
        }

        clients.get(id).unsubscribe(topicList);
    }

    @ReactMethod
    public void publish(final String id, final String topic, final String base64Payload, final int qos, final boolean retained) {
        if (!clients.containsKey(id)) {
            return;
        }

        clients.get(id).publish(topic, base64Payload, qos, retained);
    }

    @ReactMethod
    public void disconnect(final String id) {
        if (!clients.containsKey(id)) {
            return;
        }

        clients.get(id).disconnect();
    }

    @ReactMethod
    public void close(final String id) {
        if (!clients.containsKey(id)) {
            return;
        }

        clients.get(id).close();
        clients.remove(id);
    }
}