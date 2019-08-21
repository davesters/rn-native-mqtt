//
//  MqttClient.swift
//  NativeMqtt
//
//  Created by David Corona on 5/7/19.
//  Copyright © 2019 David Corona. All rights reserved.
//

import Foundation

class MqttClient {

    private let eventEmitter: EventEmitter
    private let id: String
    private let client: CocoaMQTT

    private var connectCallback: RCTResponseSenderBlock? = nil

    init(withEmitter emitter: EventEmitter, id: String) {
        self.eventEmitter = emitter
        self.id = id
        self.client = CocoaMQTT(clientID: "")
    }

    func connect(host: String, options: NSDictionary, callback: @escaping RCTResponseSenderBlock) {
        self.connectCallback = callback

        guard let url = URLComponents(string: host) else {
            callback([ "Error parsing host URL" ])
            return
        }

        self.client.host = url.host!
        self.client.port = UInt16(url.port != nil ? url.port! : 1883)
        self.client.delegate = self

        if let clientId = options["clientId"] as! String? {
            self.client.clientID = clientId
        }
        if let enableSsl = options["enableSsl"] as! Bool? {
            self.client.enableSSL = enableSsl
        }
        if let allowUntrustedCA = options["allowUntrustedCA"] as! Bool? {
            self.client.allowUntrustCACertificate = allowUntrustedCA
        }
        if let cleanSession = options["cleanSession"] as! Bool? {
            self.client.cleanSession = cleanSession
        }
        if let keepAlive = options["keepAliveInterval"] as! Int? {
            self.client.keepAlive = UInt16(keepAlive)
        }
        if let maxInFlightMessages = options["maxInFlightMessages"] as! Int? {
            self.client.bufferSilosMaxNumber = UInt(maxInFlightMessages)
        }
        if let autoReconnect = options["autoReconnect"] as! Bool? {
            self.client.autoReconnect = autoReconnect
        }
        if let username = options["username"] as! String? {
            self.client.username = username
        }
        if let password = options["password"] as! String? {
            self.client.password = password
        }

        if let tlsOptions = options["tls"] as! NSDictionary? {
            if let p12base64Cert = tlsOptions["p12"] as! String?, let p12Pass = tlsOptions["pass"] as! String? {
                let opts: NSDictionary = [kSecImportExportPassphrase: p12Pass]
                var items: CFArray?

                guard let p12Data = NSData(base64Encoded: p12base64Cert, options: .ignoreUnknownCharacters) else {
                    callback([ "Failed to read p12 certificate" ])
                    return
                }
                let securityError = SecPKCS12Import(p12Data, opts, &items)

                guard securityError == errSecSuccess else {
                    if securityError == errSecAuthFailed {
                        callback([ "SecPKCS12Import returned errSecAuthFailed. Incorrect password?" ])
                    } else {
                        callback([ "Failed to read p12 certificate" ])
                    }
                    return
                }

                guard let theArray = items, CFArrayGetCount(theArray) > 0 else {
                    callback([ "Failed to properly read p12 certificate" ])
                    return
                }

                let dictionary = (theArray as NSArray).object(at: 0)
                guard let identity = (dictionary as AnyObject).value(forKey: kSecImportItemIdentity as String) else {
                    callback([ "Failed to properly read p12 certificate" ])
                    return
                }

                var sslSettings: [String: NSObject] = [:]

                sslSettings["kCFStreamSSLIsServer"] = NSNumber(value: false)
                sslSettings["kCFStreamSSLCertificates"] = [identity] as CFArray
                self.client.sslSettings = sslSettings
            }
        }

        self.client.connect()
    }

    func subscribe(topic: String, qos: CocoaMQTTQOS) {
        self.client.subscribe(topic, qos: qos)
    }

    func unsubscribe(topic: String) {
        self.client.unsubscribe(topic)
    }

    func publish(topic: String, base64Payload: String, qos: CocoaMQTTQOS, retained: Bool) {
        guard let payload = Data(base64Encoded: base64Payload) else {
            return
        }

        let message = CocoaMQTTMessage(topic: topic, payload: [UInt8](payload))
        message.qos = qos
        message.retained = retained
        
        self.client.publish(message)
    }

    func disconnect() {
        self.client.disconnect()
    }
}

extension MqttClient: CocoaMQTTDelegate {
    func mqtt(_ mqtt: CocoaMQTT, didConnectAck ack: CocoaMQTTConnAck) {
        if (ack == .accept) {
            self.connectCallback?(nil)
            return
        }

        sendEvent(name: EventType.Error.rawValue, body: [
            "id": self.id,
            "error": "Error connecting: \(ack.description)"
        ])
    }

    func mqtt(_ mqtt: CocoaMQTT, didStateChangeTo state: CocoaMQTTConnState) {
        if (state == .connected) {
            sendEvent(name: EventType.Connect.rawValue, body: [
                "id": self.id,
            ])
        }
        if (state == .disconnected) {
            sendEvent(name: EventType.Disconnect.rawValue, body: [
                "id": self.id,
            ])
        }
    }

    func mqtt(_ mqtt: CocoaMQTT, didPublishMessage message: CocoaMQTTMessage, id: UInt16) {}

    func mqtt(_ mqtt: CocoaMQTT, didPublishAck id: UInt16) {}

    func mqtt(_ mqtt: CocoaMQTT, didReceiveMessage message: CocoaMQTTMessage, id: UInt16) {
        sendEvent(name: EventType.Message.rawValue, body: [
            "id": self.id,
            "topic": message.topic,
            "message": message.payload
        ])
    }

    func mqtt(_ mqtt: CocoaMQTT, didSubscribeTopic topic: String) {}

    func mqtt(_ mqtt: CocoaMQTT, didUnsubscribeTopic topic: String) {}

    func mqttDidPing(_ mqtt: CocoaMQTT) {}

    func mqttDidReceivePong(_ mqtt: CocoaMQTT) {}

    func mqttDidDisconnect(_ mqtt: CocoaMQTT, withError err: Error?) {
        if let error = err {
            sendEvent(name: EventType.Disconnect.rawValue, body: [
                "id": self.id,
                "cause": error.localizedDescription
            ])
        } else {
            sendEvent(name: EventType.Disconnect.rawValue, body: [
                "id": self.id
            ])
        }
    }

    func mqtt(_ mqtt: CocoaMQTT, didReceive trust: SecTrust, completionHandler: @escaping (Bool) -> Void) {
        var accept = true
        var secresult = SecTrustResultType.invalid
        SecTrustEvaluate(trust, &secresult)

        if (secresult == .invalid || secresult == .deny) {
            accept = false
        }

        completionHandler(accept)
    }

    func sendEvent(name: String, body: Any) {
        eventEmitter.dispatch(name: name, body: body)
    }
}
