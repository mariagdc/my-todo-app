package app.todo.taskmanagement.ui.view;

import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringPageRequest;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import app.todo.taskmanagement.domain.Person;
import app.todo.taskmanagement.service.PersonService;
import jakarta.annotation.security.PermitAll;

@Route("person-list")
@PageTitle("Person List")
@Menu(order = 0, icon = "vaadin:clipboard-check", title = "Lista de estudiantes")
@PermitAll // When security is enabled, allow all authenticated users
public class PersonListView extends Main {

    private Binder<Person> binder = new BeanValidationBinder<>(Person.class);
    private TextField lastname = new TextField("Apellido");
    private TextField firstname = new TextField("Nombre");
    private TextField dni = new TextField("DNI");
    private Button edit = new Button("Nuevo");
    private Button save = new Button("Guardar");
    private Grid<Person> personGrid;
    private PersonService personService;

    /**@entity PersonListView
     * @param personService
     */
    public PersonListView(PersonService personService) {
        this.personService = personService;

        addClassName("contact-form");
        binder.bindInstanceFields(this);

        H1 title = new H1("Registo de estudiantes");
        add(title);
        VerticalLayout content = new VerticalLayout();
//con el Boton nuevo tiene que quedar el read only false
        lastname.setReadOnly(true);
        firstname.setReadOnly(true);
        dni.setReadOnly(true);

        edit.addClickListener(event -> {
            lastname.setReadOnly(false);
            firstname.setReadOnly(false);
            dni.setReadOnly(false);
            lastname.clear();
            firstname.clear();
            dni.clear();
        });


        HorizontalLayout buttons = new HorizontalLayout();
        edit.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttons.add(edit, save);

        content.add(lastname, firstname, dni, buttons);

        personGrid = new Grid<>();
        personGrid.setItems(query -> personService.list(toSpringPageRequest(query)).stream());
        personGrid.addColumn(Person::getId).setHeader("ID");
        personGrid.addColumn(Person::getApellido).setHeader("Apellido");
        personGrid.addColumn(Person::getNombre).setHeader("Nombre");
        personGrid.addColumn(Person::getDni).setHeader("DNI");

        personGrid.addComponentColumn(person -> {
            Button delete = new Button("Eliminar");
            delete.addClickListener(event -> {
                personService.deletePerson(person.getId());
                personGrid.getDataProvider().refreshAll();
                Notification.show("Persona eliminada"); // Actualiza la grilla después de eliminar AHORA SE VE TODO FUNCIONA NO TOCAR
            });
            return delete;
        }).setHeader("Acciones");

        content.add(personGrid);
        add(content);
//EVENTO Despues de guardar mensaje de vaadin dni must not be blank???????????????????????????????
       save.addClickListener(event -> {
    try {
        String apellido = lastname.getValue();
        String nombre = firstname.getValue();
        String documento = dni.getValue();

        if (apellido.isEmpty() || nombre.isEmpty() || documento.isEmpty()) {
            Notification.show("Todos los campos son obligatorios");
            return;
        }

        personService.createPerson(apellido, nombre, documento);
        personGrid.getDataProvider().refreshAll(); // Actualiza la grilla
        Notification.show("Persona guardada con éxito");

        // Limpia el formulario
        lastname.clear();
        firstname.clear();
        dni.clear();

    } catch (Exception e) {
        Notification.show("Error al guardar la persona: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
    }
});


    }
}
