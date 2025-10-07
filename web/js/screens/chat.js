// Chat Screen Controller
export default class ChatScreen {
    constructor(app) {
        this.app = app;
        this.element = document.getElementById('chat-screen');
        this.backBtn = document.getElementById('chat-back-btn');
        this.nextBtn = document.getElementById('next-btn');
        this.messagesContainer = document.getElementById('messages-container');
        this.messageInput = document.getElementById('message-input');
        this.sendBtn = document.getElementById('send-btn');
        this.strangerName = document.getElementById('stranger-name');
        this.strangerInfo = document.getElementById('stranger-info');
        this.strangerFlag = document.querySelector('.stranger-flag');
        this.loadingOverlay = document.getElementById('loading-overlay');
        this.suggestionsContainer = document.getElementById('suggestion-chips-container');
        
        this.currentStranger = null;
        this.hasSentFirstMessage = false;
        this.suggestionChips = ['Hi!', 'Hey', 'Hello', 'ASL?', "What's up?"];
        
        // Bot data (same as Android app)
        this.bots = [
            { name: 'Aria', gender: 'female', age: 22, country: 'CA', flag: '🇨🇦' },
            { name: 'Leo', gender: 'male', age: 25, country: 'US', flag: '🇺🇸' },
            { name: 'Mia', gender: 'female', age: 21, country: 'GB', flag: '🇬🇧' },
            { name: 'Zoe', gender: 'female', age: 23, country: 'AU', flag: '🇦🇺' },
            { name: 'Kai', gender: 'male', age: 24, country: 'IN', flag: '🇮🇳' }
        ];
        
        this.setupEventListeners();
    }

    setupEventListeners() {
        this.backBtn.addEventListener('click', () => this.handleBack());
        this.nextBtn.addEventListener('click', () => this.connectToNewStranger());
        this.sendBtn.addEventListener('click', () => this.sendMessage());
        this.messageInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                this.sendMessage();
            }
        });
    }

    getElement() {
        return this.element;
    }

    onEnter(data) {
        this.userData = data;
        this.connectToNewStranger();
    }

    onExit() {
        // Clean up
        this.currentStranger = null;
        this.messagesContainer.innerHTML = '';
    }

    connectToNewStranger() {
        // Show loading
        this.loadingOverlay.classList.add('active');
        this.messagesContainer.innerHTML = '';
        this.hasSentFirstMessage = false;
        this.hideSuggestions();
        
        // Simulate connection delay
        setTimeout(() => {
            // Select random bot
            const randomBot = this.bots[Math.floor(Math.random() * this.bots.length)];
            this.currentStranger = randomBot;
            
            // Update header
            this.updateStrangerInfo(randomBot);
            
            // Hide loading
            this.loadingOverlay.classList.remove('active');
            
            // Add system message
            this.addMessage({
                text: `You're now chatting with ${randomBot.name}. Be nice!`,
                type: 'system'
            });
            
            // Show suggestion chips
            this.showSuggestions();
        }, 2000);
    }

    updateStrangerInfo(stranger) {
        this.strangerName.textContent = stranger.name;
        this.strangerInfo.textContent = `${stranger.gender}, ${stranger.age}`;
        this.strangerFlag.textContent = stranger.flag;
    }

    showSuggestions() {
        this.suggestionsContainer.innerHTML = '';
        this.suggestionChips.forEach(text => {
            const chip = document.createElement('div');
            chip.className = 'suggestion-chip';
            chip.textContent = text;
            chip.addEventListener('click', () => {
                this.messageInput.value = text;
                this.sendMessage();
            });
            this.suggestionsContainer.appendChild(chip);
        });
        this.suggestionsContainer.classList.add('active');
    }

    hideSuggestions() {
        this.suggestionsContainer.classList.remove('active');
        this.suggestionsContainer.innerHTML = '';
    }

    sendMessage() {
        const text = this.messageInput.value.trim();
        if (!text || !this.currentStranger) return;
        
        // Add sent message
        this.addMessage({
            text,
            type: 'sent'
        });
        
        // Clear input
        this.messageInput.value = '';
        
        // Hide suggestions after first message
        if (!this.hasSentFirstMessage) {
            this.hideSuggestions();
            this.hasSentFirstMessage = true;
        }
        
        // Simulate bot response
        setTimeout(() => {
            this.addBotResponse(text);
        }, 1000 + Math.random() * 1000);
    }

    addBotResponse(userMessage) {
        if (!this.currentStranger) return;
        
        const response = this.generateBotResponse(userMessage);
        this.addMessage({
            text: response,
            type: 'received'
        });
    }

    generateBotResponse(userMessage) {
        const lowerMessage = userMessage.toLowerCase();
        
        // Contextual responses
        if (lowerMessage.includes('hello') || lowerMessage.includes('hi') || lowerMessage.includes('hey')) {
            const greetings = ['Hello there!', 'Hi! How are you?', 'Hey! Nice to chat with you.'];
            return greetings[Math.floor(Math.random() * greetings.length)];
        }
        
        if (lowerMessage.includes('how are you')) {
            return "I'm doing great! Thanks for asking. How about you?";
        }
        
        if (lowerMessage.includes('asl')) {
            return `I'm ${this.currentStranger.age}, ${this.currentStranger.gender} from ${this.currentStranger.country}!`;
        }
        
        if (lowerMessage.includes('name')) {
            return `My name is ${this.currentStranger.name}!`;
        }
        
        if (lowerMessage.includes('bye')) {
            return 'It was nice chatting with you! Bye!';
        }
        
        if (lowerMessage.includes("what's up") || lowerMessage.includes('wassup')) {
            return 'Just chatting with cool people like you!';
        }
        
        // Generic responses
        const genericResponses = [
            "That's interesting!",
            'Tell me more.',
            "I'm not sure I understand. Can you explain?",
            'Haha, that\'s funny!',
            'What do you think?',
            'Cool!',
            'I see.',
            'Really? That sounds nice!',
            'Interesting perspective!',
            'I agree!'
        ];
        
        return genericResponses[Math.floor(Math.random() * genericResponses.length)];
    }

    addMessage(message) {
        const messageEl = document.createElement('div');
        messageEl.className = `message ${message.type}`;
        messageEl.textContent = message.text;
        
        this.messagesContainer.appendChild(messageEl);
        
        // Scroll to bottom
        this.messagesContainer.scrollTop = this.messagesContainer.scrollHeight;
    }

    handleBack() {
        // Show confirmation dialog
        this.app.showDialog({
            title: 'Leave Chat',
            message: 'Are you sure you want to leave this chat?',
            buttons: [
                {
                    label: 'Cancel',
                    onClick: () => {}
                },
                {
                    label: 'Leave',
                    primary: true,
                    onClick: () => {
                        // Add system message
                        this.addMessage({
                            text: 'You left the chat.',
                            type: 'system'
                        });
                        
                        // Navigate back after a delay
                        setTimeout(() => {
                            this.app.navigate('home');
                        }, 900);
                    }
                }
            ]
        });
    }
}
