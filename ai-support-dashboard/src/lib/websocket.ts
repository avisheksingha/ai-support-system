import { Client } from "@stomp/stompjs";

interface DomainEvent<T> {
  eventId: string;
  eventType: string;
  entityType: string;
  entityId: string;
  correlationId: string;
  sourceService: string;
  timestamp: string;
  payload: T;
}

export class WebSocketClient {
  private client: Client;
  private subscriptions: Map<string, any> = new Map();
  private callbacks: Map<string, (event: DomainEvent<any>) => void> = new Map();

  constructor(brokerUrl: string = "ws://localhost:8080/ws") {
    this.client = new Client({
      // Use native WebSocket (no SockJS needed)
      brokerURL: brokerUrl,
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    this.client.onConnect = () => {
      console.log("Connected to STOMP Broker via native WebSocket");
      // Re-subscribe all active subscriptions on (re)connect
      this.callbacks.forEach((callback, topic) => {
        this.performSubscribe(topic, callback);
      });
    };

    this.client.onStompError = (frame) => {
      console.error("Broker reported error: " + frame.headers["message"]);
      console.error("Additional details: " + frame.body);
    };

    this.client.onWebSocketClose = () => {
      console.log("WebSocket connection closed. Will auto-reconnect...");
    };

    this.client.activate();
  }

  public subscribe<T>(
    topic: string,
    callback: (event: DomainEvent<T>) => void
  ) {
    // Store the callback so it can be (re)subscribed on connect/reconnect
    this.callbacks.set(topic, callback as (event: DomainEvent<any>) => void);

    if (this.client.connected) {
      this.performSubscribe(topic, callback);
    }
    // If not connected yet, onConnect will pick it up from this.callbacks
  }

  private performSubscribe<T>(
    topic: string,
    callback: (event: DomainEvent<T>) => void
  ) {
    // Unsubscribe existing subscription to this topic if any
    const existing = this.subscriptions.get(topic);
    if (existing) {
      existing.unsubscribe();
    }

    const subscription = this.client.subscribe(topic, (message) => {
      if (message.body) {
        const event = JSON.parse(message.body) as DomainEvent<T>;
        callback(event);
      }
    });
    this.subscriptions.set(topic, subscription);
    console.log(`Subscribed to STOMP topic: ${topic}`);
  }

  public unsubscribe(topic: string) {
    const subscription = this.subscriptions.get(topic);
    if (subscription) {
      subscription.unsubscribe();
      this.subscriptions.delete(topic);
    }
    this.callbacks.delete(topic);
  }

  public disconnect() {
    this.client.deactivate();
  }
}

// Singleton instance for the V1 architecture
export const wsClient = new WebSocketClient();
