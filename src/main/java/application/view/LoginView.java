package application.view;

import application.backend.model.UserInfo;
import application.backend.service.UserRegistrationService;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@Route("registration")
@PageTitle("Registration | Log in")
public class LoginView extends VerticalLayout {
    private final LoginForm loginForm = new LoginForm();
    @Autowired
    private UserRegistrationService userRegistrationService;

    public LoginView() {
        setSizeFull();
        setSpacing(true);
        setMargin(true);
        loginForm.addLoginListener(event -> validateUserInfo(event.getUsername(), event.getPassword()));

        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        loginForm.setForgotPasswordButtonVisible(false);
        add(loginForm);
    }

    private void validateUserInfo(String username, String password){
        var userRegResult = userRegistrationService.registerUser(username, password);
        if (Boolean.FALSE.equals(userRegResult.getLeft())){
            Notification.show("There is a user with the username " + username + " or you entered wrong password. Please try again.", 3000, Notification.Position.BOTTOM_START);
            loginForm.setError(true);
        }
        else{
            UserInfo right = userRegResult.getRight();
            getUI().ifPresent(ui -> ui.navigate(String.format("/lobby/%d/%s", right.getId(), right.getUserName())));
        }

    }
}
