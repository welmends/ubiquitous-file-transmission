package application.ui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import application.Main_Device;
import application.MainConstants;
import application.socket.SocketFile;
import application.ts.TupleSpace;
import application.ui.constants.FXMLConstants;
import application.ui.constants.ImageConstants;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class MainController implements Initializable {
	
	// FXML Variables
	@FXML AnchorPane root;
	@FXML HBox mainHBox;
	
	// FXML Loaders
	private FXMLLoader configLoader;
	
	// COM Variables
	private TupleSpace ts;
	private SocketFile socket_file_server;
	private SocketFile socket_file_client;
	
	// Controllers
	private ConfigController configController;
	
	// Main Object
	private Main_Device main;
    public void setMainApp(Main_Device main) {
        this.main = main;
    }
    
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// Initialize Objects
		ts = new TupleSpace();
		socket_file_server = new SocketFile();
		socket_file_client = new SocketFile();
		
		Scene configScene = null;
		
		configLoader = new FXMLLoader(getClass().getResource(FXMLConstants.FXML_CONFIG_CONTROLLER));
		
		//Load Scenes
		try {
			configScene = new Scene(configLoader.load());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Add nodes to MainController holders 
		mainHBox.getChildren().add(configScene.getRoot());
		
		// Get Controller
		configController = configLoader.getController();
		
		// Authentication
		authentication();
		
		// Load common objects from parent
		configController.loadFromParent(ts, socket_file_server, socket_file_client);
	}
	
	public void closeApplication() {
		ts.disconnect();
		socket_file_server.disconnect(true);
		socket_file_client.disconnect(true);
	}

    private Boolean authentication() {
        Parent layout;
        Scene scene;
        Stage auth_stage;
        FXMLLoader loader = new FXMLLoader();
        AuthController popupController = new AuthController(ts, this, configController);
        
        loader.setLocation(getClass().getResource(FXMLConstants.FXML_AUTH_CONTROLLER));
        loader.setController(popupController);
        
        try {
            layout = loader.load();
            scene = new Scene(layout);
            auth_stage = new Stage();
            popupController.setStage(auth_stage);
            if(this.main!=null) { auth_stage.initOwner(this.main.getPrimaryStage()); }
            auth_stage.getIcons().add(ImageConstants.FILE_TRANSMISSION_ICON);
            auth_stage.setTitle(MainConstants.TITLE_TEXT);
            auth_stage.initModality(Modality.WINDOW_MODAL);
            auth_stage.setResizable(false);
            auth_stage.setScene(scene);
            auth_stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
    		    @Override public void handle(WindowEvent t) {
    		        Platform.exit();
    		        System.exit(0);
    		    }
    		});
            auth_stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
		return true;
    }
}