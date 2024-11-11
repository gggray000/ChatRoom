# ChatRoom

<h2>TODO</h2>
<h3>Short-term</h3>
0. Add a new branch for modification.
1. Refactor the project structure.
2. Add the current avatar and username to the input box.<br>
3. Add a list of users to the left of the page, updated in real-time.<br>
4. Add a status prompt to the users in the user list when they are typing.<br>
5. Figure out why to use SockJS and STOMP.<br>
6. Add a setup page for admin.<br>

<h3>Long-term</h3>
1. Migrate from JavaScript to React.<br>
2. Integrate OpenAI API.<br>
3. Deploy the application on the cloud.<br>
4. QR-Code and Access URL generation.
5. Database function for storing chat history.
6. Downloadable file and URL generation.
7. Use Spring Security to secure messages based on STOMP destinations and message types.

<h2>Update Log</h2>
<h3>Version 0.0</h3>
Finished prototype based on this video: <URL>https://www.youtube.com/watch?v=TywlS9iAZCM&list=WL&index=114</URL>.<br>
There's a bug in his video, <code>message.type</code> should be <code>message.messageType</code> in the JS file.
