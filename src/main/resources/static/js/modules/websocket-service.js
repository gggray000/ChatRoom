import {elements} from './dom-elements.js';
import {createUserInfo} from './avatar-service.js';

export class WebSocketService {
    constructor(userListService, timer) {
        this.stompClient = null;
        this.username = null;
        this.userListService = userListService;
        this.timer = timer;
        this.roomId = window.ROOM_ID;
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
        this.timer.destroy();

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
                if (message.sender === this.username) {
                    this.updateUserInfo(message.sender);
                }
                this.userListService.addUserToList(message.sender, message.tokenId);
                messageElement.classList.add('event-message');
                message.content = `${message.sender} joined!`;
                break;

            case 'LEAVE':
                this.userListService.removeUserFromList(message.tokenId);
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
                this.userListService.clearUserList();
                //console.log("Received user list:", message.userList);
                if (Array.isArray(message.userList)) {
                    message.userList.forEach(user => {
                        this.userListService.addUserToList(user);
                    });
                }
                return;

            case 'TYPING':
            case 'TYPING_STOPPED':
                this.userListService.handleTypingIndicator(message.sender, message.messageType === 'TYPING');
                return;

            case 'SUMMARY':
                this.handleSummaryAndPdf(this.roomId, message);
                return;

            case 'GENERATE_SUMMARY':
                messageElement.classList.add('event-message');
                message.content = 'Generating discussion summary...';
                break;

            case 'SHOW_HISTORY':
                messageElement.classList.add('event-message');
                message.content = `--- Previous Messages ---`;
                break;

            case 'UPDATE_TIME':
                if (this.timer.isRunning) {
                    this.timer.totalSeconds = message.timeInSeconds;
                    break;
                } else if (this.timer.isFistRun && localStorage.getItem('isAdmin') === 'false') {
                    this.timer.totalSeconds = message.timeInSeconds;
                    this.timer.start()
                    break;
                }
                break;

            case 'TIMER_START':
                if (localStorage.getItem('isAdmin') === 'false') {
                    this.timer.start();
                }
                messageElement.classList.add('event-message');
                message.content = '--- Timer has been started ---';
                break;

            case 'TIMER_PAUSE':
                if (localStorage.getItem('isAdmin') === 'false') {
                    this.timer.pause();
                }
                messageElement.classList.add('event-message');
                message.content = '--- Timer has been paused ---';
                break;

            case 'SHUTDOWN':
                messageElement.classList.add('event-message');
                message.content = '--- This discussion had been terminated by admin ---\n' +
                    '--- History will be deleted after tab closed or refreshed  ---';
                if (this.stompClient) {
                    this.stompClient.disconnect();
                }
                localStorage.clear();
                this.userListService.clearUserList();
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