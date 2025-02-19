package hexlet.code.component;

import hexlet.code.service.CustomUserDetailsService;
import hexlet.code.model.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private final CustomUserDetailsService userService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        var userData = new User();
        var email = "hexlet@example.com";
        userData.setEmail(email);
        userData.setFirstName("Admin");
        userData.setLastName("Admin");
        userData.setPasswordDigest("qwerty");
        userService.createUser((UserDetails) userData);
    }
}
