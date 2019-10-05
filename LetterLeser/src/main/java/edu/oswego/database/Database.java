package edu.oswego.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;

import SentimentAnalyzer.SentimentScore;
import edu.oswego.mail.Mailer;
import edu.oswego.model.Label;
import edu.oswego.model.UserFavourites;
import edu.oswego.model.UserFolder;
import edu.oswego.props.Settings;

/**
 * Database class to get connection, push/pull data, and submit queries.
 * 
 * @author Jimmy
 * @since 10/04/2019
 */

public class Database {

	private static Connection connection;
	private static List<Address> addrList = new ArrayList<>();
	private static List<UserFolder> folderList = new ArrayList<>();

	/*
	 * Pulls all emails from IMAP server and separates meta-data
	 */
	public static void pull(String folderName) {
		// should pull for all known folders
		Message[] msgs = Mailer.pullEmails(folderName); // "[Gmail]/All Mail");
//		int msgNum = 301;
//		try {
//			System.out.println(msgs[msgNum].getFrom()[0] + "\t::\t" + msgs[msgNum].getSubject());
//		} catch (MessagingException e) {
//			e.printStackTrace();
//		}
		
		
		for (Message m : msgs) {
			try {
//				if (m.getFrom().length > 1) {	// multiple from-ers... recips? confusing... recip linked to email i believe
//					insertEmailAddress(m.getFrom());
//					break;
//				} else {
//					insertEmailAddress(m.getFrom());
//				}
				
				insertEmailAddress(m.getFrom());
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		}
	}

	public static int getEmailCountByFolder(String folderName) {
		// can get sent count by putting foldername as sent. received is all folders aggregate.
		// lets be able to get all folders to do this?
		ResultSet queryTbl;
		int size = 0;
		try {
			queryTbl = getConnection().prepareStatement("SELECT * FROM user " +
							"JOIN user_email ON user.id = user_email.user_id " +
							"JOIN email ON email.id = user_email.email_id " +
							"JOIN folder ON folder.id = email.folder_id " +
							"WHERE folder.fold_name != '" + folderName + "';")
					.executeQuery();

			while (queryTbl.next())
				size++;

			queryTbl.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return size;
	}

	// WIP
	// SQL DATE // filter on attachment? // need attachment name
	// private static static void getEmailByFilters(String email, String folder,
	// String label, boolean byAttachment,
	// boolean bySeen, Date startDate, Date endDate, int interval) {
	// if (startDate != null || endDate != null) { // same with label
	// // concat dates into query
	// }
	// }

	/*
	 * Displays all INBOX folder messages through console output.
	 */
	public static void showTables() {
		long ct = System.currentTimeMillis();
		ResultSet queryTbl;
		try {
			// show all tables
			queryTbl = getConnection().prepareStatement("show tables").executeQuery();

			while (queryTbl.next()) {
				String tbl = queryTbl.getString(1);
				System.out.println("[INBOX] Table: " + tbl + "\n-------------------");

				// show all attributes from the tables
				ResultSet queryAttr = Database.getConnection().prepareStatement("select * from " + tbl).executeQuery();
				while (queryAttr.next()) {
					ResultSetMetaData md = queryAttr.getMetaData();
					for (int i = 1; i < md.getColumnCount() + 1; i++)
						System.out.println(
								md.getColumnName(i) + "_" + md.getColumnTypeName(i) + " :: " + queryAttr.getString(i));
					System.out.println();
				}
				System.out.println();
				queryAttr.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		System.out.println("Total runtime: " + (System.currentTimeMillis() - ct) + " ms\n");

	}

	/*
	 * Creates a query on database. Should mostly be used for insertions or updates.
	 * Does not return pingback value.
	 * 
	 * @param query statement
	 */
	public static void query(String statement) {
		PreparedStatement ps;
		try {
			ps = getConnection().prepareStatement(statement);
			ps.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	//

	/*
	 * Inserts into email_addr. Removes duplicates with addrList.
	 * 
	 * @param Array of address objects from Message.getFrom()
	 */
	// ERROR IN HERE?
	private static void insertEmailAddress(Address[] addresses) {
		for (int i = 0; i < addresses.length; i++) {
			PreparedStatement ps;
			try {
				if (!addrList.contains(addresses[i])) {
					addrList.add(addresses[i]);
					
					String address = addresses[i].toString();
					

					//address.replaceAll(, "");
					System.out.print("THIS ONE: " + addresses[i]);
//					if (addresses[i].toString().contains("'") || addresses[i].toString().contains(", "))
//						address.replace("'", "''");
					
					ps = getConnection().prepareStatement("INSERT INTO email_addr (email_address) VALUE ('" + address + "');");
					ps.execute();
					System.out.println();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * For testing/debug purposes via Database Team.
	 */
	public static void insertDummyData(String[] dummyStatements) {
		PreparedStatement ps;
		try {
			for (String statement : dummyStatements) {
				ps = getConnection().prepareStatement(statement);
				ps.execute();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// NEED INSERT FOLDER FROM DATABASE

	/*
	 * Fetches folder object (if already exists) or creates one.
	 * 
	 * @param id and name of folder
	 * 
	 * @return new Folder object if it doesn't exist.
	 */
	public static UserFolder getFolder(int id, String name) {
		for (UserFolder folder : folderList)
			if (folder.getId() == id)
				return folder;

		UserFolder fold = new UserFolder(id, name);
		folderList.add(fold);

		return fold;
	}

	// needs return list of emails
	// public static void getRecipientList(int emailId) {
	// String statement = "SELECT email_addr.email_address FROM recipient_list "
	// + "JOIN email_addr ON email_addr.id = recipient_list.email_addr_id "
	// + "WHERE email_id = '"
	// + emailId + "';";
	// }
	//
	// public static void getEmail() {
	// String statement = "SELECT * FROM email WHERE"; // NEED TO HAVE another table
	// for all emails linking to a userId
	// }
	/**
	 * --------------- ALL DONE GO HERE
	 * ----------------------------------------------------------------------------------------------------
	 */

	/*
	 * Singleton-style connection fetch method.
	 * 
	 * @return a MySQL JDBC Connection object
	 */
	public static Connection getConnection() {
		try {
			if (connection == null || connection.isClosed())
				connection = DriverManager.getConnection("jdbc:mysql://" + Settings.DATABASE_HOST + ":"
						+ Settings.DATABASE_PORT + "/" + Settings.DATABASE_SCHEMA
						+ "?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC&user="
						+ Settings.DATABASE_USERNAME + "&password=" + Settings.DATABASE_PASSWORD);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return connection;
	}

	/*
	 * Gets list of validated emails set in user attribute (3).
	 * 
	 * @return number of validated emails (must be updated)
	 */
	public static int getValidatedEmails(String emailAddress) {
		int validatedEmails = 0;
		ResultSet queryTbl;
		try {
			queryTbl = getConnection()
					.prepareStatement("SELECT * FROM user WHERE user.email_address = '" + emailAddress + "'")
					.executeQuery();
			while (queryTbl.next())
				validatedEmails = queryTbl.getInt(3);

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return validatedEmails;
	}

	/*
	 * Truncates all tables in the Schema.
	 * 
	 * @see truncateTable method
	 */
	public static void truncateTables() {
		for (String tbl : Settings.DATABASE_TABLES)
			truncateTable(tbl);
	}

	/*
	 * Truncates one table in the Schema.
	 * 
	 * @param table name
	 */
	public static void truncateTable(String table) {
		PreparedStatement ps;
		try {
			ps = getConnection().prepareStatement("TRUNCATE TABLE " + table + ";");
			ps.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Figures out if a user has email.
	 * 
	 * @return whether a user has emails in their account
	 * 
	 * @param email address of the user.
	 */
	public static boolean hasEmails(String emailAddress) {
		ResultSet queryTbl;
		try {
			queryTbl = getConnection()
					.prepareStatement("SELECT * from user " + "JOIN user_email ON user.id = user_email.id "
							+ "JOIN email ON email.id = user_email.email_id WHERE email = " + emailAddress + ";")
					.executeQuery();
			int size = 0;

			while (queryTbl.next()) {
				size++;
				if (size > 0)
					return true;
			}

			queryTbl.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static List<Label> getLabels() {
		try {
			Folder[] f = Mailer.getStorage().getDefaultFolder().list();
			for (Folder fd : f) {
				System.out.println(">> " + fd.getName());
				System.out.println(fd.getFolder("Alumni").exists());
			}
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * Fetches all attributes of user_favourites table.
	 * 
	 * @param user email address to be queried.
	 * 
	 * @return List of UserFavourites objects.
	 */
	public static List<UserFavourites> fetchFavourites(String emailAddress) {
		List<UserFavourites> favsList = new ArrayList<>(); // HMMM CLASS LIST MAYBE?

		System.out.println("FETCHING FAVOURITES FOR :" + emailAddress + "\n----------------------");
		String query = "SELECT user_favourites.id, filter_settings.fav_name, filter_settings.start_date, filter_settings.end_date, filter_settings.interval_range, folder.id, folder.fold_name FROM user JOIN user_favourites ON user.id = user_favourites.user_id JOIN filter_settings ON user_favourites.filter_settings_id = filter_settings.id JOIN folder ON filter_settings.folder_id = folder.id WHERE email_address = '"
				+ emailAddress + "';";

		try {
			ResultSet rs = getConnection().prepareStatement(query).executeQuery();
			while (rs.next()) {
				ResultSetMetaData md = rs.getMetaData();

				// CHECK FOR FOLDER DUPLICATES.
				UserFolder folder = getFolder(rs.getInt(6), rs.getString(7));

				favsList.add(new UserFavourites(1, rs.getString(2), rs.getDate(3), rs.getDate(4), 5, folder));

				// SHOWS ALL ATTR FOR CONSOLE PURPOSE
				for (int i = 1; i < md.getColumnCount() + 1; i++) {
					System.out.println(md.getColumnName(i) + "_" + md.getColumnTypeName(i) + " :: " + rs.getString(i)); // create
				}
				System.out.println("....................");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return favsList;
	}

	/*
	 * Calculates and inserts sentiment score.
	 * 
	 * @param email id in database table and SentimentScore object.
	 * 
	 * @see insertSentimentScore method
	 */
	public static void calculateSentimentScore(int emailId, SentimentScore score) {
		int sentimentId = insertSentimentScore(score);

		if (sentimentId == -1) {
			System.out.println("ERROR HAS OCCURED CALCULATING SENTIMENT SCORE");
			return;
		}

		insertSentimentScoreIntoEmail(emailId, sentimentId);
	}

	/*
	 * Inserts sentiment score into the database.
	 * 
	 * @param SentimentScore object (pos, neg, neu, cmp)
	 * 
	 * @return id attribute from sentiment_score table of inserted object
	 */
	private static int insertSentimentScore(SentimentScore score) {
		try {
			PreparedStatement ps = getConnection()
					.prepareStatement("INSERT INTO sentiment_score (positive, negative, neutral, compound) VALUE ("
							+ score.getPositive() + ", " + score.getNegative() + ", " + score.getNeutral() + ", "
							+ score.getCompound() + ");", Statement.RETURN_GENERATED_KEYS);
			if (ps.executeUpdate() == 1) { // AFFECTED ROW
				ResultSet rs = ps.getGeneratedKeys();
				if (rs.next())
					return rs.getInt(1);
			}
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

	/*
	 * Links sentiment score to an email via Foreign Key UPDATE.
	 * 
	 * @param email id and sentiment score id from database table
	 */
	private static void insertSentimentScoreIntoEmail(int emailId, int sentimentScoreId) {
		try {
			PreparedStatement pstmt = getConnection().prepareStatement(
					"UPDATE email SET sentiment_score_id = " + sentimentScoreId + " WHERE id = " + emailId + ";");
			pstmt.execute();
			System.out.println(sentimentScoreId);

			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
// http://makble.com/gradle-example-to-connect-to-mysql-with-jdbc-in-eclipse