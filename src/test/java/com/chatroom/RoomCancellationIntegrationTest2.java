package com.chatroom;

import com.chatroom.chat.*;
import com.chatroom.room.RoomService;
import com.chatroom.room.TimeService;
import com.chatroom.room.UrlService;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RoomCancellationIntegrationTest2 {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoomService roomService;

    @Autowired
    private TextMessageService textMessageService;

    @Autowired
    private WebSocketMessageService webSocketMessageService;

    @Autowired
    private TimeService timeService;
    @Autowired
    private UrlService urlService;

    @Test
    void whenRoomIsCancelled_thenEverythingIsDeleted() throws Exception {
        String roomName = "Test Room 2";

        MvcResult createResult = mockMvc.perform(post("/admin/create-room")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + roomName + "\", \"timerMinutes\": \"5\"}"))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = createResult.getResponse().getContentAsString();
        String roomId = JsonPath.read(responseJson, "$.roomId");

        assertEquals(1, roomService.getAllRooms().size());
        assertEquals(300, timeService.getTimeForRoom(roomId));
        assertEquals(1, urlService.getRoomUrlTable().size());

        TextMessage msg1 = new TextMessage("a", "Hi!");
        TextMessage msg2 = new TextMessage("b", "Hello!!!");
        textMessageService.saveMessage(roomId, msg1);
        textMessageService.saveMessage(roomId, msg2);

        assertEquals(2, textMessageService.getMessageHistoryMap().get(roomId).size());

        WebSocketMessage wsMsg = WebSocketMessage.builder()
                .messageType(MessageType.CHAT)
                .sender("Ray")
                .content("Msg from Ray")
                .build();

        WebSocketMessage wsMsg2 = WebSocketMessage.builder()
                .messageType(MessageType.CHAT)
                .sender("Ray")
                .content("Ray again")
                .build();

        webSocketMessageService.saveMessage(roomId, wsMsg);
        webSocketMessageService.saveMessage(roomId, wsMsg2);

        assertEquals(2, webSocketMessageService.getMessageHistoryMap().get(roomId).size());

        mockMvc.perform(post("/admin/cancel-room")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roomId\":\"" + roomId + "\"}"))
                .andExpect(status().isOk());

        assertNull(textMessageService.getMessageHistoryMap().get(roomId));
        assertNull(webSocketMessageService.getMessageHistoryMap().get(roomId));
        assertEquals(0, timeService.getGlobalTimeMap().size());
        assertNull(roomService.getRoom(roomId));
    }
}
