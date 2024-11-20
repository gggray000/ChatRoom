document.addEventListener('DOMContentLoaded', function() {
    const form = document.querySelector('form');
    const successMessage = document.getElementById('successMessage');
    const roomUrlContainer = document.getElementById('roomUrl');

    form.addEventListener('submit', async function(e) {
        e.preventDefault();

        const roomName = document.getElementById('roomName').value;

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
            roomUrlContainer.style.display = 'block';
            roomUrlContainer.innerHTML = `
                <h3>Room URL:</h3>
                <p>${window.location.origin}${data.url}</p>
            `;

            // Clear the form
            form.reset();

        } catch (error) {
            console.error('Error:', error);
            alert('Failed to create room. Please try again.');
        }
    });
});