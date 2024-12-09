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
        const userCountElement = document.querySelector('#user-count');
        if (userCountElement) {
            userCountElement.textContent = `Online Users: ${this.connectedUsers.size}`;
        }
    }

    addUserToList(username, tokenId) {
        if (!tokenId || !username) {
            console.error('Attempted to add user without token ID or username');
            return;
        }

        const userListElement = document.querySelector('#user-list');
        if (!userListElement) return;

        if (!this.connectedUsers.has(tokenId)) {
            const userElement = document.createElement('div');
            userElement.classList.add('user-list-item');
            userElement.setAttribute('data-token-id', tokenId);

            const { avatarElement, usernameElement } = createUserInfo(username);

            const userInfoContainer = document.createElement('div');
            userInfoContainer.classList.add('user-info-container');

            const usernameTooltip = document.createElement('div');
            usernameTooltip.classList.add('username-tooltip');
            usernameTooltip.setAttribute('data-token-id', tokenId);

            const usernameText = document.createTextNode(username);
            usernameTooltip.appendChild(usernameText);

            const tooltipText = document.createElement('span');
            tooltipText.classList.add('tooltiptext');
            tooltipText.textContent = username;
            usernameTooltip.appendChild(tooltipText);

            const typingIndicator = document.createElement('div');
            typingIndicator.classList.add('typing-indicator');
            typingIndicator.textContent = 'typing...';

            userInfoContainer.appendChild(usernameTooltip);
            userInfoContainer.appendChild(typingIndicator);
            userElement.appendChild(avatarElement);
            userElement.appendChild(userInfoContainer);

            userListElement.appendChild(userElement);

            this.connectedUsers.set(tokenId, {
                element: userElement,
                username: username,
                typingIndicator: typingIndicator
            });

            this.updateUserCounter();
        }
    }

    removeUserFromList(tokenId) {
        if (!tokenId) {
            console.error('Attempted to remove user without token ID');
            return;
        }

        const userInfo = this.connectedUsers.get(tokenId);
        if (userInfo && userInfo.element) {
            const userListElement = document.querySelector('#user-list');
            if (userListElement && userInfo.element.parentNode === userListElement) {
                userListElement.removeChild(userInfo.element);
            }
            this.connectedUsers.delete(tokenId);
            this.updateUserCounter();
        }
    }

    clearUserList() {
        const userListElement = document.querySelector('#user-list');
        if (userListElement) {
            userListElement.innerHTML = '';
        }
        this.connectedUsers.clear();
        this.updateUserCounter();
    }

    handleTypingIndicator(tokenId, isTyping) {
        if (!tokenId) {
            console.error('Attempted to update typing status without token ID');
            return;
        }

        const userInfo = this.connectedUsers.get(tokenId);
        if (userInfo && userInfo.typingIndicator) {
            userInfo.typingIndicator.classList[isTyping ? 'add' : 'remove']('active');
        }
    }
}