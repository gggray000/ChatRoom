'use strict'

var usernamePage = document.querySelector('#username-page');
var chatPage = document.querySelector('#chat-page');
var usernameForm = document.querySelector('#usernameForm');
var messageForm = document.querySelector('#messageForm');
var messageInput = document.querySelector('#textArea');
var messageArea = document.querySelector('#messageArea');
var connectingElement = document.querySelector('.connecting');
var userInfoRow = document.querySelector('#user-info');
var userListElement = document.querySelector('#user-list');
var userCountElement = document.querySelector('#user-count');
var connectedUsers = new Map();

var stompClient = null;
var username = null;

let typingTimeout;
const TYPING_TIMER_LENGTH = 2000;

//color list for the avatar
var colors = [
    // Blues
    '#2196F3', '#1976D2', '#4FC3F7', '#0D47A1', '#00BCD4', '#39bbb0', '#006064',
    // Greens
    '#32c787', '#4CAF50', '#8BC34A', '#00796B', '#43A047', '#7CB342', '#558B2F',
    // Purples & Pinks
    '#9C27B0', '#673AB7', '#E91E63', '#ff85af', '#D81B60', '#8E24AA', '#BA68C8',
    // Reds & Oranges
    '#ff5652', '#F44336', '#FF9800', '#FF5722', '#E64A19', '#F57C00', '#FFB74D',
    // Warm Colors
    '#ffc107', '#FFA000', '#FBC02D', '#F9A825', '#FB8C00', '#FFB300', '#FFD54F',
    // Teals & Cyans
    '#009688', '#00ACC1', '#26C6DA', '#00838F', '#0097A7', '#00BFA5', '#1DE9B6'
];

//FNV hashing algorithm
function getAvatarColor(messageSender) {
    const PRIME = 16777619 ;
    let hash = 2166136261;
    const username = messageSender.toLowerCase();
    // Use FNV-1a hash algorithm for better distribution
    for (let i = 0; i < username.length; i++) {
        hash ^= username.charCodeAt(i);
        hash *= PRIME;
    }
    // Ensure positive index and use modulo for color selection
    const index = Math.abs(hash % colors.length);
    return colors[index];
}

function createUserInfo(sender){
    const avatarElement = document.createElement('i');
    avatarElement.textContent = sender[0];
    avatarElement.style['background-color'] = getAvatarColor(sender);
    avatarElement.classList.add('avatar');

    // Create username element
    const usernameElement = document.createElement('span');
    usernameElement.textContent = sender;
    usernameElement.classList.add('username');

    return { avatarElement, usernameElement };
}

function connect(event) {
    username = document.querySelector('#name').value.trim();
    if(username){
        usernamePage.classList.add('hidden');
        chatPage.classList.remove('hidden');

        var socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);
        stompClient.connect({}, onConnected, onError);
    }
    event.preventDefault();
}

function onConnected() {
    console.log('Connected to WebSocket!')
    //subscribe to public topic
    stompClient.subscribe('/topic/public', onMessageReceived);
    //tell username to the server
    const joinMessage = {
        sender: username,
        messageType: 'JOIN'};
    console.log('Sending JOIN message: ', joinMessage);

    addUserToList(username);

    stompClient.send("/app/chat.addUser",
        {},
        JSON.stringify(joinMessage)
    );
    connectingElement.classList.add('hidden');

    const { avatarElement,
            usernameElement } = createUserInfo(username);
    userInfoRow.appendChild(avatarElement);
    userInfoRow.appendChild(usernameElement);
    userInfoRow.classList.remove('hidden');
}

function onError() {
    connectingElement.textContent = 'Could not connect to server. Please retry.';
    connectingElement.style.color = 'red';
}

function updateUserCounter() {
    userCountElement.textContent = `Online Users: ${connectedUsers.size}`;
}

function addUserToList(username) {
    if (!connectedUsers.has(username)) {
        const userElement = document.createElement('div');
        userElement.classList.add('user-list-item');
        // Create container for username and typing indicator
        const userInfoContainer = document.createElement('div');
        userInfoContainer.style.position = 'relative';
        const { avatarElement, usernameElement } = createUserInfo(username);
        // Create typing indicator
        const typingIndicator = document.createElement('div');
        typingIndicator.classList.add('typing-indicator');
        typingIndicator.textContent = 'typing...';
       // typingIndicator.classList.add('hidden');
        userElement.appendChild(avatarElement);
        userInfoContainer.appendChild(usernameElement);
        userInfoContainer.appendChild(typingIndicator);
        userElement.appendChild(userInfoContainer);

        userListElement.appendChild(userElement);
        connectedUsers.set(username, {
            element: userElement,
            typingIndicator: typingIndicator
        });
        updateUserCounter();
    }
}

