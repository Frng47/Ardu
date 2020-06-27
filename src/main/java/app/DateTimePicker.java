package app;

import javafx.beans.property.ObjectProperty;
import javafx.scene.control.DatePicker;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimePicker extends DatePicker {
    public static final String defaultFormat="dd-mm-yyyy HH:mm";
    private DateTimeFormatter formatter;
    private ObjectProperty<LocalDateTime> dateTimeValue;
}
