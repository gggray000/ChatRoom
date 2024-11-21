document.addEventListener('DOMContentLoaded', function() {
    const roomNameInput = document.getElementById('roomName');
    const createRoomButton = document.getElementById('createRoom');
    const roomUrlContainer = document.getElementById('roomUrl');
    const qrCodeContainer = document.getElementById('qrCodeContainer');
    const successMessage = document.getElementById('successMessage');

    createRoomButton.addEventListener('click', async function() {
        const roomName = roomNameInput.value.trim();

        if (!roomName) {
            alert('Please enter a room name');
            return;
        }

        try {
            const response = await fetch('/admin/create-room', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ name: roomName })
            });

            if (!response.ok) {
                throw new Error('Network response was not ok');
            }

            const data = await response.json();

            // Show success message
            successMessage.style.display = 'block';
            setTimeout(() => {
                successMessage.style.display = 'none';
            }, 3000);

            // Display room URL
            const roomUrl = window.location.origin + data.url;
            const roomUrlLink = roomUrlContainer.querySelector('a');
            roomUrlLink.href = roomUrl;
            roomUrlLink.textContent = roomUrl;
            roomUrlContainer.style.display = 'block';

            // Display QR Code
            const qrCode = document.getElementById('qrCode');
            const roomId = data.roomId; // Get roomId from URL
            qrCode.src = `/admin/qrcode/${roomId}`;// Sending get request to AppController.java
            qrCodeContainer.style.display = 'block';

            // Clear input
            roomNameInput.value = '';

        } catch (error) {
            console.error('Error:', error);
            alert('Failed to create room. Please try again.');
        }
    });
});