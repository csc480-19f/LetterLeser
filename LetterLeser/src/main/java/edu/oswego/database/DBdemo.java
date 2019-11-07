package edu.oswego.database;

import java.sql.SQLException;

import javax.mail.Folder;
import javax.mail.MessagingException;

import edu.oswego.mail.Mailer;

/**
 * Test class demonstrating some database functionality.
 *
 * @author nguyen
 * @since 10/29/2019
 * @deprecated
 */
public class DBdemo {

	public static void main(String[] args) throws MessagingException {
		long ct = System.currentTimeMillis();
		Settings.loadCredentials();
		Mailer mailer = new Mailer(edu.oswego.mail.Settings.EMAIL_ADDRESS, edu.oswego.mail.Settings.EMAIL_PWD);
		OldDatabase db = new OldDatabase(edu.oswego.mail.Settings.EMAIL_ADDRESS, mailer);

		System.out.println(db.getValidatedEmails());
		// db.truncateTables();
		db.pull();

		// try {
		// Date utilDate = (Date) new
		// SimpleDateFormat("yyyy-MM-dd").parse("2014-01-28");
		// db.insertUserFavourites("Awesome favs", utilDate, utilDate, Interval.WEEK,
		// false, false, "Apple Developer");
		// db.insertUserFavourites("Crappy favs", utilDate, utilDate, Interval.YEAR,
		// true, true, "INBOX");
		// db.insertUserFavourites("Mediocre favs", utilDate, utilDate, Interval.MONTH,
		// false, true, "Misc/UUP/CELT");
		// db.insertUserFavourites("Jimmys favs", utilDate, utilDate, Interval.WEEK,
		// true, false, "[Gmail]/Sent Mail");
		// // db.removeUserFavourite("Awesome favs");
		// } catch (java.text.ParseException e) {
		// e.printStackTrace();
		// }

		db.showTables();
		//
		// String startDate = Time.parseDateTime(Time.getDate("2010-03-12"));
		// String endDate = Time.parseDateTime(Time.getDate("2014-03-12"));
		// List<Email> emailList = db.getEmailByFilter(null, startDate, endDate, false,
		// "Apple Developer");
		//
		// System.out.println(emailList);

		double time = (double) ((System.currentTimeMillis() - ct) * .001);
		System.out.println("Total runtime: " + time + " seconds\n");

		db.truncateTables();
	}

	public static boolean needsUpdate(Database db, Mailer mailer) {
		try {
			int validatedEmails = db.getValidatedEmails();
			Folder validationFolder = mailer.getFolder("CSC480_19f");
			int newEmails = validationFolder.getMessageCount();
			if (newEmails != validatedEmails) {
				System.out.println("The database emails and user's emails do not match.");
				System.out.println(newEmails + "_" + validatedEmails);
				return true;
			}
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		return false;
	}

}