// Chat Screen Controller
import { auth, database } from '../firebase.js';
import { ref, set, get, onValue, runTransaction, remove, push, serverTimestamp, onDisconnect, onChildAdded, onChildChanged } from "https://www.gstatic.com/firebasejs/10.12.2/firebase-database.js";

export default class ChatScreen {
    constructor(app) {
        this.app = app;
        this.element = document.getElementById('chat-screen');

        // Toolbar
        this.backBtn = document.getElementById('chat-back-btn');
        this.nextBtn = document.getElementById('next-btn');
        this.strangerName = document.getElementById('stranger-name');
        this.strangerInfo = document.getElementById('stranger-info');
        this.strangerFlag = document.querySelector('.stranger-flag');

        // Messages
        this.messagesContainer = document.getElementById('messages-container');
        this.messageInput = document.getElementById('message-input');
        this.sendBtn = document.getElementById('send-btn');

        // Overlay
        this.loadingOverlay = document.getElementById('loading-overlay');
        this.cancelMatchmakingBtn = document.getElementById('cancel-matchmaking-btn');

        // Typing Indicator
        this.typingIndicator = this.element.querySelector('.typing-indicator-container');
        this.typingTimeout = null;

        // Reactions
        this.reactionPicker = document.getElementById('reaction-picker');
        this.selectedMessage = null;
        this.pressTimer = null;
        this.lastClickTime = 0;

        // Reply
        this.replyPreviewContainer = document.getElementById('reply-preview-container');
        this.replyPreviewName = document.querySelector('.reply-preview-name');
        this.replyPreviewText = document.querySelector('.reply-preview-text');
        this.cancelReplyBtn = document.getElementById('cancel-reply-btn');
        this.messageToReply = null;
        this.isSwiping = false;
        this.swipeStartX = 0;
        this.swipeCurrentX = 0;

        // Suggestions
        this.suggestionsContainer = document.getElementById('suggestion-chips-container');
        this.suggestionChips = ['Hi!', 'Hey', 'Hello', 'ASL?', "What's up?"];
        this.hasSentFirstMessage = false;

        // State
        this.isMatchmaking = false;
        this.currentUser = null;
        this.myQueueRef = null;
        this.queueListener = null;
        this.chatRoomId = null;
        this.otherUserId = null;
        this.messageMap = {};
        
        this.setupEventListeners();
        this.setupReactionPickerListeners();
    }

