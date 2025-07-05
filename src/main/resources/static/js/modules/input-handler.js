import {TYPING_TIMER_LENGTH} from './constants.js';

export class InputHandler {
    constructor(webSocketService) {
        this.webSocketService = webSocketService;
        this.typingTimeout = null;
    }

    handleTyping() {
        if (!this.typingTimeout) {
            this.webSocketService.sendTypingStatus(true);
        }

        clearTimeout(this.typingTimeout);

        this.typingTimeout = setTimeout(() => {
            this.webSocketService.sendTypingStatus(false);
            this.typingTimeout = null;
        }, TYPING_TIMER_LENGTH);
    }
}