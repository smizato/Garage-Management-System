package bookings;

import common.*;

import java.net.URL;
import java.io.*;
import java.util.*;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.collections.*;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.scene.paint.Color;
import java.sql.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.text.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.scene.control.Alert.AlertType;
import javafx.beans.value.ObservableValue;

public class BookingAppController implements Initializable {
    //Variables for database connection
    public Database conn = new Database();
    public Connection c; 

    //Stage for popup window
    public Stage dialog;

    //Observable list contains vehicle objects for table view
    private ObservableList<Booking> bookings;

    //FXML Variables for Bookings
    @FXML private TableView<Booking> bookingsTable;
    @FXML private TableColumn<Booking,String> bNum, bType, regNum, mec, firstN, lastN, date, time, lastSD, cost, mileage;
    //@FXML private Label test, title;
    @FXML private Button addBooking, editBooking, deleteBooking;

    //Booking details
    @FXML private Label fullName, custType, address, postcode, phone, email;

    //Variables to manage labels and controls of popup window
    @FXML final private ArrayList<Label> labels = new ArrayList<>();
    @FXML final private ArrayList<Control> controls = new ArrayList<>();
    ChoiceBox<String> bookingTypes;
    ChoiceBox<String> times;
    ChoiceBox<String> temps;
    ChoiceBox<String> customers;
    DatePicker datePicker;
    DatePicker lastServicedDatePicker;
    TextField bookingNumberField; 
    TextField regNumberField;
    TextField mechanicField;
    TextField firstNameField;
    TextField lastNameField;
    TextField mileageField;
    TextField costField;

    //QUERIES TEXTFIELD
    @FXML private TextField searchField;

    //Buttons
    Button add;
    Button cancel;

