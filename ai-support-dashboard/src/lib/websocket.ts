import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

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

  constructor(brokerUrl: string = "http://localhost:8080/ws") {
    this.client = new Client({
      // STOMP over SockJS
      webSocketFactory: () => new SockJS(brokerUrl),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    this.client.onConnect = () => {
      console.log("Connected to STOMP Broker");
    };

    this.client.onStompError = (frame) => {
      console.error("Broker reported error: " + frame.headers["message"]);
      console.error("Additional details: " + frame.body);
    };

    this.client.activate();
  }

  public subscribe<T>(
    topic: string,
    callback: (event: DomainEvent<T>) => void
  ) {
    if (!this.client.connected) {
      // If not connected yet, we wait until it is connected
      this.client.onConnect = () => {
        this.performSubscribe(topic, callback);
      };
    } else {
      this.performSubscribe(topic, callback);
    }
  }

  private performSubscribe<T>(
    topic: string,
    callback: (event: DomainEvent<T>) => void
  ) {
    const subscription = this.client.subscribe(topic, (message) => {
      if (message.body) {
        const event = JSON.parse(message.body) as DomainEvent<T>;
        callback(event);
      }
    });
    this.subscriptions.set(topic, subscription);
  }

  public unsubscribe(topic: string) {
    const subscription = this.subscriptions.get(topic);
    if (subscription) {
      subscription.unsubscribe();
      this.subscriptions.delete(topic);
    }
  }

  public disconnect() {
    this.client.deactivate();
  }
}

// Singleton instance for the V1 architecture
export const wsClient = new WebSocketClient();
