package com.chatroom.room;

public class JwtUserDetails {
    private final String username;
    private final String tokenId;
    private final String roomId;
    private final boolean isAdmin;
    private final boolean isHuman;

    public JwtUserDetails(String username, String tokenId, String roomId, boolean isAdmin, boolean isHuman) {
        this.username = username;
        this.tokenId = tokenId;
        this.roomId = roomId;
        this.isAdmin = isAdmin;
        this.isHuman = isHuman;
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

    public boolean isHuman() {
        return isHuman;
    }
}
