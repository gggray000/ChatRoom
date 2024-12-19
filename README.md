# ChatRoom

A web application for real-time anonymous group discussions with built-in AI summarization and PDF export capabilities, ideal for classroom discussions, brainstorming, and workshops.

<h2>TODO</h2>
<h3>Short-term</h3>
1. Do more tests.<br>
2. Fix bugs (including QR-code) on <code>dev_docker</code> branch.<br>
3. Get a domain -> use CloudFlare as CDN and configure Caddy properly.

<h3>Long-term</h3>
1. JPA implementation.<br>
2. Deploy to the professor's virtual machine.<br>
3. Secure endpoints with Spring Security.<br>
4. Integration test/Module test/Unit test.
5. Check if JWT can work with Spring Security.

<br>**Pending**
1. Migrate frontend from JavaScript to React.<br>
2. Use HTTPS instead of HTTP.<br>
3. Wait for Llama 3.3 new releases, now there's only a 70B version available.

<h2>Update Log</h2>
<h3>v0.9.4</h3>
Implemented a simple Caddy service as reverse proxy.
<h3>v0.9.3</h3>
1. Changed <code>docker-compose.yml</code>code> and used SSH reverse tunneling to allow application on AWS EC2 to access ChatBot on local device.<br>
2. Fixed the bug of typing status indicator.
<h3>v0.9.2</h3>
Added <code>Dockerfile</code> and <code>docker-compose.yml</code>, deployed the application to AWS EC2(with limited features).
<h3>v0.9.1</h3>
Changed <code>roomId</code> generation rule and PDF file naming rule.
<h3>v0.9</h3>
Streamlined connection and verification process for the admin and normal users.
<h3>v0.8.9</h3>
Fixed bug like new users could replace old users at the front end, changed frontend JWT handling logic, rewrote <code>Room</code> class, <code>WebSocketMessage</code> class, <code>ChatController</code>, added <code>User</code> class.
<h3>v0.8.8</h3>
Resolved race condition for ChatBot's <code>SystemMessage</code>.
<h3>v0.8.7</h3>
Resolved race condition for text messages export.
<h3>v0.8.6</h3>
1. Improved JWT service, now admin and users get tokens from the same service but with different flags.<br>
2. Improved design of the admin page, and added the logo to the chat page.
<h3>v0.8.5</h3>
Improved JWT service to fix the bug where users with the same username cause conflicts. Now every user gets his/her own JWT.
<h3>v0.8.4</h3>
1. Fixed the bug where the typing indicator overlaps with a long username.<br>
2. Fixed the bug where a long username isn't displayed correctly.
<h3>v0.8.3</h3>
Refactored <code>ChatBotController.java</code>.
<h3>v0.8.2</h3>
Implemented PDF summary download feature.
<h3>v0.8.1</h3>
Fixed a bug where the <code>alt</code> of QR-Code image get displayed all the time.
<h3>v0.8</h3>
1. Implemented PDF generation service.<br>
2. Added Markdown formatting for summary on the chat page.<br>
3. Fixed a bug where the ChatBot is not built without an admin prompt.<br>
<h3>v0.7.3</h3>
Redesigned chat page for mobile phone.
<h3>v0.7.2</h3>
Added a feature that the admin can provide instructions to ChatBot on the admin page.<br>
<h3>v0.7.1</h3>
Enhanced security by implementing server-side JWT validation, and protecting frontend admin operation.
<h3>v0.7</h3>
Implemented an easy JWT service, now only the admin can generate a summary.
<h3>v0.6.5</h3>
Fixed a bug that leads to duplicated "Generating summary..." messages and summaries.
<h3>v0.6.4</h3>
1. Redesigned the chat page, so that current user's info is displayed at the left bottom.<br>
2. Now the user list enables scrolling.<br>
3. Renamed <code>ChatMessage</code> to <code>WebSocketMessage</code>, and <code>TextChatMessage</code> to <code>TextMessage</code>, which are actually text messages sent by users.<br>
<h3>v0.6.3</h3>
1. Added a feature that allows the admin to press <code>ENTER</code> key to create a chat room.<br>
2. Redesigned the admin page (after room creation).
<h3>v0.6.2</h3>
1. Fixed the bug where ChatBot's name was not displayed.<br>
2. Added a prompt message for the chat page, which will be displayed after a request for a summary is received.
<h3>v0.6.1</h3>
1. Fixed the bug where the summary cannot be displayed at the front end.<br>
2. Refactored ChatController.java.
<h3>v0.6</h3>
Implemented local run Llama3.2 3B in the application, using LangChain4j with Ollama.
<h3>v0.5.2</h3>
1. Added a ChatBot prototype, using LangChain4j, Ollama and Llama3.2 3B.<br>
2. Added a feature that allows user to click the URL generated at the admin page.
<h3>v0.5.1</h3>
Added QR-Code generation feature.
<h3>v0.5</h3>
1. Added URL generation, and redesigned the admin page. <br>
2. Now each URL stands for a unique <code>ChatRoom</code>. Refactored controllers and WebSocket code, added new directory <code>/room</code> to handle this.<br>
3. Fixed the bug where the chat room's name was not displayed on the chat page.<br>
4. Added /.gitignore
<h3>v0.4.3</h3>
Refactored and modularized the <code>main.js</code> (the previous main.js is now <code>main-old.js</code>, which is kept for future reference and emergency).
<h3>v0.4.2</h3>
Fixed a bug where the left messages weren't displayed.
<h3>v0.4.1</h3>
1. Refactored the application again, adding <code>./app</code> for application control, deleted <code>./room</code>.<br>
2. Added admin-page prototype, more features on the way.
<h3>v0.4</h3>
1. Changed <code>index.html</code>, added <code>main2.css</code>, brand new UI design.<br>
2. Added typing status indicator to the user list.
<h3>v0.3</h3>
1. Added a global user list to the chat page.<br>
2. Refactored the project again, based on the domain partition principle.
<h3>v0.2</h3>
Added avatar and username above the input box.<br>
<h3>v0.1</h3>
Refactored the project structure for future development and created a new branch <code>dev</code>.<br>
Further development will be committed to the <code>dev</code> branch now.
<h3>v0.0</h3>
Finished prototype based on this video: <URL>https://www.youtube.com/watch?v=TywlS9iAZCM&list=WL&index=114</URL>.<br>
There's a bug in his video, <code>message.type</code> should be <code>message.messageType</code> in <code>main.js</code>.

<h2>Acknowledgement</h2>
@vsch<br>
<URL>https://github.com/vsch/flexmark-java</URL><br>
@cure53<br>
<URL>https://github.com/cure53/DOMPurify</URL><br>
@markdown-it<br>
<URL>https://github.com/markdown-it/markdown-it</URL><br>
@jwtk<br>
<URL>https://github.com/jwtk/jjwt</URL><br>
@ali-bouali<br>
<URL>https://github.com/ali-bouali/spring-boot-websocket-chat-app</URL><br>
@Dheeraj Malik<br>
<URL>https://stackoverflow.com/questions/75173568/how-to-generate-qr-code-with-some-text-using-java</URL><br>
@E-ICEBLUE<br>
<URL>https://www.e-iceblue.com/Download/barcode-for-java.html</URL><br>
@meta-llama<br>
<URL>https://github.com/meta-llama/llama</URL><br>
@Ollama<br>
<URL>https://github.com/ollama/ollama/blob/main/LICENSE</URL><br>
@langchain4j<br>
<URL>https://github.com/langchain4j/langchain4j?tab=Apache-2.0-1-ov-file</URL><br>
