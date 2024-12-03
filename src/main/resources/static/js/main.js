import { elements } from './modules/dom-elements.js';
import { UserListService } from './modules/user-list-service.js';
import { WebSocketService } from './modules/websocket-service.js';
import { InputHandler } from './modules/input-handler.js';

const userListService = new UserListService();
const webSocketService = new WebSocketService(userListService);
const inputHandler = new InputHandler(webSocketService);
const roomId = window.ROOM_ID;
const roomName = window.ROOM_NAME;

function connect(event) {
    event.preventDefault();
    const username = elements.usernameForm.querySelector('#name').value.trim();
    const adminToken = localStorage.getItem('roomAdminToken_' + roomId);

    if (username) {
        const adminControls = document.querySelector('.admin-controls');
        if (adminToken) {
            adminControls.classList.remove('hidden');
        }
        elements.usernamePage.classList.add('hidden');
        elements.chatPage.classList.remove('hidden');
        document.querySelector('.chat-header h2').textContent = roomName;
        webSocketService.connect(username, roomId);
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
        elements.endButton.addEventListener('click',() => webSocketService.endDiscussion());
    });
}

initializeEventListeners();