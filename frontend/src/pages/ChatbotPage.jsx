import React, { useEffect, useRef, useState } from 'react';
import axiosClient from '../api/axiosClient';

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
  const [messages, setMessages] = useState(() => {
    try {
      const saved = sessionStorage.getItem('skillbridge_chat_history');
      return saved ? JSON.parse(saved) : INITIAL_MESSAGES;
    } catch {
      return INITIAL_MESSAGES;
    }
  });
  const [question, setQuestion] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  const [error, setError] = useState('');
  const messagesEndRef = useRef(null);

  useEffect(() => {
    axiosClient
      .get('/chatbot/status')
      .then((res) => setStatus(res.data))
      .catch(() => {});
  }, []);

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
      const errMsg =
        err.response?.data?.message ||
        'Failed to communicate with the AI chatbot backend endpoint.';
      setError(errMsg);
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
  };

  return (
    <div>
      <div className="section-header">
        <div>
          <h1 className="page-title">AI Learning Assistant (RAG)</h1>
          <p className="page-subtitle" style={{ marginBottom: 0 }}>
            Ask questions about SkillBridge courses, prerequisites, or study plans.
          </p>
        </div>
        <button type="button" className="btn btn-secondary btn-sm" onClick={handleClearSession}>
          Clear Session History
        </button>
      </div>

      {status && !status.configured && (
        <div className="alert alert-warning">
          <strong>Configuration Notice:</strong> The external LLM API key is not currently set on
          the backend. Set the <code>LLM_API_KEY</code> (or <code>GEMINI_API_KEY</code>) environment
          variable before starting the Spring Boot server to enable live AI generation. MySQL
          course context retrieval (RAG) remains active.
        </div>
      )}

      {status && status.configured && (
        <div className="alert alert-info">
          Connected to external LLM provider (<strong>{status.provider}</strong> /{' '}
          <code>{status.model}</code>) with <strong>{status.indexedCourseCount}</strong> courses
          indexed in MySQL for RAG context.
        </div>
      )}

      {error && <div className="alert alert-error">{error}</div>}

      <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', marginBottom: 12 }}>
        {[
          'What free courses are available on SkillBridge?',
          'Which course covers Spring Boot and REST APIs?',
          'What are the prerequisites for the Security & JWT course?',
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
        <div className="chat-messages">
          {messages.map((msg, index) => (
            <div key={index} className={`chat-bubble ${msg.role}`}>
              <div>{msg.content}</div>
              {msg.retrievedCourses && msg.retrievedCourses.length > 0 && (
                <div
                  style={{
                    marginTop: 8,
                    paddingTop: 6,
                    borderTop: '1px solid var(--border-color)',
                    fontSize: 12,
                    color: 'var(--text-grey)',
                  }}
                >
                  <strong>MySQL RAG Context Retrieved:</strong> {msg.retrievedCourses.join(', ')}
                </div>
              )}
            </div>
          ))}

          {isTyping && (
            <div className="chat-bubble assistant" role="status">
              <em>Typing response...</em>
            </div>
          )}
          <div ref={messagesEndRef} />
        </div>

        <form onSubmit={handleSubmit} className="chat-input-bar">
          <input
            type="text"
            className="form-control"
            placeholder="Ask a question about SkillBridge courses..."
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
            {isTyping ? 'Waiting...' : 'Send'}
          </button>
        </form>
      </div>
    </div>
  );
}
