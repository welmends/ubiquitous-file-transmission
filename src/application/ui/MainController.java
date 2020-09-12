package application.ui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import application.Main;
import application.MainConstants;
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
	private FXMLLoader chatLoader;
	private FXMLLoader configLoader;
	
	// COM Variables
	private TupleSpace ts;
	
	// Controllers
	private ChatController chatController;
	private ConfigController configController;
	
	// Main Object
	private Main main;
    public void setMainApp(Main main) {
        this.main = main;
    }
    
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// Initialize Objects
		ts = new TupleSpace();
		
		Scene chatScene = null;
		Scene configScene = null;
		
		chatLoader = new FXMLLoader(getClass().getResource(FXMLConstants.FXML_CHAT_CONTROLLER));
		configLoader = new FXMLLoader(getClass().getResource(FXMLConstants.FXML_CONFIG_CONTROLLER));
		
		//Load Scenes
		try {
			chatScene = new Scene(chatLoader.load());
			configScene = new Scene(configLoader.load());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Add nodes to MainController holders 
		mainHBox.getChildren().add(configScene.getRoot());
		mainHBox.getChildren().add(chatScene.getRoot());
		
		// Get Controller
		chatController = chatLoader.getController();
		configController = configLoader.getController();
		
		// Authentication
		authentication();
		
		// Load common objects from parent
		chatController.loadFromParent(ts);
		configController.loadFromParent(ts, chatController);
	}
	
	public void closeApplication() {
		ts.disconnect();
	}

    private Boolean authentication() {
        Parent layout;
        Scene scene;
        Stage auth_stage;
        FXMLLoader loader = new FXMLLoader();
        AuthController popupController = new AuthController(ts, this, chatController, configController);
        
        loader.setLocation(getClass().getResource(FXMLConstants.FXML_AUTH_CONTROLLER));
        loader.setController(popupController);
        
        try {
            layout = loader.load();
            scene = new Scene(layout);
            auth_stage = new Stage();
            popupController.setStage(auth_stage);
            if(this.main!=null) { auth_stage.initOwner(this.main.getPrimaryStage()); }
            auth_stage.getIcons().add(ImageConstants.CHAT_TOP_ICON);
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