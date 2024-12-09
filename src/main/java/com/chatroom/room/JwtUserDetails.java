package com.chatroom.room;

public class JwtUserDetails {
    private String username;
    private String tokenId;
    private String roomId;
    private String role;

    public JwtUserDetails(String username, String tokenId, String roomId, String role) {
        this.username = username;
        this.tokenId = tokenId;
        this.roomId = roomId;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public String getTokenId() {
        return tokenId;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getRole() {
        return role;
    }
}
