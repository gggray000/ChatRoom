# ChatRoom

A web application for real-time anonymous group discussions with built-in AI summarization and PDF export capabilities, ideal for classroom discussions, brainstorming, and workshops.

<h2>TODO</h2>
<h3>Short-term</h3>

1. Fix i18n bugs.<br>
2. Write more test cases, learn <code>@SpringBootTest</code>.<br>
3. Configure the domain with Cloudflare again but with Nginx, and keep WebSocket working.<br>

<h3>Long-term</h3>
1. JPA implementation.<br>
2. ChatBot Moderation.<br>

<br>**Pending**<br>
Migrate frontend to <code>React.js</code>.<br>

<h2>Update Log</h2>
<h3>v0.9.21</h3>
Implemented i18n features.<br>
<h3>v0.9.20</h3>
1. Changed implementation for timer feature.<br>
2. Changed local LLM from phi4:14b to gemma3:4b.<br>
<h3>v0.9.19</h3>
Fixed bugs related to the display order of chat history.
<h3>v0.9.18</h3>
Simplified Timer's logic.
<h3>v0.9.17</h3>
1. Refactored <code>TextMessage.java</code>, <code>WebSocketMessage.java</code>, <code>TextMessageService.java</code>
, <code>WebSocketMessageService.java</code>, <code>Room.java</code> and <code>RoomService.java</code>.<br>
2. Fixed bugs with duplicated username.<br>
3. Resolved error messages from <code>openhtmltopdf</code>.
<h3>v0.9.16</h3>
Added landing page, and a new button at access-denied page and username page.<br>
<h3>v0.9.15</h3>
1. Refactored <code>timer.js</code>.<br>
2. Added broadcast message when time's up.<br>
3. Refactored <code>AppController.java</code>, <code>ChatController.java</code>.<br>
4. Refactored <code>websocket-service.js</code>.
<h3>v0.9.14</h3>
1. Added Timer feature.<br>
2. Added Shutdown feature for admin.
   <h3>v0.9.13</h3>
Changed local LLM from llama3.2:3b to phi4:14b.
   <h3>v0.9.12</h3>
1. Improved PDF formatting.<br>
2. Redesigned chat page UI for future features (Timer, Shutdown).
   <h3>v0.9.11</h3>
1. Improved UI design of chat page for mobile device.<br>
2. Added empty username warning.<br>
3. Fixed bugs for SSH Tunnel and static resource access.
   <h3>v0.9.10</h3>
Changed web server from <code>Caddy</code> to <code>Nginx</code>, implemented refresh rate control.<br>
<h3>v0.9.9</h3>
1. User can now refresh or close the tab but retain his/her username, without being disconnected from the room.<br>
2. Now new users or reconnected old users can see the chat room's message history.<br>
3. Added a disconnect button on the chat page, users can exit the chat room or change their username.
<h3>v0.9.8</h3>
Changed the URL generation logic.
<h3>v0.9.7</h3>
Implemented random username generation feature.
<h3>v0.9.6</h3>
1. Added a button on the admin page, which allows the admin to cancel room creation, yet keep all the original input in place.<br>
2. Created <code>access-denied</code> page, redesigned some API endpoints' logic in <code>AppController</code>.<br>
3. Refactored and modified <code>Room</code> and <code>User</code> class.
<h3>v0.9.5</h3>
Fixed the bug that QR Code can't be displayed on EC2 server. Solution: install the font in Docker's Linux system.
<h3>v0.9.4</h3>
Implemented a simple Caddy service as a reverse proxy.
<h3>v0.9.3</h3>
1. Changed <code>docker-compose.yml</code> and used SSH reverse tunneling to allow application on AWS EC2 to access ChatBot on local device.<br>
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
Redesigned chat page for mobile devices.
<h3>v0.7.2</h3>
Added a feature that the admin can provide instructions to ChatBot on the admin page.<br>
<h3>v0.7.1</h3>
Enhanced security by implementing server-side JWT validation, and protecting frontend admin operation.
<h3>v0.7</h3>
Implemented an easy JWT service, now only the admin can generate a summary.
<h3>v0.6.5</h3>
Fixed a bug that leads to duplicated "Generating summary..." messages and summaries.
<h3>v0.6.4</h3>
1. Redesigned the chat page, so that the current user's info is displayed at the left bottom.<br>
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
1. Added a ChatBot prototype, using LangChain4j, Ollama and Llama3.2:3b.<br>
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

@i18next<br>
<URL>https://github.com/i18next/i18next</URL><br>
@Google<br>
<URL>https://ollama.com/library/gemma3:4b</URL><br>
@Microsoft<br>
<URL>https://ollama.com/library/phi4</URL><br>
@nginx<br>
<URL>https://github.com/nginx/nginx</URL><br>
@jamo<br>
<URL>https://github.com/jamo/nick-generator</URL><br>
@vsch<br>
<URL>https://github.com/vsch/flexmark-java</URL><br>
@cure53<br>
<URL>https://github.com/cure53/DOMPurify</URL><br>
@markdown-it<br>
<URL>https://github.com/markdown-it/markdown-it</URL><br>
@jwtk<br>
<URL>https://github.com/jwtk/jjwt</URL><br>
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
@ali-bouali<br>
<URL>https://github.com/ali-bouali/spring-boot-websocket-chat-app</URL><br>