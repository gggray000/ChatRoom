# ChatRoom

<h2>TODO</h2>
<h3>Short-term</h3>
1. QR-Code generation, and modify the admin page design accordingly.<br>
2. Allow user to click the URL generated in the admin page to go to the chat room directly.<br>
3. Put the user status element back to the top of the text area.

<h3>Long-term</h3>
1. Migrate from JavaScript to React.<br>
2. AI Chatbot.<br>
3. Deploy the application on the cloud.<br>
4. JPA implementation for Room entity.<br>
5. Database function for storing chat history.<br>
6. Downloadable file and URL generation.<br>
7. Use Spring Security to secure messages based on STOMP destinations and message types.<br>

<h2>Update Log</h2>
<h3>v0.5</h3>
1. Added URL generation, redesigned the admin page. <br>
2. Now each URL stands for a unique ChatRoom. Refactored controller and websocket code, added new directory /room to handle this.<br>
3. Fixed the bug where the chat room's name wasn't displayed in the chat page.<br>
4. Added /.gitignore
<h3>v0.4.3</h3>
Refactored and modularized the main.js (the previous main.js is now main-old.js, which is kept for future reference and emergency).
<h3>v0.4.2</h3>
Fixed a bug where the left messages weren't displayed.
<h3>v0.4.2</h3>
Fixed a bug where the left messages weren't displayed.
<h3>v0.4.1</h3>
1. Refactored the application again, adding ./app for application control, deleted ./room.<br>
2. Added admin-page prototype, more features on the way.
<h3>v0.4</h3>
1. Changed index.html, added main2.css, brand new UI design.<br>
2. Added typing status tag in the user list.
<h3>v0.3</h3>
1. Added global user list to the chat page.<br>
2. Refractored the project again, based on domain partition principle.
<h3>v0.2</h3>
Added avatar and username above the input box.<br>
<h3>v0.1</h3>
Refactored the project structure for future development and created a new branch "dev".<br>
Further development will be committed to the "dev" branch now.
<h3>v0.0</h3>
Finished prototype based on this video: <URL>https://www.youtube.com/watch?v=TywlS9iAZCM&list=WL&index=114</URL>.<br>
There's a bug in his video, <code>message.type</code> should be <code>message.messageType</code> in the JS file.
