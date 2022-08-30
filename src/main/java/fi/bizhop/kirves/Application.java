package fi.bizhop.kirves;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class Application {
    static ConfigurableApplicationContext ctx;

    public static void main(String...args) {
        ctx = SpringApplication.run(Application.class, args);
    }

    public static void exit() {
        ctx.close();
    }
}
