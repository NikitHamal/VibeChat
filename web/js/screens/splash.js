// Splash Screen Controller
export default class SplashScreen {
    constructor(app) {
        this.app = app;
        this.element = document.getElementById('splash-screen');
    }

    getElement() {
        return this.element;
    }

    onEnter(data) {
        // Splash screen is static, no interaction needed
    }

    onExit() {
        // Clean up if needed
    }
}
