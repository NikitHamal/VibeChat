// Profile Screen Controller
import { auth, database } from '../firebase.js';
import { ref, get, update } from "https://www.gstatic.com/firebasejs/10.12.2/firebase-database.js";

export default class ProfileScreen {
    constructor(app) {
        this.app = app;
        this.element = document.getElementById('profile-screen');
        this.backBtn = document.getElementById('profile-back-btn');
        this.fab = document.getElementById('profile-fab');
        
        // Display elements
        this.nameDisplay = document.getElementById('name-display');
        this.genderDisplayProfile = document.getElementById('gender-display-profile');
        this.ageDisplayProfile = document.getElementById('age-display-profile');
        this.countryDisplayProfile = document.getElementById('country-display-profile');
        
        // Edit elements
        this.nameEdit = document.getElementById('name-edit');
        this.genderEdit = document.getElementById('gender-edit');
        this.ageEdit = document.getElementById('age-edit');
        this.countryEdit = document.getElementById('country-edit');
        
        this.isEditMode = false;
        this.genders = ['Male', 'Female', 'Other'];
        
        this.setupEventListeners();
    }

    setupEventListeners() {
        this.backBtn.addEventListener('click', () => this.handleBack());
        this.fab.addEventListener('click', () => this.toggleEditMode());
        this.genderEdit.addEventListener('click', () => this.showGenderDialog());
    }

    getElement() {
        return this.element;
    }

    onEnter(data) {
        this.currentUser = auth.currentUser;
        if (!this.currentUser || this.currentUser.isAnonymous) {
            this.app.navigate('settings'); // Should not be here if anonymous
            return;
        }
        this.loadProfile();
    }

    onExit() {
        if (this.isEditMode) {
            this.exitEditMode(false); // Don't reload data on exit
        }
    }

    async loadProfile() {
        const userRef = ref(database, 'users/' + this.currentUser.uid);
        const snapshot = await get(userRef);
        const profile = snapshot.val() || {};
        
        // Update display elements
        this.nameDisplay.textContent = profile.name || 'Not specified';
        this.genderDisplayProfile.textContent = profile.gender || 'Not specified';
        this.ageDisplayProfile.textContent = profile.age || 'Not specified';
        this.countryDisplayProfile.textContent = profile.country || 'Not specified';
        
        // Update edit elements
        this.nameEdit.value = profile.name || '';
        this.ageEdit.value = profile.age || '';
        this.countryEdit.value = profile.country || '';
        this.genderEdit.textContent = profile.gender || 'Not specified';
    }

    toggleEditMode() {
        if (this.isEditMode) {
            this.saveProfile();
        } else {
            this.enterEditMode();
        }
    }

    enterEditMode() {
        this.isEditMode = true;
        this.element.classList.add('edit-mode');
        this.fab.querySelector('.material-symbols-outlined').textContent = 'check';
        this.nameEdit.focus();
    }

    async saveProfile() {
        const name = this.nameEdit.value.trim();
        const age = this.ageEdit.value.trim();
        const country = this.countryEdit.value.trim();
        const gender = this.genderEdit.textContent.trim();
        
        if (!name) {
            this.app.showToast('Name cannot be empty');
            return;
        }
        
        const userRef = ref(database, 'users/' + this.currentUser.uid);
        await update(userRef, {
            name,
            gender: gender === 'Not specified' ? '' : gender,
            age: age ? parseInt(age) : null,
            country
        });
        
        this.exitEditMode(true); // Reload data after saving
        this.app.showToast('Profile updated successfully');
    }

    exitEditMode(reload = true) {
        this.isEditMode = false;
        this.element.classList.remove('edit-mode');
        this.fab.querySelector('.material-symbols-outlined').textContent = 'edit';
        if (reload) {
            this.loadProfile();
        }
    }

    showGenderDialog() {
        const currentGender = this.genderEdit.textContent.trim();
        const options = this.genders.map(gender => ({
            label: gender,
            value: gender,
            selected: gender === currentGender
        }));

        this.app.showDialog({
            title: 'Select Gender',
            options,
            buttons: [{ label: 'Cancel', onClick: () => {} }],
            onSelect: (option) => {
                this.genderEdit.textContent = option.value;
                setTimeout(() => this.app.hideDialog(), 200);
            }
        });
    }

    handleBack() {
        if (this.isEditMode) {
            this.app.showDialog({
                title: 'Unsaved Changes',
                message: 'Do you want to save your changes?',
                buttons: [
                    {
                        label: 'Discard',
                        onClick: () => {
                            this.exitEditMode(true);
                            this.app.navigate('settings');
                        }
                    },
                    {
                        label: 'Save',
                        primary: true,
                        onClick: async () => {
                            await this.saveProfile();
                            this.app.navigate('settings');
                        }
                    }
                ]
            });
        } else {
            this.app.navigate('settings');
        }
    }
}
