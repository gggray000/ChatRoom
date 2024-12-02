# ChatRoom

<h2>TODO</h2>
<h3>Short-term</h3>
1. Figure out why ChatBot sends two summaries.<br>
2. Figure out how to set @SystemMessage for ChatBot from the admin page.<br>
3. Figure out how to implement "Role" for the application, so that the admin is the only one who can generate a summary.<br>
4. Add an "End Discussion" button for the admin.<br>

<h3>Long-term</h3>
1. Migrate from JavaScript to React.<br>
2. JPA implementation for chat history.<br>
3. Downloadable file and URL generation.<br>
4. Deployment with docker container, using the virtual machine provided by the professor.<br>
5. Use Spring Security to secure messages based on STOMP destinations and message types.<br>

<h2>Update Log</h2>
<h4>v0.6.4</h3>
1. Redesigned the chat page, so that current user info is displayed at the left bottom.<br>
2. Now the user list enables scrolling (haven't tested yet).<br>
3. Renamed "ChatMessage" to "WebSocketMessage", "TextChatMessage" to "TextMessage", which are actually text messages sent by users.<br>
<h3>v0.6.3</h3>
1. Added a feature which allows the admin to press ENTER key to create a chat room.<br>
2. Redesigned the admin page (after room creation).
<h3>v0.6.2</h3>
1. Fixed the bug where ChatBot's name was not displayed.<br>
2. Added a prompt message for chat page, which will be displayed after a request for summary was received.
<h3>v0.6.1</h3>
1. Fixed the bug where summary cannot be displayed at the frontend.<br>
2. Refactored ChatController.java.
<h3>v0.6</h3>
Implemented local run Llama3.2 3B in the application, using LangChain4j with with Ollama.
<h3>v0.5.2</h3>
1. Added a ChatBot prototype, using LangChain4j, Ollama and Llama3.2 3B.<br>
2. Added a feature that allows user to click the URL generated at the admin page.
<h3>v0.5.1</h3>
Added QR-Code generation feature.
<h3>v0.5</h3>
1. Added URL generation, and redesigned the admin page. <br>
2. Now each URL stands for a unique ChatRoom. Refactored controller and WebSocket code, added new directory /room to handle this.<br>
3. Fixed the bug where the chat room's name wasn't displayed on the chat page.<br>
4. Added /.gitignore
<h3>v0.4.3</h3>
Refactored and modularized the main.js (the previous main.js is now main-old.js, which is kept for future reference and emergency).
<h3>v0.4.2</h3>
Fixed a bug where the left messages weren't displayed.
<h3>v0.4.1</h3>
1. Refactored the application again, adding ./app for application control, deleted ./room.<br>
2. Added admin-page prototype, more features on the way.
<h3>v0.4</h3>
1. Changed index.html, added main2.css, brand new UI design.<br>
2. Added typing status tag in the user list.
<h3>v0.3</h3>
1. Added a global user list to the chat page.<br>
2. Refractored the project again, based on the domain partition principle.
<h3>v0.2</h3>
Added avatar and username above the input box.<br>
<h3>v0.1</h3>
Refactored the project structure for future development and created a new branch "dev".<br>
Further development will be committed to the "dev" branch now.
<h3>v0.0</h3>
Finished prototype based on this video: <URL>https://www.youtube.com/watch?v=TywlS9iAZCM&list=WL&index=114</URL>.<br>
There's a bug in his video, <code>message.type</code> should be <code>message.messageType</code> in the JS file.

<h2>Acknowledgement</h2>
@ali-bouali<br>
<URL>https://github.com/ali-bouali/spring-boot-websocket-chat-app</URL><br>
@Dheeraj Malik<br>
<URL>https://stackoverflow.com/questions/75173568/how-to-generate-qr-code-with-some-text-using-java</URL><br>
@E-ICEBLUE<br>
<URL>https://www.e-iceblue.com/Download/barcode-for-java.html</URL><br>
@Ollama<br>
<URL>https://github.com/ollama/ollama/blob/main/LICENSE</URL><br>
@langchain4j<br>
<URL>https://github.com/langchain4j/langchain4j?tab=Apache-2.0-1-ov-file</URL><br>
