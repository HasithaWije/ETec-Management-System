package lk.ijse.etecmanagementsystem.util;

import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

public class FieldsValidation {

    public static boolean validateCustomerFields(TextField txtName, TextField txtContact, TextField txtEmail, TextField txtAddress, TextField txtId) {


        final String ID_REGEX = "^\\d+$";
        final String NAME_REGEX = "^[a-zA-Z0-9\\s.\\-&]+$";
        final String CONTACT_REGEX = "^0\\d{9}$";
        final String EMAIL_REGEX = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,}$";
        final String ADDRESS_REGEX = "^[A-Za-z0-9, ./-]{4,}$";


        String idText = (txtId.getText() == null ? "" : txtId.getText().trim());
        String nameText = (txtName.getText() == null ? "" : txtName.getText().trim());
        String contactText = (txtContact.getText() == null ? "" : txtContact.getText().trim());
        String emailText = (txtEmail.getText() == null ? "" : txtEmail.getText().trim());
        String addressText = (txtAddress.getText() == null ? "" : txtAddress.getText().trim());

        // 1. ID Validation
        if (!idText.isEmpty() && !idText.matches(ID_REGEX)) {
            new Alert(Alert.AlertType.ERROR, "Invalid ID: Must be a number (Integer) only.").show();
            txtId.requestFocus();
            return true;
        }


        // 2. Name Validation
        if (nameText.isEmpty() || !nameText.matches(NAME_REGEX)) {
            new Alert(Alert.AlertType.ERROR, "Invalid Name: Must contain at least 3 letters.").show();
            txtName.requestFocus();
            return true;
        }

        // 3. Contact Validation (THIS WAS CAUSING THE ERROR)
        if (contactText.isEmpty() || !contactText.matches(CONTACT_REGEX)) {
            new Alert(Alert.AlertType.ERROR, "Invalid Contact: Must start with 0 and have exactly 10 digits.").show();
            txtContact.requestFocus();
            return true;
        }

        // 4. Email Validation
        if (!emailText.isEmpty() && !emailText.matches(EMAIL_REGEX)) {
            new Alert(Alert.AlertType.ERROR, "Invalid Email: Please enter a valid email address.").show();
            txtEmail.requestFocus();
            return true;
        }

        // 5. Address Validation
        if (!addressText.isEmpty() && !addressText.matches(ADDRESS_REGEX)) {
            new Alert(Alert.AlertType.ERROR, "Invalid Address: Must be at least 4 characters long.").show();
            txtAddress.requestFocus();
            return true;
        }

        return false;
    }

    public static boolean validateUserFields(TextField txtName, TextField txtContact, TextField txtEmail, TextField txtAddress, TextField txtId, TextField txtUserName, TextField txtPassword) {


        final String ID_REGEX = "^\\d+$"; // ID must be numeric
        final String NAME_REGEX = "^[a-zA-Z0-9\\s.\\-&]+$"; // Name can contain letters, numbers, spaces, dots, hyphens, and ampersands
        final String CONTACT_REGEX = "^0\\d{9}$"; // Contact must start with 0 and have exactly 10 digits
        final String EMAIL_REGEX = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,}$"; // Standard email format
        final String ADDRESS_REGEX = "^[A-Za-z0-9, ./-]{4,}$"; // Address must be at least 4 characters
        final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)\\S{6,}$"; // Password must be at least 6 characters, include letters and numbers
        final String USERNAME_REGEX = "^[a-zA-Z0-9_]{4,20}$"; // Username must be 4-20 characters, letters, numbers, underscores


        String idText = (txtId.getText() == null ? "" : txtId.getText().trim());
        String nameText = (txtName.getText() == null ? "" : txtName.getText().trim());
        String contactText = (txtContact.getText() == null ? "" : txtContact.getText().trim());
        String emailText = (txtEmail.getText() == null ? "" : txtEmail.getText().trim());
        String addressText = (txtAddress.getText() == null ? "" : txtAddress.getText().trim());
        String password = (txtPassword.getText() == null ? "" : txtPassword.getText().trim());
        String userName = (txtUserName.getText() == null ? "" : txtUserName.getText().trim());

        // 1. ID Validation
        if (!idText.isEmpty() && !idText.matches(ID_REGEX)) {
            new Alert(Alert.AlertType.ERROR, "Invalid ID: Must be a number (Integer) only.").show();
            txtId.requestFocus();
            return true;
        }


        // 2. Name Validation
        if (nameText.isEmpty() || !nameText.matches(NAME_REGEX)) {
            new Alert(Alert.AlertType.ERROR, "Invalid Name: Must contain at least 3 letters.").show();
            txtName.requestFocus();
            return true;
        }

        // 3. Contact Validation (THIS WAS CAUSING THE ERROR)
        if (contactText.isEmpty() || !contactText.matches(CONTACT_REGEX)) {
            new Alert(Alert.AlertType.ERROR, "Invalid Contact: Must start with 0 and have exactly 10 digits.").show();
            txtContact.requestFocus();
            return true;
        }

        // 4. Email Validation
        if (!emailText.isEmpty() && !emailText.matches(EMAIL_REGEX)) {
            new Alert(Alert.AlertType.ERROR, "Invalid Email: Please enter a valid email address.").show();
            txtEmail.requestFocus();
            return true;
        }

        // 5. Address Validation
        if (!addressText.isEmpty() && !addressText.matches(ADDRESS_REGEX)) {
            new Alert(Alert.AlertType.ERROR, "Invalid Address: Must be at least 4 characters long.").show();
            txtAddress.requestFocus();
            return true;
        }

        if (userName.isEmpty() || !userName.matches(USERNAME_REGEX)) {
            new Alert(Alert.AlertType.ERROR, "Username must be 4-20 characters and can include letters, numbers, and underscores.").show();
            txtUserName.requestFocus();
            return true;
        }


        if (password.isEmpty() || !password.matches(PASSWORD_REGEX)) {
            new Alert(Alert.AlertType.ERROR, "Password must be at least 6 characters and include both letters and numbers.").show();
            txtPassword.requestFocus();
            return true; // Return true to indicate an error found
        }

        return false;
    }

    public static void formatTxtFieldAsNumber(TextField textField, boolean allowDecimal) {
        String regex = allowDecimal ? "\\d*(\\.\\d{0,2})?" : "\\d*";
        textField.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches(regex) ? change : null));
    }

}
