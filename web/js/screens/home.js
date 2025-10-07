// Home Screen Controller
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
        // Load saved user data
        this.loadUserData();
    }

    onExit() {
        // Clean up if needed
    }

    loadUserData() {
        const profile = Storage.getUserProfile();
        
        if (profile.username) {
            this.usernameInput.value = profile.username;
        }
        
        if (profile.gender) {
            this.selectedGender = profile.gender;
            this.genderDisplay.textContent = profile.gender;
            this.genderDisplay.classList.add('filled');
        }
        
        if (profile.age) {
            this.ageInput.value = profile.age;
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
            buttons: [
                {
                    label: 'Cancel',
                    onClick: () => {}
                }
            ],
            onSelect: (option) => {
                this.selectedGender = option.value;
                this.genderDisplay.textContent = option.value;
                this.genderDisplay.classList.add('filled');
                
                // Auto-close after selection
                setTimeout(() => {
                    this.app.hideDialog();
                }, 200);
            }
        });
    }

    openSettings() {
        this.app.navigate('settings');
    }

    async startChat() {
        const username = this.usernameInput.value.trim();
        const age = this.ageInput.value.trim();

        // Validation
        if (!username) {
            this.showError('Please enter a username');
            return;
        }

        if (!this.selectedGender) {
            this.showError('Please select a gender');
            return;
        }

        // Save user data
        Storage.setUserProfile({
            username,
            gender: this.selectedGender,
            age,
            country: await this.getUserCountry()
        });

        // Navigate to chat
        this.app.navigate('chat', {
            username,
            gender: this.selectedGender,
            age
        });
    }

    async getUserCountry() {
        try {
            const response = await fetch('https://ipapi.co/json/');
            const data = await response.json();
            return data.country_code || 'Unknown';
        } catch (error) {
            console.error('Error fetching country:', error);
            return 'Unknown';
        }
    }

    showError(message) {
        this.app.showDialog({
            title: 'Error',
            message,
            buttons: [
                {
                    label: 'OK',
                    primary: true,
                    onClick: () => {}
                }
            ]
        });
    }
}
