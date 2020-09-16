package application.ui;

import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

import application.socket.SocketTS;
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
    @FXML private TextField ipTF2;
    @FXML private TextField portTF2;
    @FXML private TextField axisTF;
    @FXML private Button enterButton;
    
	// COM Variables
    private SocketTS socket_TS_client;
	
	// Variables
    private Stage stage = null;
    private HashMap<String, String> credentials = new HashMap<String, String>();
    
    private MainController main;
    private ConfigController config;
    
    public AuthController(SocketTS socket_TS_client, MainController main, ConfigController config) {
    	this.socket_TS_client = socket_TS_client;
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
    	socket_TS_client.ts_start();
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
        	String username            = credentials.get(AuthConstants.HASHCODE_USERNAME);
        	String ip_address          = credentials.get(AuthConstants.HASHCODE_IPADDRESS);
        	Integer port_number        = Integer.valueOf(credentials.get(AuthConstants.HASHCODE_PORTNUMBER));
        	String ip_address_server   = credentials.get(AuthConstants.HASHCODE_IPADDRESS_SERVER);
        	Integer port_number_server = Integer.valueOf(credentials.get(AuthConstants.HASHCODE_PORTNUMBER_SERVER));
        	String axis                = credentials.get(AuthConstants.HASHCODE_AXIS);
        	socket_TS_client.setup(ip_address_server, port_number_server);
        	if(!socket_TS_client.ts_connect(username, ip_address, port_number, axis)) {
        		main.closeApplication();
        		Alert alert = new Alert(Alert.AlertType.ERROR);
        		alert.setTitle("Connection Fail");
        		alert.setResizable(false);
        		alert.setHeaderText("Verify if the Apache River service is running!\nExiting the application...");
        		alert.showAndWait();
        		Platform.exit();
		        System.exit(0);
        	}else {
        		String env = socket_TS_client.ts_init_admin_tuple();
        		if(env=="") {
            		Alert alert = new Alert(Alert.AlertType.WARNING);
            		alert.setTitle("Invalid port number");
            		alert.setResizable(false);
            		alert.setHeaderText("The port number is invalid. Try another one.");
            		alert.showAndWait();
            		disableComponents(false);
            		return;
        		}else {
        			socket_TS_client.ts_env_name = env;
                	socket_TS_client.ts_device_name = username;
                	socket_TS_client.ts_ip_address = ip_address;
                	socket_TS_client.ts_port_number = port_number;
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
    		// ip address server
    		if(ipTF2.getText().equals("")) {
    			credentials.put(AuthConstants.HASHCODE_IPADDRESS_SERVER, AuthConstants.DEFAULT_IPADDRESS_SERVER);
    		}else {
    			credentials.put(AuthConstants.HASHCODE_IPADDRESS_SERVER, ipTF.getText());
    		}
    		// port number server
    		if(portTF2.getText().equals("")) {
    			credentials.put(AuthConstants.HASHCODE_PORTNUMBER_SERVER, AuthConstants.DEFAULT_PORTNUMBER_SERVER);
    		}else {
    			credentials.put(AuthConstants.HASHCODE_PORTNUMBER_SERVER, portTF.getText());
    		}
    		// axis
    		if(axisTF.getText().equals("")) {
        		credentials.put(AuthConstants.HASHCODE_AXIS, AuthConstants.DEFAULT_AXIS);
    		}else {
    			credentials.put(AuthConstants.HASHCODE_AXIS, axisTF.getText());
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