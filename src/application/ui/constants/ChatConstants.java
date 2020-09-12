package application.ui.constants;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class ChatConstants {
	// Sleep
	public static Integer THREAD_SLEEP_TIME_MILLIS = 500;
	
	// Components Style
	public static String STYLE_CHAT_LABEL_USER = "-fx-font-family: 'Arial'; -fx-font-weight:bold; -fx-text-fill: #555555;";
	public static String STYLE_CHAT_LABEL_CONTACT = "-fx-font-family: 'Arial'; -fx-font-weight:bold; -fx-text-fill: #555555;";
	public static String STYLE_TEXT_FIELD_CHAT = "-fx-background-radius: 5em;";
	public static String STYLE_SCROLL_PANE_CHAT = "-fx-background-color:#dddddd; -fx-background-radius: 10 10 10 10; -fx-border-color: #999999; -fx-border-width: 2; -fx-border-radius: 5 5 5 5;";
	public static String STYLE_VBOX_CHAT = "-fx-background-color:#dddddd;";
	
	// Components Alignment
	public static Pos TEXT_ALIGNMENT_LABEL_CHAT_USER = Pos.CENTER_RIGHT;
	public static Pos TEXT_ALIGNMENT_LABEL_CHAT_CONTACT = Pos.CENTER_LEFT;
	
	// Time Label
	public static String SPACE_FOR_LABEL_TIME = "          ";
	public static String TIME_FORMAT = "hh:mm a";
	public static Font FONT_LABEL_TIME = Font.font("System", FontPosture.ITALIC, 9);
	public static Insets PADDING_LABEL_TIME = new Insets(0,6,2,0);
	public static TextAlignment TEXT_ALIGNMENT_LABEL_TIME = TextAlignment.RIGHT;
	public static Pos ALIGNMENT_STACK_PANE_LABEL_TIME = Pos.BOTTOM_RIGHT;
	
	// Sender Label
	public static Font FONT_LABEL_SENDER = Font.font("System", FontWeight.BOLD, 10);
	public static Insets PADDING_LABEL_SENDER = new Insets(0,0,2,6);
	public static TextAlignment TEXT_ALIGNMENT_LABEL_SENDER = TextAlignment.LEFT;
	public static Pos ALIGNMENT_STACK_PANE_LABEL_SENDER = Pos.TOP_LEFT;
	
	// Message Receive
	public static Color COLOR_LABEL_TEXT_RECEIVE = Color.BLACK;
	public static String STYLE_LABEL_TEXT_RECEIVE = "-fx-font-family: 'Arial'; -fx-font-weight:bold; -fx-background-color: #ffffff; -fx-background-radius: 0 20 20 20;";
	public static Insets PADDING_LABEL_TEXT_RECEIVE_ROOM = new Insets(17, 10, 10, 10);
	public static Insets PADDING_LABEL_TEXT_RECEIVE_CONTACT = new Insets(10, 10, 10, 10);
	public static Pos ALIGNMENT_LABEL_TEXT_RECEIVE = Pos.CENTER;
	
	public static Insets PADDING_STACK_PANE_RECEIVE = new Insets(0, 0, 5, 0);
	public static Pos ALIGNMENT_STACK_PANE_RECEIVE = Pos.CENTER_LEFT;
	
	// Message Send
	public static Color COLOR_LABEL_TEXT_SEND = Color.BLACK;
	public static String STYLE_LABEL_TEXT_SEND = "-fx-font-family: 'Arial'; -fx-font-weight:bold; -fx-background-color: #e2ffc9; -fx-background-radius: 20 0 20 20;";
	public static Insets PADDING_LABEL_TEXT_SEND = new Insets(10, 10, 10, 10);
	public static Pos ALIGNMENT_LABEL_TEXT_SEND = Pos.CENTER;
	
	public static Insets PADDING_STACK_PANE_SEND = new Insets(0, 0, 5, 0);
	public static Pos ALIGNMENT_STACK_PANE_SEND = Pos.CENTER_RIGHT;
	
}
