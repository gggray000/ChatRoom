import { elements } from './dom-elements.js';
import { createUserInfo } from './avatar-service.js';

export class UserService {
    constructor() {
        this.connectedUsers = new Map();
    }

    updateUserCounter() {
        elements.userCountElement.textContent = `Online Users: ${this.connectedUsers.size}`;
    }

    addUserToList(username) {
        if (!this.connectedUsers.has(username)) {
            const userElement = document.createElement('div');
            userElement.classList.add('user-list-item');

            const userInfoContainer = document.createElement('div');
            userInfoContainer.style.position = 'relative';

            const { avatarElement, usernameElement } = createUserInfo(username);

            const typingIndicator = document.createElement('div');
            typingIndicator.classList.add('typing-indicator');
            typingIndicator.textContent = 'typing...';

            userElement.appendChild(avatarElement);
            userInfoContainer.appendChild(usernameElement);
            userInfoContainer.appendChild(typingIndicator);
            userElement.appendChild(userInfoContainer);

            elements.userListElement.appendChild(userElement);
            this.connectedUsers.set(username, {
                element: userElement,
                typingIndicator: typingIndicator
            });
            this.updateUserCounter();
        }
    }

    removeUserFromList(username) {
        const userListObject = this.connectedUsers.get(username);
        if (userListObject) {
            elements.userListElement.removeChild(userListObject.element);
            this.connectedUsers.delete(username);
            this.updateUserCounter();
        }
    }

    handleTypingIndicator(username, isTyping) {
        const userListObject = this.connectedUsers.get(username);
        if (userListObject && userListObject.typingIndicator) {
            userListObject.typingIndicator.classList[isTyping ? 'add' : 'remove']('active');
        }
    }
}