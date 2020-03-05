package com.davesters.reactnative.mqtt;

import android.util.Base64;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.RCTNativeAppEventEmitter;
import com.heroku.sdk.EnvKeyStore;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import java.util.concurrent.atomic.AtomicReference;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

class MqttClient {

    private static final String EVENT_NAME_CONNECT = "rn-native-mqtt_connect";
    private static final String EVENT_NAME_ERROR = "rn-native-mqtt_error";
    private static final String EVENT_NAME_DISCONNECT = "rn-native-mqtt_disconnect";
    private static final String EVENT_NAME_MESSAGE = "rn-native-mqtt_message";

    private final ReactApplicationContext reactContext;
    private final String id;

    private final AtomicReference<IMqttAsyncClient> client;
    private final AtomicReference<Callback> connectCallback;

    MqttClient(final ReactApplicationContext reactContext, final String id) {
        this.reactContext = reactContext;
        this.client = new AtomicReference<>();
        this.connectCallback = new AtomicReference<>();
        this.id = id;
    }

    void connect(final String host, final ReadableMap options, Callback callback) {
        connectCallback.set(callback);

        try {
            this.client.set(new MqttAsyncClient(host, options.getString("clientId"), new MemoryPersistence()));

            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(!options.hasKey("cleanSession") || options.getBoolean("cleanSession"));
            connOpts.setKeepAliveInterval(options.hasKey("keepAliveInterval") ? options.getInt("keepAliveInterval") : 60);
            connOpts.setConnectionTimeout(options.hasKey("timeout") ? options.getInt("timeout") : 10);
            connOpts.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
            connOpts.setMaxInflight(options.hasKey("maxInFlightMessages") ? options.getInt("maxInFlightMessages") : 10);
            connOpts.setAutomaticReconnect(options.hasKey("autoReconnect") && options.getBoolean("autoReconnect"));
            connOpts.setUserName(options.hasKey("username") ? options.getString("username") : "");
            connOpts.setPassword(options.hasKey("password") ? options.getString("password").toCharArray() : "".toCharArray());

            if (options.hasKey("tls")) {
                ReadableMap tlsOptions = options.getMap("tls");
                String ca = tlsOptions.hasKey("ca") ? tlsOptions.getString("ca") : null;
                String cert = tlsOptions.hasKey("cert") ? tlsOptions.getString("cert") : null;
                String key = tlsOptions.hasKey("key") ? tlsOptions.getString("key") : null;

                SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
                KeyManager[] keyManagers = new KeyManager[0];
                TrustManager[] trustManagers = new TrustManager[0];

                if (cert != null && key != null) {
                    KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("PKIX");
                    KeyStore keyStore = EnvKeyStore.createFromPEMStrings(key, cert, "").keyStore();
                    keyManagerFactory.init(keyStore, "".toCharArray());
                    keyManagers = keyManagerFactory.getKeyManagers();
                }

                if (ca != null) {
                    TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                    KeyStore trustStore = EnvKeyStore.createFromPEMStrings(ca, "").keyStore();
                    trustManagerFactory.init(trustStore);
                    trustManagers = trustManagerFactory.getTrustManagers();
                }

                if (keyManagers.lengh > 0 || trustManagers.length > 0) {
                    sslContext.init(keyManagers, trustManagers, null);
                    connOpts.setSocketFactory(sslContext.getSocketFactory());
                }
            }

            this.client.get().setCallback(new MqttEventCallback());
            this.client.get().connect(connOpts, null, new ConnectMqttActionListener());
        } catch (Exception ex) {
            callback.invoke(ex.getMessage());
        }
    }

    void subscribe(final ReadableArray topicList, final ReadableArray qosList) {
        try {
            String[] topic = new String[topicList.size()];
            int[] qos = new int[qosList.size()];

            for (int x = 0; x < topicList.size(); x++) {
                topic[x] = topicList.getString(x);
            }
            for (int y = 0; y < qosList.size(); y++) {
                qos[y] = qosList.getInt(y);
            }

            this.client.get().subscribe(topic, qos, null, new SubscribeMqttActionListener());
        } catch (Exception ex) {
            WritableMap params = Arguments.createMap();
            params.putString("message", "Error subscribing");
            params.putString("error", ex.getMessage());

            sendEvent(EVENT_NAME_ERROR, params);
        }
    }

