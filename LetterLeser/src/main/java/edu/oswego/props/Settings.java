package edu.oswego.props;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Simple static class to access credentials through other classes.
 * 
 * @author Jimmy Nguyen
 * @since 09/18/2019
 */

public class Settings {

	public static String DATABASE_SCHEMA = "";			// should be final when we deploy. Only not final so we can load credentials
	public static String DATABASE_USERNAME = "";
	public static String DATABASE_PASSWORD = "";
	public static String DATABASE_HOST = "";
	public static String DATABASE_PORT = "";
	
	public static String EMAIL_ADDRESS = "";
	public static String EMAIL_PWD = "";

	public static final String[] DATABASE_TABLES = new String[] { "email", "email_addr", "filter_settings", "folder",
			"label", "label_list", "received_email", "recipient_list", "user", "user_favourites", "sentiment_score" };
	
	/*
	 * We don't need this. Only for testing since it's a public repo.
	 */
	public static void loadCredentials() {
		try {
			Scanner scanner = new Scanner(new File("credentials.txt"));
			
			DATABASE_SCHEMA = scanner.nextLine();
			DATABASE_USERNAME = scanner.nextLine();
			DATABASE_PASSWORD = scanner.nextLine();
			DATABASE_HOST = scanner.nextLine();
			DATABASE_PORT = scanner.nextLine();
			EMAIL_ADDRESS = scanner.nextLine();
			EMAIL_PWD = scanner.nextLine();
		
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
}