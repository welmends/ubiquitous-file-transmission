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
    @FXML private TextField usernameTF;
    @FXML private Button enterButton;
    
	// COM Variables
    private TupleSpace ts;
	
	// Variables
    private Stage stage = null;
    private HashMap<String, String> credentials = new HashMap<String, String>();
    
    private MainController main;
    private ChatController chat;
    private ConfigController config;
    
    public AuthController(TupleSpace ts, MainController main, ChatController chat, ConfigController config) {
    	this.ts = ts;
    	this.main = main;
    	this.chat = chat;
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
    	chat.start();
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
        	String username = credentials.get(AuthConstants.HASHCODE_USERNAME);
        	
        	if(!ts.connect(username)) {
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
        			chat.chatLabelUser.setText(ts.get_user_name());
        		}
        	}
        	
        	closeStage();
        });
    }
    
    private void disableComponents(Boolean b) {
    	usernameTF.setDisable(b);
    	enterButton.setDisable(b);
    }
    
    private Boolean acquireCredentials() {
    	credentials.clear();
    	if(!usernameTF.getText().equals("")) {
    		credentials.put(AuthConstants.HASHCODE_USERNAME, usernameTF.getText());
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