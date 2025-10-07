// Auth Screen Controller
import Storage from '../storage.js';

export default class AuthScreen {
    constructor(app) {
        this.app = app;
        this.element = document.getElementById('auth-screen');
        this.googleBtn = document.getElementById('google-signin-btn');
        this.guestBtn = document.getElementById('guest-continue-btn');
        
        this.setupEventListeners();
    }

    setupEventListeners() {
        this.googleBtn.addEventListener('click', () => this.handleGoogleSignIn());
        this.guestBtn.addEventListener('click', () => this.handleGuestContinue());
    }

    getElement() {
        return this.element;
    }

    onEnter(data) {
        // Reset UI if needed
    }

    onExit() {
        // Clean up if needed
    }

    handleGoogleSignIn() {
        // Save auth mode
        Storage.setAuthMode('google');
        
        // TODO: Implement actual Google Sign In
        // For now, just show a message and navigate
        this.showToast('Google Sign In - Coming Soon');
        
        // Navigate to home
        setTimeout(() => {
            this.app.navigate('home');
        }, 500);
    }

    handleGuestContinue() {
        // Save auth mode
        Storage.setAuthMode('guest');
        
        // Navigate to home
        this.app.navigate('home');
    }

    showToast(message) {
        // Simple toast notification (you can enhance this)
        const toast = document.createElement('div');
        toast.style.cssText = `
            position: fixed;
            bottom: 24px;
            left: 50%;
            transform: translateX(-50%);
            background-color: var(--surface-container-highest);
            color: var(--on-surface);
            padding: 12px 24px;
            border-radius: 8px;
            box-shadow: var(--elevation-3);
            z-index: 1000;
            font-size: 14px;
        `;
        toast.textContent = message;
        document.body.appendChild(toast);
        
        setTimeout(() => {
            toast.remove();
        }, 3000);
    }
}
