package app.todo.taskmanagement.ui.view;

import app.todo.base.ui.component.ViewToolbar;
import app.todo.taskmanagement.domain.Person;
import app.todo.taskmanagement.domain.Task;
import app.todo.taskmanagement.service.PersonService;
import app.todo.taskmanagement.service.TaskService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import org.springframework.data.domain.Pageable;

import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Optional;

import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringPageRequest;

@Route("task-list")
@PageTitle("Tareas")
@Menu(order = 0, icon = "vaadin:clipboard-check", title = "Lista de tareas")
@PermitAll // When security is enabled, allow all authenticated users
public class TaskListView extends Main {

        private static final long serialVersionUID = 1L;

        private final TextField description;
        private final DatePicker dueDate;
        private final Button createBtn;
        private final Grid<Task> taskGrid;
        private final ComboBox<Person> personComboBox;
        private final TaskService taskService;
        private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);
       
        

        public TaskListView(PersonService personService, TaskService taskService, Clock clock) {
                this.taskService = taskService;

                personComboBox = new ComboBox<>("Asignar a");
                personComboBox.setItems(personService.list(Pageable.unpaged())); // trae todas las personas
                personComboBox.setItemLabelGenerator(person -> person.getApellido() + ", " + person.getNombre());
                personComboBox.setPlaceholder("Seleccione una persona");
                personComboBox.setMinWidth("20em");

                description = new TextField();
                description.setPlaceholder("¿Qué tarea quieres agregar?");
                description.setAriaLabel("Task description");
                description.setMaxLength(Task.DESCRIPTION_MAX_LENGTH);
                description.setMinWidth("20em");

                dueDate = new DatePicker();
                dueDate.setPlaceholder("Fecha de vencimiento");
                dueDate.setAriaLabel("Due date");
///////////////////////////////////////////////////////////////////////////////////////////////////
                createBtn = new Button("Crear", event -> createTask());
                createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
////////////////////////////////////////////////////////////////////////////////////////////////////
                var dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                                .withZone(clock.getZone())
                                .withLocale(getLocale());
                taskGrid = new Grid<>();//NOSOTROS CONTROLAMOS LAS COLUMNAS
                taskGrid.setItems(query -> taskService.list(toSpringPageRequest(query)).stream());
              ////////////////////////////////////////////////////////////////////////////
                // taskGrid.addColumn(Task::getDone).setHeader("Done");

                taskGrid.addComponentColumn(task -> {
                        Checkbox checkbox = new Checkbox(task.isDone());
                        checkbox.addValueChangeListener(event -> {
                                task.setDone(event.getValue());
                                taskService.updateTask(task); // Make sure you have this method to persist
                                // changes
                        });
                        return checkbox;
                }).setHeader("Realizado");

//////////////////////////////////////////////////////////////////////////////////////////////////
                taskGrid.addColumn(task -> {
                        Object personObj = task.getPerson();
                        if (personObj instanceof Person person) {
                                return person.getApellido() + ", " + person.getNombre();
                        } else if (personObj instanceof String str) {
                                return str;
                        } else {
                                return "Sin asignar";
                        }
                }).setHeader("Responsable");
                ///////////////////////////////////////////////////////////////////////////////////

                taskGrid.addColumn(Task::getDescription).setHeader("Descripcion");
                taskGrid.addColumn(task -> Optional.ofNullable(task.getDueDate()).map(dateFormatter::format)
                                .orElse("Nunca"))
                                .setHeader("Fecha de Vencimiento");
                taskGrid.addColumn(task -> dateTimeFormatter.format(task.getCreationDate())).setHeader("Fecha de Creación");
                taskGrid.addComponentColumn(task -> {
                        Button deleteButton = new Button(new Icon(VaadinIcon.TRASH), event -> {

                                Dialog dialog = new Dialog();

                                dialog.setHeaderTitle(
                                                String.format("Borrar tarea: \"%s\"?", task.getDescription()));
                                dialog.add("¿Estas seguro de querer borrar esta tarea?");

                                Button deleteDlgButton = new Button("Borrar", (e) -> {
                                        taskService.deleteTask(task.getId());
                                        taskGrid.getDataProvider().refreshAll();
                                        dialog.close();
                                });
                                deleteDlgButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,
                                                ButtonVariant.LUMO_ERROR);
                                deleteDlgButton.getStyle().set("margin-right", "auto");
                                dialog.getFooter().add(deleteDlgButton);

                                Button cancelButton = new Button("Cancelar", (e) -> dialog.close());
                                cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                                dialog.getFooter().add(cancelButton);

                                dialog.open();
                        });
                        deleteButton.addThemeVariants(ButtonVariant.LUMO_ICON);
                        deleteButton.setAriaLabel("Delete");
                        deleteButton.setTooltipText("Close the dialog");
                        // deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
                        return deleteButton;
                }).setHeader("Borrar");

                taskGrid.setSizeFull();

                setSizeFull();
                addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                                LumoUtility.Padding.MEDIUM, LumoUtility.Gap.SMALL);

                add(new ViewToolbar("Lista de Tareas", ViewToolbar.group(personComboBox,description, dueDate, createBtn)));
                add(taskGrid);
        }

        private void createTask() {
                taskService.createTask(personComboBox.getValue(),description.getValue(), dueDate.getValue());
                taskGrid.getDataProvider().refreshAll();
                description.clear();
                dueDate.clear();
                Notification.show("Tarea añadida", 3000, Notification.Position.BOTTOM_END)
                                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        }

       

}
