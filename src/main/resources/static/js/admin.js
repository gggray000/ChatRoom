document.addEventListener('DOMContentLoaded', function() {
    const roomNameInput = document.getElementById('roomName');
    const createRoomButton = document.getElementById('createRoom');
    const roomUrlContainer = document.getElementById('roomUrl');
    const qrCodeContainer = document.getElementById('qrCodeContainer');
    const successMessage = document.getElementById('successMessage');
    const systemPromptInput = document.getElementById('systemPrompt');
    const roomHeader = document.getElementById('room-header');
    const promptHeader = document.getElementById('prompt-header');
    const cancelButton = document.getElementById('cancel-button');
    const timerSliderContainer = document.querySelector('.timer-slider-container');
    const timerHeader = document.getElementById('timer-header');
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
            // First create the room
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
            cancelButton.dataset.roomId = roomData.roomId;

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

            // Update UI
            successMessage.style.display = 'block';
            roomNameInput.style.display = 'none';
            systemPromptInput.style.display = 'none';
            createRoomButton.style.display = 'none';
            roomHeader.style.display = 'none';
            promptHeader.style.display = 'none';
            timerSliderContainer.style.display = 'none';
            timerHeader.style.display = 'none';

            const roomUrl = window.location.origin + roomData.url;
            const roomUrlLink = roomUrlContainer.querySelector('a');
            roomUrlLink.href = roomUrl;
            roomUrlLink.textContent = roomUrl;
            roomUrlContainer.style.display = 'block';

            const qrCodeUrl = `/admin/qrcode/${roomData.roomId.trim()}`;
            const qrCodeImg = document.createElement('img');
            qrCodeImg.src = qrCodeUrl;
            qrCodeImg.alt = "Fail to load QR-Code"
            qrCodeContainer.appendChild(qrCodeImg);
            qrCodeContainer.style.display = 'block';

            cancelButton.style.display = 'block';

        } catch (error) {
            console.error('Error:', error);
            alert('Failed to create room. Please try again.');
        }
    }

    async function cancelCreation(){
        const roomId = cancelButton.dataset.roomId;

        try {
            const cancelationResponse = await fetch('/admin/cancel-room', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    roomId: roomId
                })
            });

            if (!cancelationResponse.ok) {
                throw new Error(`HTTP error! status: ${cancelationResponse.status}`);
            }

            const responseData = await cancelationResponse.json();

            // Reset UI first
            successMessage.style.display = 'none';
            roomHeader.style.display = 'block';
            promptHeader.style.display = 'block';

            // Show and populate input fields
            roomNameInput.style.display = 'block';
            systemPromptInput.style.display = 'block';
            timerSliderContainer.style.display = 'block';
            createRoomButton.style.display = 'block';
            timerHeader.style.display = 'block';

            // Set values from response
            roomNameInput.value = responseData.originalName;
            systemPromptInput.value = responseData.originalPrompt;
            timerSlider.value = parseInt(responseData.originalMinutes);

            // Hide post-creation elements
            roomUrlContainer.style.display = 'none';
            qrCodeContainer.style.display = 'none';
            qrCodeContainer.innerHTML = ''; // Clear QR code
            cancelButton.style.display = 'none';

            // Clear button dataset
            delete cancelButton.dataset.roomId;

        } catch (error) {
            console.error('Error:', error);
            alert('Failed to cancel room. Please try again.');
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

    cancelButton.addEventListener('click', cancelCreation);

});