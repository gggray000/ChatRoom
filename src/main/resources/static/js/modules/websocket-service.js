import {elements} from './dom-elements.js';
import {createUserInfo} from './avatar-service.js';

export class WebSocketService {
    constructor(userListService, timer) {
        this.stompClient = null;
        this.username = null;
        this.userListService = userListService;
        this.roomId = window.ROOM_ID;
        this.timer = timer;
        this.md = window.markdownit({
            html: false,
            breaks: true,
            linkify: true,
            typographer: true,
        });
    }

    async connect(username, roomId) {
        try {
            this.username = username;
            const socketUrl = `${window.location.protocol === 'https:' ? 'https:' : 'http:'}//${window.location.host}/ws`;
            const socket = new SockJS(socketUrl);
            this.stompClient = Stomp.over(socket);
            this.stompClient.debug = function(str) {
                console.log('STOMP: ' + str);
            };

            const token = localStorage.getItem('userToken');
            const headers = {
                username: username,
                token: token,
                roomId: roomId
            };

            this.stompClient.connect(headers,
                () => this.onConnected(),
                error => this.onError(error)
            );
        } catch (error) {
            console.error('Connection error:', error);
            this.onError('Failed to connect. Please try again.');
        }
    }

    onConnected() {
        this.stompClient.subscribe(`/topic/public/${this.roomId}`,
            (payload) => this.onMessageReceived(payload),
            { id: 'sub-0' }
        );

        this.stompClient.subscribe(`/topic/private/${this.roomId}/${this.username}`,
            (payload) => this.onMessageReceived(payload),
            {id: 'sub-1'}
        );

        const joinMessage = {
            sender: this.username,
            messageType: 'JOIN',
            tokenId: localStorage.getItem('userToken')
        };

        this.stompClient.send(`/app/chat/${this.roomId}/addUser`, {}, JSON.stringify(joinMessage));
        elements.connectingElement.classList.add('hidden');

        const getHistoryRequest = {
            sender: this.username,
            messageType: 'GET_HISTORY',
            tokenId: localStorage.getItem('userToken')
        };

        this.stompClient.send(
            `/app/chat/${this.roomId}/history`,
            {},
            JSON.stringify(getHistoryRequest)
        );
    }

    onError(message) {
        elements.connectingElement.textContent = message || 'Could not connect to WebSocket server. Please refresh this page to try again!';
        elements.connectingElement.style.color = 'red';
        elements.connectingElement.classList.remove('hidden');
    }

    disconnect() {
        if (this.timer) {
            this.timer.destroy();
        }

        if (this.stompClient) {
            this.stompClient.disconnect();
        }
        localStorage.clear();

        elements.chatPage.classList.add('hidden');
        elements.usernamePage.classList.remove('hidden');
        elements.messageArea.innerHTML = '';
        this.userListService.clearUserList();
    }

    updateUserInfo(username) {
        this.username = username;
        elements.userInfoRow.innerHTML = '';
        const { avatarElement, usernameElement } = createUserInfo(username);
        elements.userInfoRow.appendChild(avatarElement);
        elements.userInfoRow.appendChild(usernameElement);
        elements.userInfoRow.classList.remove('hidden');
    }

    sendMessage(messageContent) {
        if (messageContent && this.stompClient) {
            const chatMessage = {
                sender: this.username,
                content: messageContent,
                messageType: 'CHAT',
                tokenId: localStorage.getItem('userToken')
            };
            this.stompClient.send(
                `/app/chat/${this.roomId}/sendMessage`,
                {},
                JSON.stringify(chatMessage)
            );
        }
    }

    sendTypingStatus(isTyping) {
        if (this.stompClient) {
            const typingMessage = {
                sender: this.username,
                messageType: isTyping ? 'TYPING' : 'TYPING_STOPPED',
                tokenId: localStorage.getItem('userToken')
            };
            this.stompClient.send(
                `/app/chat/${this.roomId}/sendMessage`,
                {},
                JSON.stringify(typingMessage)
            );
        }
    }

