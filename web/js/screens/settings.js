// Settings Screen Controller
import Storage from '../storage.js';
import { auth } from '../firebase.js';
import { signOut } from "https://www.gstatic.com/firebasejs/10.12.2/firebase-auth.js";

export default class SettingsScreen {
    constructor(app) {
        this.app = app;
        this.element = document.getElementById('settings-screen');
        this.backBtn = document.getElementById('settings-back-btn');
        this.profileSetting = document.getElementById('profile-setting');
        this.themeSetting = document.getElementById('theme-setting');
        this.themeValue = document.getElementById('theme-value');
        this.accountSection = document.getElementById('account-section');
        this.profileHeaderCard = document.getElementById('profile-header-card');
        this.profileAvatar = this.profileHeaderCard.querySelector('.profile-avatar');
        this.profileInfo = this.profileHeaderCard.querySelector('.profile-info h3');
        this.userModeText = document.getElementById('user-mode-text');
        this.signOutSetting = document.getElementById('sign-out-setting');
        
        this.themeOptions = [
            { label: 'Light', value: 'light' },
            { label: 'Dark', value: 'dark' },
            { label: 'System Default', value: 'system' }
        ];
        
        this.setupEventListeners();
    }

    setupEventListeners() {
        this.backBtn.addEventListener('click', () => this.handleBack());
        this.profileSetting.addEventListener('click', () => this.openProfile());
        this.themeSetting.addEventListener('click', () => this.showThemeDialog());
        this.signOutSetting.addEventListener('click', () => this.handleSignOut());
    }

    getElement() {
        return this.element;
    }

    onEnter(data) {
        this.updateThemeDisplay();
        this.updateUIForUser();
    }

    onExit() {
        // Clean up if needed
    }

    updateThemeDisplay() {
        const currentTheme = Storage.getTheme();
        const themeOption = this.themeOptions.find(opt => opt.value === currentTheme);
        this.themeValue.textContent = themeOption ? themeOption.label : 'System Default';
    }

    updateUIForUser() {
        const user = auth.currentUser;
        if (!user) {
            this.app.navigate('auth');
            return;
        }

        if (user.isAnonymous) {
            this.profileAvatar.innerHTML = `<span class="material-symbols-outlined">person</span>`;
            this.profileInfo.textContent = 'Guest User';
            this.userModeText.textContent = 'Anonymous Mode';
            this.userModeText.style.display = 'block';
            this.accountSection.style.display = 'none';
        } else {
            this.profileAvatar.innerHTML = `<img src="${user.photoURL}" alt="User Avatar">`;
            this.profileInfo.textContent = user.displayName;
            this.userModeText.style.display = 'none';
            this.accountSection.style.display = 'block';
        }
    }

    showThemeDialog() {
        const currentTheme = Storage.getTheme();
        const options = this.themeOptions.map(opt => ({
            ...opt,
            selected: opt.value === currentTheme
        }));

        this.app.showDialog({
            title: 'Choose theme',
            options,
            buttons: [{ label: 'Cancel', onClick: () => {} }],
            onSelect: (option) => {
                this.app.setTheme(option.value);
                this.updateThemeDisplay();
                setTimeout(() => this.app.hideDialog(), 200);
            }
        });
    }

    handleSignOut() {
        signOut(auth).catch(error => {
            console.error("Sign Out Error", error);
            this.app.showToast(`Error: ${error.message}`);
        });
        // onAuthStateChanged in app.js will handle navigation
    }

    openProfile() {
        this.app.navigate('profile');
    }

    handleBack() {
        this.app.navigate('home');
    }
}
