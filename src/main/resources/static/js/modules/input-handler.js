import { elements } from './dom-elements.js';
import { TYPING_TIMER_LENGTH } from './constants.js';

export class InputHandler {
    constructor(webSocketService) {
        this.webSocketService = webSocketService;
        this.typingTimeout = null;
    }

    autoResizeInput(event) {
        const input = event.target;
        input.style.height = 'auto';
        const newHeight = Math.min(Math.max(input.scrollHeight, 40), 120);
        input.style.height = newHeight + 'px';
    }

    handleKeyPress(event) {
        if (event.key === 'Enter' && (event.ctrlKey || event.metaKey)) {
            event.preventDefault();
            this.webSocketService.sendMessage(elements.messageInput.value.trim());
            elements.messageInput.value = '';
        }
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