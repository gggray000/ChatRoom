import i18next from "./i18n.js";

export class Timer {
    constructor(roomId, timerMinutes = 0, timerSeconds = 0, isAdmin) {
        this.totalSeconds = (timerMinutes * 60) + timerSeconds;
        this.timerButton = document.querySelector('#timerBtn');
        this.minutesDisplay = document.querySelector('.minutes');
        this.secondsDisplay = document.querySelector('.seconds');
        this.roomId = roomId;
        this.isAdmin = isAdmin;
        this.timerState = localStorage.getItem('timerState') === null ? null : localStorage.getItem('timerState');
    }

    initialize() {
        if (this.totalSeconds === 0) {
            document.querySelector('.countdown').style.display = 'none';
            this.timerButton.style.display = 'none';
        } else if (this.isAdmin) {
            this.timerButton.addEventListener('click', () => this.onClick());

            switch (this.timerState) {
                case 'running':
                    this.timerButton.textContent = i18next.t('timer_button.pause');
                    break;
                case 'paused':
                    this.timerButton.textContent = i18next.t('timer_button.resume');
                    break;
                case 'terminated':
                    this.timerButton.textContent = i18next.t('timer_button.ended');
                    this.timerButton.style.backgroundColor = 'gray';
                    this.timerButton.disabled = true;
                    break;
                default:
                    this.timerButton.textContent = i18next.t('timer_button.start');
            }
        }
        this.updateDisplay();
    }

    setOnTimerAction(callback) {
        this.onTimerAction = callback;
    }

    onClick() {
        switch (this.timerState) {
            case 'running':
                this.pause();
                break;
            case 'paused':
                this.start();
                break;
            default:
                this.start();
        }
    }

    updateDisplay() {
        if (this.totalSeconds === 0 && this.timerState === 'running') {
            localStorage.setItem('timerState', 'terminated');
            this.timerState = 'terminated';
            this.timerButton.textContent = i18next.t('timer_button.ended');
            this.timerButton.style.backgroundColor = 'gray';
            this.timerButton.disabled = true;
        }
        const minutes = Math.floor(this.totalSeconds / 60);
        const seconds = this.totalSeconds % 60;
        this.minutesDisplay.textContent = minutes.toString().padStart(2, '0');
        this.secondsDisplay.textContent = seconds.toString().padStart(2, '0');
    }

    start() {
        this.onTimerAction('TIMER_START', this.totalSeconds);
        localStorage.setItem('timerState', 'running');
        this.timerState = 'running';
        this.timerButton.textContent = i18next.t('timer_button.pause');
    }

    pause() {
        this.onTimerAction('TIMER_PAUSE', this.totalSeconds);
        localStorage.setItem('timerState', 'paused');
        this.timerState = 'paused';
        this.timerButton.textContent = i18next.t('timer_button.resume');
    }

    terminate() {
        if (this.isAdmin) {
            this.onTimerAction('TIMES_UP', this.totalSeconds);
        }
        localStorage.setItem('timerState', 'terminated')
        this.timerState = 'terminated';
        this.timerButton.textContent = i18next.t('timer_button.ended');
        this.timerButton.style.backgroundColor = 'gray';
        this.timerButton.disabled = true;
    }
}
