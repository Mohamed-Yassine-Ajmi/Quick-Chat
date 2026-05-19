import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import ContactList from '../components/ContactList';
import ChatWindow from '../components/ChatWindow';
import { getAllUsers } from '../services/api';
import { connectSocket, disconnectSocket } from '../services/socket';
import './Chat.css';

export default function Chat() {
  const [contacts, setContacts] = useState([]);
  const [selectedUser, setSelectedUser] = useState(null);
  const [currentUser, setCurrentUser] = useState(null);
  const [incomingMessage, setIncomingMessage] = useState(null);
  const [readReceipt, setReadReceipt] = useState(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const token = localStorage.getItem('token');
    const user = JSON.parse(localStorage.getItem('user') || 'null');

    if (!token || !user) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      navigate('/login');
      return;
    }

    setCurrentUser(user);

    // Load contacts
    const loadContacts = async () => {
      try {
        const res = await getAllUsers();
        setContacts(res.data || []);
      } catch (err) {
        console.error('Failed to load contacts:', err);
      } finally {
        setLoading(false);
      }
    };

    loadContacts();

    // Refresh contacts every 15 seconds to update online status
    const interval = setInterval(async () => {
      try {
        const res = await getAllUsers();
        setContacts(res.data || []);
      } catch (err) {
        console.error('Failed to refresh contacts:', err);
      }
    }, 15000);

    // Connect WebSocket
    connectSocket(
      token,
      (message) => {
        setIncomingMessage(message);
      },
      (error) => {
        console.error('[WS] Error:', error);
      },
      (receipt) => {
        setReadReceipt(receipt);
      }
    );

    return () => {
      clearInterval(interval);
      disconnectSocket();
    };
  }, [navigate]);

  const handleSelectUser = useCallback((user) => {
    setSelectedUser(user);
  }, []);

  if (loading) {
    return (
      <div className="chat-page">
        <Navbar />
        <div className="chat-loading">
          <div className="spinner-large" />
          <p>Loading conversations...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="chat-page" id="chat-page">
      <Navbar />
      <div className="chat-layout">
        <div className="chat-sidebar">
          <ContactList
            contacts={contacts}
            selectedUser={selectedUser}
            onSelect={handleSelectUser}
            currentUserId={currentUser?.id}
          />
        </div>
        <div className="chat-main">
          <ChatWindow
            selectedUser={selectedUser}
            currentUser={currentUser}
            incomingMessage={incomingMessage}
            readReceipt={readReceipt}
          />
        </div>
      </div>
    </div>
  );
}
