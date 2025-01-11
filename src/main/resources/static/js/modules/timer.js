export class Timer {
    constructor(roomId) {
        this.countdown = null;
        this.totalSeconds = 0;
        this.initialMinutes = 0;
        this.initialSeconds = 0;
        this.isFistRun = true;
        this.isRunning = false;
        this.isPaused = false;
        this.countdownElement = document.querySelector('.countdown');
        this.minutesDisplay = this.countdownElement.querySelector('.minutes');
        this.secondsDisplay = this.countdownElement.querySelector('.seconds');
        this.timerButton = document.querySelector('#timerBtn');
        this.webSocketService = null;
        this.roomId = roomId;

        this.initialize();
        if (this.timerButton) {
            this.timerButton.addEventListener('click', () => this.toggleTimer());
        }
    }

    setWebSocketService(webSocketService) {
        this.webSocketService = webSocketService;
    }

    initialize() {
        // Get initial time from data attributes
        this.initialMinutes = parseInt(this.countdownElement.getAttribute('data-minutes'), 10) || 0;
        this.initialSeconds = parseInt(this.countdownElement.getAttribute('data-seconds'), 10) || 0;
        this.totalSeconds = (this.initialMinutes * 60) + this.initialSeconds;
        this.updateDisplay();
        this.timerButton.textContent = 'Start';
    }

    updateDisplay() {
        const minutes = Math.floor(this.totalSeconds / 60);
        const seconds = this.totalSeconds % 60;
        this.minutesDisplay.textContent = minutes.toString().padStart(2, '0');
        this.secondsDisplay.textContent = seconds.toString().padStart(2, '0');
    }

    start() {
        if ((!this.isRunning && !this.isPaused && this.isFistRun && this.totalSeconds > 0) ||
            (this.isPaused && !this.isFistRun && this.totalSeconds > 0)) {
            this.isFistRun = false;
            this.isRunning = true;
            this.isPaused = false;
            this.countdownElement.setAttribute('data-running', 'true');
            this.countdown = setInterval(() => {
                if (this.totalSeconds > 0) {
                    this.totalSeconds--;
                    this.updateDisplay();
                    if (localStorage.getItem('isAdmin') === 'true') {
                        this.updateGlobalTime();
                    }
                } else {
                    this.stop();
                    this.onTimerEnd();
                }
            }, 1000);
            if (localStorage.getItem('isAdmin') === 'true') {
                this.startGlobal();
            }
            this.timerButton.textContent = 'Pause';
        }
    }

    stop() {
        if (this.isRunning) {
            this.isRunning = false;
            this.isPaused = false;
            this.countdownElement.setAttribute('data-running', 'false');
            clearInterval(this.countdown);
            if (localStorage.getItem('isAdmin') === 'true') {
                this.updateGlobalTime();
            }
            //this.timerButton.textContent = 'Start';
            // Reset timer to initial values
            // this.totalSeconds = (this.initialMinutes * 60) + this.initialSeconds;
            // this.updateDisplay();
        }
    }

    pause() {
        if (this.isRunning) {
            this.isRunning = false;
            this.isPaused = true;
            this.countdownElement.setAttribute('data-running', 'false');
            clearInterval(this.countdown);
            if (localStorage.getItem('isAdmin') === 'true') {
                this.pauseGlobal();
                this.updateGlobalTime();
            }
            this.timerButton.textContent = 'Resume';
        }
    }

    resume() {
        if (this.isPaused) {
            this.start();
        }
    }

    toggleTimer() {
        if (!this.isRunning && !this.isPaused) {
            // Timer is stopped, start it
            this.start();
        } else if (this.isRunning) {
            // Timer is running, pause it
            this.pause();
        } else if (this.isPaused) {
            // Timer is paused, resume it
            this.resume();
        }
    }

    updateGlobalTime() {
        const updateTimeMessage = {
            sender: localStorage.getItem('username'),
            tokenId: localStorage.getItem('userToken'),
            roomId: this.roomId,
            messageType: 'UPDATE_TIME',
            timeInSeconds: this.totalSeconds
        };
        this.webSocketService.stompClient.send(`/app/admin/${this.roomId}/updateTime`, {}, JSON.stringify(updateTimeMessage));
    }

    startGlobal() {
        const startTimerMessage = {
            sender: localStorage.getItem('username'),
            tokenId: localStorage.getItem('userToken'),
            roomId: this.roomId,
            messageType: 'TIMER_START',
        };
        this.webSocketService.stompClient.send(`/app/admin/${this.roomId}/timerOperation`, {}, JSON.stringify(startTimerMessage));
    }

    pauseGlobal() {
        const startTimerMessage = {
            sender: localStorage.getItem('username'),
            tokenId: localStorage.getItem('userToken'),
            roomId: this.roomId,
            messageType: 'TIMER_PAUSE',
        };
        this.webSocketService.stompClient.send(`/app/admin/${this.roomId}/timerOperation`, {}, JSON.stringify(startTimerMessage));
    }

    // reset() {
    //     this.stop();
    //     this.isPaused = false;
    //     this.totalSeconds = (this.initialMinutes * 60) + this.initialSeconds;
    //     this.updateDisplay();
    //     this.timerButton.textContent = 'Start';
    // }

    onTimerEnd() {
        console.log('Timer ended');
        // Custom end behavior can be defined in main.js
    }

    // getTimeLeft() {
    //     return {
    //         minutes: Math.floor(this.totalSeconds / 60),
    //         seconds: this.totalSeconds % 60,
    //         totalSeconds: this.totalSeconds
    //     };
    // }
    //
    // setTime(minutes, seconds = 0) {
    //     this.stop();
    //     this.isPaused = false;
    //     this.initialMinutes = minutes;
    //     this.initialSeconds = seconds;
    //     this.totalSeconds = (minutes * 60) + seconds;
    //     this.updateDisplay();
    // }
    //
    // destroy() {
    //     this.stop();
    //     if (this.timerButton) {
    //         this.timerButton.removeEventListener('click', this.toggleTimer);
    //     }
    // }

}