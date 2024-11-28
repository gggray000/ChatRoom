package com.chatroom.room;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class RoomController {
    private final RoomRepository roomRepository;

    public RoomController(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @PostMapping("/rooms")
    public RoomDto create(
            @RequestBody RoomDto dto
    ) {
        var room = toRoom(dto);
        roomRepository.save(room);
        return dto;

    }
    private Room toRoom(RoomDto dto){
        return new Room(dto.name());
    }
    private RoomDto toRoomDto(Room room){
        return new RoomDto(room.getName());
    }

    @GetMapping("/rooms")
    public List<RoomDto> findAllRoom() {
        return roomRepository.findAll()
                .stream().map(this::toRoomDto).collect(Collectors.toList());
    }

}

