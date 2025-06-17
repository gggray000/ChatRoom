import {elements} from './modules/dom-elements.js';
import {UserListService} from './modules/user-list-service.js';
import {WebSocketService} from './modules/websocket-service.js';
import {InputHandler} from './modules/input-handler.js';
import {generateRandomNickname} from "./modules/avatar-service.js";
import {Timer} from "./modules/timer.js";
import i18next from "./modules/i18n.js";

const userListService = new UserListService();
const roomId = window.ROOM_ID;
const roomName = window.ROOM_NAME;
const timerMinutes = window.TIMER_MINUTES || 0;
const timerSeconds = window.TIMER_SECONDS || 0;
const webSocketService = new WebSocketService(userListService);
const inputHandler = new InputHandler(webSocketService);
const isAdmin = document.referrer.includes('/admin') ||
    localStorage.getItem('isAdmin') === 'true';

localStorage.setItem('isHuman', 'false');

if (isAdmin) {
    elements.capWidget.style.display = "none";
    localStorage.setItem('isHuman', 'true');
} else {
    elements.capWidget.addEventListener("solve", function (e) {
        fetch('/captcha', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({
                token: e.detail.token
            })
        }).then(response => {
            if (response.ok) localStorage.setItem('isHuman', 'true')
        })
    })
}

async function connect(event) {
    if (event) {
        event.preventDefault();
    }
    // Get username either from form or localStorage
    const username = event ?
        elements.usernameForm.querySelector('#name').value.trim() :
        localStorage.getItem('username');

    if (!username) {
        alert(i18next.t('username_page.username_null'));
        return;
    }

    if (localStorage.getItem('isHuman') === 'false') {
        alert(i18next.t('username_page.cap_failed'))
        return
    }

    try {
        const verifyResponse = await fetch(`/chat/${roomId}/verifyUsername`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                username: username
            })
        });

        if (!verifyResponse.ok) {
            throw new Error('Failed to verify username.');
        }

        const verifiedUsername = await verifyResponse.text();
        console.log("Verified username: " + verifiedUsername);
        localStorage.setItem('username', verifiedUsername);


            // Only get new token if we don't have one or if this is a new connection
            if (!localStorage.getItem('userToken') || event) {
                const response = await fetch('/admin/token', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        username: verifiedUsername,
                        roomId: roomId,
                        isAdmin: isAdmin
                    })
                });

                if (!response.ok) {
                    throw new Error('Failed to get token');
                }

                const token = await response.text();
                localStorage.setItem('userToken', token);
                localStorage.setItem('isAdmin', isAdmin.toString());
                localStorage.setItem('roomId', roomId);
            }

            elements.usernamePage.classList.add('hidden');
            elements.chatPage.classList.remove('hidden');
            document.querySelector('.chat-header h2').textContent = roomName;

            if (localStorage.getItem('isAdmin') === 'true') {
                elements.buttonContainerAdmin.classList.remove('hidden');
            } else {
                elements.buttonContainerUser.classList.remove('hidden');
            }

        await webSocketService.connect(verifiedUsername, roomId);

        const timer = new Timer(roomId, parseInt(timerMinutes), parseInt(timerSeconds));
        timer.initialize();
        webSocketService.setTimer(timer);

        if (isAdmin) {
            timer.setOnTimerAction((actionType, timeInSeconds) => {
                const message = {
                    sender: localStorage.getItem('username'),
                    tokenId: localStorage.getItem('userToken'),
                    roomId: roomId,
                    messageType: actionType,
                    timeInSeconds: timeInSeconds
                };

                const endpoint = actionType === 'TIMER_START'
                    ? `/app/chat/${roomId}/setTimer`
                    : `/app/chat/${roomId}/operateTimer`;

                webSocketService.stompClient.send(endpoint, {}, JSON.stringify(message));
            });
        }
    } catch (error) {
        console.error('Connection error:', error);
        alert(i18next.t('username_page.connect_fail'));
        localStorage.clear();
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
        const roomId = window.ROOM_ID;
        const existingUsername = localStorage.getItem('username');
        const existingToken = localStorage.getItem('userToken');

        if (existingUsername && existingToken && roomId === localStorage.getItem('roomId')) {
            connect();
        }

        elements.sidebarToggle.addEventListener('click', () => {
            elements.userListSidebar.classList.toggle('expanded');
        });
        elements.messageInput.addEventListener('input', (e) => inputHandler.autoResizeInput(e));
        elements.messageInput.addEventListener('input', () => inputHandler.handleTyping());
        elements.messageInput.addEventListener('keydown', (e) => inputHandler.handleKeyPress(e));
        elements.messageForm.addEventListener('submit', sendMessage);
        elements.summarizeButton.addEventListener('click', () => webSocketService.generateSummary());
        elements.disconnectButton.addEventListener('click', () => {
            let warning = i18next.t('chat_page.disconnect_confirm')
            if (confirm(warning)) {
                webSocketService.disconnect();
            }
        });
        elements.disconnectButtonUser.addEventListener('click', () => {
            let warning = i18next.t('chat_page.disconnect_confirm')
            if (confirm(warning)) {
                webSocketService.disconnect();
            }
        });
        elements.shutdownButton.addEventListener('click', () => {
            let warning = i18next.t('chat_page.shutdown_confirm')
            if (confirm(warning)) {
                webSocketService.shutdownRoom();
            }
        })
    });
}

initializeEventListeners();