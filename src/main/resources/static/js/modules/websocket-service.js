import { elements } from './dom-elements.js';
import { createUserInfo } from './avatar-service.js';

export class WebSocketService {
    constructor(userListService) {
        this.stompClient = null;
        this.username = null;
        this.userListService = userListService;
        this.roomId = window.ROOM_ID;

        // Initialize markdown-it with presets
        this.md = window.markdownit({
            html: false,        // Disable HTML tags in source
            breaks: true,       // Convert '\n' in paragraphs into <br>
            linkify: true,      // Autoconvert URL-like text to links
            typographer: true,  // Enable smartquotes and other replacements
        });
    }

    connect(username, roomId) {
        this.username = username;
        const socket = new SockJS('/ws');
        this.stompClient = Stomp.over(socket);

        const adminToken = localStorage.getItem('roomAdminToken_' + roomId);
        const headers = adminToken ? {
            'adminToken': adminToken,
            'roomId': roomId
        } : {};

        this.stompClient.connect(headers,
            () => this.onConnected(),
            () => {
                this.onError();
                if (error.includes("Invalid admin token")) {
                    // Clear invalid token
                    localStorage.removeItem('roomAdminToken_' + roomId);
                    // Handle invalid token error (e.g., show message to user)
                }
            }
        );
    }

    onConnected() {
        console.log('Connected to WebSocket!');
        this.stompClient.subscribe(`/topic/public/${this.roomId}`,
            (payload) => this.onMessageReceived(payload)
        );

        const joinMessage = {
            sender: this.username,
            messageType: 'JOIN',
            roomId: this.roomId
        };

        //this.userListService.addUserToList(this.username);
        this.stompClient.send(`/app/chat/${this.roomId}/addUser`, {}, JSON.stringify(joinMessage));
        this.stompClient.send(`/app/chat/${this.roomId}/sendMessage`, {}, JSON.stringify(joinMessage));
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
                messageType: 'CHAT',
                roomId: this.roomId
            };
            this.stompClient.send(
                `/app/chat/${this.roomId}/sendMessage`,
                {},
                JSON.stringify(chatMessage)
            );
        }
    }

    sendTypingStatus(isTyping) {
        const typingMessage = {
            sender: this.username,
            messageType: isTyping ? 'TYPING' : 'TYPING_STOPPED',
            content: null,
            roomId: this.roomId
        };
        this.stompClient.send(`/app/chat/${this.roomId}/sendMessage`, {}, JSON.stringify(typingMessage));
    }

    endDiscussion(){
        const endMessage = {
            sender: this.username,
            messageType: 'END',
            content: null,
        };
        this.stompClient.send(`/app/chat/${this.roomId}/relayEndMessage`, {}, JSON.stringify(endMessage));
        this.stompClient.send(`/app/chat/${this.roomId}/endDiscussion`, {}, JSON.stringify(endMessage));
    }

    handleSummaryAndPdf(message) {
        // Handle Summary
        const summaryElement = document.createElement('li');
        summaryElement.classList.add('chat-message');

        const { avatarElement: botAvatarElement, usernameElement: botNameElement }
            = createUserInfo(message.sender);

        summaryElement.appendChild(botAvatarElement);
        summaryElement.appendChild(botNameElement);

        const summaryTextElement = document.createElement('div');
        summaryTextElement.classList.add('markdown-content');
        const html = DOMPurify.sanitize(this.md.render(message.content));
        summaryTextElement.innerHTML = html;
        summaryElement.appendChild(summaryTextElement);
        elements.messageArea.appendChild(summaryElement);

        // Handle PDF Download Link
        if (message.resource) {
            const pdfElement = document.createElement('li');
            pdfElement.classList.add('event-message');
            const downloadLink = document.createElement('a');
            downloadLink.href = `/pdf/${message.resource}`;
            downloadLink.textContent = 'Download Discussion Summary PDF';
            downloadLink.classList.add('pdf-download-link');
            pdfElement.appendChild(downloadLink);
            elements.messageArea.appendChild(pdfElement);
        }

        elements.messageArea.scrollTop = elements.messageArea.scrollHeight;
    }

    onMessageReceived(payload) {
        const message = JSON.parse(payload.body);
        const messageElement = document.createElement('li');

        switch (message.messageType) {
            case 'JOIN':
                this.userListService.addUserToList(message.sender);
                messageElement.classList.add('event-message');
                message.content = `${message.sender} joined!`;
                break;

            case 'LEAVE':
                this.userListService.removeUserFromList(message.sender);
                messageElement.classList.add('event-message');
                message.content = `${message.sender} left!`;
                break;

            case 'CHAT':
                messageElement.classList.add('chat-message');
                const { avatarElement, usernameElement } = createUserInfo(message.sender);
                messageElement.appendChild(avatarElement);
                messageElement.appendChild(usernameElement);
                break;

            case 'END':
                messageElement.classList.add('event-message');
                message.content = 'Generating discussion summary...';
                break;

            case 'USER_LIST':
                elements.userListElement.innerHTML = '';
                this.userListService.connectedUsers.clear();
                message.users.forEach(user => {
                    this.userListService.addUserToList(user);
                });
                break;

            case 'TYPING':
            case 'TYPING_STOPPED':
                this.userListService.handleTypingIndicator(message.sender, message.messageType === 'TYPING');
                break;

            case 'SUMMARY':
              this.handleSummaryAndPdf(message);
              return;
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