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
        this.userDetailsForm = document.getElementById('user-details-form');
        this.anonymousCard = document.getElementById('anonymous-mode-card');
        
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
            if (this.currentUser.isAnonymous) {
                this.userDetailsForm.style.display = 'none';
                this.anonymousCard.style.display = 'block';
            } else {
                this.userDetailsForm.style.display = 'block';
                this.anonymousCard.style.display = 'none';
                this.loadProfileFromCache(); // Load cached data for instant UI
                this.loadProfileFromFirebase(); // Sync with Firebase
            }
        } else {
            this.app.navigate('auth');
        }
    }

    onExit() {
        // Clean up if needed
    }

    loadProfileFromCache() {
        const profile = Storage.getUserProfile();
        this.usernameInput.value = profile.username || '';
        if (profile.gender) {
            this.selectedGender = profile.gender;
            this.genderDisplay.textContent = profile.gender;
            this.genderDisplay.classList.add('filled');
        } else {
            this.selectedGender = '';
            this.genderDisplay.textContent = 'Gender';
            this.genderDisplay.classList.remove('filled');
        }
        this.ageInput.value = profile.age || '';
    }

    async loadProfileFromFirebase() {
        const userRef = ref(database, 'users/' + this.currentUser.uid);
        const snapshot = await get(userRef);

        if (!snapshot.exists()) {
             // If no profile in DB, create one from auth details
            const newUserData = {
                name: this.currentUser.displayName || 'User',
                email: this.currentUser.email,
                photoURL: this.currentUser.photoURL,
                gender: '',
                age: ''
            };
            await set(userRef, newUserData);
            this.updateUIAndCache(newUserData);
        } else {
            this.updateUIAndCache(snapshot.val());
        }
    }

    updateUIAndCache(userData) {
        // Update UI
        this.usernameInput.value = userData.name || '';
        if (userData.gender) {
            this.selectedGender = userData.gender;
            this.genderDisplay.textContent = userData.gender;
            this.genderDisplay.classList.add('filled');
        }
        this.ageInput.value = userData.age || '';

        // Update local storage cache
        Storage.setUserProfile({
            username: userData.name,
            gender: userData.gender,
            age: userData.age
        });
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
        if (this.currentUser.isAnonymous) {
            this.app.navigate('chat');
            return;
        }

        const name = this.usernameInput.value.trim();
        const age = this.ageInput.value.trim();
        const gender = this.selectedGender;

        if (!name || !age || !gender) {
            this.app.showToast('Please fill all the details');
            return;
        }

        const profileData = {
            name,
            age: parseInt(age, 10),
            gender
        };

        // Save to Firebase
        const userRef = ref(database, 'users/' + this.currentUser.uid);
        await update(userRef, profileData);

        // Save to local cache
        Storage.setUserProfile({
            username: name,
            gender: gender,
            age: age
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
