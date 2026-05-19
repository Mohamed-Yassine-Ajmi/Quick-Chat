package chat.app;

import chat.app.repository.jpa.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AppApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppApplication.class, args);
	}

	// Reset all users to offline on startup
	// This prevents stale online status after server restart
	@Bean
	CommandLineRunner resetOnlineStatus(UserRepository userRepository) {
		return args -> {
			userRepository.findAll().forEach(user -> {
				user.setOnline(false);
				userRepository.save(user);
			});
			System.out.println("[STARTUP] All users reset to offline");
		};
	}
}
