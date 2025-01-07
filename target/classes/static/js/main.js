import {elements} from './modules/dom-elements.js';
import {UserListService} from './modules/user-list-service.js';
import {WebSocketService} from './modules/websocket-service.js';
import {InputHandler} from './modules/input-handler.js';
import {generateRandomNickname} from "./modules/avatar-service.js";

const userListService = new UserListService();
const webSocketService = new WebSocketService(userListService);
const inputHandler = new InputHandler(webSocketService);
const roomId = window.ROOM_ID;
const roomName = window.ROOM_NAME;

async function connect(event) {
    if (event) {
        event.preventDefault();
    }

    // Get username either from form or localStorage
    const username = event ?
        elements.usernameForm.querySelector('#name').value.trim() :
        localStorage.getItem('username');

    if (username) {
        try {
            const isAdmin = document.referrer.includes('/admin') ||
                localStorage.getItem('isAdmin') === 'true';

            // Only get new token if we don't have one or if this is a new connection
            if (!localStorage.getItem('userToken') || event) {
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
                localStorage.setItem('userToken', token);
                localStorage.setItem('username', username);
                localStorage.setItem('isAdmin', isAdmin);
                localStorage.setItem('roomId', roomId);
            }

            elements.usernamePage.classList.add('hidden');
            elements.chatPage.classList.remove('hidden');
            document.querySelector('.chat-header h2').textContent = roomName;

            if (localStorage.getItem('isAdmin') === 'true') {
                elements.endButton.classList.remove('hidden');
            }

            await webSocketService.connect(username, roomId);

        } catch (error) {
            console.error('Connection error:', error);
            alert('Failed to connect. Please try again.');
            // Clear storage on connection error
            localStorage.clear();
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

function getRandomName(){
    let nickname = generateRandomNickname()
    console.log(nickname);
    elements.usernameForm.querySelector('#name').value = nickname;
}

function initializeEventListeners() {
    elements.usernameForm.addEventListener('submit', connect, true);
    elements.nickNameButton.addEventListener('click', getRandomName);

    document.addEventListener('DOMContentLoaded', () => {
        if (elements.messageInput.tagName.toLowerCase() === 'input') {
            const textarea = document.createElement('textarea');
            textarea.id = 'textArea';
            textarea.className = elements.messageInput.className;
            textarea.placeholder = 'Ctrl+Enter or hit send button to send message';
            elements.messageInput.parentNode.replaceChild(textarea, elements.messageInput);
            elements.messageInput = textarea;
        }
        elements.sidebarToggle.addEventListener('click', () => {
            elements.userListSidebar.classList.toggle('expanded');
        });
        elements.messageInput.addEventListener('input', (e) => inputHandler.autoResizeInput(e));
        elements.messageInput.addEventListener('input', () => inputHandler.handleTyping());
        elements.messageInput.addEventListener('keydown', (e) => inputHandler.handleKeyPress(e));
        elements.messageForm.addEventListener('submit', sendMessage);
        elements.endButton.addEventListener('click', () => webSocketService.endDiscussion());
        elements.disconnectButton.addEventListener('click', () => {
            if (confirm('Are you sure to disconnect or change username?')) {
                webSocketService.disconnect();
            }
        });
    });
}

initializeEventListeners();

// Check for existing session on page load
document.addEventListener('DOMContentLoaded', () => {
    const roomId = window.ROOM_ID;
    const existingUsername = localStorage.getItem('username');
    const existingToken = localStorage.getItem('userToken');

    if (existingUsername && existingToken && roomId === localStorage.getItem('roomId')) {
        connect();
    }
});