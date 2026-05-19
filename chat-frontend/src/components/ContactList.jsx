import './ContactList.css';

export default function ContactList({ contacts, selectedUser, onSelect, currentUserId }) {
  return (
    <div className="contact-list" id="contact-list">
      <div className="contact-list-header">
        <h2>Messages</h2>
        <span className="contact-count">{contacts.length}</span>
      </div>

      <div className="contact-list-items">
        {contacts.length === 0 && (
          <div className="contact-empty">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" opacity="0.3">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
              <circle cx="9" cy="7" r="4"/>
              <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
              <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
            </svg>
            <p>No contacts yet</p>
          </div>
        )}

        {contacts.map((contact) => (
          <div
            key={contact.id}
            className={`contact-item ${selectedUser?.id === contact.id ? 'selected' : ''}`}
            onClick={() => onSelect(contact)}
            id={`contact-${contact.id}`}
          >
            <div className="contact-avatar-wrapper">
              <div className="contact-avatar">
                {contact.username?.charAt(0)?.toUpperCase()}
              </div>
              <span className={`contact-status ${contact.online ? 'online' : 'offline'}`} />
            </div>

            <div className="contact-info">
              <span className="contact-name">{contact.username}</span>
              <span className="contact-status-text">
                {contact.online ? 'Online' : 'Offline'}
              </span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
