# VibeZ Web App

A comprehensive web version of the VibeChat Android app with pixel-perfect UI/UX parity.

## 📁 Project Structure

```
web/
├── index.html              # Main HTML entry point
├── css/
│   ├── theme.css          # Colors, variables, global styles
│   ├── components.css     # Reusable UI components
│   └── screens.css        # Screen-specific styles
├── js/
│   ├── app.js             # Main application controller
│   ├── storage.js         # localStorage management
│   └── screens/
│       ├── splash.js      # Splash screen controller
│       ├── auth.js        # Authentication screen
│       ├── home.js        # Home/user input screen
│       ├── chat.js        # Chat interface
│       ├── settings.js    # Settings screen
│       └── profile.js     # Profile management
└── assets/
    └── images/            # App images and icons
```

## 🎯 Features

### Screens (100% Android Parity)
- ✅ **Splash Screen** - Animated logo on app load
- ✅ **Auth Screen** - Google Sign In & Guest options
- ✅ **Home Screen** - User details input (name, gender, age)
- ✅ **Chat Screen** - Real-time chat with bots
- ✅ **Settings Screen** - Theme selection, profile access
- ✅ **Profile Screen** - Edit user information with FAB

### Core Features
- ✅ Material 3 Design System
- ✅ Dark/Light/System theme support
- ✅ Theme persistence via localStorage
- ✅ Auth mode tracking (Guest vs Google)
- ✅ Dynamic UI based on auth mode
- ✅ Profile management
- ✅ Chat with suggestion chips
- ✅ Bot responses with context
- ✅ Smooth screen transitions
- ✅ Modal dialogs for selections
- ✅ Fully responsive design

## 🎨 Design System

### Colors (Material 3)
- **Primary**: #6750A4 (Purple)
- **Secondary**: #625B71
- **Tertiary**: #7D5260
- **Surface**: Dynamic based on theme
- **Error**: #B3261E

### Typography
- **Font Family**: Poppins (300, 400, 500, 600, 700)
- **Icons**: Material Symbols Outlined

### Component Library
- Buttons (Icon, Primary, Outlined, Filled, Tonal)
- Input Fields (Text, Clickable containers)
- Cards (Profile, Settings)
- Dialogs (Options, Confirmation)
- FAB (Floating Action Button)
- Toolbars (Standard, Chat)
- Suggestion Chips
- Loading Overlays

## 🚀 Getting Started

### Local Development

1. **Simple HTTP Server:**
   ```bash
   cd web
   python -m http.server 8000
   # or
   npx http-server -p 8000
   ```

2. **Open Browser:**
   ```
   http://localhost:8000
   ```

### Deployment

Deploy the `web/` directory to any static hosting service:
- GitHub Pages
- Netlify
- Vercel
- Firebase Hosting
- AWS S3 + CloudFront

## 📱 Responsive Breakpoints

- **Mobile**: < 480px
- **Tablet**: 480px - 768px
- **Desktop**: > 768px
- **Large Desktop**: > 1024px

## 💾 Data Persistence

Uses localStorage with these keys:
- `vibez_theme` - User's theme preference
- `vibez_auth_mode` - Authentication mode (guest/google)
- `vibez_username` - User's name
- `vibez_gender` - User's gender
- `vibez_age` - User's age
- `vibez_country` - User's country

## 🔄 Screen Flow

```
Splash (1.5s)
    ↓
Auth Screen
    ├── Google Sign In → Home Screen
    └── Guest Continue → Home Screen
            ↓
    Home Screen
        ├── Settings → Settings Screen
        │               ├── Profile → Profile Screen
        │               └── Theme Selection
        └── Start Chat → Chat Screen
                            ├── Next (New Stranger)
                            └── Back → Home Screen
```

## 🎭 Auth Mode Behavior

### Guest Mode
- Shows "Anonymous Mode" in settings
- Hides Profile section
- Basic user data only

### Google Mode (Future)
- Hides "Anonymous Mode" text
- Shows Profile section
- Full profile management
- Cloud sync (when implemented)

## 🛠️ Technical Stack

- **HTML5** - Semantic markup
- **CSS3** - Custom properties, Grid, Flexbox
- **JavaScript (ES6+)** - Modules, Classes, Async/Await
- **LocalStorage API** - Data persistence
- **Fetch API** - IP geolocation
- **Material Symbols** - Icon font

## 📝 Code Organization

### Modular Architecture
Each screen has its own controller class:
```javascript
class ScreenController {
    constructor(app) { }
    getElement() { }
    onEnter(data) { }
    onExit() { }
}
```

### Event-Driven Design
- Centralized routing in app.js
- Screen lifecycle methods
- Clean separation of concerns

### State Management
- localStorage for persistence
- Screen-level state
- No external state management needed

## 🎯 Android Parity Checklist

| Feature | Android | Web |
|---------|---------|-----|
| Splash Screen | ✅ | ✅ |
| Auth Screen | ✅ | ✅ |
| Home Screen | ✅ | ✅ |
| Chat Screen | ✅ | ✅ |
| Settings Screen | ✅ | ✅ |
| Profile Screen | ✅ | ✅ |
| Dark Mode | ✅ | ✅ |
| Theme Persistence | ✅ | ✅ |
| Auth Mode Tracking | ✅ | ✅ |
| Material 3 Design | ✅ | ✅ |
| Suggestion Chips | ✅ | ✅ |
| Bot Responses | ✅ | ✅ |
| Profile Edit Mode | ✅ | ✅ |
| FAB | ✅ | ✅ |
| Dialogs | ✅ | ✅ |
| Responsive Design | N/A | ✅ |

## 🔮 Future Enhancements

- [ ] Real-time chat with WebSocket
- [ ] Google Sign In integration
- [ ] PWA support (offline mode)
- [ ] Push notifications
- [ ] WebRTC video chat
- [ ] Chat history
- [ ] User avatars
- [ ] Emoji picker
- [ ] GIF support
- [ ] Voice messages
- [ ] Location sharing
- [ ] Multi-language support
- [ ] Accessibility improvements

## 🐛 Known Issues

None currently. Report issues on GitHub.

## 📄 License

Same as parent project.

## 👨‍💻 Developer

Nikit Hamal

---

**Note**: This web app perfectly mirrors the Android app's UI/UX and functionality. Any updates to the Android app should be reflected here to maintain parity.
