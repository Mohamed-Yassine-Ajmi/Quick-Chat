package chat.app.service;

import chat.app.dto.AuthResponse;
import chat.app.dto.UserDTO;
import chat.app.model.User;
import chat.app.exception.*;
import chat.app.repository.jpa.UserRepository;
import chat.app.security.JwtUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    public UserService(UserRepository userRepository,JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil =jwtUtil;
    }

    public AuthResponse login(User user) throws WrongEmailException,WrongPasswordException{
        Optional<User> founf =userRepository.findByEmail(user.getEmail());
        if (founf.isEmpty()){
            throw new WrongEmailException();
        }else{
            User found= founf.get();
            if (!founf.get().getPassword().equals(user.getPassword())){
                throw new WrongPasswordException();
            }
            else{
              String token = jwtUtil.generateToken(found);
                return new AuthResponse(token, new UserDTO(found));
            }
        }
    }
    public AuthResponse register(User user) throws UserAlreadyExistsException,WrongPasswordException{

        // 1. Check null/empty fields
        if (user.getEmail() == null || user.getEmail().isEmpty() ||
                user.getPassword() == null || user.getPassword().isEmpty() ||
                user.getDisplayName() == null || user.getDisplayName().isEmpty()) {
            throw new IllegalArgumentException("Fields cannot be empty");
        }

        // 2. Check email availability
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            throw new UserAlreadyExistsException();
        }

        // 3. Check password strength
        if (!isValidPassword(user.getPassword())) {
            throw new WrongPasswordException();
        }

        // 4. Save and return
        user.setRole("ROLE_USER");  // ← add this!
        User savedUser = userRepository.save(user);
        String token=jwtUtil.generateToken(savedUser);
        return new AuthResponse(token,new UserDTO(savedUser));

    }

    public UserDTO getUserProfile(String email) throws WrongEmailException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(WrongEmailException::new);
        return new UserDTO(user);
    }

    public UserDTO updateSettings(String email, User updatedUser) throws WrongEmailException, WrongPasswordException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(WrongEmailException::new);

        if (updatedUser.getDisplayName() != null && !updatedUser.getDisplayName().isBlank())
            user.setDisplayName(updatedUser.getDisplayName());

        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank()) {
            if (!isValidPassword(updatedUser.getPassword())) throw new WrongPasswordException();
            user.setPassword(updatedUser.getPassword());
        }

        return new UserDTO(userRepository.save(user));
    }

    public List<UserDTO> getAllUsersExcept(String email) {
        return userRepository.findAll().stream()
                .filter(u -> !u.getEmail().equals(email))
                .map(UserDTO::new)
                .toList();
    }

    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) return false;

        boolean hasUpperCase = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLowerCase = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);

        return hasUpperCase && hasLowerCase && hasDigit;
    }
}

