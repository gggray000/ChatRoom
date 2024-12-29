package com.chatroom;

import com.chatroom.app.AppController;
import com.chatroom.bot.ChatBotConfiguration;
import com.chatroom.room.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AppController.class)
public class AppControllerWebMvcTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private RoomService roomService;

	@MockBean
	private UrlService urlService;

	@MockBean
	private QrCodeService qrCodeService;

	@MockBean
	private JwtService jwtService;

	@MockBean
	private ChatBotConfiguration chatBotConfiguration;

	@Test
    public void testRoomCreationWithEmptyName() throws Exception {
		this.mockMvc.perform(post("/admin/create-room")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\": \"\", \"systemPrompt\": \"test prompt\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("Room name is required"));
	}

}