package chat.app.controller;

import chat.app.dto.AuthResponse;
import chat.app.exception.UserAlreadyExistsException;
import chat.app.exception.WrongEmailException;
import chat.app.exception.WrongPasswordException;
import chat.app.model.User;
import chat.app.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try{
            AuthResponse response=userService.register(user);
            return ResponseEntity.status(201).body(response); // ✅ 201 = Created
        }catch(WrongPasswordException w){
            return ResponseEntity.status(400).body("Wrong password");
        }catch(UserAlreadyExistsException u){
            return ResponseEntity.status(409).body("User with this email exists already");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        try{
            AuthResponse response=userService.login(user);
            return ResponseEntity.status(200).body(response); // ✅ 201 = Created
        }catch(WrongPasswordException w){
            return ResponseEntity.status(401).body("Wrong password");
        }catch(WrongEmailException u){
            return ResponseEntity.status(404).body("Email not found");
        }    }
}
