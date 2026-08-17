const messages = document.getElementById('messages');
const form = document.getElementById('chatForm');
const input = document.getElementById('messageInput');
const sendButton = document.getElementById('sendButton');
const sessionId = `WEB-${crypto.randomUUID()}`;

document.getElementById('quickActions').addEventListener('click', event => {
    if (event.target.tagName === 'BUTTON') {
        input.value = event.target.textContent;
        form.requestSubmit();
    }
});

form.addEventListener('submit', async event => {
    event.preventDefault();
    const message = input.value.trim();
    if (!message) return;

    appendMessage(message, 'user');
    input.value = '';
    sendButton.disabled = true;
    sendButton.textContent = '处理中';

    try {
        const response = await fetch('/api/v1/chat', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({sessionId, message})
        });
        const body = await response.json();
        if (!response.ok) {
            throw new Error(body.message || '请求失败');
        }
        appendMessage(body.answer, 'agent', body.toolCalls, body.mode);
    } catch (error) {
        appendMessage(`请求失败：${error.message}`, 'agent');
    } finally {
        sendButton.disabled = false;
        sendButton.textContent = '发送';
        input.focus();
    }
});

function appendMessage(text, role, tools = [], mode = '') {
    const article = document.createElement('article');
    article.className = `message ${role}`;

    const wrapper = document.createElement('div');
    const bubble = document.createElement('div');
    bubble.className = 'bubble';
    bubble.textContent = text;
    wrapper.appendChild(bubble);

    if (tools.length > 0 || mode) {
        const meta = document.createElement('div');
        meta.className = 'tool-list';
        if (mode) meta.append(`模式：${mode} `);
        tools.forEach(tool => {
            const tag = document.createElement('span');
            tag.textContent = tool;
            meta.appendChild(tag);
        });
        wrapper.appendChild(meta);
    }

    article.appendChild(wrapper);
    messages.appendChild(article);
    messages.scrollTop = messages.scrollHeight;
}
