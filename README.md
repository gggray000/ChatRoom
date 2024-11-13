# ChatRoom

<h2>TODO</h2>
<h3>Short-term</h3>
1. Add a setup page for admin, the setup from admin-page need to be applied to chat page.<br>

<h3>Long-term</h3>
1. Migrate from JavaScript to React.<br>
2. Integrate OpenAI API.<br>
3. Deploy the application on the cloud.<br>
4. QR-Code and Access URL generation.<br>
5. Database function for storing chat history.<br>
6. Downloadable file and URL generation.<br>
7. Use Spring Security to secure messages based on STOMP destinations and message types.<br>

<h2>Update Log</h2>
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
