import { elements } from './modules/dom-elements.js';
import { UserListService } from './modules/user-list-service.js';
import { WebSocketService } from './modules/websocket-service.js';
import { InputHandler } from './modules/input-handler.js';
import { createUserInfo } from './modules/avatar-service.js';

const userListService = new UserListService();
const webSocketService = new WebSocketService(userListService);
const inputHandler = new InputHandler(webSocketService);
const roomId = window.ROOM_ID;
const roomName = window.ROOM_NAME;

async function connect(event) {
    event.preventDefault();
    const username = elements.usernameForm.querySelector('#name').value.trim();

    if (username) {
        try {
            const isAdmin = document.referrer.includes('/admin');

                const response = await fetch('/admin/token', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        username: username,
                        roomId: roomId,
                        isAdmin: isAdmin
                    })
                });

                if (!response.ok) {
                    throw new Error('Failed to get token');
                }

                const token = await response.text();
                localStorage.setItem('userToken',token);

            elements.usernamePage.classList.add('hidden');
            elements.chatPage.classList.remove('hidden');
            document.querySelector('.chat-header h2').textContent = roomName;
            if(isAdmin){
                elements.endButton.classList.remove('hidden');
            }

            await webSocketService.connect(username, roomId);

        } catch (error) {
            console.error('Connection error:', error);
            alert('Failed to connect. Please try again.');
        }
    }
}

function sendMessage(event) {
    event.preventDefault();
    const messageContent = elements.messageInput.value.trim();
    if (messageContent) {
        webSocketService.sendMessage(messageContent);
        elements.messageInput.value = '';
        elements.messageInput.style.height = 'auto';
    }
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
        elements.endButton.addEventListener('click', () => webSocketService.endDiscussion());
        elements.sidebarToggle.addEventListener('click', () => {
            elements.userListSidebar.classList.toggle('expanded');
        });
    });
}

initializeEventListeners();