package application.ui;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import application.socket.SocketFile;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;

public class ConfigController extends Thread implements Initializable  {
	
	// FXML Variables
	@FXML HBox mainHBox;
	@FXML Label uftLabel;
	@FXML ImageView uftImageView;
	@FXML ScrollPane contactsScrollPane;
	@FXML VBox vboxOnScroll;
	
	// COM Variables
	private TupleSpace ts;
	private SocketFile socket_file_server;
	private SocketFile socket_file_client;
	
	// Controllers
	
	// Variables
	private ConfigComponentsArrayUtils componentsArray_utils;
	
	public void loadFromParent(TupleSpace ts, SocketFile socket_file_server, SocketFile socket_file_client) {
		this.ts = ts;
		this.socket_file_server = socket_file_server;
		this.socket_file_client = socket_file_client;
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
		try {
			Thread.sleep(ConfigConstants.THREAD_SLEEP_TIME_MILLIS);
		} catch (InterruptedException e) {
			System.out.println("Error: ConfigController (thread)");
		}
		socket_file_server.setup(ts.get_ip_address(), ts.get_port_number());
		socket_file_server.run_server();
		while(true) {
			try {
				Thread.sleep(ConfigConstants.THREAD_SLEEP_TIME_MILLIS);
			} catch (InterruptedException e) {
				System.out.println("Error: ConfigController (thread)");
			}
			
			Pair<String, String> pair = socket_file_server.receive_file_call();
			if(pair!=null) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						Alert alert = new Alert(Alert.AlertType.INFORMATION);
						alert.setTitle("File Received");
						alert.setResizable(false);
						alert.setHeaderText("Device "+pair.getKey()+" send a file:\n"+pair.getValue());
						alert.showAndWait();
					}
				});
			}
			
			List<Environment> ts_envs = ts.get_environments_list();
			List<Device> ts_devices = ts.get_devices_list();
			HashMap<Device, Environment> ts_hash = ts.get_hash_devices_environments();
			
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					componentsArray_utils.updateComponentsList(ts.get_device_name(), ts.get_environment_name(), ts_envs, ts_devices, ts_hash);
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
					
					Label l_axis = new Label();
					l_axis.setText("("+axis+")");
					l_axis.setStyle(ConfigConstants.DEVICE_STYLE);
					b_device.setGraphic(l_axis);
				}
	        });
		}else {
			b_device.setOnAction((event)->{
				Device dev = ts.get_device(b_device.getText());
				socket_file_client.setup(dev.ip_address, dev.port_number);
				if(socket_file_client.connect()) {
					FileChooser chooser = new FileChooser();
					File file = chooser.showOpenDialog(new Stage());
			        if (file != null) {
			        	socket_file_client.send_file_call(file, ts.get_device_name());
			        	socket_file_client.disconnect(false);
			        }
	    		}else {
    				Alert alert = new Alert(Alert.AlertType.ERROR);
    				alert.setTitle("Connection Information");
    				alert.setResizable(false);
    				alert.setHeaderText("Connection Unsuccessful!");
    				alert.showAndWait();
	    		}
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