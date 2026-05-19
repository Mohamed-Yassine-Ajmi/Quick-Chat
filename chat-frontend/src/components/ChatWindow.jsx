import { useState, useEffect, useRef, useCallback } from 'react';
import MessageBubble from './MessageBubble';
import { getMessages, markAsRead } from '../services/api';
import { sendMessage } from '../services/socket';
import './ChatWindow.css';

export default function ChatWindow({ selectedUser, currentUser, incomingMessage, readReceipt }) {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(false);
  const [hasMore, setHasMore] = useState(true);
  const messagesEndRef = useRef(null);
  const messagesContainerRef = useRef(null);
  const prevSelectedRef = useRef(null);

  // Load messages when user is selected
  useEffect(() => {
    if (!selectedUser) return;

    // Reset state for new conversation
    if (prevSelectedRef.current !== selectedUser.id) {
      setMessages([]);
      setPage(0);
      setHasMore(true);
      prevSelectedRef.current = selectedUser.id;
    }

    const loadMessages = async () => {
      setLoading(true);
      try {
        const res = await getMessages(selectedUser.id, 0);
        const msgs = res.data.content || res.data || [];
        const sorted = Array.isArray(msgs) ? [...msgs].reverse() : [];
        setMessages(sorted);
        setHasMore(msgs.length >= 20);
        // Mark as read
        await markAsRead(selectedUser.id).catch(() => {});
      } catch (err) {
        console.error('Failed to load messages:', err);
      } finally {
        setLoading(false);
      }
    };

    loadMessages();
  }, [selectedUser]);

  // Scroll to bottom on new messages or conversation change
  useEffect(() => {
    if (page === 0) {
      messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }
  }, [messages, page]);

  // Handle incoming WebSocket messages
  useEffect(() => {
    if (!incomingMessage || !selectedUser) return;

    if (
      (incomingMessage.senderId === selectedUser.id && incomingMessage.receiverId === currentUser.id) ||
      (incomingMessage.senderId === currentUser.id && incomingMessage.receiverId === selectedUser.id)
    ) {
      setMessages((prev) => [...prev, incomingMessage]);
      // Mark as read if message is from selected user
      if (incomingMessage.senderId === selectedUser.id) {
        markAsRead(selectedUser.id).catch(() => {});
      }
    }
  }, [incomingMessage, selectedUser, currentUser]);

  // Handle read receipts — mark messages as read in real-time
  useEffect(() => {
    if (!readReceipt || !selectedUser || !currentUser) return;

    // The read receipt tells us that selectedUser has read our messages
    if (readReceipt.readBy === selectedUser.id || readReceipt.conversationWith === selectedUser.id) {
      setMessages((prev) =>
        prev.map((msg) =>
          msg.senderId === currentUser.id && msg.receiverId === selectedUser.id
            ? { ...msg, read: true }
            : msg
        )
      );
    }
  }, [readReceipt, selectedUser, currentUser]);

  // Load older messages on scroll up
  const handleScroll = useCallback(async () => {
    const container = messagesContainerRef.current;
    if (!container || loading || !hasMore || !selectedUser) return;

    if (container.scrollTop < 60) {
      const prevHeight = container.scrollHeight;
      const nextPage = page + 1;
      setLoading(true);

      try {
        const res = await getMessages(selectedUser.id, nextPage);
        const msgs = res.data.content || res.data || [];
        if (Array.isArray(msgs) && msgs.length > 0) {
          const olderSorted = [...msgs].reverse();
          setMessages((prev) => [...olderSorted, ...prev]);
          setPage(nextPage);
          setHasMore(msgs.length >= 20);
          // Maintain scroll position
          requestAnimationFrame(() => {
            container.scrollTop = container.scrollHeight - prevHeight;
          });
        } else {
          setHasMore(false);
        }
      } catch (err) {
        console.error('Failed to load older messages:', err);
      } finally {
        setLoading(false);
      }
    }
  }, [loading, hasMore, selectedUser, page]);

  const handleSend = () => {
    const content = input.trim();
    if (!content || !selectedUser) return;

    sendMessage(currentUser.id, selectedUser.id, content);
    setInput('');
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  if (!selectedUser) {
    return (
      <div className="chat-window-empty" id="chat-window-empty">
        <div className="empty-state">
          <svg width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1" strokeLinecap="round" strokeLinejoin="round" opacity="0.2">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
          </svg>
          <h3>Welcome to QuickChat</h3>
          <p>Select a conversation to start messaging</p>
        </div>
      </div>
    );
  }

  return (
    <div className="chat-window" id="chat-window">
      {/* Chat header */}
      <div className="chat-header">
        <div className="chat-header-user">
          <div className="chat-header-avatar-wrapper">
            <div className="chat-header-avatar">
              {selectedUser.username?.charAt(0)?.toUpperCase()}
            </div>
            <span className={`chat-header-status ${selectedUser.online ? 'online' : 'offline'}`} />
          </div>
          <div className="chat-header-info">
            <span className="chat-header-name">{selectedUser.username}</span>
            <span className="chat-header-status-text">
              {selectedUser.online ? 'Online' : 'Offline'}
            </span>
          </div>
        </div>
      </div>

      {/* Messages area */}
      <div
        className="chat-messages"
        ref={messagesContainerRef}
        onScroll={handleScroll}
        id="chat-messages"
      >
        {loading && page > 0 && (
          <div className="chat-loading-more">
            <div className="spinner-small" />
            Loading older messages...
          </div>
        )}

        {messages.map((msg, idx) => (
          <MessageBubble
            key={msg.id || idx}
            message={msg}
            isMine={msg.senderId === currentUser.id}
          />
        ))}
        <div ref={messagesEndRef} />
      </div>

      {/* Input area */}
      <div className="chat-input-area" id="chat-input-area">
        <div className="chat-input-wrapper">
          <input
            type="text"
            className="chat-input"
            placeholder="Type a message..."
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={handleKeyDown}
            id="chat-message-input"
          />
          <button
            className="chat-send-btn"
            onClick={handleSend}
            disabled={!input.trim()}
            id="chat-send-btn"
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <line x1="22" y1="2" x2="11" y2="13" />
              <polygon points="22 2 15 22 11 13 2 9 22 2" />
            </svg>
          </button>
        </div>
      </div>
    </div>
  );
}
