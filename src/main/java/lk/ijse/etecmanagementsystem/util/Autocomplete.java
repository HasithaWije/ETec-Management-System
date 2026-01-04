package lk.ijse.etecmanagementsystem.util;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;

import java.awt.event.KeyEvent;
import java.util.List;
import java.util.stream.Collectors;

public class Autocomplete {
    public static void setupSearchWithSuggestions(TextField searchField, List<String> entries) { // what is this called - autocomplete?
        ContextMenu suggestionsPopup = new ContextMenu();

        // Listen for text changes
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                suggestionsPopup.hide();
            } else {
                // Filter the list
                List<String> filteredList = entries.stream()
                        .filter(e -> e.toLowerCase().contains(newValue.toLowerCase()))
                        .collect(Collectors.toList());

                if (!filteredList.isEmpty()) {
                    suggestionsPopup.getItems().clear();
                    // Add menu items for each match
                    for (String match : filteredList) {
                        MenuItem item = new MenuItem(match);
                        item.setOnAction(e -> {
                            searchField.setText(match);
                            searchField.positionCaret(match.length());
                            searchField.fireEvent(new ActionEvent());
                            suggestionsPopup.hide();
                        });
                        suggestionsPopup.getItems().add(item);
                    }

                    // Show the popup if not already showing
                    if (!suggestionsPopup.isShowing()) {
                        suggestionsPopup.show(searchField, Side.BOTTOM, 0, 0);
                    }
                } else {
                    suggestionsPopup.hide();
                }
            }
        });
    }
}
