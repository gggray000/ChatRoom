document.addEventListener('DOMContentLoaded', function() {
    const roomNameInput = document.getElementById('roomName');
    const createRoomButton = document.getElementById('createRoom');
    const roomUrlContainer = document.getElementById('roomUrl');
    const qrCodeContainer = document.getElementById('qrCodeContainer');
    const successMessage = document.getElementById('successMessage');
    const systemPromptInput = document.getElementById('systemPrompt');
    const roomHeader = document.getElementById('room-header');
    const promptHeader = document.getElementById('prompt-header');

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
                throw new Error(`HTTP error! status: ${systemPrompt.status}`);
            }

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

        } catch (error) {
            console.error('Error:', error);
            alert('Failed to create room. Please try again.');
        }
    }

    createRoomButton.addEventListener('click', createRoom);
    roomNameInput.addEventListener('keypress', function(event) {
        if (event.key === 'Enter') {
            event.preventDefault();
            createRoom();
        }
    });
});