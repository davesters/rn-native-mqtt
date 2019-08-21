//
//  NativeMqttModule.swift
//  NativeMqtt
//
//  Created by David Corona on 5/7/19.
//  Copyright © 2019 David Corona. All rights reserved.
//

import Foundation

@objc(NativeMqtt)
class NativeMqtt: RCTEventEmitter {
    
    var clients: [ String: MqttClient ] = [:]
    
    override init() {
        super.init()
        EventEmitter.sharedInstance.registerEventEmitter(eventEmitter: self)
    }
    
    @objc
    open override func supportedEvents() -> [String] {
        return EventEmitter.sharedInstance.allEvents
    }
    
    @objc(newClient:)
    func newClient(id: String) {
        clients[id] = MqttClient(withEmitter: EventEmitter.sharedInstance, id: id)
    }
    
    @objc(connect:host:options:callback:)
    func connect(id: String, host: String, options: NSDictionary, callback: @escaping RCTResponseSenderBlock) {
        clients[id]?.connect(host: host, options: options, callback: callback)
    }
    
    @objc(subscribe:topicList:qosList:)
    func subscribe(id: String, topicList: NSArray, qosList: NSArray) {
        for x in 0..<topicList.count {
            clients[id]?.subscribe(topic: topicList[x] as! String, qos: forQosInt(qos: qosList[x] as! NSInteger))
        }
    }
    
    func forQosInt(qos: NSInteger) -> CocoaMQTTQOS {
        switch qos {
        case 1:
            return CocoaMQTTQOS.qos1
        case 2:
            return CocoaMQTTQOS.qos2
        default:
            return CocoaMQTTQOS.qos0
        }
    }
    
    @objc(unsubscribe:topicList:)
    func unsubscribe(id: String, topicList: NSArray) {
        for x in 0..<topicList.count {
            clients[id]?.unsubscribe(topic: topicList[x] as! String)
        }
    }

    @objc(publish:topic:base64Payload:qos:retained:)
    func publish(id: String, topic: String, base64Payload: String, qos: NSInteger, retained: Bool) {
        clients[id]?.publish(topic: topic, base64Payload: base64Payload, qos: forQosInt(qos: qos), retained: retained)
    }

    @objc(disconnect:)
    func disconnect(id: String) {
        clients[id]?.disconnect()
    }

    @objc(close:)
    func close(id: String) {
        clients[id] = nil
        clients.removeValue(forKey: id)
    }
}
