import { elements } from './modules/dom-elements.js';
import { UserService } from './modules/user-service.js';
import { WebSocketService } from './modules/websocket-service.js';
import { InputHandler } from './modules/input-handler.js';

const userService = new UserService();
const webSocketService = new WebSocketService(userService);
const inputHandler = new InputHandler(webSocketService);

function connect(event) {
    event.preventDefault();
    const username = elements.usernameForm.querySelector('#name').value.trim();

    if (username) {
        elements.usernamePage.classList.add('hidden');
        elements.chatPage.classList.remove('hidden');
        webSocketService.connect(username);
    }
}

function sendMessage(event) {
    event.preventDefault();
    const messageContent = elements.messageInput.value.trim();
    webSocketService.sendMessage(messageContent);
    elements.messageInput.value = '';
}

function initializeEventListeners() {
    elements.usernameForm.addEventListener('submit', connect, true);

    document.addEventListener('DOMContentLoaded', () => {
        if (elements.messageInput.tagName.toLowerCase() === 'input') {
            const textarea = document.createElement('textarea');
            textarea.id = 'textArea';
            textarea.className = elements.messageInput.className;
            textarea.placeholder = 'Ctrl+Enter or hit send button to send message';
            elements.messageInput.parentNode.replaceChild(textarea, elements.messageInput);
            elements.messageInput = textarea;
        }

        elements.messageInput.addEventListener('input', (e) => inputHandler.autoResizeInput(e));
        elements.messageInput.addEventListener('input', () => inputHandler.handleTyping());
        elements.messageInput.addEventListener('keydown', (e) => inputHandler.handleKeyPress(e));
        elements.messageForm.addEventListener('submit', sendMessage);
    });
}

initializeEventListeners();