//
//  EventEmitter.swift
//  NativeMqtt
//
//  Created by David Corona on 5/7/19.
//  Copyright © 2019 David Corona. All rights reserved.
//

import Foundation

class EventEmitter {

    /// Shared Instance.
    public static var sharedInstance = EventEmitter()

    // NativeMqtt is instantiated by React Native with the bridge.
    private var eventEmitter: NativeMqtt!

    private init() {}

    // When React Native instantiates the emitter it is registered here.
    func registerEventEmitter(eventEmitter: NativeMqtt) {
        self.eventEmitter = eventEmitter
    }

    func dispatch(name: String, body: Any?) {
        self.eventEmitter.sendEvent(withName: name, body: body)
    }

    // All Events which must be support by React Native.
    lazy var allEvents: [String] = {
        return [
            EventType.Connect.rawValue,
            EventType.Disconnect.rawValue,
            EventType.Error.rawValue,
            EventType.Message.rawValue,
        ]
    }()
}
