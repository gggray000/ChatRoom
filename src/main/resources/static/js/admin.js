import {elements} from './modules/dom-elements.js';
import i18next from './modules/i18n.js';

var isHuman = false;

document.addEventListener('DOMContentLoaded', function() {

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

    async function createRoom() {
        const currentLocale = new URL(window.location.href).searchParams.get("locale") || "en";
        const roomName = elements.roomNameInput.value.trim();
        const systemPrompt = elements.systemPromptInput ? elements.systemPromptInput.value.trim() : '';
        const timerMinutes = parseInt(elements.timerSlider.value);

        if (!roomName) {
            alert(i18next.t('admin_page.enter_name'));
            return;
        }

        if (!isHuman) {
            alert(i18next.t('admin_page.cap_failed'))
            return
        }

        try {
            const createRoomResponse = await fetch('/admin/create-room', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    name: roomName,
                    systemPrompt: systemPrompt,
                    timerMinutes: timerMinutes
                })
            });

            if (!createRoomResponse.ok) {
                throw new Error(createRoomResponse.statusText);
            }

            const roomData = await createRoomResponse.json();

            const setPromptResponse = await fetch('/admin/set-system-prompt', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    name: roomName,
                    roomId: roomData.roomId,
                    systemPrompt: systemPrompt
                })
            });

            if (!setPromptResponse.ok) {
                throw new Error(setPromptResponse.statusText);
            }

            if (localStorage.getItem('timerState') !== null) localStorage.removeItem('timerState');

            localStorage.removeItem('userToken')
            generateAdminJWT(roomData.roomId);

            const urlId = roomData.url.split('/').pop();
            const confirmUrl = `/admin/confirm?name=${roomName}&id=${roomData.roomId}&url=${urlId}&locale=${currentLocale}`;
            window.open(confirmUrl, '_blank');

        } catch (error) {
            alert(i18next.t('admin_page.fail_create', {error}));
        }
    }

    async function generateAdminJWT(roomId) {
        const response = await fetch('/admin/token', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                username: "admin",
                roomId: roomId,
                isAdmin: true,
                isHuman: isHuman
            })
        });
        if (!response.ok) {
            throw new Error('Failed to get admin token');
        }
        const token = await response.text();
        localStorage.setItem('userToken', token);
    }


    function updateTimerDisplay(value) {
        const timerDisplay = document.getElementById('timer-display');
        if (value === 0) {
            timerDisplay.textContent = i18next.t('admin_page_timer.disabled');
        } else {
            timerDisplay.textContent = i18next.t('admin_page_timer.display', {value});
        }
    }

    elements.timerSlider.addEventListener('input', function () {
        updateTimerDisplay(parseInt(timerSlider.value));
    });

    elements.sliderLabels.addEventListener('click', function (e) {
        if (e.target.tagName === 'SPAN') {
            const value = parseInt(e.target.textContent);
            elements.timerSlider.value = value;
            updateTimerDisplay(value);
        }
    });

    elements.createRoomButton.addEventListener('click', createRoom);

    elements.roomNameInput.addEventListener('keypress', function (event) {
        if (event.key === 'Enter') {
            event.preventDefault();
            createRoom();
        }
    });
});