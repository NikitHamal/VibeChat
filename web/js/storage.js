// Storage Manager - Handles localStorage operations
export class Storage {
    static KEYS = {
        THEME: 'vibez_theme',
        AUTH_MODE: 'vibez_auth_mode',
        USERNAME: 'vibez_username',
        GENDER: 'vibez_gender',
        AGE: 'vibez_age',
        COUNTRY: 'vibez_country',
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

    // Remove item from localStorage
    static remove(key) {
        try {
            localStorage.removeItem(key);
            return true;
        } catch (error) {
            console.error('Error removing from localStorage:', error);
            return false;
        }
    }

    // Clear all app data
    static clearAll() {
        try {
            Object.values(Storage.KEYS).forEach(key => {
                localStorage.removeItem(key);
            });
            return true;
        } catch (error) {
            console.error('Error clearing localStorage:', error);
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

    // Auth mode management
    static getAuthMode() {
        return Storage.get(Storage.KEYS.AUTH_MODE, 'guest');
    }

    static setAuthMode(mode) {
        return Storage.set(Storage.KEYS.AUTH_MODE, mode);
    }

    // User profile management
    static getUserProfile() {
        return {
            username: Storage.get(Storage.KEYS.USERNAME, ''),
            gender: Storage.get(Storage.KEYS.GENDER, ''),
            age: Storage.get(Storage.KEYS.AGE, ''),
            country: Storage.get(Storage.KEYS.COUNTRY, ''),
        };
    }

    static setUserProfile(profile) {
        const { username, gender, age, country } = profile;
        Storage.set(Storage.KEYS.USERNAME, username || '');
        Storage.set(Storage.KEYS.GENDER, gender || '');
        Storage.set(Storage.KEYS.AGE, age || '');
        Storage.set(Storage.KEYS.COUNTRY, country || '');
    }

    static updateUserField(field, value) {
        const key = Storage.KEYS[field.toUpperCase()];
        if (key) {
            return Storage.set(key, value);
        }
        return false;
    }
}

export default Storage;
