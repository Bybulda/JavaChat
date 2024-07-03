package application;

import com.vaadin.flow.component.page.Push;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Push
@SpringBootApplication
public class JavaChatApplication {
    public static void main(String[] args) {
        SpringApplication.run(JavaChatApplication.class);
    }
}