    void unsubscribe(final ReadableArray topicList) {
        try {
            String[] topic = new String[topicList.size()];

            for (int x = 0; x < topicList.size(); x++) {
                topic[x] = topicList.getString(x);
            }

            this.client.get().unsubscribe(topic, null, new UnsubscribeMqttActionListener());
        } catch (Exception ex) {
            WritableMap params = Arguments.createMap();
            params.putString("message", "Error unsubscribing");
            params.putString("error", ex.getMessage());

            sendEvent(EVENT_NAME_ERROR, params);
        }
    }

    void publish(final String topic, final String base64Payload, final int qos, final boolean retained) {
        MqttMessage message = new MqttMessage(Base64.decode(base64Payload, Base64.DEFAULT));
        message.setQos(qos);
        message.setRetained(retained);

        try {
            this.client.get().publish(topic, message);
        } catch (MqttException ex) {
            WritableMap params = Arguments.createMap();
            params.putString("message", "Error publishing message");
            params.putString("error", ex.getMessage());

            sendEvent(EVENT_NAME_ERROR, params);
        }
    }

    void disconnect() {
        try {
            this.client.get().disconnect(null, new DisconnectMqttActionListener());
        } catch (MqttException ex) {
            WritableMap params = Arguments.createMap();
            params.putString("message", "Error disconnecting");
            params.putString("error", ex.getMessage());

            sendEvent(EVENT_NAME_ERROR, params);
        }
    }

    void close() {
        try {
            this.client.get().close();
        } catch (MqttException ex) {
            WritableMap params = Arguments.createMap();
            params.putString("message", "Error closing");
            params.putString("error", ex.getMessage());

            sendEvent(EVENT_NAME_ERROR, params);
        }
    }

    private void sendEvent(String eventName, WritableMap params) {
        params.putString("id", this.id);

        this.reactContext
            .getJSModule(RCTNativeAppEventEmitter.class)
            .emit(eventName, params);
    }

    private class MqttEventCallback implements MqttCallbackExtended {
        @Override
        public void connectionLost(Throwable cause) {
            WritableMap params = Arguments.createMap();
            params.putString("cause", cause.getMessage());

            sendEvent(EVENT_NAME_DISCONNECT, params);
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            WritableMap params = Arguments.createMap();
            params.putString("topic", topic);
            params.putString("message", Base64.encodeToString(message.getPayload(), Base64.DEFAULT));

            sendEvent(EVENT_NAME_MESSAGE, params);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {}

        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
            WritableMap params = Arguments.createMap();
            params.putBoolean("reconnect", reconnect);

            sendEvent(EVENT_NAME_CONNECT, params);
        }
    }
    
    private class ConnectMqttActionListener implements IMqttActionListener {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            connectCallback.get().invoke();
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable ex) {
            connectCallback.get().invoke(ex.getMessage());
        }
    }

    private class DisconnectMqttActionListener implements IMqttActionListener {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            WritableMap params = Arguments.createMap();
            params.putString("cause", "User disconnected");

            sendEvent(EVENT_NAME_DISCONNECT, params);
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable ex) {
            WritableMap params = Arguments.createMap();
            params.putString("message", "Error connecting");
            params.putString("error", ex.getMessage());

            sendEvent(EVENT_NAME_ERROR, params);
        }
    }

    private class SubscribeMqttActionListener implements IMqttActionListener {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {}

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable ex) {
            WritableMap params = Arguments.createMap();
            params.putString("message", "Error subscribing");
            params.putString("error", ex.getMessage());

            sendEvent(EVENT_NAME_ERROR, params);
        }
    }

    private class UnsubscribeMqttActionListener implements IMqttActionListener {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {}

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable ex) {
            WritableMap params = Arguments.createMap();
            params.putString("message", "Error unsubscribing");
            params.putString("error", ex.getMessage());

            sendEvent(EVENT_NAME_ERROR, params);
        }
    }
}