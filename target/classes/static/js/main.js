'use strict'

var usernamePage = document.querySelector('#username-page');
var chatPage = document.querySelector('#chat-page');
var usernameForm = document.querySelector('#usernameForm');
var messageForm = document.querySelector('#messageForm');
var messageInput = document.querySelector('#message');
var messageArea = document.querySelector('#messageArea');
var connectingElement = document.querySelector('.connecting');
var userInfoRow = document.querySelector('#user-info');
var userListElement = document.querySelector('#user-list');
var userCountElement = document.querySelector('#user-count');
var connectedUsers = new Map();

var stompClient = null;
var username = null;

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

        const { avatarElement,
                usernameElement } = createUserInfo(username);
        userElement.appendChild(avatarElement);
        userElement.appendChild(usernameElement);

        userListElement.appendChild(userElement);
        connectedUsers.set(username, userElement);
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
    }

    if(message.content) {
        var textElement = document.createElement('p');
        var messageText = document.createTextNode(message.content);
        textElement.appendChild(messageText);
        messageElement.appendChild(textElement);
        messageArea.appendChild(messageElement);
        messageArea.scrollTop = messageArea.scrollHeight;
    }
}


//connect
usernameForm.addEventListener('submit', connect, true);
messageForm.addEventListener('submit', sendMessage, true);