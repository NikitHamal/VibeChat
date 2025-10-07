// Home Screen Controller
import { auth, database } from '../firebase.js';
import { ref, set, get, update } from "https://www.gstatic.com/firebasejs/10.12.2/firebase-database.js";
import Storage from '../storage.js';

export default class HomeScreen {
    constructor(app) {
        this.app = app;
        this.element = document.getElementById('home-screen');
        this.settingsBtn = document.getElementById('settings-btn');
        this.usernameInput = document.getElementById('username-input');
        this.genderContainer = document.getElementById('gender-container');
        this.genderDisplay = document.getElementById('gender-display');
        this.ageInput = document.getElementById('age-input');
        this.startChatBtn = document.getElementById('start-chat-btn');
        
        this.selectedGender = '';
        this.genders = ['Male', 'Female', 'Other'];
        
        this.setupEventListeners();
    }

    setupEventListeners() {
        this.settingsBtn.addEventListener('click', () => this.openSettings());
        this.genderContainer.addEventListener('click', () => this.showGenderDialog());
        this.startChatBtn.addEventListener('click', () => this.startChat());
    }

    getElement() {
        return this.element;
    }

    onEnter(data) {
        if (data && data.user) {
            this.currentUser = data.user;
            this.loadUserData();
        } else {
            // Handle case where user is not passed, maybe navigate to auth
            this.app.navigate('auth');
        }
    }

    onExit() {
        // Clean up if needed
    }

    async loadUserData() {
        const userRef = ref(database, 'users/' + this.currentUser.uid);
        const snapshot = await get(userRef);
        const userExists = snapshot.exists();
        const userData = userExists ? snapshot.val() : {};

        // Pre-fill with Google data for new users
        if (this.currentUser.providerData[0].providerId === 'google.com' && !userExists) {
            userData.name = this.currentUser.displayName;
            userData.email = this.currentUser.email;
            userData.photoURL = this.currentUser.photoURL;
        }

        // Populate form
        this.usernameInput.value = userData.name || '';
        if (userData.gender) {
            this.selectedGender = userData.gender;
            this.genderDisplay.textContent = userData.gender;
            this.genderDisplay.classList.add('filled');
        }
        this.ageInput.value = userData.age || '';

        // Get country if not present
        if (!userData.country) {
            try {
                const response = await fetch('https://ipapi.co/json/');
                userData.country = (await response.json()).country_code || 'Unknown';
            } catch (error) {
                console.error('Error fetching country:', error);
                userData.country = 'Unknown';
            }
        }
        
        // Save initial data if it's a new user
        if (!userExists) {
            await set(userRef, userData);
        }
    }

    showGenderDialog() {
        const genderOptions = this.genders.map(gender => ({
            label: gender,
            value: gender,
            selected: gender === this.selectedGender
        }));

        this.app.showDialog({
            title: 'Gender',
            options: genderOptions,
            buttons: [{ label: 'Cancel', onClick: () => {} }],
            onSelect: (option) => {
                this.selectedGender = option.value;
                this.genderDisplay.textContent = option.value;
                this.genderDisplay.classList.add('filled');
                setTimeout(() => this.app.hideDialog(), 200);
            }
        });
    }

    openSettings() {
        this.app.navigate('settings');
    }

    async startChat() {
        const name = this.usernameInput.value.trim();
        const age = this.ageInput.value.trim();
        const gender = this.selectedGender;

        if (!name || !age || !gender) {
            this.app.showToast('Please fill all the details');
            return;
        }

        // Save user data to Firebase
        const userRef = ref(database, 'users/' + this.currentUser.uid);
        await update(userRef, {
            name,
            age: parseInt(age),
            gender
        });

        // Navigate to chat
        this.app.navigate('chat');
    }

    showError(message) {
        this.app.showDialog({
            title: 'Error',
            message,
            buttons: [{ label: 'OK', primary: true, onClick: () => {} }]
        });
    }
}
