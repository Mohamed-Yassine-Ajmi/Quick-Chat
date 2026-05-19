import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const SOCKET_URL = 'http://localhost:8080/chat';

let stompClient = null;
let messageCallback = null;
let readReceiptCallback = null;
let errorCallback = null;

export const connectSocket = (token, onMessage, onError, onReadReceipt) => {
  messageCallback = onMessage;
  errorCallback = onError;
  readReceiptCallback = onReadReceipt;

  stompClient = new Client({
    webSocketFactory: () => new SockJS(SOCKET_URL),
    connectHeaders: {
      Authorization: `Bearer ${token}`,
    },
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
    onConnect: () => {
      console.log('[WS] Connected');

      stompClient.subscribe('/user/queue/messages', (message) => {
        const parsed = JSON.parse(message.body);
        if (messageCallback) messageCallback(parsed);
      });

      stompClient.subscribe('/user/queue/read-receipt', (message) => {
        const parsed = JSON.parse(message.body);
        if (readReceiptCallback) readReceiptCallback(parsed);
      });

      stompClient.subscribe('/user/queue/errors', (message) => {
        const parsed = JSON.parse(message.body);
        if (errorCallback) errorCallback(parsed);
      });
    },
    onStompError: (frame) => {
      console.error('[WS] STOMP error:', frame.headers['message']);
      if (errorCallback) errorCallback({ message: frame.headers['message'] });
    },
    onDisconnect: () => {
      console.log('[WS] Disconnected');
    },
  });

  stompClient.activate();
};

export const sendMessage = (senderId, receiverId, content) => {
  if (stompClient && stompClient.connected) {
    stompClient.publish({
      destination: '/app/chat.send',
      body: JSON.stringify({ senderId, receiverId, content }),
    });
  }
};

export const disconnectSocket = () => {
  if (stompClient) {
    stompClient.deactivate();
    stompClient = null;
    messageCallback = null;
    readReceiptCallback = null;
    errorCallback = null;
    console.log('[WS] Deactivated');
  }
};
