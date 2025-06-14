document.addEventListener('DOMContentLoaded', function() {
    const roomNameInput = document.getElementById('roomName');
    const createRoomButton = document.getElementById('createRoom');
    const systemPromptInput = document.getElementById('systemPrompt');
    const timerSlider = document.getElementById('timerSlider');
    const sliderLabels = document.querySelector('.slider-labels');

    async function createRoom() {
        const currentLocale = new URL(window.location.href).searchParams.get("locale") || "en";
        const roomName = roomNameInput.value.trim();
        const systemPrompt = systemPromptInput ? systemPromptInput.value.trim() : '';
        const timerMinutes = parseInt(timerSlider.value);

        if (!roomName) {
            alert(i18next.t('admin_page.enter_name'));
            return;
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

            localStorage.setItem('isAdmin', true);
            const urlId = roomData.url.split('/').pop();
            const confirmUrl = `/admin/confirm?name=${roomName}&id=${roomData.roomId}&url=${urlId}&locale=${currentLocale}`;
            window.open(confirmUrl, '_blank');

        } catch (error) {
            alert(i18next.t('admin_page.fail_create', {error}));
        }
    }

    function updateTimerDisplay(value) {
        const timerDisplay = document.getElementById('timer-display');
        if (value === 0) {
            timerDisplay.textContent = i18next.t('admin_page_timer.disabled');
        } else {
            timerDisplay.textContent = i18next.t('admin_page_timer.display', {value});
        }
    }

    timerSlider.addEventListener('input', function () {
        updateTimerDisplay(parseInt(timerSlider.value));
    });

    sliderLabels.addEventListener('click', function (e) {
        if (e.target.tagName === 'SPAN') {
            const value = parseInt(e.target.textContent);
            timerSlider.value = value;
            updateTimerDisplay(value);
        }
    });

    createRoomButton.addEventListener('click', createRoom);

    roomNameInput.addEventListener('keypress', function(event) {
        if (event.key === 'Enter') {
            event.preventDefault();
            createRoom();
        }
    });
});