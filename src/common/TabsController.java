package common;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 * Tabs Pane Controller
 *
 * @author Luca
 */

public class TabsController implements Initializable {
    @FXML private Tab customersTab,vehiclesTab,bookingsTab,partsTab,specialistsTab,usersTab;
    @FXML private TabPane tabPane;
    
    private String userType;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            customersTab.setContent((Node) FXMLLoader.load(this.getClass().getResource("/customers/CustomerGUI.fxml")));
            vehiclesTab.setContent((Node) FXMLLoader.load(this.getClass().getResource("/vehicles/VehiclesGUI.fxml")));
            bookingsTab.setContent((Node) FXMLLoader.load(this.getClass().getResource("/bookings/BookingApp.fxml")));
            //partsTab.setContent((Node) FXMLLoader.load(this.getClass().getResource("/parts/PartsGUI.fxml")));
            //specialistsTab.setContent((Node) FXMLLoader.load(this.getClass().getResource("/specialists/SpecialistsGUI.fxml")));
            
           
            if(userType.equals("admin")){
                usersTab.setDisable(false);
                usersTab.setContent((Node) FXMLLoader.load(this.getClass().getResource("/users/UsersGUI.fxml")));
            
            }
            else{
                usersTab.setDisable(true);
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }    
    
    public void setUserType(String ut){
        userType = ut;
    }
}
