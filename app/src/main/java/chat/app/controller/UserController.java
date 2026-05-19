package chat.app.controller;

import chat.app.dto.UserDTO;
import chat.app.exception.WrongEmailException;
import chat.app.exception.WrongPasswordException;
import chat.app.model.User;
import chat.app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        try {
            User user =(User) authentication.getPrincipal();
            String email=user.getEmail();
            return ResponseEntity.ok(userService.getUserProfile(email));
        } catch (WrongEmailException e) {
            return ResponseEntity.status(404).body("User not found");
        }
    }

    @PutMapping("/settings")
    public ResponseEntity<?> updateSettings(@RequestBody User updatedUser, Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            return ResponseEntity.ok(userService.updateSettings(user.getEmail(), updatedUser));
        }catch (WrongEmailException e) {
            return ResponseEntity.status(404).body("User not found");
        } catch (WrongPasswordException e) {
            return ResponseEntity.status(400).body("Password too weak");
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers(Authentication authentication) {

            User user = (User) authentication.getPrincipal();
            List<UserDTO> users=userService.getAllUsersExcept(user.getEmail());
            return ResponseEntity.ok(users);

    }



}
