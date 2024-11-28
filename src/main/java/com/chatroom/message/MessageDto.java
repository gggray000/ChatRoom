package com.chatroom.message;

public record MessageDto (
        String username,
        String textMessage,
        Integer roomId){
}
