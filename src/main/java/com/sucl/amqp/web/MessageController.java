package com.sucl.amqp.web;

import com.sucl.amqp.server.MessageSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/amqp")
public class MessageController {
    @Autowired
    private MessageSender messageSender;

    @PostMapping(value = "/send")
    public HttpStatus send(@RequestParam("message") String message){
        for(int i=0 ; i<1000;i++){
            messageSender.send(message+i);
        }
        return HttpStatus.OK;
    }

    @PostMapping(value = "/delay")
    public HttpStatus delay(@RequestParam("message") String message){
        messageSender.delaySend(message);
        return HttpStatus.OK;
    }
}
