// Profile Screen Controller
import Storage from '../storage.js';

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
        this.loadProfile();
    }

    onExit() {
        if (this.isEditMode) {
            this.exitEditMode();
        }
    }

    loadProfile() {
        const profile = Storage.getUserProfile();
        
        // Update display elements
        this.nameDisplay.textContent = profile.username || 'Not specified';
        this.genderDisplayProfile.textContent = profile.gender || 'Not specified';
        this.ageDisplayProfile.textContent = profile.age || 'Not specified';
        this.countryDisplayProfile.textContent = profile.country || 'Not specified';
        
        // Update edit elements
        this.nameEdit.value = profile.username || '';
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
        
        // Hide display, show edit
        this.nameDisplay.style.display = 'none';
        this.nameEdit.style.display = 'block';
        
        this.genderDisplayProfile.style.display = 'none';
        this.genderEdit.style.display = 'block';
        
        this.ageDisplayProfile.style.display = 'none';
        this.ageEdit.style.display = 'block';
        
        this.countryDisplayProfile.style.display = 'none';
        this.countryEdit.style.display = 'block';
        
        // Change FAB icon
        this.fab.querySelector('.material-symbols-outlined').textContent = 'check';
        
        // Focus name field
        this.nameEdit.focus();
    }

    saveProfile() {
        const name = this.nameEdit.value.trim();
        const age = this.ageEdit.value.trim();
        const country = this.countryEdit.value.trim();
        const gender = this.genderEdit.textContent.trim();
        
        // Validation
        if (!name) {
            this.showError('Name cannot be empty');
            return;
        }
        
        // Save to storage
        Storage.setUserProfile({
            username: name,
            gender: gender === 'Not specified' ? '' : gender,
            age,
            country
        });
        
        // Update display
        this.loadProfile();
        this.exitEditMode();
        
        // Show success message
        this.showToast('Profile updated successfully');
    }

    exitEditMode() {
        this.isEditMode = false;
        
        // Show display, hide edit
        this.nameDisplay.style.display = 'block';
        this.nameEdit.style.display = 'none';
        
        this.genderDisplayProfile.style.display = 'block';
        this.genderEdit.style.display = 'none';
        
        this.ageDisplayProfile.style.display = 'block';
        this.ageEdit.style.display = 'none';
        
        this.countryDisplayProfile.style.display = 'block';
        this.countryEdit.style.display = 'none';
        
        // Change FAB icon back
        this.fab.querySelector('.material-symbols-outlined').textContent = 'edit';
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
            buttons: [
                {
                    label: 'Cancel',
                    onClick: () => {}
                }
            ],
            onSelect: (option) => {
                this.genderEdit.textContent = option.value;
                
                // Auto-close after selection
                setTimeout(() => {
                    this.app.hideDialog();
                }, 200);
            }
        });
    }

    handleBack() {
        if (this.isEditMode) {
            // Show unsaved changes dialog
            this.app.showDialog({
                title: 'Unsaved Changes',
                message: 'Do you want to save your changes?',
                buttons: [
                    {
                        label: 'Discard',
                        onClick: () => {
                            this.loadProfile();
                            this.exitEditMode();
                            this.app.navigate('settings');
                        }
                    },
                    {
                        label: 'Save',
                        primary: true,
                        onClick: () => {
                            this.saveProfile();
                            this.app.navigate('settings');
                        }
                    }
                ]
            });
        } else {
            this.app.navigate('settings');
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

    showToast(message) {
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
