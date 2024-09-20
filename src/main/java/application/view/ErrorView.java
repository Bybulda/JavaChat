package application.view;

import com.vaadin.flow.router.*;
import com.vaadin.flow.component.html.Div;
import jakarta.servlet.http.HttpServletResponse;

@Route("error")
public class ErrorView extends Div implements HasErrorParameter<NotFoundException> {

    @Override
    public int setErrorParameter(BeforeEnterEvent beforeEnterEvent, ErrorParameter<NotFoundException> errorParameter) {
        setText("Ошибка: переданы неверные данные в пути.");
        // Возвращаем код ошибки, например, 404
        return HttpServletResponse.SC_NOT_FOUND;
    }
}

