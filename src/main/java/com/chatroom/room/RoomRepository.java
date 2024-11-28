package com.chatroom.room;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Integer> {
//List<Room> findAllByNameContaining(String name);
}