function removeUserFromList(username) {
    const userElement = connectedUsers.get(username);
    if (userElement) {
        userListElement.removeChild(userElement);
        connectedUsers.delete(username);
        updateUserCounter();
    }
}

function sendMessage(event){
    var messageContent = messageInput.value.trim();
    if(messageContent && stompClient){
        var chatMessage = {
            sender: username,
            content: messageContent,
            messageType: 'CHAT'
        };
        stompClient.send(
            "/app/chat.sendMessage",
            {},
            JSON.stringify(chatMessage)
        );
        messageInput.value = '';
    }
    event.preventDefault();
}

function onMessageReceived(payload) {
    console.log('Message received:', payload);
    var message = JSON.parse(payload.body);
    console.log('Parsed message:', message);
    var messageElement = document.createElement('li');

    if(message.messageType === 'JOIN') {
        addUserToList(message.sender);
        messageElement.classList.add('event-message');
        message.content = message.sender + ' joined!';
    } else if (message.messageType === 'LEAVE') {
        removeUserFromList(message.sender);
        messageElement.classList.add('event-message');
        message.content = message.sender + ' left!';
    } else if(message.messageType === 'CHAT'){
        messageElement.classList.add('chat-message');
        // Create new avatar and username elements for each chat message
        const { avatarElement,
                usernameElement } =
            createUserInfo(message.sender);
        messageElement.appendChild(avatarElement);
        messageElement.appendChild(usernameElement);
    } else if (message.messageType === 'USER_LIST') {
        // Clear existing user list first
        userListElement.innerHTML = '';
        connectedUsers.clear();
        message.users.forEach(user => {
            addUserToList(user);
        });
    } else if (message.messageType === 'TYPING') {
            const user = connectedUsers.get(message.sender);
            if (user && user.typingIndicator) {
                user.typingIndicator.classList.add('active');
            }
        } else if (message.messageType === 'TYPING_STOPPED') {
            const user = connectedUsers.get(message.sender);
            if (user && user.typingIndicator) {
                user.typingIndicator.classList.remove('active');
            }
        }

    if(message.content) {
        var textElement = document.createElement('p');
        // Set white-space style to preserve line breaks
        textElement.style.whiteSpace = 'pre-wrap';
        // Use textContent instead of createTextNode to preserve line breaks
        textElement.textContent = message.content;
        messageElement.appendChild(textElement);
        messageArea.appendChild(messageElement);
        messageArea.scrollTop = messageArea.scrollHeight;
    }
}

// Function to handle input box auto-resize
function autoResizeInput(event) {
    const input = event.target;
    // Reset height to auto to get the correct scrollHeight
    input.style.height = 'auto';
    // Set new height based on scrollHeight, within limits
    const newHeight = Math.min(Math.max(input.scrollHeight, 40), 120);
    input.style.height = newHeight + 'px';
}

// Handle textarea key events for sending messages
function handleKeyPress(event) {
    // Check if it's Ctrl+Enter (or Cmd+Enter for Mac)
    if (event.key === 'Enter' && (event.ctrlKey || event.metaKey)) {
        event.preventDefault(); // Prevent new line
        sendMessage(); // Send the message
    }
}

function handleTyping(event) {
    // Step 1: Check if this is the first keystroke
    if (!typingTimeout) {  // If no timer running
        // Send TYPING_START because this is the first keystroke
        const typingMessage = {
            sender: username,
            messageType: 'TYPING',
            content: null
        };
        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(typingMessage));
    }
    // Step 2: Clear any existing timer
    clearTimeout(typingTimeout);  // Cancel the previous timer
    // Step 3: Start a new timer
    typingTimeout = setTimeout(() => {
        // This code runs after TYPING_TIMER_LENGTH milliseconds of no typing
        const typingMessage = {
            sender: username,
            messageType: 'TYPING_STOPPED',
            content: null
        };
        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(typingMessage));
        typingTimeout = null;  // Reset the timer ID
    }, TYPING_TIMER_LENGTH);
}

usernameForm.addEventListener('submit', connect, true);

document.addEventListener('DOMContentLoaded', function() {
    // Convert input to textarea if it's not already
    if (messageInput.tagName.toLowerCase() === 'input') {
        const textarea = document.createElement('textarea');
        textarea.id = 'textArea';
        textarea.className = messageInput.className;
        textarea.placeholder = 'Ctrl+Enter or hit send button to send message';
        messageInput.parentNode.replaceChild(textarea, messageInput);
        messageInput = textarea; // Update the reference
    }
    messageInput.addEventListener('input', autoResizeInput);
    messageInput.addEventListener('input', handleTyping);
    messageInput.addEventListener('keydown', handleKeyPress);
    messageForm.addEventListener('submit', sendMessage);
});