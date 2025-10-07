// Auth Screen Controller
import Storage from '../storage.js';
import { auth } from '../firebase.js';
import { GoogleAuthProvider, signInWithPopup, signInAnonymously } from "https://www.gstatic.com/firebasejs/10.12.2/firebase-auth.js";

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
        this.googleBtn.disabled = false;
        this.guestBtn.disabled = false;
    }

    onExit() {
        // Clean up if needed
    }

    async handleGoogleSignIn() {
        this.googleBtn.disabled = true;
        const provider = new GoogleAuthProvider();
        try {
            await signInWithPopup(auth, provider);
            // Auth state listener in app.js will handle navigation
        } catch (error) {
            console.error("Google Sign-In Error:", error);
            this.app.showToast(`Error: ${error.message}`);
            this.googleBtn.disabled = false;
        }
    }

    async handleGuestContinue() {
        this.guestBtn.disabled = true;
        try {
            await signInAnonymously(auth);
            // Auth state listener in app.js will handle navigation
        } catch (error) {
            console.error("Anonymous Sign-In Error:", error);
            this.app.showToast(`Error: ${error.message}`);
            this.guestBtn.disabled = false;
        }
    }
}