   @Override
    public void initialize(URL url, ResourceBundle rb) {
        //Check connection with database
        if (conn.isDbConnected()) {
            System.out.println("Connected");
            //test.setText("Connected to database");

            try{
                c = Database.connector(); 
            }
            catch(Exception e){
                System.out.println(e);
            }

            //Set columns of TableView
            bNum.setCellValueFactory(b -> b.getValue().getBookingNumProperty());
            bType.setCellValueFactory(b -> b.getValue().getBookingTypeProperty());
            regNum.setCellValueFactory(b -> b.getValue().getRegistrationNumberProperty());
            mec.setCellValueFactory(b -> b.getValue().getMechanicProperty());
            firstN.setCellValueFactory(b -> b.getValue().getFirstNameProperty());
            lastN.setCellValueFactory(b -> b.getValue().getLastNameProperty());
            date.setCellValueFactory(b -> b.getValue().getDateProperty());
            time.setCellValueFactory(b -> b.getValue().getTimeProperty());
            lastSD.setCellValueFactory(b -> b.getValue().getLastServicedDateProperty());
            cost.setCellValueFactory(b -> b.getValue().getDateProperty());
            mileage.setCellValueFactory(b -> b.getValue().getTimeProperty());

            //Disable edit and delete buttons
            editBooking.setDisable(true);
            deleteBooking.setDisable(true);

            //Fill Table View
            try{
                //Retrieve data from database
                 buildData("");
            }
            catch(SQLException e){
                System.out.println("ERROR");
            }
            //Fill table view with data from database

            //Listener to set edit and delete buttons disabled or active
            bookingsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if(newSelection != null) {
                    //Active buttons
                    editBooking.setDisable(false);
                    deleteBooking.setDisable(false);

                    //Fill Booking info Details on bottom tab
                    try{
                        buildBooking(newSelection);
                    }
                    catch(SQLException e){
                        System.out.println(e);
                    }
                }
                else{
                    editBooking.setDisable(true);
                    deleteBooking.setDisable(true);
                }
            });

            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                try{
                    buildData(newValue);
                }
                catch(SQLException e){
                    System.out.println(e);
                }
            });
        }
        else {
            //test.setText("Not Connected to database");
        }

    }

    //Users add new booking info and push it to database and database gets updated afterwards.
    @FXML
    private void addBooking() throws IOException{
        //Create the pop-up form to add a new vehicle
        try{
            createPopup("Add Booking","Add");
        }
        catch(SQLException e){
            System.out.println(e);
        }

        //Add button
        add.setOnAction((ActionEvent e) -> {
            PreparedStatement s = null;
            if (fieldsCheck()) {
                try {
                    String bookingNum1 = bookingNumberField.getText();
                    String regNumber1 = regNumberField.getText();
                    String bookingType1 = bookingTypes.getValue().toString();
                    String mechanic1 = mechanicField.getText();
                    String firstName1 = firstNameField.getText();
                    String lastName1 = lastNameField.getText();
                    String times1 = times.getValue().toString();
                    String mileage1 = mileageField.getText();
                    String cost1 = costField.getText();
                    String date1 = datePicker.getValue().toString();
                    String lastServicedDate1 = lastServicedDatePicker.getValue().toString();

                    s = c.prepareStatement("INSERT INTO bookings (booking_id,booking_type,vehicle_reg,firstname,lastname,date,time,current_mileage,last_service_date,mechanics,total_cost) VALUES (?,?,?,?,?,?,?,?,?,?,?);");
                    s.setString(1, bookingNum1);
                    s.setString(2, bookingType1);
                    s.setString(3, regNumber1);
                    s.setString(4, firstName1);
                    s.setString(5, lastName1);
                    s.setString(6, date1);
                    s.setString(7, times1);
                    s.setString(8, mileage1);
                    s.setString(9, lastServicedDate1);
                    s.setString(10, mechanic1);
                    s.setString(11, cost1);
                    s.executeUpdate();
                }catch(SQLException err) {
                    System.out.println(err);
                } finally {
                    if(s != null) {
                        try {
                            s.close();
                        } catch (SQLException ex) {
                            System.out.println(ex);
                        }
                    }
                }
                dialog.close();
                bookingsTable.refresh();
                try{
                    buildData("");
                }
                catch(SQLException exe){
                    System.out.println(exe);
                }
            }
        });
    }

    //Users select a row and click edit to change booking details
    @FXML
    private void editBooking(ActionEvent event) throws IOException {
        Booking b = bookingsTable.getSelectionModel().getSelectedItem();
        try{
            createPopup("Edit Booking","Edit");
        }
        catch(SQLException e){
            System.out.println(e);
        }
        
        LocalDate l1 = LocalDate.parse(b.getDate(),DateTimeFormatter.ofPattern("yyyy-MM-dd")); //date settings for future booking
        LocalDate l2 = LocalDate.parse(b.getLastServicedDate(),DateTimeFormatter.ofPattern("yyyy-MM-dd")); //date settings for last serviced date
        
        bookingNumberField.setText(b.getBookingNum());
        bookingTypes.setValue(b.getBookingType());
        regNumberField.setText(b.getRegistrationNumber());
        regNumberField.setDisable(true); //To avoid collisions in database
        mechanicField.setText(b.getMechanic());
        firstNameField.setText(b.getLastName());
        lastNameField.setText(b.getFirstName());
        datePicker.setValue(l1);
        times.setValue(b.getTime());
        costField.setText(b.getCost());
        mileageField.setText(b.getMileage()); 
        lastServicedDatePicker.setValue(l1);
        
        //UPDATE ROW ON DATABASE
        add.setOnAction((ActionEvent e) -> {
            PreparedStatement s = null;
            if (fieldsCheck()) {
                try {
                    String bookingNum1 = bookingNumberField.getText();
                    String regNumber1 = regNumberField.getText();
                    String bookingType1 = bookingTypes.getValue();
                    String mechanic1 = mechanicField.getText();
                    String firstName1 = firstNameField.getText();
                    String lastName1 = lastNameField.getText();
                    String times1 = times.getValue();
                    String mileage1 = mileageField.getText();
                    String cost1 = costField.getText();
                    String date1 = datePicker.getValue().toString();
                    String lastSavedDate1 = lastServicedDatePicker.getValue().toString();
                    
                    //Update row in database (users can't change registration number because is primary key
                    s = c.prepareStatement("UPDATE booking SET booking_id=? , booking_type=?, mechanics=?, firstname=?, lastname=?, date=?, time=?, last_service_date=?, total_cost=?, current_mileage=? WHERE vehicle_reg=?;");

                    s.setString(1, bookingNum1);
                    s.setString(2, regNumber1);
                    s.setString(3, bookingType1);
                    s.setString(4, mechanic1);
                    s.setString(5, firstName1);
                    s.setString(6, lastName1);
                    s.setString(7, times1);
                    s.setString(8, mileage1);
                    s.setString(9, cost1);
                    s.setString(10, date1);
                    s.setString(11, lastSavedDate1);
                    s.executeUpdate();
                }catch(SQLException err) {
                    System.out.println(err);
                } finally {
                    if(s != null) {
                        try {
                            s.close();
                        } catch (SQLException ex) {
                            System.out.println(ex);
                        }
                    }
                }
                dialog.close();
                bookingsTable.refresh();
                try{
                    buildData("");
                }
                catch(SQLException exe){
                    System.out.println(exe);
                }
            }
        });

    }

    @FXML
    private void deleteBooking(ActionEvent event) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Delete Confirmation");
        alert.setHeaderText("Delete Confirmation");
        alert.setContentText("Are you sure you want to delete this booking?");

        Optional<ButtonType> result = alert.showAndWait();
        if(result.get() == ButtonType.OK){
            Booking selectedItem = bookingsTable.getSelectionModel().getSelectedItem();
            bookingsTable.getItems().remove(selectedItem);

            //Remove from database
            PreparedStatement s = null;

            try{
                //Update row in database
                s = c.prepareStatement("DELETE FROM booking WHERE vehicle_reg=?;");

                s.setString(1,selectedItem.getRegistrationNumber());
                s.executeUpdate();
            }

            catch(SQLException err) {
                System.out.println(err);
            }
            finally {
                if(s != null) {
                    try {
                        s.close();
                    } catch (SQLException ex) {
                        System.out.println(ex);
                    }
                }
            }
            bookingsTable.refresh();

            try{
                buildData("");
            }
            catch(SQLException exe){
                System.out.println(exe);
            }
        }
        else {
            alert.close();
        }
    }

    public void buildData(String query) throws SQLException{
        bookings = FXCollections.observableArrayList();

        PreparedStatement state = null;
        try {
            if(query.equals("")){
                state = c.prepareStatement("SELECT * FROM bookings;");
            }
            else{

                state = c.prepareStatement("SELECT * FROM booking WHERE booking_id LIKE ? OR vehicle_reg LIKE ?;");

                state.setString(1,"%"+query+"%");
                state.setString(2,"%"+query+"%");
            }

            ResultSet rs = state.executeQuery();

            while (rs.next()) {
                String bookingNum = rs.getString("booking_id");
                String regNumber = rs.getString("vehicle_reg");
                String bookingType = rs.getString("booking_type");
                String mechanic = rs.getString("mechanics");
                String firstName = rs.getString("firstname");
                String lastName = rs.getString("Lastname");
                String times = rs.getString("times");
                String mileage = rs.getString("current_mielage");
                String cost = rs.getString("total_cost");
                String date = rs.getString("date");
                String lastServicedDate = rs.getString("last_serviced_date");

                Booking b = new Booking(bookingNum, regNumber, bookingType, mechanic, firstName, lastName, date, times, mileage, cost, lastServicedDate);

                bookings.add(b);
            }

            bookingsTable.getItems().setAll(bookings);
            
        } catch (SQLException e ) {
            System.out.println(e);
        } finally {
            if (state != null) { state.close(); }
        }
    }

    public boolean fieldsCheck(){
        int counter = 0;
        int checkCounter = 0;
        for(Control c : controls){
            if((c instanceof ChoiceBox && ((ChoiceBox)c).getValue()==null && !c.isDisabled()) || (c instanceof DatePicker && ((DatePicker)c).getValue()==null && !c.isDisabled()) || (c instanceof TextField && ((TextField)c).getText().equals("")) && !c.isDisabled()){
                labels.get(counter).setTextFill(Color.web("#FF0000"));
                counter++;
                checkCounter++;

            }
            else{
                labels.get(counter).setTextFill(Color.web("#000000"));
                counter++;
            }
        }
        return checkCounter==0;
    }

    public void createPopup(String operation,String buttonText) throws SQLException{
        //Nodes
        HBox hbox = new HBox();
        //Controls
        Label title = new Label(operation + " Booking");
        bookingNumberField = new TextField();
        bookingTypes = new ChoiceBox();
        regNumberField = new TextField();
        mechanicField = new TextField();
        firstNameField = new TextField();
        lastNameField = new TextField();
        datePicker = new DatePicker();
        times = new ChoiceBox();
        costField = new TextField();
        mileageField = new TextField();
        lastServicedDatePicker = new DatePicker();
        
        //Buttons
        add = new Button();
        cancel = new Button();

        //Setting up - Button appearances
        add.setPrefSize(120, 25);
        add.setText(buttonText);
        cancel.setPrefSize(120,25);
        cancel.setText("Cancel");

        //Setting up - Stage for popup
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        VBox dialogVbox = new VBox(15);
        dialogVbox.setPadding(new Insets(10,10,10,20));

        hbox = new HBox(5);
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 18));
        hbox.getChildren().add(title);
        dialogVbox.getChildren().add(hbox);

        String getCustomers = "SELECT name, surname FROM customers";
        customers = new ChoiceBox<>();
        hbox = new HBox(5);
        Label customersLab = new Label("Customer: ");
        hbox.getChildren().addAll(customersLab, customers);
        dialogVbox.getChildren().add(hbox);

        try (PreparedStatement s = c.prepareStatement(getCustomers)) {
            ResultSet rs = s.executeQuery();
            while(rs.next()){
                String name = rs.getString("name");
                String surname = rs.getString("surname");
                customers.getItems().add(name+" "+surname);

            }
        } catch(SQLException e ) {
            System.out.println(e);
        }

        hbox = new HBox(5);
        Label regNumberLab = new Label("Registration Number: ");
        hbox.getChildren().addAll(regNumberLab, regNumberField);
        dialogVbox.getChildren().add(hbox);

        ObservableList<String> tp = FXCollections.observableArrayList("Repair", "Diagnostic");

        bookingTypes = new ChoiceBox<>(tp);
        hbox = new HBox(8);
        Label bookingTypeLab = new Label("Type: ");
        hbox.getChildren().addAll(bookingTypeLab, bookingTypes);
        dialogVbox.getChildren().addAll(hbox);
        
        
        ChoiceBox templates = new ChoiceBox<>();
        Collections.sort(tp, (String s1, String s2) -> s1.compareTo(s2));

        templates.setItems(tp);
        hbox = new HBox(8);
        Label templatesLab = new Label("Template: ");
        hbox.getChildren().addAll(templatesLab, templates);
        dialogVbox.getChildren().addAll(hbox);
        temps.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            String[] parts = newValue.split(" ");
            mechanicField.setText(parts[0]);
            mileageField.setText(parts[1]);
            bookingNumberField.setText(parts[2]);
            costField.setText(parts[3]);
        });

        hbox = new HBox(8);
        Label mechanicLab = new Label("Mechanic name: ");
        hbox.getChildren().addAll(mechanicLab, mechanicField);
        dialogVbox.getChildren().addAll(hbox);

        hbox = new HBox(8);
        Label mileageLab = new Label("Current mileage of the car: ");
        hbox.getChildren().addAll(mileageLab, mileageField);
        dialogVbox.getChildren().add(hbox);

        hbox = new HBox(8);
        Label bookingNumberLab = new Label("Booking Number: ");
        hbox.getChildren().addAll(bookingNumberLab, bookingNumberField);
        dialogVbox.getChildren().add(hbox);

        hbox = new HBox(8);
        Label dateLab = new Label("Date for your booking: ");
        hbox.getChildren().addAll(dateLab, datePicker);
        dialogVbox.getChildren().add(hbox);
        
        hbox = new HBox(8);
        Label costLab = new Label("Cost of the Booking: ");
        hbox.getChildren().addAll(costLab, costField);
        costField.setDisable(true);
        dialogVbox.getChildren().add(hbox);

        hbox = new HBox(8);
        Label lsDateLab = new Label("Expiration Date: ");
        hbox.getChildren().addAll(lsDateLab,lastServicedDatePicker);
        lastServicedDatePicker.setDisable(true);
        dialogVbox.getChildren().add(hbox);

         //List of Controls
        controls.add(customers); 
        controls.add(regNumberField); 
        controls.add(bookingTypes);
        controls.add(mechanicField);
        controls.add(mileageField);
        controls.add(bookingNumberField);
        controls.add(datePicker);
        controls.add(costField);
        controls.add(lastServicedDatePicker);

        //List of Labels
        labels.add(customersLab);
        labels.add(regNumberLab);
        labels.add(bookingTypeLab);
        labels.add(mechanicLab);
        labels.add(mileageLab);
        labels.add(bookingNumberLab);
        labels.add(dateLab);
        labels.add(costLab);
        labels.add(lsDateLab);

        //BUTTONS
        hbox = new HBox(30);
        hbox.setAlignment(Pos.CENTER_RIGHT);
        hbox.getChildren().addAll(cancel,add);
        dialogVbox.getChildren().add(hbox);
        cancel.setOnAction(event -> dialog.close());
        
        //Pop us scene settings
        Scene dialogScene = new Scene(dialogVbox, 700, 645);
        dialog.setTitle(operation);
        dialog.setScene(dialogScene);
        dialog.setResizable(false);
        dialog.show();
    }
    
    //build booking details to show on the lower part of the panel
    private void buildBooking(Booking b) throws SQLException{
        String firstNameParts = b.getFirstName();
        String lastNameParts = b.getLastName();
        String caddress="";
        String ctype="";
        String cpostcode=""; 
        String cphone="";
        String cemail="";
        String getCustomerInfo = "SELECT * FROM customers WHERE name=? AND surname=?";

        try(PreparedStatement s = c.prepareStatement(getCustomerInfo)) {
            s.setString(1,firstNameParts);
            s.setString(2,lastNameParts);

            ResultSet rs = s.executeQuery();

            while(rs.next()) {
                caddress = rs.getString("address");
                cpostcode = rs.getString("postcode");
                ctype = rs.getString("type");
                cphone = rs.getString("phone");
                cemail = rs.getString("email");
            }

            fullName.setText(firstNameParts+" "+lastNameParts);
            address.setText(caddress);
            custType.setText(ctype);
            postcode.setText(cpostcode);
            phone.setText(cphone);
            email.setText(cemail);

        } catch (SQLException e ) {
            System.out.println(e);
        }
    }
}

