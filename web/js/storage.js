// Storage Manager - Handles localStorage operations for theme
export class Storage {
    static KEYS = {
        THEME: 'vibez_theme',
        USERNAME: 'vibez_username',
        GENDER: 'vibez_gender',
        AGE: 'vibez_age',
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

    // User profile management
    static getUserProfile() {
        return {
            username: Storage.get(Storage.KEYS.USERNAME, ''),
            gender: Storage.get(Storage.KEYS.GENDER, ''),
            age: Storage.get(Storage.KEYS.AGE, ''),
        };
    }

    static setUserProfile(profile) {
        const { username, gender, age } = profile;
        Storage.set(Storage.KEYS.USERNAME, username || '');
        Storage.set(Storage.KEYS.GENDER, gender || '');
        Storage.set(Storage.KEYS.AGE, age || '');
    }
}

export default Storage;
