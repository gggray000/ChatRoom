'use strict'

var usernamePage = document.querySelector('#username-page');
var chatPage = document.querySelector('#chat-page');
var usernameForm = document.querySelector('#usernameForm');
var messageForm = document.querySelector('#messageForm');
var messageInput = document.querySelector('#message');
var messageArea = document.querySelector('#messageArea');
var connectingElement = document.querySelector('.connecting');

var stompClient = null;
var username = null;
var connecting = true;

//assigning colors to users
var colors = [
    '#2196F3', '#32c787', '#00BCD4', '#ff5652',
    '#ffc107', '#ff85af', '#FF9800', '#39bbb0'
];

// need a function for Event.LEAVE

function connect(event) {
    username = document.querySelector('#name').value.trim();

    if(username && connecting){
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

    stompClient.send("/app/chat.addUser",
        {},
        JSON.stringify(joinMessage)
    );
    connectingElement.classList.add('hidden');
}

function onError() {
    connectingElement.textContent = 'Could not connect to server. Please retry.';
    connectingElement.style.color = 'red';
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
        messageElement.classList.add('event-message');
        message.content = message.sender + ' joined!';
    } else if (message.messageType === 'LEAVE') {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' left!';
    } else if(message.messageType === 'CHAT'){
        messageElement.classList.add('chat-message');

        var avatarElement = document.createElement('i');
        var avatarText = document.createTextNode(message.sender[0]);
        avatarElement.appendChild(avatarText);
        avatarElement.style['background-color'] = getAvatarColor(message.sender);

        messageElement.appendChild(avatarElement);

        var usernameElement = document.createElement('span');
        var usernameText = document.createTextNode(message.sender);
        usernameElement.appendChild(usernameText);
        messageElement.appendChild(usernameElement);
    }

    if(message.content) {
        var textElement = document.createElement('p');
        var messageText = document.createTextNode(message.content);
        textElement.appendChild(messageText);
        messageElement.appendChild(textElement);
    }

    messageArea.appendChild(messageElement);
    messageArea.scrollTop = messageArea.scrollHeight;
}

function getAvatarColor(messageSender) {
    var hash = 0;
    for (var i = 0; i < messageSender.length; i++) {
        hash = 31 * hash + messageSender.charCodeAt(i);
    }
    var index = Math.abs(hash % colors.length);
    return colors[index];
}

//connect
usernameForm.addEventListener('submit', connect, true);
messageForm.addEventListener('submit', sendMessage, true);