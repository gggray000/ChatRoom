import { COLORS } from './constants.js';

export function getAvatarColor(messageSender) {
    const PRIME = 16777619;
    let hash = 2166136261;
    const username = messageSender.toLowerCase();

    for (let i = 0; i < username.length; i++) {
        hash ^= username.charCodeAt(i);
        hash *= PRIME;
    }

    const index = Math.abs(hash % COLORS.length);
    return COLORS[index];
}

export function createUserInfo(sender) {
    const avatarElement = document.createElement('i');
    avatarElement.textContent = sender[0];
    avatarElement.style['background-color'] = getAvatarColor(sender);
    avatarElement.classList.add('avatar');

    const usernameElement = document.createElement('span');
    usernameElement.textContent = sender;
    usernameElement.classList.add('username');

    return { avatarElement, usernameElement };
}