    setupEventListeners() {
        this.backBtn.addEventListener('click', () => this.handleBack());
        this.cancelMatchmakingBtn.addEventListener('click', () => this.cancelMatchmaking());
        this.nextBtn.addEventListener('click', () => this.findNewMatch());
        this.sendBtn.addEventListener('click', () => this.sendMessage());
        this.cancelReplyBtn.addEventListener('click', () => this.hideReplyPreview());
        this.messageInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') this.sendMessage();
        });
    }

    getElement() {
        return this.element;
    }

    onEnter() {
        this.currentUser = auth.currentUser;
        if (!this.currentUser) {
            this.app.navigate('auth');
            return;
        }
        this.startMatchmaking();
    }

    onExit() {
        // Cleanup matchmaking listeners
        if (this.isMatchmaking && this.myQueueRef) {
            remove(this.myQueueRef);
        }
        if (this.queueListener) {
            ref(database, 'queue').off('value', this.queueListener);
        }
        this.myQueueRef = null;
        this.queueListener = null;

        // Cleanup chat listeners
        if (this.chatRoomRef) {
            this.chatRoomRef.off(); // Detaches all listeners on this reference
        }
        if (this.typingRef) {
            this.typingRef.off();
            this.typingRef.child(this.currentUser.uid).remove();
        }
        clearTimeout(this.typingTimeout);

        // Reset state
        this.chatRoomId = null;
        this.otherUserId = null;
        this.chatRoomRef = null;
        this.typingRef = null;
        this.isMatchmaking = false;
        this.messageMap = {};
        this.hideReplyPreview();
        this.hasSentFirstMessage = false;
    }

    startMatchmaking() {
        this.isMatchmaking = true;
        this.loadingOverlay.classList.add('active');
        this.messagesContainer.innerHTML = '';
        this.element.querySelector('.chat-toolbar').style.visibility = 'hidden';

        this.myQueueRef = ref(database, 'queue/' + this.currentUser.uid);
        
        onDisconnect(this.myQueueRef).remove();

        set(this.myQueueRef, true).then(() => {
            this.listenForMatch();
            // Start a timeout to avoid getting stuck
            this.matchmakingTimeout = setTimeout(() => {
                if (this.isMatchmaking) {
                    this.app.showToast("No users found. Please try again later.");
                    this.cancelMatchmaking();
                }
            }, 30000); // 30-second timeout
        });
    }

    async cancelMatchmaking() {
        // Guard clause to prevent multiple calls
        if (!this.isMatchmaking) return;

        this.isMatchmaking = false;
        clearTimeout(this.matchmakingTimeout);

        // Detach the listener first to stop processing queue updates
        if (this.queueListener) {
            const queueRef = ref(database, 'queue');
            queueRef.off('value', this.queueListener);
            this.queueListener = null;
        }

        // Remove user from the backend queue
        if (this.myQueueRef) {
            try {
                await remove(this.myQueueRef);
            } catch (error) {
                console.error("Failed to remove user from queue:", error);
                // Even if backend fails, proceed with UI cleanup
            } finally {
                this.myQueueRef = null;
            }
        }

        // Update UI and navigate, ensuring the user is never stuck
        this.loadingOverlay.classList.remove('active');
        this.app.navigate('home');
    }

    listenForMatch() {
        const queueRef = ref(database, 'queue');
        this.queueListener = onValue(queueRef, (snapshot) => {
            if (!this.isMatchmaking) {
                queueRef.off('value', this.queueListener);
                return;
            }

            const queue = snapshot.val();
            if (!queue) return;

            const myQueueEntry = queue[this.currentUser.uid];
            if (myQueueEntry && typeof myQueueEntry === 'object' && myQueueEntry.matchedWith) {
                this.isMatchmaking = false;
                clearTimeout(this.matchmakingTimeout);
                this.chatRoomId = myQueueEntry.chatRoomId;
                this.otherUserId = myQueueEntry.matchedWith;
                this.connectToChat();
                return;
            }

            for (const otherUserId in queue) {
                if (otherUserId !== this.currentUser.uid && queue[otherUserId] === true) {
                    if (this.currentUser.uid < otherUserId) {
                        this.attemptToClaim(otherUserId);
                    }
                    break;
                }
            }
        });
    }

    connectToChat() {
        if (this.queueListener) {
            ref(database, 'queue').off('value', this.queueListener);
            this.queueListener = null;
        }
        if (this.myQueueRef) {
            remove(this.myQueueRef);
        }

        this.loadingOverlay.classList.remove('active');
        this.element.querySelector('.chat-toolbar').style.visibility = 'visible';

        this.fetchAndDisplayStrangerInfo(this.otherUserId);
        this.listenForMessages();
        this.setupTypingIndicator();
    }

    async attemptToClaim(otherUserId) {
        if (!this.isMatchmaking) return;

        const otherUserRef = ref(database, 'queue/' + otherUserId);
        const newChatRoomId = push(ref(database, 'chats')).key;

        try {
            const result = await runTransaction(otherUserRef, (currentData) => {
                if (currentData === true) {
                    return { matchedWith: this.currentUser.uid, chatRoomId: newChatRoomId };
                }
                return; // Abort
            });

            if (result.committed) {
                this.isMatchmaking = false;
                clearTimeout(this.matchmakingTimeout);

                await set(ref(database, `queue/${this.currentUser.uid}`), {
                    matchedWith: otherUserId,
                    chatRoomId: newChatRoomId
                });

                const participantsRef = ref(database, `chats/${newChatRoomId}/participants`);
                await set(participantsRef, {
                    [this.currentUser.uid]: { status: 'active', joined: serverTimestamp() },
                    [otherUserId]: { status: 'active', joined: serverTimestamp() }
                });

                this.chatRoomId = newChatRoomId;
                this.otherUserId = otherUserId;
                this.connectToChat();
            }
        } catch (error) {
            console.error("Claiming transaction failed: ", error);
        }
    }

    async fetchAndDisplayStrangerInfo(userId) {
        const userRef = ref(database, 'users/' + userId);
        const snapshot = await get(userRef);
        if (snapshot.exists()) {
            const stranger = snapshot.val();
            this.strangerName.textContent = stranger.name;
            this.strangerInfo.textContent = `${stranger.gender}, ${stranger.age}`;
            this.strangerFlag.textContent = this.getFlagFromCountry(stranger.country);

            // Add system message and show suggestions
            this.addMessageToUI({
                type: 'system',
                text: `You're now chatting with ${stranger.name}. Be nice!`
            });
            this.showSuggestions();
        }
    }

    getFlagFromCountry(countryCode) {
        const flags = { 'US': '🇺🇸', 'CA': '🇨🇦', 'GB': '🇬🇧', 'AU': '🇦🇺', 'IN': '🇮🇳' };
        return flags[countryCode] || '🏳️';
    }

    setupTypingIndicator() {
        this.typingRef = ref(database, `chats/${this.chatRoomId}/typing`);
        const myTypingRef = ref(database, `chats/${this.chatRoomId}/typing/${this.currentUser.uid}`);
        const otherUserTypingRef = ref(database, `chats/${this.chatRoomId}/typing/${this.otherUserId}`);

        // Listen for other user's typing status
        onValue(otherUserTypingRef, (snapshot) => {
            if (snapshot.exists() && snapshot.val() === true) {
                this.typingIndicator.style.display = 'block';
                this.messagesContainer.scrollTop = this.messagesContainer.scrollHeight;
            } else {
                this.typingIndicator.style.display = 'none';
            }
        });

        // Set my typing status
        this.messageInput.addEventListener('input', () => {
            if (this.typingRef) {
                set(myTypingRef, true);
                clearTimeout(this.typingTimeout);
                this.typingTimeout = setTimeout(() => {
                    set(myTypingRef, false);
                }, 2000); // 2 seconds
            }
        });

        // Ensure my typing status is removed on disconnect
        onDisconnect(myTypingRef).remove();
    }

    listenForMessages() {
        this.chatRoomRef = ref(database, `chats/${this.chatRoomId}`);
        const messagesRef = ref(database, `chats/${this.chatRoomId}/messages`);

        // Clear UI and map for new chat
        const typingIndicator = this.messagesContainer.querySelector('.typing-indicator-container');
        this.messagesContainer.innerHTML = '';
        if(typingIndicator) this.messagesContainer.appendChild(typingIndicator);
        this.messageMap = {};

        onChildAdded(messagesRef, (snapshot) => {
            const message = snapshot.val();
            if (message && message.messageId) {
                this.messageMap[message.messageId] = message;
                this.addMessageToUI(message);
            }
        });

        onChildChanged(messagesRef, (snapshot) => {
            const updatedMessage = snapshot.val();
            if (updatedMessage && updatedMessage.messageId) {
                this.messageMap[updatedMessage.messageId] = updatedMessage;
                // Find the message wrapper in the DOM and update its reactions
                const messageWrapper = this.messagesContainer.querySelector(`[data-message-id="${updatedMessage.messageId}"]`);
                if (messageWrapper) {
                    const oldReactionsContainer = messageWrapper.querySelector('.reactions-container');
                    const newReactionsContainer = this.createReactionsContainer(updatedMessage.reactions);
                    if (oldReactionsContainer) {
                        messageWrapper.replaceChild(newReactionsContainer, oldReactionsContainer);
                    } else {
                        messageWrapper.appendChild(newReactionsContainer);
                    }
                }
            }
        });
    }

    // --- UI RENDERING ---
    addMessageToUI(message) {
        const wrapper = document.createElement('div');
        const messageEl = document.createElement('div');

        const type = message.type === 'system' ? 'system' : (message.senderId === this.currentUser.uid ? 'sent' : 'received');
        
        wrapper.className = `message-wrapper ${type}`;
        wrapper.dataset.messageId = message.messageId;
        
        messageEl.className = `message ${type}`;

        // Render reply quote if it exists
        if (message.replyToMessageId && this.messageMap[message.replyToMessageId]) {
            const originalMessage = this.messageMap[message.replyToMessageId];
            const originalSenderName = originalMessage.senderId === this.currentUser.uid ? "You" : this.strangerName.textContent;

            const replyQuote = document.createElement('div');
            replyQuote.className = 'reply-quote';
            replyQuote.innerHTML = `<p class="reply-quote-name">${originalSenderName}</p><p class="reply-quote-text">${originalMessage.text}</p>`;
            messageEl.appendChild(replyQuote);
        }
        
        const textNode = document.createElement('span');
        textNode.textContent = message.text;
        messageEl.appendChild(textNode);

        if (type !== 'system') {
            wrapper.addEventListener('pointerdown', (e) => this.handleGestureStart(e, message, wrapper));
        }

        const reactionsContainer = this.createReactionsContainer(message.reactions);

        wrapper.appendChild(messageEl);
        wrapper.appendChild(reactionsContainer);
        this.messagesContainer.insertBefore(wrapper, this.typingIndicator);
        this.messagesContainer.scrollTop = this.messagesContainer.scrollHeight;
    }

    // --- GESTURE HANDLING (SWIPE, LONG-PRESS, DOUBLE-TAP) ---
    handleGestureStart(e, message, wrapper) {
        if (this.reactionPicker.classList.contains('show')) {
            this.hideReactionPicker();
            return;
        }
        
        this.swipeStartX = e.clientX;
        this.isSwiping = false;

        const onMove = (moveEvent) => {
            this.swipeCurrentX = moveEvent.clientX;
            const dx = this.swipeCurrentX - this.swipeStartX;

            if (Math.abs(dx) > 10 && !this.pressTimer) { // Start swipe
                this.isSwiping = true;
                clearTimeout(this.pressTimer);
            }

            if (this.isSwiping) {
                const swipeAmount = Math.max(-80, Math.min(0, dx)); // Limit swipe to the left
                wrapper.style.transform = `translateX(${swipeAmount}px)`;
            }
        };

        const onEnd = () => {
            wrapper.removeEventListener('pointermove', onMove);
            wrapper.removeEventListener('pointerup', onEnd);
            wrapper.removeEventListener('pointerleave', onEnd);

            clearTimeout(this.pressTimer);

            if (this.isSwiping) {
                const dx = this.swipeCurrentX - this.swipeStartX;
                if (dx < -60) { // Threshold for reply
                    this.showReplyPreview(message);
                }
            } else { // It was a click/tap
                const currentTime = new Date().getTime();
                if (currentTime - this.lastClickTime < 300) {
                    this.addReaction('❤️', message);
                }
                this.lastClickTime = currentTime;
            }

            // Reset style and state
            wrapper.style.transform = 'translateX(0)';
            this.isSwiping = false;
        };

        this.pressTimer = setTimeout(() => { // Long press for reactions
            this.showReactionPicker(wrapper, message);
            this.pressTimer = null; // Prevent it from being cleared in onEnd
            onEnd();
        }, 500);

        wrapper.addEventListener('pointermove', onMove);
        wrapper.addEventListener('pointerup', onEnd);
        wrapper.addEventListener('pointerleave', onEnd);
    }

    // --- REACTION AND REPLY UI ---
    showReactionPicker(messageWrapper, message) {
        this.selectedMessage = message;
        const rect = messageWrapper.getBoundingClientRect();
        this.reactionPicker.style.top = `${rect.top - 50}px`;
        this.reactionPicker.style.left = `${rect.left + (rect.width / 2) - (this.reactionPicker.offsetWidth / 2)}px`;
        this.reactionPicker.classList.add('show');
    }

    hideReactionPicker() {
        this.reactionPicker.classList.remove('show');
        this.selectedMessage = null;
    }

    showReplyPreview(message) {
        this.messageToReply = message;
        const senderName = message.senderId === this.currentUser.uid ? "You" : this.strangerName.textContent;
        this.replyPreviewName.textContent = `Replying to ${senderName}`;
        this.replyPreviewText.textContent = message.text;
        this.replyPreviewContainer.style.display = 'flex';
        this.messageInput.focus();
    }

    hideReplyPreview() {
        this.messageToReply = null;
        this.replyPreviewContainer.style.display = 'none';
    }

    createReactionsContainer(reactions) {
        const container = document.createElement('div');
        container.className = 'reactions-container';
        if (!reactions) {
            container.style.display = 'none';
            return container;
        }

        const reactionMap = {};
        Object.values(reactions).forEach(emoji => {
            reactionMap[emoji] = (reactionMap[emoji] || 0) + 1;
        });

        Object.entries(reactionMap).forEach(([emoji, count]) => {
            const reactionEl = document.createElement('div');
            reactionEl.className = 'reaction';
            reactionEl.innerHTML = `${emoji} <span class="count">${count > 1 ? count : ''}</span>`;
            container.appendChild(reactionEl);
        });

        return container;
    }

    // --- FIREBASE INTERACTIONS ---
    setupReactionPickerListeners() {
        this.reactionPicker.querySelectorAll('.reaction-emoji').forEach(emojiEl => {
            emojiEl.addEventListener('click', (e) => {
                e.stopPropagation();
                const reaction = e.target.dataset.reaction;
                if (this.selectedMessage) this.addReaction(reaction, this.selectedMessage);
                this.hideReactionPicker();
            });
        });

        document.addEventListener('click', (e) => {
            if (!this.reactionPicker.classList.contains('show')) return;
            if (!e.target.closest('.message-wrapper')) {
                this.hideReactionPicker();
            }
        });
    }

    async addReaction(emoji, message) {
        if (!message || !this.chatRoomRef || !this.currentUser) return;
        const reactionRef = ref(database, `chats/${this.chatRoomId}/messages/${message.messageId}/reactions/${this.currentUser.uid}`);
        const snapshot = await get(reactionRef);
        if (snapshot.exists() && emoji === snapshot.val()) {
            remove(reactionRef);
        } else {
            set(reactionRef, emoji);
        }
    }

    sendMessage() {
        const text = this.messageInput.value.trim();
        if (text && this.chatRoomRef) {
            const messagesRef = ref(database, `chats/${this.chatRoomId}/messages`);
            const newMessageRef = push(messagesRef);

            const messageData = {
                messageId: newMessageRef.key,
                text: text,
                senderId: this.currentUser.uid,
                timestamp: serverTimestamp(),
                type: 'text'
            };

            if (this.messageToReply) {
                messageData.replyToMessageId = this.messageToReply.messageId;
            }

            set(newMessageRef, messageData);

            this.messageInput.value = '';
            this.hideReplyPreview();

            if (!this.hasSentFirstMessage) {
                this.hideSuggestions();
                this.hasSentFirstMessage = true;
            }
        }
    }

    async leaveChat(isFindingNewMatch = false) {
        if (this.chatRoomRef && this.currentUser) {
            // 1. Send "User left" system message
            const messagesRef = ref(database, `chats/${this.chatRoomId}/messages`);
            const newMessageRef = push(messagesRef);
            await set(newMessageRef, {
                messageId: newMessageRef.key,
                text: 'User left the chat',
                senderId: 'system',
                timestamp: serverTimestamp(),
                type: 'system'
            });

            // 2. Update participant status to "left"
            const participantRef = ref(database, `chats/${this.chatRoomId}/participants/${this.currentUser.uid}`);
            await set(participantRef, { status: 'left', left: serverTimestamp() });

            // 3. Check if both participants have left to delete the chat
            this.checkAndDeleteChatRoom();
        }

        // 4. Handle exit behavior
        if (isFindingNewMatch) {
            this.onExit();
            this.onEnter(); // Restart matchmaking
        } else {
            this.app.navigate('home');
        }
    }

    async checkAndDeleteChatRoom() {
        if (!this.chatRoomRef) return;
        const participantsRef = ref(database, `chats/${this.chatRoomId}/participants`);
        const snapshot = await get(participantsRef);
        
        if (snapshot.exists()) {
            const participants = snapshot.val();
            const allLeft = Object.values(participants).every(p => p.status === 'left');
            if (allLeft) {
                remove(this.chatRoomRef);
            }
        }
    }

    findNewMatch() {
        this.leaveChat(true);
    }

    handleBack() {
        if (this.isMatchmaking) {
            this.cancelMatchmaking();
        } else {
            this.app.showDialog({
                title: 'Leave Chat',
                message: 'Are you sure you want to leave this chat?',
                buttons: [
                    { label: 'Cancel' },
                    { label: 'Leave', primary: true, onClick: () => this.leaveChat(false) }
                ]
            });
        }
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
}
