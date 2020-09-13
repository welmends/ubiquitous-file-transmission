package application.ui;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import application.ts.TupleSpace;
import application.ts.TupleSpaceConstants;
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
import javafx.scene.control.TitledPane;
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
				componentsArray_utils.init_all_room(ts.get_user_name());
			}
		});
		
		while(true) {
			try {
				Thread.sleep(ConfigConstants.THREAD_SLEEP_TIME_MILLIS);
			} catch (InterruptedException e) {
				System.out.println("Error: ConfigController (thread)");
			}
			
			List<String> ts_rooms = ts.get_rooms_list();
			List<String> ts_contacts = ts.get_contacts_list();
			HashMap<String, String> ts_hash = ts.get_hash_rooms_contacts();
			
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					componentsArray_utils.updateComponentsList(ts.get_user_name(), ts_rooms, ts_contacts, ts_hash);
				}
			});
			
		}
	}
	
	private void setupComponents() {
		uftLabel.setText(ConfigConstants.UFT_LABEL_TEXT);
		uftLabel.setStyle(ConfigConstants.UFT_LABEL_STYLE);
		
		uftImageView.setImage(ImageConstants.FILE_TRANSMISSION_ICON);
	}
	
	public void setRoomBtnPressedBehavior(TitledPane tp_room, Button b_enter_room, Button b_leave_room) {
		b_enter_room.setOnAction((event)->{
			do {
				if(ts.select_room(tp_room.getText())) {
					break;
				}
			}while(true);
			
			ts.set_chat_type(TupleSpaceConstants.ROOM_CHAT);
			ts.set_room_name(tp_room.getText());
        });
		
		b_leave_room.setOnAction((event)->{
			if(ts.get_room_name().equals(tp_room.getText())) {
				do {
					if(ts.deselect_room()) {
						break;
					}
				}while(true);
				ts.set_chat_type(null);
				ts.set_room_name("");
				ts.set_contact_name("");
			}
        });
    }
	
	public void setContactBtnPressedBehavior(Button b_contact) {
		b_contact.setOnAction((event)->{
			ts.set_contact_name(b_contact.getText());
			ts.set_chat_type(TupleSpaceConstants.CONTACT_CHAT);
        });
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