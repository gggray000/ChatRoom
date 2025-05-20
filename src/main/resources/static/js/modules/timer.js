export class Timer {
    constructor(roomId, timerMinutes = 0, timerSeconds = 0) {
        this.totalSeconds = (timerMinutes * 60) + timerSeconds;
        this.timerButton = document.querySelector('#timerBtn');
        this.minutesDisplay = document.querySelector('.minutes');
        this.secondsDisplay = document.querySelector('.seconds');
        this.webSocketService = null;
        this.roomId = roomId;
        this.timerState = localStorage.getItem('timerState')
        this.initialize();
    }

    setWebSocketService(webSocketService) {
        this.webSocketService = webSocketService;
    }

    initialize() {
        if (this.totalSeconds === 0) {
            document.querySelector('.countdown').style.display = 'none';
        } else if (localStorage.getItem('isAdmin') === 'true') {
            this.timerButton.classList.remove('hidden');

            this.timerButton.addEventListener('click', () => this.onClick());

            switch (this.timerState) {
                case 'running':
                    this.timerButton.textContent = 'Pause';
                    break;
                case 'paused':
                    this.timerButton.textContent = 'Resume';
                    break;
                case 'terminated':
                    this.timerButton.textContent = 'Ended';
                    this.timerButton.style.backgroundColor = 'gray';
                    this.timerButton.disabled = true;
                    break;
                default:
                    this.timerButton.textContent = 'Start';
            }
        }
        this.updateDisplay();
    }

    onClick() {
        switch (this.timerState) {
            case 'running':
                this.pause();
                break;
            case 'paused':
                this.start();
                break;
            case 'terminated':
                break;
            default:
                this.start();
        }
    }

    updateDisplay() {
        if (this.totalSeconds === 0) {
            localStorage.setItem('timerState', 'terminated');
            this.timerState = 'terminated';
            this.timerButton.textContent = 'Ended';
            this.timerButton.style.backgroundColor = 'gray';
            this.timerButton.disabled = true;
        }
        const minutes = Math.floor(this.totalSeconds / 60);
        const seconds = this.totalSeconds % 60;
        this.minutesDisplay.textContent = minutes.toString().padStart(2, '0');
        this.secondsDisplay.textContent = seconds.toString().padStart(2, '0');
    }

    start() {
        this.sendWebSocketMessage('TIMER_START')
        localStorage.setItem('timerState', 'running');
        this.timerState = 'running';
        this.timerButton.textContent = 'Pause';
    }

    pause() {
        this.sendWebSocketMessage('TIMER_PAUSE')
        localStorage.setItem('timerState', 'paused');
        this.timerState = 'paused';
        this.timerButton.textContent = 'Resume';
    }

    stop() {
        this.sendWebSocketMessage('TIMER_PAUSE')
        localStorage.setItem('timerState', 'paused');
        this.timerState = 'paused';
        this.timerButton.textContent = 'Start';
    }

    destroy() {
        localStorage.setItem('timerState', 'terminated');
        this.timerState = 'paused';
        this.timerButton.textContent = 'Start';
    }

    sendWebSocketMessage(messageType) {
        const message = {
            sender: localStorage.getItem('username'),
            tokenId: localStorage.getItem('userToken'),
            roomId: this.roomId,
            messageType: messageType,
            timeInSeconds: this.totalSeconds
        };

        const endpoint = messageType === 'TIMER_START' ? `/app/chat/${this.roomId}/setTimer` :
            `/app/chat/${this.roomId}/pauseTimer`;

        this.webSocketService.stompClient.send(endpoint, {}, JSON.stringify(message));
    }
}
