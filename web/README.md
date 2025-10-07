# VibeZ Web App

A comprehensive, real-time web version of the VibeChat Android app, built with Firebase. This application is fully synced with the Android app, providing a seamless cross-platform experience.

## 📁 Project Structure

```
web/
├── index.html              # Main HTML entry point
├── css/
│   ├── theme.css           # Colors, variables, global styles
│   ├── components.css      # Reusable UI components
│   └── screens.css         # Screen-specific styles
├── js/
│   ├── app.js              # Main application controller
│   ├── firebase.js         # Firebase initialization
│   ├── firebase-config.js  # Firebase credentials (in .gitignore)
│   ├── storage.js          # Theme persistence (localStorage)
│   └── screens/
│       ├── splash.js       # Splash screen controller
│       ├── auth.js         # Authentication screen (Firebase)
│       ├── home.js         # Home/user input screen
│       ├── chat.js         # Real-time chat interface
│       ├── settings.js     # Settings screen
│       └── profile.js      # Profile management
└── assets/
    └── images/             # App images and icons
```

## 🎯 Features

### Screens (100% Android Parity)
- ✅ **Splash Screen** - Animated logo on app load
- ✅ **Auth Screen** - Firebase Google Sign In & Anonymous options
- ✅ **Home Screen** - User details input (name, gender, age)
- ✅ **Chat Screen** - Real-time chat with strangers
- ✅ **Settings Screen** - Theme selection, profile access, sign out
- ✅ **Profile Screen** - Edit user information with FAB

### Core Features
- ✅ **Real-time Matchmaking** via Firebase Realtime Database
- ✅ **Real-time Chat** with live message updates
- ✅ **Typing Indicator**
- ✅ **Message Reactions** (Long-press & Double-tap)
- ✅ **Swipe-to-Reply** functionality
- ✅ **Firebase Integration** (Auth & Realtime Database)
- ✅ **Cross-Platform Sync** with Android app
- ✅ Material 3 Design System
- ✅ Dark/Light/System theme support
- ✅ Theme persistence via localStorage
- ✅ Fully responsive design

## 🎨 Design System

### Colors (Material 3)
- **Primary**: #6750A4 (Purple)
- **Secondary**: #625B71
- **Tertiary**: #7D5260

### Typography
- **Font Family**: Poppins
- **Icons**: Material Symbols Outlined

## 🚀 Getting Started

### Firebase Setup
1.  Create a `web/js/firebase-config.js` file.
2.  Copy the contents of `firebase-config.example.js` into it.
3.  Populate the file with your actual Firebase project configuration. This file is git-ignored for security.

### Local Development
1.  **Simple HTTP Server:**
   ```bash
   cd web
   python -m http.server 8000
   # or
   npx http-server -p 8000
   ```
2.  **Open Browser:** `http://localhost:8000`

### Deployment (Automated with GitHub Actions)

This project is configured for automated deployment to GitHub Pages whenever changes are pushed to the `main` branch. The workflow is defined in `.github/workflows/deploy-web.yml`.

**Security:** The deployment process securely handles your Firebase API keys by using GitHub Repository Secrets. The workflow reads these secrets and injects them into the `firebase-config.js` file at build time. This means your keys are never exposed in the repository.

**Setup Instructions:**

To enable automated deployments, you must add the following secrets to your GitHub repository settings (`Settings > Secrets and variables > Actions > New repository secret`):

- `FIREBASE_API_KEY`: Your Firebase project's API Key.
- `FIREBASE_AUTH_DOMAIN`: Your Firebase project's auth domain (e.g., `your-project-id.firebaseapp.com`).
- `FIREBASE_DATABASE_URL`: Your Firebase Realtime Database URL (e.g., `https://your-project-id-default-rtdb.firebaseio.com`).
- `FIREBASE_PROJECT_ID`: Your Firebase project's ID.
- `FIREBASE_STORAGE_BUCKET`: Your Firebase project's storage bucket (e.g., `your-project-id.appspot.com`).
- `FIREBASE_MESSAGING_SENDER_ID`: Your Firebase project's messaging sender ID.
- `FIREBASE_APP_ID`: Your Firebase web app's ID.

Once these secrets are configured, any push to the `main` branch will automatically update the live site on GitHub Pages.

## 💾 Data Persistence
- **User Data & Chat:** Firebase Realtime Database
- **Theme Preference:** `localStorage` (`vibez_theme`)

## 🛠️ Technical Stack
- **HTML5** - Semantic markup
- **CSS3** - Custom properties, Grid, Flexbox
- **JavaScript (ES6+)** - Modules, Classes, Async/Await
- **Firebase** - Authentication and Realtime Database

## 🎯 Android Parity Checklist

| Feature | Android | Web |
|---|---|---|
| Splash Screen | ✅ | ✅ |
| Auth Screen (Google/Anon) | ✅ | ✅ |
| Home Screen | ✅ | ✅ |
| Settings & Profile | ✅ | ✅ |
| **Real-time Matchmaking** | ✅ | ✅ |
| **Real-time Chat** | ✅ | ✅ |
| **Typing Indicator** | ✅ | ✅ |
| **Message Reactions** | ✅ | ✅ |
| **Swipe-to-Reply** | ✅ | ✅ |
| Dark Mode & Theming | ✅ | ✅ |
| Material 3 Design | ✅ | ✅ |
| Responsive Design | N/A | ✅ |

## 🔮 Future Enhancements
- [ ] PWA support (offline mode)
- [ ] Push notifications for new messages
- [ ] WebRTC video chat
- [ ] GIF and image sharing
- [ ] Voice messages
- [ ] Multi-language support
- [ ] Enhanced accessibility

## 🐛 Known Issues
None currently. Report issues on GitHub.

## 📄 License
Same as parent project.

## 👨‍💻 Developer
Nikit Hamal
---
**Note**: This web app now perfectly mirrors the Android app's UI/UX and functionality. Any updates to the Android app should be reflected here to maintain parity.