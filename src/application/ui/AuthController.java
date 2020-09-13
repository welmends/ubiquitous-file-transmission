package application.ui;

import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

import application.ts.TupleSpace;
import application.ui.constants.AuthConstants;
import application.ui.constants.ImageConstants;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AuthController implements Initializable {

	// FXML Variables
    @FXML private TextField deviceTF;
    @FXML private TextField ipTF;
    @FXML private TextField portTF;
    @FXML private TextField axisTF;
    @FXML private Button enterButton;
    
	// COM Variables
    private TupleSpace ts;
	
	// Variables
    private Stage stage = null;
    private HashMap<String, String> credentials = new HashMap<String, String>();
    
    private MainController main;
    private ConfigController config;
    
    public AuthController(TupleSpace ts, MainController main, ConfigController config) {
    	this.ts = ts;
    	this.main = main;
    	this.config = config;
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    	enterButton.setGraphic(ImageConstants.ADD_BTN_ICON);
    	setEnterBtnPressedBehavior();
    }
	
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    private void closeStage() {
    	ts.start();
    	config.start();
        if(stage!=null) {
            stage.close();
        }
    }
    
    private void setEnterBtnPressedBehavior() {
    	enterButton.setOnAction((event)->{
    		if(!acquireCredentials()) {
    			return;
    		}
        	disableComponents(true);
        	String username     = credentials.get(AuthConstants.HASHCODE_USERNAME);
        	String ip_address   = credentials.get(AuthConstants.HASHCODE_IPADDRESS);
        	Integer port_number = Integer.valueOf(credentials.get(AuthConstants.HASHCODE_PORTNUMBER));
        	Integer axis_x      = Integer.valueOf(credentials.get(AuthConstants.HASHCODE_AXIS_X));
        	Integer axis_y      = Integer.valueOf(credentials.get(AuthConstants.HASHCODE_AXIS_Y));
        	
        	if(!ts.connect(username, ip_address, port_number, axis_x, axis_y)) {
        		main.closeApplication();
        		Alert alert = new Alert(Alert.AlertType.ERROR);
        		alert.setTitle("Connection Fail");
        		alert.setResizable(false);
        		alert.setHeaderText("Verify if the Apache River service is running!\nExiting the application...");
        		alert.showAndWait();
        		Platform.exit();
		        System.exit(0);
        	}else {
        		if(!ts.init_admin_tuple()) {
            		Alert alert = new Alert(Alert.AlertType.WARNING);
            		alert.setTitle("Invalid username");
            		alert.setResizable(false);
            		alert.setHeaderText("The username is invalid. Try another one.");
            		alert.showAndWait();
            		disableComponents(false);
            		return;
        		}else {
        			//chat.chatLabelUser.setText(ts.get_user_name());
        		}
        	}
        	
        	closeStage();
        });
    }
    
    private void disableComponents(Boolean b) {
    	deviceTF.setDisable(b);
    	ipTF.setDisable(b);
    	portTF.setDisable(b);
    	axisTF.setDisable(b);
    	enterButton.setDisable(b);
    }
    
    private Boolean acquireCredentials() {
    	credentials.clear();
    	if(!deviceTF.getText().equals("")) {
    		// device name
    		credentials.put(AuthConstants.HASHCODE_USERNAME, deviceTF.getText());
    		// ip address
    		if(ipTF.getText().equals("")) {
    			credentials.put(AuthConstants.HASHCODE_IPADDRESS, AuthConstants.DEFAULT_IPADDRESS);
    		}else {
    			credentials.put(AuthConstants.HASHCODE_IPADDRESS, ipTF.getText());
    		}
    		// port number
    		if(portTF.getText().equals("")) {
    			credentials.put(AuthConstants.HASHCODE_PORTNUMBER, AuthConstants.DEFAULT_PORTNUMBER);
    		}else {
    			credentials.put(AuthConstants.HASHCODE_PORTNUMBER, portTF.getText());
    		}
    		// axis
    		if(axisTF.getText().equals("")) {
        		credentials.put(AuthConstants.HASHCODE_AXIS_X, AuthConstants.DEFAULT_AXIS_X_OR_Y);
        		credentials.put(AuthConstants.HASHCODE_AXIS_Y, AuthConstants.DEFAULT_AXIS_X_OR_Y);
    		}else {
    			String axis = axisTF.getText();
    			String x_axis = axis.substring(0, axis.indexOf(","));
    			String y_axis = axis.substring(axis.indexOf(",")+1);
        		credentials.put(AuthConstants.HASHCODE_AXIS_X, x_axis);
        		credentials.put(AuthConstants.HASHCODE_AXIS_Y, y_axis);
    		}
    		return true;
    	}
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setTitle("Invalid username");
		alert.setResizable(false);
		alert.setHeaderText("The username is invalid. Try another one.");
		alert.showAndWait();
    	return false;
    }
}