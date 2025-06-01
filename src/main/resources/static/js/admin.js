document.addEventListener('DOMContentLoaded', function() {
    const roomNameInput = document.getElementById('roomName');
    const createRoomButton = document.getElementById('createRoom');
    const systemPromptInput = document.getElementById('systemPrompt');
    const timerSlider = document.getElementById('timerSlider');
    const sliderLabels = document.querySelector('.slider-labels');

    async function createRoom() {
        const roomName = roomNameInput.value.trim();
        const systemPrompt = systemPromptInput ? systemPromptInput.value.trim() : '';
        const timerMinutes = parseInt(timerSlider.value);

        if (!roomName) {
            alert('Please enter a room name');
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
                throw new Error(`HTTP error! status: ${createRoomResponse.status}`);
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
                throw new Error(`HTTP error! status: ${setPromptResponse.status}`);
            }

            const urlId = roomData.url.split('/').pop();
            const confirmUrl = `/admin/confirm?name=${roomName}&id=${roomData.roomId}&url=${urlId}`;
            window.open(confirmUrl, '_blank');

        } catch (error) {
            console.error('Error:', error);
            alert('Failed to create room. Please try again.');
        }
    }

    function updateTimerDisplay(value) {
        const timerDisplay = document.getElementById('timer-display');
        if (value === 0) {
            timerDisplay.textContent = 'Timer Disabled';
        } else {
            timerDisplay.textContent = `Selected time: ${value} minutes`;
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