document.addEventListener('DOMContentLoaded', () => {
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
    const chatUsername = document.getElementById('chat-username');
    const chatGender = document.getElementById('chat-gender');
    const chatAge = document.getElementById('chat-age');
    const userFlag = document.getElementById('user-flag');

    let user = {};
    let bot = null;

    const bots = [
        { name: 'Alice', gender: 'female', age: 22, country: 'US', messages: ['Hi there!', 'How are you?', 'What are you up to?', 'That\'s cool!', 'I love chatting with new people.'] },
        { name: 'Bob', gender: 'male', age: 25, country: 'GB', messages: ['Hey!', 'Nice to meet you.', 'Tell me something interesting.', 'I enjoy long walks on the beach.', 'lol'] },
        { name: 'Charlie', gender: 'male', age: 28, country: 'CA', messages: ['Greetings!', 'How\'s your day going?', 'I\'m a bot, but I try my best.', 'What\'s your favorite hobby?', 'See you later!'] },
        { name: 'Diana', gender: 'female', age: 21, country: 'AU', messages: ['G\'day mate!', 'What brings you here?', 'I like to surf the web.', 'Haha, you\'re funny.', 'Catch ya later!'] }
    ];

    // --- Home Screen ---
    startChatBtn.addEventListener('click', async () => {
        const username = usernameInput.value.trim();
        const gender = genderSelect.value;

        if (!username || !gender) {
            alert('Please provide a username and select a gender.');
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
            user.country = 'US'; // Default country
        }

        localStorage.setItem('nepchat-user', JSON.stringify(user));
        showScreen('chat');
        startChat();
    });

    // --- Chat Screen ---
    function startChat() {
        chatMessages.innerHTML = '<div class="message system">Connecting...</div>';
        setTimeout(() => {
            bot = bots[Math.floor(Math.random() * bots.length)];
            chatMessages.innerHTML = '';
            displayMessage(`You are now chatting with ${bot.name}.`, 'system');
            updateChatHeader(bot);
        }, 2000);
    }

    function updateChatHeader(person) {
        chatUsername.textContent = person.name;
        chatGender.textContent = `(${person.gender})`;
        chatAge.textContent = person.age ? `, ${person.age}` : '';
        userFlag.src = `https://flagcdn.com/w40/${person.country.toLowerCase()}.png`;
    }

    function displayMessage(text, sender) {
        const messageElement = document.createElement('div');
        messageElement.classList.add('message', sender);
        messageElement.textContent = text;
        chatMessages.appendChild(messageElement);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }

    sendBtn.addEventListener('click', sendMessage);
    messageInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            sendMessage();
        }
    });

    function sendMessage() {
        const messageText = messageInput.value.trim();
        if (messageText && bot) {
            displayMessage(messageText, 'sent');
            messageInput.value = '';
            setTimeout(botResponse, 1000 + Math.random() * 1000);
        }
    }

    function botResponse() {
        if (bot) {
            const response = bot.messages[Math.floor(Math.random() * bot.messages.length)];
            displayMessage(response, 'received');
        }
    }

    nextBtn.addEventListener('click', () => {
        if (bot) {
            displayMessage(`${bot.name} has disconnected.`, 'system');
            bot = null;
            startChat();
        }
    });

    quitBtn.addEventListener('click', () => {
        if (bot) {
            displayMessage(`${bot.name} has disconnected.`, 'system');
        }
        setTimeout(() => {
            bot = null;
            showScreen('home');
        }, 500);
    });


    // --- General ---
    function showScreen(screenName) {
        homeScreen.style.display = screenName === 'home' ? 'flex' : 'none';
        chatScreen.style.display = screenName === 'chat' ? 'flex' : 'none';
    }

    // Check for saved user data
    const savedUser = localStorage.getItem('nepchat-user');
    if (savedUser) {
        user = JSON.parse(savedUser);
        usernameInput.value = user.username;
        genderSelect.value = user.gender;
        ageInput.value = user.age || '';
    }
});