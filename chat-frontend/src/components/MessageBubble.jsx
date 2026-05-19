import './MessageBubble.css';

export default function MessageBubble({ message, isMine }) {
  const formatTime = (timestamp) => {
    if (!timestamp) return '';
    const date = new Date(timestamp);
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  };

  return (
    <div className={`bubble-wrapper ${isMine ? 'mine' : 'theirs'}`}>
      <div className={`bubble ${isMine ? 'bubble-mine' : 'bubble-theirs'}`}>
        <p className="bubble-content">{message.content}</p>
        <div className="bubble-meta">
          <span className="bubble-time">{formatTime(message.timestamp)}</span>
          {isMine && (
            <span className={`bubble-tick ${message.read ? 'read' : ''}`}>
              {message.read ? (
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M18 7l-8 8-4-4" /><path d="M22 7l-8 8" />
                </svg>
              ) : (
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                  <polyline points="20 6 9 17 4 12" />
                </svg>
              )}
            </span>
          )}
        </div>
      </div>
    </div>
  );
}
