package application.ui;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import application.ts.Device;
import application.ts.Environment;
import application.ts.TupleSpace;
import application.ui.constants.ConfigConstants;
import application.ui.constants.ImageConstants;
import application.ui.utils.ConfigComponentsArrayUtils;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ConfigController extends Thread implements Initializable  {
	
	// FXML Variables
	@FXML HBox mainHBox;
	@FXML Label uftLabel;
	@FXML ImageView uftImageView;
	@FXML ScrollPane contactsScrollPane;
	@FXML VBox vboxOnScroll;
	
	// COM Variables
	private TupleSpace ts;
	
	// Controllers
	
	// Variables
	private ConfigComponentsArrayUtils componentsArray_utils;
	
	public void loadFromParent(TupleSpace ts) {
		this.ts = ts;
	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// Initialize Objects
		componentsArray_utils = new ConfigComponentsArrayUtils(this, vboxOnScroll);
		
		setupComponents();
		setVBoxScrollsBehavior();
	}
	
	@Override
	public void run() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				componentsArray_utils.init_all_env(ts.get_device_name(), ts.get_x_axis(), ts.get_y_axis());
			}
		});
		
		while(true) {
			try {
				Thread.sleep(ConfigConstants.THREAD_SLEEP_TIME_MILLIS);
			} catch (InterruptedException e) {
				System.out.println("Error: ConfigController (thread)");
			}
			
			List<Environment> ts_envs = ts.get_environments_list();
			List<Device> ts_devices = ts.get_devices_list();
			HashMap<Device, Environment> ts_hash = ts.get_hash_devices_environments();
			
//			System.out.println();
//			for (int i=0; i<ts_envs.size(); i++) {
//				System.out.println(ts_envs.get(i));
//			}
//			for (int i=0; i<ts_devices.size(); i++) {
//				System.out.println(ts_devices.get(i));
//			}
//			System.out.println();
			
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					componentsArray_utils.updateComponentsList(ts.get_device_name(), ts_envs, ts_devices, ts_hash);
				}
			});
			
		}
	}
	
	private void setupComponents() {
		uftLabel.setText(ConfigConstants.UFT_LABEL_TEXT);
		uftLabel.setStyle(ConfigConstants.UFT_LABEL_STYLE);
		
		uftImageView.setImage(ImageConstants.FILE_TRANSMISSION_ICON);
	}
	
	public void setDeviceBtnPressedBehavior(Button b_device, Boolean opt) {
		if(opt) {
			b_device.setOnAction((event)->{
				TextInputDialog td = new TextInputDialog("");
				td.setResizable(false);
				td.setTitle("Setup Axis");
				td.setHeaderText("x-axis,y-axis");
				td.showAndWait();
				
				String axis = td.getResult();
				if(axis!=null) {
					ts.set_axis(axis);
					do {
						if(ts.update_device()) {
							break;
						}
					}while(true);
					
					//Possible change of environment...
					
					Label l_axis = new Label();
					l_axis.setText("("+axis+")");
					l_axis.setStyle(ConfigConstants.DEVICE_STYLE);
					b_device.setGraphic(l_axis);
				}
	        });
		}else {
			b_device.setOnAction((event)->{
				//Send File...
				System.out.println("Use javafx.stage.FileChooser to select file!");
	        });
		}
	}
	
	private void setVBoxScrollsBehavior() {
		vboxOnScroll.heightProperty().addListener(new ChangeListener<Number>() {

	        @Override
	        public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
	        	if(arg1.intValue()!=0) {
	        		contactsScrollPane.setVvalue(1.0);
	        	}
	        }
	        
		});
	}
	
}