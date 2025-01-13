package com.chatroom;

import com.chatroom.bot.ChatBotConfiguration;
import com.chatroom.room.RoomService;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RoomCancellationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoomService roomService;

    @Autowired
    private ChatBotConfiguration chatBotConfiguration;

    @Test
    void whenRoomIsCancelled_thenPromptIsDeletedCorrectly() throws Exception {
        // Setup test data
        String roomName = "Test Room";
        String systemPrompt = "Test System Prompt";
        String PROMPT_TEMPLATE =
                """
                        You are an AI assistant that helps summarize chat discussions, based on the main topic
                        provided by admin.
                        When provided with a chat history, make sure to follow the following guidelines:
                        1. Output the summary with MarkDown format.
                        2. Summary should have four sections: (1) Main Topic, (2) Main Points, (3) Key contributions from Participants, (4) Decision or Follow-ups.
                        3. In the "Main Topic" part, summarize the main topic set by admin in a few words in one line.
                        4. In the "Main Points" part, create a concise summary listing the main points of the discussion.
                        5. In the "Key Contributions from Participants" part, identify and mention key contributions from participants.
                        6. In the "Decision or Follow-ups" part, highlight any decisions or follow-up actions that were commonly agreed upon.
                        7. If there were any unresolved questions or topics, mention them.
                        8. Seperate the four sections with blank new line. Begin each section with a new line.
                        9. Use markdown strong (bold text) for important points.
                        10. Always say "Thank you for your participation!" at the end of summary, add a new blank line before this sentence.
                        
                        Main Topic from Admin:
                        %s
                        """;

        // First create a room
        MvcResult createResult = mockMvc.perform(post("/admin/create-room")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + roomName + "\", \"timerMinutes\": \"5\"}"))
                .andExpect(status().isOk())
                .andReturn();

        // Extract roomId from response
        String responseJson = createResult.getResponse().getContentAsString();
        String roomId = JsonPath.read(responseJson, "$.roomId");

        // Set system prompt
        mockMvc.perform(post("/admin/set-system-prompt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roomId\":\"" + roomId + "\",\"systemPrompt\":\"" + systemPrompt + "\"}"))
                .andExpect(status().isOk());

        // Verify prompt is set
        String promptBeforeCancel = chatBotConfiguration.getPromptForRoom(roomId);
        assertNotNull(promptBeforeCancel);
        assertTrue(promptBeforeCancel.contains(systemPrompt));

        // Cancel the room
        mockMvc.perform(post("/admin/cancel-room")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roomId\":\"" + roomId + "\"}"))
                .andExpect(status().isOk());

        // Verify room and prompt are deleted
        assertNull(roomService.getRoom(roomId));
        String promptAfterCancel = chatBotConfiguration.getPromptForRoom(roomId);
        assertEquals(String.format(PROMPT_TEMPLATE, ""), promptAfterCancel);

        // Verify memory is cleared
        assertNull(chatBotConfiguration.getChatMemoryForRoom(roomId));
    }
}