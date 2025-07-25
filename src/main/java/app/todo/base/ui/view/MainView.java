package app.todo.base.ui.view;

import app.todo.base.ui.component.ViewToolbar;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;


/**
 * This view shows up when a user navigates to the root ('/') of the application.
 */


@Route
@PermitAll // When security is enabled, allow all authenticated users
public final class MainView extends Main {

    // TODO Replace with your own main view.

    MainView() {
        addClassName(LumoUtility.Padding.MEDIUM);
        add(new ViewToolbar("Desplegar menú"));
        add(new Div("Seleccione una opción del menú a la izquierda para comenzar."));
        
    }

    /**
     * Navigates to the main view.
     */
    public static void showMainView() {
        UI.getCurrent().navigate(MainView.class);
    }
}
