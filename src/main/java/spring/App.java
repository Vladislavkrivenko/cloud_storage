package spring;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Hello world!
 *
 */
@SpringBootApplication
@RequiredArgsConstructor
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    // ЦЕ — ТЕСТОВИЙ КОД ДЛЯ ПЕРЕВІРКИ
//    @Bean
//    CommandLineRunner testRunner(RegistrationService registrationService) {
//        return args -> {
//            LoginRequestDto dto = new LoginRequestDto("testUser2", "12345");
//            UserEntity saved = registrationService.saveUserForDB(dto);
//            System.out.println("Saved user id = " + saved.getId() + ", login = " + saved.getLogin());
//        };
//    }

//    @Bean
//    CommandLineRunner test(AuthorizationService authorizationService) {
//        return args -> {
//            System.out.println( authorizationService.findByLogin("testUser"));
//        };
//    }
}
