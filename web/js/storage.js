// Storage Manager - Handles localStorage operations for theme
export class Storage {
    static KEYS = {
        THEME: 'vibez_theme',
    };

    // Get item from localStorage
    static get(key, defaultValue = null) {
        try {
            const value = localStorage.getItem(key);
            return value !== null ? JSON.parse(value) : defaultValue;
        } catch (error) {
            console.error('Error reading from localStorage:', error);
            return defaultValue;
        }
    }

    // Set item in localStorage
    static set(key, value) {
        try {
            localStorage.setItem(key, JSON.stringify(value));
            return true;
        } catch (error) {
            console.error('Error writing to localStorage:', error);
            return false;
        }
    }

    // Theme management
    static getTheme() {
        return Storage.get(Storage.KEYS.THEME, 'system');
    }

    static setTheme(theme) {
        return Storage.set(Storage.KEYS.THEME, theme);
    }
}

export default Storage;
