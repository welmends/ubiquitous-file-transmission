package application;

import application.ui.MainController;
import application.ui.constants.FXMLConstants;
import application.ui.constants.ImageConstants;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Scene;

public class Main extends Application {
	private Stage primaryStage;
	
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		this.primaryStage = primaryStage;
		
		Parent root;
		FXMLLoader loader = new FXMLLoader();
		MainController mainController = new MainController();
		
		loader.setLocation(getClass().getResource(FXMLConstants.FXML_MAIN_CONTROLLER));
		mainController.setMainApp(this);
		loader.setController(mainController);
		root = loader.load();
		
        this.primaryStage.getIcons().add(ImageConstants.CHAT_TOP_ICON);
        this.primaryStage.setTitle(MainConstants.TITLE_TEXT);
		this.primaryStage.setResizable(false);
		this.primaryStage.setScene(new Scene(root, MainConstants.SCENE_WIDTH, MainConstants.SCENE_HEIGHT));
		this.primaryStage.show();
		this.primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
		    @Override public void handle(WindowEvent t) {
		    	mainController.closeApplication();
		        Platform.exit();
		        System.exit(0);
		    }
		});
	}
	
    public Stage getPrimaryStage() {
        return primaryStage;
    }
}