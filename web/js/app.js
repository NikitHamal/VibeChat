// Main App Controller
import Storage from './storage.js';
import SplashScreen from './screens/splash.js';
import AuthScreen from './screens/auth.js';
import HomeScreen from './screens/home.js';
import ChatScreen from './screens/chat.js';
import SettingsScreen from './screens/settings.js';
import ProfileScreen from './screens/profile.js';

class App {
    constructor() {
        this.currentScreen = null;
        this.screens = new Map();
        this.init();
    }

    init() {
        // Initialize theme
        this.applyTheme();

        // Register all screens
        this.registerScreen('splash', new SplashScreen(this));
        this.registerScreen('auth', new AuthScreen(this));
        this.registerScreen('home', new HomeScreen(this));
        this.registerScreen('chat', new ChatScreen(this));
        this.registerScreen('settings', new SettingsScreen(this));
        this.registerScreen('profile', new ProfileScreen(this));

        // Start with splash screen
        this.navigate('splash');

        // Auto-navigate after splash
        setTimeout(() => {
            this.navigate('auth');
        }, 1500);
    }

    registerScreen(name, screen) {
        this.screens.set(name, screen);
    }

    navigate(screenName, data = {}) {
        const newScreen = this.screens.get(screenName);
        if (!newScreen) {
            console.error(`Screen "${screenName}" not found`);
            return;
        }

        // Exit current screen
        if (this.currentScreen) {
            const currentScreenElement = this.currentScreen.getElement();
            currentScreenElement.classList.add('exiting');
            setTimeout(() => {
                currentScreenElement.classList.remove('active', 'exiting');
                this.currentScreen.onExit();
            }, 250);
        }

        // Enter new screen
        setTimeout(() => {
            const newScreenElement = newScreen.getElement();
            newScreenElement.classList.add('active');
            newScreen.onEnter(data);
            this.currentScreen = newScreen;
        }, this.currentScreen ? 250 : 0);
    }

    applyTheme(theme = null) {
        if (!theme) {
            theme = Storage.getTheme();
        }

        // Handle system theme
        if (theme === 'system') {
            const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
            theme = prefersDark ? 'dark' : 'light';
        }

        const themeClass = `${theme}-theme`;
        document.body.classList.remove('light-theme', 'dark-theme');
        document.body.classList.add(themeClass);
    }

    setTheme(theme) {
        Storage.setTheme(theme);
        this.applyTheme(theme);
    }

    showDialog(config) {
        const overlay = document.getElementById('dialog-overlay');
        const container = document.getElementById('dialog-container');

        // Create dialog content
        const dialogHTML = `
            <h3>${config.title}</h3>
            ${config.message ? `<p style="margin-bottom: 16px; color: var(--on-surface-variant);">${config.message}</p>` : ''}
            ${config.options ? this.createDialogOptions(config.options) : ''}
            ${config.buttons ? this.createDialogButtons(config.buttons) : ''}
        `;

        container.innerHTML = dialogHTML;

        // Add event listeners
        if (config.options) {
            const optionElements = container.querySelectorAll('.dialog-option');
            optionElements.forEach((option, index) => {
                option.addEventListener('click', () => {
                    optionElements.forEach(el => el.classList.remove('selected'));
                    option.classList.add('selected');
                    if (config.onSelect) {
                        config.onSelect(config.options[index]);
                    }
                });
            });
        }

        if (config.buttons) {
            config.buttons.forEach((button, index) => {
                const btn = container.querySelector(`.dialog-btn-${index}`);
                if (btn && button.onClick) {
                    btn.addEventListener('click', () => {
                        button.onClick();
                        if (button.closeOnClick !== false) {
                            this.hideDialog();
                        }
                    });
                }
            });
        }

        overlay.classList.add('active');

        // Close on overlay click
        overlay.addEventListener('click', (e) => {
            if (e.target === overlay && config.dismissible !== false) {
                this.hideDialog();
            }
        });
    }

    createDialogOptions(options) {
        const selectedIndex = options.findIndex(opt => opt.selected);
        return `
            <div class="dialog-options">
                ${options.map((option, index) => `
                    <div class="dialog-option ${index === selectedIndex ? 'selected' : ''}">
                        <div class="radio-indicator"></div>
                        <span>${option.label}</span>
                    </div>
                `).join('')}
            </div>
        `;
    }

    createDialogButtons(buttons) {
        return `
            <div class="dialog-actions">
                ${buttons.map((button, index) => `
                    <button class="dialog-btn-${index} ${button.primary ? 'dialog-btn-confirm' : 'dialog-btn-cancel'}">
                        ${button.label}
                    </button>
                `).join('')}
            </div>
        `;
    }

    hideDialog() {
        const overlay = document.getElementById('dialog-overlay');
        overlay.classList.remove('active');
        setTimeout(() => {
            document.getElementById('dialog-container').innerHTML = '';
        }, 300);
    }
}

// Initialize app when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.app = new App();
});

export default App;
