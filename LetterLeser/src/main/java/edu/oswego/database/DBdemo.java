package edu.oswego.database;

import edu.oswego.calc.Calculator;
import edu.oswego.props.EmailType;
import edu.oswego.props.Settings;

public class DBdemo {

	public static void main(String[] args) {
		Settings.loadCredentials();

		Database.truncateTables();
//		Database.insertDummyData(new String[]{
//			"USE csc480_19f;",
//			"INSERT INTO user (email_address) VALUE ('first@gmail.com'); ",
//			"INSERT INTO user (email_address) VALUE ('second@gmail.com'); ",
//			"INSERT INTO user (email_address) VALUE ('third@gmail.com'); ",
//			"INSERT INTO folder (fold_name) VALUE ('INBOX'); ",
//			"INSERT INTO folder (fold_name) VALUE ('SENT'); ",
//			"INSERT INTO folder (fold_name) VALUE ('TRASH'); ",
//			"INSERT INTO filter_settings (start_date, end_date, interval_range, folder_id) " +
//					"VALUES (CURDATE(), CURDATE(), 69, 1), (CURDATE(), CURDATE(), 96, 2), (CURDATE(), CURDATE(), 00, 3);",
//			" INSERT INTO user_favourites (fav_name, user_id, filter_settings_id)" +
//				"VALUES	"
//					+ "('Favourite uno', 1, 1),"
//					+ "('Favourite dos', 1, 2),"
//					+ "('Favourite tres', 2, 3),"
//					+ "('Favourite quatro', 3, 2); ",
//			"INSERT INTO email_addr (email_address)" +
//				"VALUES "
//				+ "('dan@gmail.com'), "
//				+ "('tekashi@gmail.com'), "
//				+ "('priy@gmail.com'); ",
//				"INSERT INTO sentiment_score (positive, negative, neutral, compound) " +
//					"VALUES"
//						+ "(.5, .5, .5, .5), "
//						+ "(.69, .69, .69, .69),"
//						+ "(.101, .101, .101, .101);",
//				"INSERT INTO email (date_received, subject, size, seen, has_attachment, file_name, sentiment_score_id, folder_id)" +
//					"VALUES "
//						+ "(CURDATE(), 'Where my cheese @?', 100, 1, 1, 'cheese.png', 1, 1), "
//						+ "(CURDATE(), 'Tekashi in da h0u$e', 200, 0, 0, null, 2, 2), "
//						+ "(CURDATE(), 'My Milkshake brings all the boise to...', 300, 0, 0, null, 3, 3);",
//				"INSERT INTO user_email (user_id, email_id)" +
//					"VALUES"
//						+ "(1, 1), "
//						+ "(2, 2), "
//						+ "(3, 3);",
//				"INSERT INTO received_email (email_id, email_addr_id)" +
//					"VALUES"
//						+ "(1, 1), "
//						+ "(2, 2), "
//						+ "(3, 3); ",
//				"INSERT INTO recipient_list (email_id, email_addr_id) " +
//					"VALUES"
//						+ "(1, 1), "
//						+ "(2, 2), "
//						+ "(3, 3); ",
//				"INSERT INTO label (lb_name) " +
//					"VALUES "
//					+ "('SCHOOL'), "
//					+ "('WORK'), "
//					+ "('OTHER'); ",
//				"INSERT INTO label_list (email_id, label_id)" +
//					" VALUES "
//					+ "(1, 1), "
//					+ "(2, 1), "
//					+ "(3, 2), "
//					+ "(3, 3); ",
//				"UPDATE user SET validated_emails = 10 WHERE id = 1"
//	});
		Database.pull("[Gmail]/All Mail");
//		Database.showTables();
		
		
//		Database.validate("first@gmail.com");
		
//		Calculator calc = new Calculator();
		
//		System.out.println(calc.needsValidation("first@gmail.com"));

		// Database.truncateTables();
		// Database.insertDummyData();
		
		//Database.pull(); // for all mailbox gets error?
		
		// System.out.println("PRIY: " + Database.priy());
		// Database.showTables();
		// Database.fetchFavourites("first@gmail.com");

		// Database.getLabels();
	}

}