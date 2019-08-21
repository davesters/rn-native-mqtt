//
//  EventTypes.swift
//  NativeMqtt
//
//  Created by David Corona on 5/7/19.
//  Copyright © 2019 David Corona. All rights reserved.
//

import Foundation

enum EventType: String {
    case Connect = "rn-native-mqtt_connect"
    case Disconnect = "rn-native-mqtt_disconnect"
    case Error = "rn-native-mqtt_error"
    case Message = "rn-native-mqtt_message"
}
