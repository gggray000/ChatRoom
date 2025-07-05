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
const quillPlaceHolder = i18next.t('chat_page.editor');
const quill = new Quill('#editor', {
    modules: {
        toolbar: {
            container: '#toolbar',
            handlers: {image: imageHandler}
        }
    },
    placeholder: quillPlaceHolder,
    theme: 'snow',
});
var isAdmin = false;
var isHuman = false;
var userToken = (localStorage.getItem('userToken') === null ? "" : localStorage.getItem('userToken'));

document.addEventListener('DOMContentLoaded', async () => {
    await verifyIdentity();
    initializeEventListeners();
    if (userToken && isHuman && localStorage.getItem('roomId') === roomId && localStorage.getItem('username') !== null) {
        await connect()
    }
    ;
});

async function verifyIdentity() {
    const response = await fetch(`/admin/${roomId}/verifyIdentity`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${userToken}`
        }
    });

    if (!response.ok) {
        throw new Error('Failed to verify identity');
    }

    const parsedResponse = await response.json();
    isAdmin = (parsedResponse.isAdmin === "true");
    isHuman = (parsedResponse.isHuman === "true");

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

    if (isHuman === false) {
        alert(i18next.t('username_page.cap_failed'))
        return
    }

    try {
        const usernameResponse = await fetch(`/chat/${roomId}/verifyUsername`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                username: username
            })
        });

        if (!usernameResponse.ok) {
            throw new Error('Failed to verify username.');
        }

        const verifiedUsername = await usernameResponse.text();
        console.log("Verified username: " + verifiedUsername);
        localStorage.setItem('username', verifiedUsername);

        // Admin already has token before. Only get new token for normal users. But no need to get token when users are refreshing the page.
        if (!isAdmin && !localStorage.getItem('userToken')) {
            const response = await fetch('/admin/token', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    username: verifiedUsername,
                    roomId: roomId,
                    isAdmin: false,
                    isHuman: true
                })
            });

            if (!response.ok) {
                throw new Error('Failed to get token');
            }

            const token = await response.text();
            localStorage.setItem('userToken', token);
        }

        elements.usernamePage.classList.add('hidden');
        elements.chatPage.classList.remove('hidden');
        document.querySelector('.chat-header h2').textContent = roomName;

        if (isAdmin) {
            elements.buttonContainerAdmin.classList.remove('hidden');
        } else {
            elements.buttonContainerUser.classList.remove('hidden');
        }

        await webSocketService.connect(verifiedUsername, roomId);
        localStorage.setItem('roomId', roomId);

        const timer = new Timer(roomId, parseInt(timerMinutes), parseInt(timerSeconds), isAdmin);
        timer.initialize();
        webSocketService.setTimer(timer);

        if (isAdmin) {
            timer.setOnTimerAction((actionType, timeInSeconds) => {
                const message = {
                    sender: localStorage.getItem('username'),
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
    const messageContent = quill.getSemanticHTML(0, quill.getLength());
    if (messageContent) {
        if (messageContent === "<p></p>") alert(i18next.t('chat_page.null_message'));
        const transformed = messageContent.replace(
            /<span>(.*?)<\/span>/g,
            (_, formula) => `$${formula.trim()}$`
        );
        webSocketService.sendMessage(transformed);
        quill.deleteText(0, quill.getLength());
    }
}

function imageHandler() {
    const input = document.createElement('input');
    input.setAttribute('type', 'file');
    input.setAttribute('accept', 'image/*');
    input.click();

    input.onchange = async () => {
        const file = input.files[0];
        if (!file) return;

        const resizedBlob = await resizeImage(file, 400); // Resize to max 800px width
        const formData = new FormData();
        formData.append('file', resizedBlob, file.name);

        try {
            const res = await fetch(`/chat/${roomId}/uploadImage`, {
                method: 'POST',
                body: formData
            });
            const data = await res.json();

            if (data.url) {
                const range = quill.getSelection();
                quill.insertEmbed(range.index, 'image', data.url);
            }
        } catch (err) {
            alert('Image upload failed');
        }
    };
}

function resizeImage(file, maxWidth) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = e => {
            const img = new Image();
            img.onload = () => {
                const scale = Math.min(1, maxWidth / img.width);
                const width = img.width * scale;
                const height = img.height * scale;

                const canvas = document.createElement('canvas');
                canvas.width = width;
                canvas.height = height;

                const ctx = canvas.getContext('2d');
                ctx.drawImage(img, 0, 0, width, height);

                canvas.toBlob(blob => {
                    if (blob) resolve(blob);
                    else reject(new Error('Resize failed'));
                }, file.type);
            };
            img.onerror = reject;
            img.src = e.target.result;
        };
        reader.onerror = reject;
        reader.readAsDataURL(file);
    });
}

function getRandomName(){
    let nickname = generateRandomNickname()
    console.log(nickname);
    elements.usernameForm.querySelector('#name').value = nickname;
}

function initializeEventListeners() {
    elements.usernameForm.addEventListener('submit', connect, true);
    elements.nickNameButton.addEventListener('click', getRandomName);

    if (isAdmin) {
        elements.capWidget.style.display = "none";
    } else {
        elements.capWidget.addEventListener("solve", function (e) {
            fetch('/captcha', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({
                    token: e.detail.token
                })
            }).then(response => {
                if (response.ok) isHuman = true;
            })
        })
    }
    elements.sidebarToggle.addEventListener('click', () => {
        elements.userListSidebar.classList.toggle('expanded');
    });

    quill.on('text-change', () => {
        inputHandler.handleTyping()
    })

    if (typeof renderMathInElement === 'function') {
        window.renderMathInElement = renderMathInElement;
    }
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
};