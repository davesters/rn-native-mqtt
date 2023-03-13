import { Buffer } from 'buffer';
export interface TlsOptions {
    caDer?: Buffer;
    cert?: string;
    key?: string;
    p12?: Buffer;
    pass?: string;
}
export interface ConnectionOptions {
    clientId: string;
    cleanSession?: boolean;
    keepAlive?: number;
    timeout?: number;
    maxInFlightMessages?: number;
    autoReconnect?: boolean;
    username?: string;
    password?: string;
    tls?: TlsOptions;
    allowUntrustedCA?: boolean;
    enableSsl?: boolean;
}
export interface PublishOptions {
    retained?: boolean;
    qos?: number;
}
export declare enum Event {
    Connect = "connect",
    Disconnect = "disconnect",
    Message = "message",
    Error = "error"
}
export declare type ConnectEventHandler = (reconnect: boolean) => void;
export declare type MessageEventHandler = (topic: string, message: Buffer) => void;
export declare type DisconnectEventHandler = (cause: string) => void;
export declare type ErrorEventHandler = (error: string) => void;
export declare class Client {
    private id;
    private emitter;
    private url;
    private connected;
    private closed;
    constructor(url: string);
    connect(options: ConnectionOptions, callback: (error?: Error) => void): void;
    subscribe(topics: string[], qos: number[]): void;
    unsubscribe(topics: string[]): void;
    publish(topic: string, message: Buffer, qos?: number, retained?: boolean): void;
    disconnect(): void;
    close(): void;
    on(name: Event.Connect, handler: ConnectEventHandler, context?: any): void;
    on(name: Event.Message, handler: MessageEventHandler, context?: any): void;
    on(name: Event.Disconnect, handler: DisconnectEventHandler, context?: any): void;
    on(name: Event.Error, handler: ErrorEventHandler, context?: any): void;
    once(name: Event.Connect, handler: ConnectEventHandler, context?: any): void;
    once(name: Event.Message, handler: MessageEventHandler, context?: any): void;
    once(name: Event.Disconnect, handler: DisconnectEventHandler, context?: any): void;
    once(name: Event.Error, handler: ErrorEventHandler, context?: any): void;
    off(name: Event.Connect, handler?: ConnectEventHandler): void;
    off(name: Event.Message, handler?: MessageEventHandler): void;
    off(name: Event.Disconnect, handler?: DisconnectEventHandler): void;
    off(name: Event.Error, handler?: ErrorEventHandler): void;
}
