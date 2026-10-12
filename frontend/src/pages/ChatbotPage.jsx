import React, { useCallback, useEffect, useRef, useState } from 'react';
import { RefreshCw, Send, Sparkles, Trash2 } from 'lucide-react';
import axiosClient, { extractApiError } from '../api/axiosClient';

const INITIAL_MESSAGES = [
  {
    role: 'assistant',
    content:
      'Hello! I am the SkillBridge Learning Assistant. Ask me about available courses, syllabi, prerequisites, or learning paths. Relevant course records are retrieved from MySQL (RAG) and included in the prompt context.',
    retrievedCourses: [],
  },
];

export default function ChatbotPage() {
  const [status, setStatus] = useState(null);
  const [statusError, setStatusError] = useState('');
  const [messages, setMessages] = useState(() => {
    try {
      const saved = sessionStorage.getItem('skillbridge_chat_history');
      return saved ? JSON.parse(saved) : INITIAL_MESSAGES;
    } catch {
      return INITIAL_MESSAGES;
    }
  });
  const [question, setQuestion] = useState('');
  const [lastFailedQuestion, setLastFailedQuestion] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  const [error, setError] = useState('');
  const messagesEndRef = useRef(null);

  const loadChatbotStatus = useCallback(async () => {
    try {
      setStatusError('');
      const res = await axiosClient.get('/chatbot/status');
      setStatus(res.data);
    } catch (err) {
      setStatusError(
        extractApiError(err, 'Unable to verify AI assistant status from the backend server.')
      );
    }
  }, []);

  useEffect(() => {
    loadChatbotStatus();
  }, [loadChatbotStatus]);

  useEffect(() => {
    try {
      sessionStorage.setItem('skillbridge_chat_history', JSON.stringify(messages));
    } catch {
      // Ignore storage quota issues
    }
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, isTyping]);

  const sendQuestion = async (textToSend) => {
    const trimmed = textToSend.trim();
    if (!trimmed || isTyping) return;

    setError('');
    setLastFailedQuestion('');
    const userMsg = { role: 'user', content: trimmed };
    const historyForBackend = messages.map((m) => ({
      role: m.role,
      content: m.content,
    }));

    setMessages((prev) => [...prev, userMsg]);
    setQuestion('');
    setIsTyping(true);

    try {
      const res = await axiosClient.post('/chatbot/ask', {
        message: trimmed,
        history: historyForBackend,
      });

      const assistantMsg = {
        role: 'assistant',
        content: res.data.reply,
        configured: res.data.configured,
        retrievedCourses: res.data.retrievedCourses || [],
      };
      setMessages((prev) => [...prev, assistantMsg]);
    } catch (err) {
      setLastFailedQuestion(trimmed);
      setError(
        extractApiError(
          err,
          'The AI Learning Assistant is temporarily unavailable. Please try again.'
        )
      );
    } finally {
      setIsTyping(false);
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    sendQuestion(question);
  };

  const handleClearSession = () => {
    sessionStorage.removeItem('skillbridge_chat_history');
    setMessages(INITIAL_MESSAGES);
    setError('');
    setLastFailedQuestion('');
  };

  return (
    <div>
      <div className="section-header">
        <div>
          <h1 className="page-title">AI Learning Assistant (RAG)</h1>
          <p className="page-subtitle" style={{ marginBottom: 0 }}>
            Ask questions about SkillBridge courses, prerequisites, syllabi, or study paths.
          </p>
        </div>
        <button type="button" className="btn btn-secondary btn-sm" onClick={handleClearSession}>
          <Trash2 size={14} />
          Clear Session History
        </button>
      </div>

      {statusError && (
        <div className="alert alert-error">
          <span>{statusError}</span>
          <button type="button" className="btn btn-secondary btn-sm" onClick={loadChatbotStatus}>
            <RefreshCw size={14} />
            Retry Connection
          </button>
        </div>
      )}

      {status && !status.configured && (
        <div className="alert alert-warning">
          <div>
            <strong>AI Provider Configuration Required:</strong> The server environment variable{' '}
            <code>LLM_API_KEY</code> (or <code>GEMINI_API_KEY</code>) is not currently set on the
            backend. MySQL course retrieval (RAG) is active ({status.indexedCourseCount} published
            courses indexed), and live AI responses will activate automatically once{' '}
            <code>LLM_API_KEY</code> is configured on Render.
          </div>
        </div>
      )}

      {status && status.configured && (
        <div className="alert alert-info">
          <span>
            <Sparkles size={15} style={{ verticalAlign: 'middle', marginRight: 6 }} />
            Connected to AI provider (<strong>{status.provider}</strong> /{' '}
            <code>{status.model}</code>) with <strong>{status.indexedCourseCount}</strong> published
            courses indexed in MySQL for RAG context.
          </span>
        </div>
      )}

      {error && (
        <div className="alert alert-error">
          <span>{error}</span>
          {lastFailedQuestion && (
            <button
              type="button"
              className="btn btn-secondary btn-sm"
              onClick={() => sendQuestion(lastFailedQuestion)}
              disabled={isTyping}
            >
              <RefreshCw size={14} />
              Retry Question
            </button>
          )}
        </div>
      )}

      <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', marginBottom: 14 }}>
        {[
          'What free courses are available on SkillBridge?',
          'Which course covers Spring Boot and REST APIs?',
          'What are the prerequisites for the Security & JWT course?',
          'Recommend a beginner-friendly learning path.',
        ].map((sample, idx) => (
          <button
            key={idx}
            type="button"
            className="btn btn-secondary btn-sm"
            onClick={() => sendQuestion(sample)}
            disabled={isTyping}
          >
            {sample}
          </button>
        ))}
      </div>

      <div className="chat-container">
        <div className="chat-header-strip">
          <span style={{ display: 'inline-flex', alignItems: 'center', gap: 8, fontWeight: 600 }}>
            <Sparkles size={15} style={{ color: 'var(--teal)' }} />
            SkillBridge AI Learning Assistant
          </span>
          <span style={{ fontSize: 12, opacity: 0.85 }}>
            {status
              ? `MySQL RAG Index: ${status.indexedCourseCount} courses`
              : 'Connecting to backend...'}
          </span>
        </div>

        <div className="chat-messages">
          {messages.map((msg, index) => (
            <div key={index} className={`chat-bubble ${msg.role}`}>
              <div>{msg.content}</div>
              {msg.retrievedCourses && msg.retrievedCourses.length > 0 && (
                <div
                  style={{
                    marginTop: 8,
                    paddingTop: 6,
                    borderTop: '1px solid var(--border-light)',
                    fontSize: 12,
                    color: 'var(--text-muted)',
                  }}
                >
                  <strong>MySQL RAG Context Retrieved:</strong> {msg.retrievedCourses.join(', ')}
                </div>
              )}
            </div>
          ))}

          {isTyping && (
            <div className="chat-bubble assistant" role="status">
              <em>Retrieving MySQL course context and generating response...</em>
            </div>
          )}
          <div ref={messagesEndRef} />
        </div>

        <form onSubmit={handleSubmit} className="chat-input-bar">
          <input
            type="text"
            className="form-control"
            placeholder="Ask a question about SkillBridge courses, syllabi, or prerequisites..."
            value={question}
            onChange={(e) => setQuestion(e.target.value)}
            disabled={isTyping}
            aria-label="Ask AI Learning Assistant"
          />
          <button
            type="submit"
            className="btn btn-primary"
            disabled={isTyping || !question.trim()}
          >
            <Send size={15} />
            {isTyping ? 'Sending...' : 'Send'}
          </button>
        </form>
      </div>
    </div>
  );
}
