// Settings Screen Controller
import Storage from '../storage.js';

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
        this.userModeText = document.getElementById('user-mode-text');
        
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
    }

    getElement() {
        return this.element;
    }

    onEnter(data) {
        this.updateThemeDisplay();
        this.updateUIBasedOnAuthMode();
    }

    onExit() {
        // Clean up if needed
    }

    updateThemeDisplay() {
        const currentTheme = Storage.getTheme();
        const themeOption = this.themeOptions.find(opt => opt.value === currentTheme);
        this.themeValue.textContent = themeOption ? themeOption.label : 'System Default';
    }

    updateUIBasedOnAuthMode() {
        const authMode = Storage.getAuthMode();
        
        if (authMode === 'google') {
            // User is logged in with Google
            // Hide "Anonymous Mode" text
            this.userModeText.style.display = 'none';
            // Show Profile section
            this.accountSection.style.display = 'block';
        } else {
            // User is in guest mode
            // Show "Anonymous Mode" text
            this.userModeText.style.display = 'block';
            // Hide Profile section
            this.accountSection.style.display = 'none';
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
            buttons: [
                {
                    label: 'Cancel',
                    onClick: () => {}
                }
            ],
            onSelect: (option) => {
                // Apply theme
                this.app.setTheme(option.value);
                this.updateThemeDisplay();
                
                // Auto-close after selection
                setTimeout(() => {
                    this.app.hideDialog();
                }, 200);
            }
        });
    }

    openProfile() {
        this.app.navigate('profile');
    }

    handleBack() {
        this.app.navigate('home');
    }
}
