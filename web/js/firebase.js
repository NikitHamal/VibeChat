// Import the functions you need from the SDKs you need
import { initializeApp } from "https://www.gstatic.com/firebasejs/10.12.2/firebase-app.js";
import { getAuth } from "https://www.gstatic.com/firebasejs/10.12.2/firebase-auth.js";
import { getDatabase } from "https://www.gstatic.com/firebasejs/10.12.2/firebase-database.js";
import firebaseConfig from './firebase-config.js';

// Initialize Firebase
const app = initializeApp(firebaseConfig);

// Get a reference to the database service and export it for use in other modules
const database = getDatabase(app);
const auth = getAuth(app);

export { database, auth };