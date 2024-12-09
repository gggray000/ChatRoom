package com.chatroom.room;

public class JwtUserDetails {
    private String username;
    private String tokenId;
    private String roomId;
    private final boolean isAdmin;

    public JwtUserDetails(String username, String tokenId, String roomId, boolean isAdmin) {
        this.username = username;
        this.tokenId = tokenId;
        this.roomId = roomId;
        this.isAdmin = isAdmin;
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

    public boolean isAdmin() { return isAdmin; }
}
