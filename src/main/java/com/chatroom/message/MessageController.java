package com.chatroom.message;
import com.chatroom.room.Room;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/messages")
public class MessageController {
    private final MessageRepository messageRepository;

    @Autowired
    public MessageController(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @PostMapping("/messages")
    public MessageResponseDto post(
            @RequestBody MessageDto dto
    ) {
        var message = toMessage(dto);
        var savedMessage = messageRepository.save(message);
        return toMessageResponseDto(savedMessage);
    }
    private Message toMessage(MessageDto dto){
        var message = new Message();
        message.setUsername(dto.username());
        message.setTextMessage(dto.textMessage());
        var room = new Room();
        room.setId(dto.roomId());
        message.setRoom(room);
        return message;
    }
    private MessageResponseDto toMessageResponseDto(Message message){
        return new MessageResponseDto(message.getUsername(),message.getTextMessage());
    }

    @GetMapping("/messages")
    public List<Message> findAllMessage() {
        return messageRepository.findAll();
    }

    @GetMapping("/messages/{message-id}")
    public Message findMessageById(
            @PathVariable("message-id") Integer id) {
        return messageRepository.findById(id).orElse(new Message());
    }

    @GetMapping("/messages/search/{message-username}")
    public List<Message> findMessageByUsername(
            @PathVariable("message-username") String username) {
        return messageRepository.findAllByUsernameContaining(username);
    }

    @DeleteMapping("/messages/{message-id}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(
            @PathVariable("message-id") Integer id
    ) {
        messageRepository.deleteById(id);
    }
}
