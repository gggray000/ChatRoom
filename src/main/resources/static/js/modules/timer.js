export class Timer {
    constructor(roomId, timerMinutes = 0, timerSeconds = 0) {
        this.countdown = null;
        this.totalSeconds = (timerMinutes * 60) + timerSeconds;
        this.isRunning = false;
        this.isPaused = false;
        this.timerButton = document.querySelector('#timerBtn');
        this.minutesDisplay = document.querySelector('.minutes');
        this.secondsDisplay = document.querySelector('.seconds');
        this.webSocketService = null;
        this.roomId = roomId;
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
            this.timerButton.addEventListener('click', () => this.toggleTimer());
        }
        this.updateDisplay();
        this.timerButton.textContent = 'Start';
    }

    updateDisplay() {
        const minutes = Math.floor(this.totalSeconds / 60);
        const seconds = this.totalSeconds % 60;
        this.minutesDisplay.textContent = minutes.toString().padStart(2, '0');
        this.secondsDisplay.textContent = seconds.toString().padStart(2, '0');
    }

    toggleTimer() {
        if (!this.isRunning && !this.isPaused) {
            this.start();
        } else if (this.isRunning) {
            this.pause();
        } else if (this.isPaused) {
            this.start();
        }
    }

    start() {
        this.isRunning = true;
        this.isPaused = false;
        this.sendWebSocketMessage('TIMER_START')

        this.countdown = setInterval(() => {
            if (this.totalSeconds > 0) {
                this.totalSeconds--;
                this.updateDisplay();
                if (localStorage.getItem('isAdmin') === 'true') {
                    this.sendWebSocketMessage('UPDATE_TIME')
                }
            } else {
                this.stop();
                this.onTimerEnd();
            }
        }, 1000);

        this.timerButton.textContent = 'Pause';
    }

    pause() {
        this.isRunning = false;
        this.isPaused = true;
        clearInterval(this.countdown);

        if (localStorage.getItem('isAdmin') === 'true') {
            this.sendWebSocketMessage('TIMER_PAUSE')
        }

        this.timerButton.textContent = 'Resume';
    }

    stop() {
        this.isRunning = false;
        this.isPaused = false;
        clearInterval(this.countdown);

        if (localStorage.getItem('isAdmin') === 'true') {
            this.sendWebSocketMessage('UPDATE_TIME');
            this.sendWebSocketMessage('TIMER_PAUSE')
        }

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

        const endpoint = messageType === 'UPDATE_TIME' ? `/app/chat/${this.roomId}/updateTime` :
            `/app/chat/${this.roomId}/operateTimer`;

        this.webSocketService.stompClient.send(endpoint, {}, JSON.stringify(message));
    }

    onTimerEnd() {
        console.log('Timer ended');
        this.sendWebSocketMessage('TIMES_UP');
        if (this.timerButton) {
            this.timerButton.textContent = 'Time\'s up';
            this.timerButton.disabled = true;
        }
    }

    destroy() {
        if (this.countdown) {
            clearInterval(this.countdown);
            this.countdown = null;
        }

        this.isRunning = false;
        this.isPaused = false;

        if (this.timerButton) {
            this.timerButton.textContent = 'Start';
        }
    }
}