    generateSummary() {
        if (this.stompClient) {
            const getSummaryMessage = {
                sender: this.username,
                messageType: 'GENERATE_SUMMARY',
                tokenId: localStorage.getItem('userToken')
            };
            this.stompClient.send(`/app/chat/${this.roomId}/relayGetSummaryMessage`, {}, JSON.stringify(getSummaryMessage));
            this.stompClient.send(`/app/chat/${this.roomId}/summarize`, {}, JSON.stringify(getSummaryMessage));
        }
    }

    onMessageReceived(payload) {
        const message = JSON.parse(payload.body);
        const messageElement = document.createElement('li');

        switch (message.messageType) {
            case 'JOIN':
                this.handleJoin(message, messageElement);
                break;

            case 'LEAVE':
                this.handleLeave(message, messageElement);
                break;

            case 'CHAT':
                this.handleChat(message, messageElement);
                break;

            case 'USER_LIST':
                this.handleUserList(message);
                break;

            case 'TYPING':
            case 'TYPING_STOPPED':
                this.userListService.handleTypingIndicator(message.sender, message.messageType === 'TYPING');
                return;

            case 'SUMMARY':
                this.handleSummaryAndPdf(this.roomId, message);
                return;

            case 'GENERATE_SUMMARY':
                this.sendEventMessage(message, messageElement, 'Generating discussion summary...');
                break;

            case 'SHOW_HISTORY':
                this.handleHistory(message, messageElement);
                break;

            case 'UPDATE_TIME':
                this.handleUpdateTime(message);
                break;

            case 'TIMER_START':
                this.sendEventMessage(message, messageElement, '--- Timer has been started ---');
                break;

            case 'TIMER_PAUSE':
                this.sendEventMessage(message, messageElement, '--- Timer has been paused ---');
                break;

            case 'TIMES_UP':
                this.sendEventMessage(message, messageElement, '--- Time\'s up! ---');
                break;

            case 'SHUTDOWN':
                this.handleShutDown(message, messageElement);
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

    sendEventMessage(message, messageElement, content) {
        messageElement.classList.add('event-message');
        message.content = content;
    }

    handleJoin(message, messageElement) {
        if (message.sender === this.username) {
            this.updateUserInfo(message.sender);
        }
        this.userListService.addUserToList(message.sender, message.tokenId);
        this.sendEventMessage(message, messageElement, `${message.sender} joined!`)
    }

    handleLeave(message, messageElement) {
        this.userListService.removeUserFromList(message.tokenId);
        this.sendEventMessage(message, messageElement, `${message.sender} left!`);
    }

    handleChat(message, messageElement) {
        messageElement.classList.add('chat-message');
        const {avatarElement, usernameElement} = createUserInfo(message.sender);
        messageElement.appendChild(avatarElement);
        messageElement.appendChild(usernameElement);
    }

    handleUserList(message) {
        this.userListService.clearUserList();
        if (Array.isArray(message.userList)) {
            message.userList.forEach(user => {
                this.userListService.addUserToList(user);
            });
        }
    }

    async handleHistory(message) {
        if (Array.isArray(message.history)) {
            for (const msg of message.history) {
                if (msg.messageType === 'SUMMARY') {
                    await this.handleSummaryAndPdf(this.roomId, msg);
                } else {
                    // Default handling for other message types
                    const messageElement = document.createElement('li');
                    messageElement.classList.add('chat-message');
                    const {avatarElement, usernameElement} = createUserInfo(msg.sender);
                    messageElement.appendChild(avatarElement);
                    messageElement.appendChild(usernameElement);

                    const textElement = document.createElement('p');
                    textElement.style.whiteSpace = 'pre-wrap';
                    textElement.textContent = msg.content;
                    messageElement.appendChild(textElement);

                    elements.messageArea.appendChild(messageElement);
                }
                elements.messageArea.scrollTop = elements.messageArea.scrollHeight;
            }
            const eventMessageElement = document.createElement('li');
            eventMessageElement.classList.add('event-message');
            eventMessageElement.textContent = '--- Previous Messages ---';
            elements.messageArea.appendChild(eventMessageElement);
            elements.messageArea.scrollTop = elements.messageArea.scrollHeight;
        }
    }

    handleUpdateTime(message) {
            this.timer.totalSeconds = message.timeInSeconds;
            this.timer.updateDisplay();
    }

    handleShutDown(message, messageElement) {
        this.sendEventMessage(message, messageElement,
            '--- This discussion had been terminated by admin ---\n' +
            '--- History will be deleted after tab closed or refreshed  ---');
        this.timer.pause();
        if (this.stompClient) {
            this.stompClient.disconnect();
        }
        localStorage.clear();
        this.userListService.clearUserList();
    }

    async handleSummaryAndPdf(roomId, message) {
        const summaryElement = document.createElement('li');
        summaryElement.classList.add('chat-message');

        const { avatarElement, usernameElement } = createUserInfo(message.sender);
        summaryElement.appendChild(avatarElement);
        summaryElement.appendChild(usernameElement);

        const summaryTextElement = document.createElement('div');
        summaryTextElement.classList.add('markdown-content');
        const html = DOMPurify.sanitize(this.md.render(message.content));
        summaryTextElement.innerHTML = html;
        summaryElement.appendChild(summaryTextElement);
        elements.messageArea.appendChild(summaryElement);

        if (message.resource) {
            const pdfElement = document.createElement('li');
            pdfElement.classList.add('event-message');
            const downloadButton = document.createElement('button');
            downloadButton.textContent = 'Download Discussion Summary PDF';
            downloadButton.classList.add('pdf-download-link');

            downloadButton.addEventListener('click', async () => {
                try {
                    // Show loading state
                    downloadButton.textContent = 'Downloading...';
                    downloadButton.disabled = true;

                    const token = localStorage.getItem('userToken');
                    const response = await fetch(`/api/pdf/${message.resource}`, {
                        headers: {
                            roomId: roomId,
                            token: token
                        }
                    });
                    if (!response.ok) {
                        throw new Error(`HTTP error! status: ${response.status}`);
                    }
                    // Convert the response to a blob
                    const blob = await response.blob();
                    // Create a URL for the blob
                    const url = window.URL.createObjectURL(blob);
                    // Create a temporary anchor element
                    const a = document.createElement('a');
                    a.style.display = 'none';
                    a.href = url;
                    a.download = message.resource;
                    // Add to document, click it, and remove it
                    document.body.appendChild(a);
                    a.click();
                    // Clean up
                    window.URL.revokeObjectURL(url);
                    document.body.removeChild(a);
                    // Reset button state
                    downloadButton.textContent = 'Download Discussion Summary PDF';
                    downloadButton.disabled = false;
                } catch (error) {
                    console.error('Download failed:', error);
                    downloadButton.textContent = 'Download Failed - Try Again';
                    downloadButton.disabled = false;
                }
            });
            pdfElement.appendChild(downloadButton);
            elements.messageArea.appendChild(pdfElement);
        }
        elements.messageArea.scrollTop = elements.messageArea.scrollHeight;
    }

    shutdownRoom() {
        if (this.timer) {
            this.timer.stop();
        }
        let roomId = localStorage.getItem('roomId');
        if (roomId === this.roomId) {
            const shutdownRequest = {
                sender: this.username,
                messageType: 'SHUTDOWN',
                tokenId: localStorage.getItem('userToken'),
                roomId: roomId
            };

            this.stompClient.send(
                `/app/admin/${this.roomId}/shutdown`,
                {},
                JSON.stringify(shutdownRequest)
            );
        }
    }
}
