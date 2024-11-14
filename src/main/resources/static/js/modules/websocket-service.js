import { elements } from './dom-elements.js';
import { createUserInfo } from './avatar-service.js';

export class WebSocketService {
    constructor(userService) {
        this.stompClient = null;
        this.username = null;
        this.userService = userService;
    }

    connect(username) {
        this.username = username;
        const socket = new SockJS('/ws');
        this.stompClient = Stomp.over(socket);
        this.stompClient.connect({},
            () => this.onConnected(),
            () => this.onError()
        );
    }

    onConnected() {
        console.log('Connected to WebSocket!');
        this.stompClient.subscribe('/topic/public',
            (payload) => this.onMessageReceived(payload)
        );

        const joinMessage = {
            sender: this.username,
            messageType: 'JOIN'
        };

        this.userService.addUserToList(this.username);
        this.stompClient.send("/app/chat.addUser", {}, JSON.stringify(joinMessage));
        elements.connectingElement.classList.add('hidden');

        const { avatarElement, usernameElement } = createUserInfo(this.username);
        elements.userInfoRow.appendChild(avatarElement);
        elements.userInfoRow.appendChild(usernameElement);
        elements.userInfoRow.classList.remove('hidden');
    }

    onError() {
        elements.connectingElement.textContent = 'Could not connect to server. Please retry.';
        elements.connectingElement.style.color = 'red';
    }

    sendMessage(messageContent) {
        if (messageContent && this.stompClient) {
            const chatMessage = {
                sender: this.username,
                content: messageContent,
                messageType: 'CHAT'
            };
            this.stompClient.send(
                "/app/chat.sendMessage",
                {},
                JSON.stringify(chatMessage)
            );
        }
    }

    sendTypingStatus(isTyping) {
        const typingMessage = {
            sender: this.username,
            messageType: isTyping ? 'TYPING' : 'TYPING_STOPPED',
            content: null
        };
        this.stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(typingMessage));
    }

    onMessageReceived(payload) {
        console.log('Message received:', payload);
        const message = JSON.parse(payload.body);
        console.log('Parsed message:', message);

        const messageElement = document.createElement('li');

        switch (message.messageType) {
            case 'JOIN':
                this.userService.addUserToList(message.sender);
                messageElement.classList.add('event-message');
                message.content = `${message.sender} joined!`;
                break;

            case 'LEAVE':
                this.userService.removeUserFromList(message.sender);
                messageElement.classList.add('event-message');
                message.content = `${message.sender} left!`;
                break;

            case 'CHAT':
                messageElement.classList.add('chat-message');
                const { avatarElement, usernameElement } = createUserInfo(message.sender);
                messageElement.appendChild(avatarElement);
                messageElement.appendChild(usernameElement);
                break;

            case 'USER_LIST':
                elements.userListElement.innerHTML = '';
                this.userService.connectedUsers.clear();
                message.users.forEach(user => {
                    this.userService.addUserToList(user);
                });
                break;

            case 'TYPING':
            case 'TYPING_STOPPED':
                this.userService.handleTypingIndicator(message.sender, message.messageType === 'TYPING');
                break;
        }

        if (message.content) {
            const textElement = document.createElement('p');
            textElement.style.whiteSpace = 'pre-wrap';
            textElement.textContent = message.content;
            messageElement.appendChild(textElement);
            elements.messageArea.appendChild(messageElement);
            elements.messageArea.scrollTop = elements.messageArea.scrollHeight;
        }
    }
}