package database;

import edu.oswego.database.Database;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import edu.oswego.database.Settings;
import edu.oswego.mail.Mailer;

/**
 * USE THIS AS A TEMPLATE
 * 
 * @author nguyen
 */
class EmailAddrTest {

	private Database db;
	private Mailer mailer;

	@BeforeEach
	void setUp() throws Exception {
		Settings.loadCredentials();
		db = new Database(edu.oswego.mail.Settings.EMAIL_ADDRESS, new Mailer(edu.oswego.mail.Settings.EMAIL_ADDRESS, edu.oswego.mail.Settings.EMAIL_PWD));
	}

	@AfterEach
	void tearDown() throws Exception {
		db.truncateTables();
		db.closeConnection();
	}

}
