package com.chatroom.room;

import org.springframework.stereotype.Service;

@Service
public class UrlService {

    public String createUrl(Room room){
        return "/chat/" + room.getRoomId();
    }

}
