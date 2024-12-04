document.addEventListener('DOMContentLoaded', function() {
    const roomNameInput = document.getElementById('roomName');
    const createRoomButton = document.getElementById('createRoom');
    const roomUrlContainer = document.getElementById('roomUrl');
    const qrCodeContainer = document.getElementById('qrCodeContainer');
    const successMessage = document.getElementById('successMessage');
    const systemPromptInput = document.getElementById('systemPrompt');

    async function createRoom() {
        const roomName = roomNameInput.value.trim();
        const systemPrompt = systemPromptInput ? systemPromptInput.value.trim() : '';
        if (!roomName) {
            alert('Please enter a room name');
            return;
        }

        try {
            if (systemPrompt) {
                await fetch('/admin/set-system-prompt', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ prompt: systemPrompt })
                });
            }
            const response = await fetch('/admin/create-room', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    name: roomName,
                    systemPrompt: systemPrompt
                })
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();

            if (data.adminToken && data.roomId) {
                localStorage.setItem('roomAdminToken_' + data.roomId, data.adminToken);

                successMessage.style.display = 'block';
                roomNameInput.style.display = 'none';
                systemPromptInput.style.display = 'none';
                createRoomButton.style.display = 'none';

                const roomUrl = window.location.origin + data.url;
                const roomUrlLink = roomUrlContainer.querySelector('a');
                roomUrlLink.href = roomUrl;
                roomUrlLink.textContent = roomUrl;
                roomUrlContainer.style.display = 'block';

                const qrCodeUrl = `/admin/qrcode/${data.roomId}?roomName=${encodeURIComponent(roomName)}`;
                const qrCodeImg = document.createElement('img');
                qrCodeImg.src = qrCodeUrl;
                qrCodeContainer.appendChild(qrCodeImg);
                qrCodeContainer.style.display = 'block';
            }
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