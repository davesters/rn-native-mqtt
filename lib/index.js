"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const react_native_1 = require("react-native");
const tiny_emitter_1 = require("tiny-emitter");
const randomId = require("random-id");
const buffer_1 = require("buffer");
const { NativeMqtt } = react_native_1.NativeModules;
const mqttEventEmitter = new react_native_1.NativeEventEmitter(NativeMqtt);
var Event;
(function (Event) {
    Event["Connect"] = "connect";
    Event["Disconnect"] = "disconnect";
    Event["Message"] = "message";
    Event["Error"] = "error";
})(Event = exports.Event || (exports.Event = {}));
class Client {
    constructor(url) {
        this.connected = false;
        this.closed = false;
        this.emitter = new tiny_emitter_1.TinyEmitter();
        this.id = randomId(12);
        this.url = url;
        NativeMqtt.newClient(this.id);
        mqttEventEmitter.addListener('rn-native-mqtt_connect', (event) => {
            if (event.id !== this.id) {
                return;
            }
            this.connected = true;
            this.emitter.emit(Event.Connect, event.reconnect);
        });
        mqttEventEmitter.addListener('rn-native-mqtt_message', (event) => {
            if (event.id !== this.id) {
                return;
            }
            this.emitter.emit(Event.Message, event.topic, buffer_1.Buffer.from(event.message, 'base64'));
        });
        mqttEventEmitter.addListener('rn-native-mqtt_disconnect', (event) => {
            if (event.id !== this.id) {
                return;
            }
            this.connected = false;
            this.emitter.emit(Event.Disconnect, event.cause);
        });
        mqttEventEmitter.addListener('rn-native-mqtt_error', (event) => {
            if (event.id !== this.id) {
                return;
            }
            this.emitter.emit(Event.Error, event.error);
        });
    }
    connect(options, callback) {
        if (this.closed) {
            throw new Error('client already closed');
        }
        if (this.connected) {
            throw new Error('client already connected');
        }
        const opts = Object.assign({}, options);
        if (opts.tls && opts.tls.p12) {
            opts.tls = Object.assign({}, opts.tls);
            opts.tls.p12 = opts.tls.p12.toString('base64');
        }
        if (opts.tls && opts.tls.caDer) {
            opts.tls = Object.assign({}, opts.tls);
            opts.tls.caDer = opts.tls.caDer.toString('base64');
        }
        NativeMqtt.connect(this.id, this.url, opts, (err) => {
            if (err) {
                callback(new Error(err));
                return;
            }
            this.connected = true;
            callback();
        });
    }
    subscribe(topics, qos) {
        if (this.closed) {
            throw new Error('client already closed');
        }
        if (!this.connected) {
            throw new Error('client not connected');
        }
        NativeMqtt.subscribe(this.id, topics, qos);
    }
    unsubscribe(topics) {
        if (this.closed) {
            throw new Error('client already closed');
        }
        if (!this.connected) {
            throw new Error('client not connected');
        }
        NativeMqtt.unsubscribe(this.id, topics);
    }
    publish(topic, message, qos = 0, retained = false) {
        if (this.closed) {
            throw new Error('client already closed');
        }
        if (!this.connected) {
            throw new Error('client not connected');
        }
        NativeMqtt.publish(this.id, topic, message.toString('base64'), qos, retained);
    }
    disconnect() {
        if (this.closed) {
            throw new Error('client already closed');
        }
        NativeMqtt.disconnect(this.id);
    }
    close() {
        if (this.connected) {
            throw new Error('client not disconnected');
        }
        NativeMqtt.close(this.id);
        this.closed = true;
        this.emitter = null;
    }
    on(name, handler, context) {
        if (this.closed) {
            throw new Error('client already closed');
        }
        this.emitter.on(name, handler, context);
    }
    once(name, handler, context) {
        if (this.closed) {
            throw new Error('client already closed');
        }
        this.emitter.once(name, handler, context);
    }
    off(name, handler) {
        if (this.closed) {
            throw new Error('client already closed');
        }
        this.emitter.off(name, handler);
    }
}
exports.Client = Client;
//# sourceMappingURL=index.js.map