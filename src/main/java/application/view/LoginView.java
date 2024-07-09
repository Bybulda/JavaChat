package application.view;

import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("registration")
@PageTitle("Registration | Log in")
public class LoginView extends VerticalLayout {
    private final LoginForm loginForm = new LoginForm();

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
        // доступ к бд и сравнение
        getUI().ifPresent(ui -> ui.navigate(String.format("/lobby/%d", 1L)));
    }
}
