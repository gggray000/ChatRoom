package com.chatroom.app;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AppController {

    //need to handle get request for "/" and "/denied"

    @GetMapping("/admin")
    public String adminPage(){
        return "admin-page";
    }

    @GetMapping("/chat")
    public String enterChatRoom(){
        return "forward:/index.html";
    }

    @GetMapping("/denied")
    public String accessDenied(){
        return "access-denied";
    }

}
