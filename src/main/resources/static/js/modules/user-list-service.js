import { elements } from './dom-elements.js';
import { createUserInfo } from './avatar-service.js';

function setupTooltip(usernameTooltip, tooltipText) {
    let hideTimeout;

    usernameTooltip.addEventListener('mouseenter', (e) => {
        clearTimeout(hideTimeout);
        const tooltip = tooltipText;
        const rect = usernameTooltip.getBoundingClientRect();

        // Position the tooltip above the username
        tooltip.style.display = 'block';
        tooltip.style.left = `${rect.left}px`;
        tooltip.style.top = `${rect.top - tooltip.offsetHeight - 10}px`; // 10px offset

        // Handle tooltip going off-screen
        const tooltipRect = tooltip.getBoundingClientRect();
        if (tooltipRect.left < 0) {
            tooltip.style.left = '5px';
        }
        if (tooltipRect.right > window.innerWidth) {
            tooltip.style.left = `${window.innerWidth - tooltipRect.width - 5}px`;
        }
        if (tooltipRect.top < 0) {
            // If tooltip would go above viewport, show it below instead
            tooltip.style.top = `${rect.bottom + 10}px`;
        }
    });

    usernameTooltip.addEventListener('mouseleave', () => {
        hideTimeout = setTimeout(() => {
            tooltipText.style.display = 'none';
        }, 100); // Small delay to prevent flickering
    });

    // Handle window resize
    window.addEventListener('resize', () => {
        if (tooltipText.style.display === 'block') {
            const rect = usernameTooltip.getBoundingClientRect();
            tooltipText.style.left = `${rect.left}px`;
            tooltipText.style.top = `${rect.top - tooltipText.offsetHeight - 10}px`;
        }
    });
}

export class UserListService {
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

            const avatarElement = createUserInfo(username).avatarElement;

            const userInfoContainer = document.createElement('div');
            userInfoContainer.classList.add('user-info-container');

            const usernameTooltip = document.createElement('div');
            usernameTooltip.classList.add('username-tooltip');

            const usernameText = document.createTextNode(username);
            usernameTooltip.appendChild(usernameText);

            const tooltipText = document.createElement('span');
            tooltipText.classList.add('tooltiptext');
            tooltipText.textContent = username;

            usernameTooltip.appendChild(tooltipText);

            // Setup tooltip positioning
            setupTooltip(usernameTooltip, tooltipText);

            const typingIndicator = document.createElement('div');
            typingIndicator.classList.add('typing-indicator');
            typingIndicator.textContent = 'typing...';

            userInfoContainer.appendChild(usernameTooltip);
            userInfoContainer.appendChild(typingIndicator);
            userElement.appendChild(avatarElement);
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