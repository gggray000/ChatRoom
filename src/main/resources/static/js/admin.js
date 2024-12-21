document.addEventListener('DOMContentLoaded', function() {
    const roomNameInput = document.getElementById('roomName');
    const createRoomButton = document.getElementById('createRoom');
    const roomUrlContainer = document.getElementById('roomUrl');
    const qrCodeContainer = document.getElementById('qrCodeContainer');
    const successMessage = document.getElementById('successMessage');
    const systemPromptInput = document.getElementById('systemPrompt');
    const roomHeader = document.getElementById('room-header');
    const promptHeader = document.getElementById('prompt-header');
    const cancelButton = document.getElementById('cancel-button')

    async function createRoom() {
        const roomName = roomNameInput.value.trim();
        const systemPrompt = systemPromptInput ? systemPromptInput.value.trim() : '';
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
                    systemPrompt: systemPrompt
                })
            });

            if (!createRoomResponse.ok) {
                throw new Error(`HTTP error! status: ${createRoomResponse.status}`);
            }

            const roomData = await createRoomResponse.json();
            cancelButton.dataset.roomId = roomData.roomId;
            cancelButton.dataset.roomName = roomData.name;

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

            cancelButton.dataset.prompt = systemPrompt;

            // Update UI
            successMessage.style.display = 'block';
            roomNameInput.style.display = 'none';
            systemPromptInput.style.display = 'none';
            createRoomButton.style.display = 'none';
            roomHeader.style.display = 'none';
            promptHeader.style.display = 'none';


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
        const roomName = cancelButton.dataset.roomName;
        const systemPrompt = cancelButton.dataset.prompt;

        try {
            const cancelationResponse = await fetch('/admin/cancel-room', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    name: roomName,
                    roomId: roomId,
                    systemPrompt: systemPrompt
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
            createRoomButton.style.display = 'block';

            // Set values from response
            roomNameInput.value = responseData.originalName;
            systemPromptInput.value = responseData.originalPrompt;

            // Hide post-creation elements
            roomUrlContainer.style.display = 'none';
            qrCodeContainer.style.display = 'none';
            qrCodeContainer.innerHTML = ''; // Clear QR code
            cancelButton.style.display = 'none';

            // Clear button dataset
            delete cancelButton.dataset.roomId;
            delete cancelButton.dataset.roomName;
            delete cancelButton.dataset.prompt;

        } catch (error) {
            console.error('Error:', error);
            alert('Failed to cancel room. Please try again.');
        }
    }

    createRoomButton.addEventListener('click', createRoom);
    roomNameInput.addEventListener('keypress', function(event) {
        if (event.key === 'Enter') {
            event.preventDefault();
            createRoom();
        }
    });
    cancelButton.addEventListener('click', cancelCreation);

});