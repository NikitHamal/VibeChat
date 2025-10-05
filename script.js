document.addEventListener('DOMContentLoaded', () => {
    // --- DOM Elements ---
    const body = document.body;
    const themeSwitcher = document.getElementById('theme-switcher');
    const homeScreen = document.getElementById('home-screen');
    const chatScreen = document.getElementById('chat-screen');
    const startChatBtn = document.getElementById('start-chat-btn');
    const usernameInput = document.getElementById('username');
    const genderSelect = document.getElementById('gender');
    const ageInput = document.getElementById('age');
    const nextBtn = document.getElementById('next-btn');
    const quitBtn = document.getElementById('quit-btn');
    const messageInput = document.getElementById('message-input');
    const sendBtn = document.getElementById('send-btn');
    const chatMessages = document.querySelector('.chat-messages');
    const connectingOverlay = document.getElementById('connecting-overlay');

    // Chat screen specific elements
    const strangerName = document.getElementById('stranger-name');
    const strangerDetails = document.getElementById('stranger-details');
    const strangerFlag = document.getElementById('stranger-flag');

    let user = {};
    let bot = null;

    // --- Bots ---
    const bots = [
        { name: 'Sophia', gender: 'female', age: 24, country: 'US', messages: ['Hey there!', 'How\'s it going?', 'Tell me about your day.', 'That\'s really interesting!', 'I love learning new things.'] },
        { name: 'Liam', gender: 'male', age: 27, country: 'GB', messages: ['Hello!', 'Great to connect with you.', 'What are your hobbies?', 'I\'m a fan of classic movies.', 'Haha, nice one!'] },
        { name: 'Aria', gender: 'female', age: 22, country: 'CA', messages: ['Hi!', 'What a beautiful day!', 'I\'m exploring the world of AI.', 'What music are you into?', 'Talk to you soon!'] },
        { name: 'Mason', gender: 'male', age: 30, country: 'AU', messages: ['G\'day!', 'How are things on your end?', 'I enjoy a good book.', 'That sounds like fun.', 'Cheers!'] }
    ];

    // --- Theme Switcher ---
    function applyTheme(theme) {
        body.classList.remove('light-theme', 'dark-theme');
        body.classList.add(theme);
        themeSwitcher.querySelector('.material-symbols-outlined').textContent = theme === 'dark-theme' ? 'light_mode' : 'dark_mode';
        localStorage.setItem('nepchat-theme', theme);
    }

    themeSwitcher.addEventListener('click', () => {
        const newTheme = body.classList.contains('light-theme') ? 'dark-theme' : 'light-theme';
        applyTheme(newTheme);
    });

    // --- Screen Management ---
    function showScreen(screenName) {
        homeScreen.classList.remove('active');
        chatScreen.classList.remove('active');
        document.getElementById(`${screenName}-screen`).classList.add('active');
    }

    // --- Home Screen Logic ---
    startChatBtn.addEventListener('click', async () => {
        const username = usernameInput.value.trim();
        const gender = genderSelect.value;

        if (!username || !gender) {
            alert('Please enter a username and select a gender.');
            return;
        }

        user = {
            username,
            gender,
            age: ageInput.value.trim() || null
        };

        try {
            const response = await fetch('https://ipapi.co/json/');
            const data = await response.json();
            user.country = data.country_code;
        } catch (error) {
            console.error('Error fetching country:', error);
            user.country = 'XX'; // Default country on error
        }

        localStorage.setItem('nepchat-user', JSON.stringify(user));
        showScreen('chat');
        startChat();
    });

    // --- Chat Logic ---
    function startChat() {
        connectingOverlay.classList.add('active');
        chatMessages.innerHTML = '';

        setTimeout(() => {
            bot = bots[Math.floor(Math.random() * bots.length)];
            updateChatHeader(bot);
            connectingOverlay.classList.remove('active');
            displayMessage(`You're now chatting with ${bot.name}. Be nice!`, 'system');
        }, 2500);
    }

    function updateChatHeader(person) {
        strangerName.textContent = person.name;
        let details = `${person.gender}`;
        if (person.age) {
            details += `, ${person.age}`;
        }
        strangerDetails.textContent = details;
        strangerFlag.src = `https://flagcdn.com/w40/${person.country.toLowerCase()}.png`;
        strangerFlag.alt = `${person.country} Flag`;
    }

    function displayMessage(text, type) {
        const messageElement = document.createElement('div');
        messageElement.classList.add('message', type);
        messageElement.textContent = text;
        chatMessages.appendChild(messageElement);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }

    function sendMessage() {
        const messageText = messageInput.value.trim();
        if (messageText && bot) {
            displayMessage(messageText, 'sent');
            messageInput.value = '';
            setTimeout(botResponse, 1200 + Math.random() * 800);
        }
    }

    function botResponse() {
        if (bot) {
            const response = bot.messages[Math.floor(Math.random() * bot.messages.length)];
            displayMessage(response, 'received');
        }
    }

    sendBtn.addEventListener('click', sendMessage);
    messageInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            sendMessage();
        }
    });

    nextBtn.addEventListener('click', () => {
        if (bot) {
            displayMessage(`${bot.name} has disconnected.`, 'system');
            bot = null;
            startChat();
        }
    });

    quitBtn.addEventListener('click', () => {
        if (bot) {
            displayMessage(`You have disconnected.`, 'system');
        }
        setTimeout(() => {
            bot = null;
            showScreen('home');
        }, 500);
    });

    // --- Initialization ---
    function init() {
        // Load saved theme
        const savedTheme = localStorage.getItem('nepchat-theme') || 'light-theme';
        applyTheme(savedTheme);

        // Load saved user data
        const savedUser = localStorage.getItem('nepchat-user');
        if (savedUser) {
            user = JSON.parse(savedUser);
            usernameInput.value = user.username || '';
            genderSelect.value = user.gender || '';
            ageInput.value = user.age || '';
        }

        showScreen('home');
    }

    init();
});