package edu.oswego.model;

/**
 * EmailAddress object based on SQL table model: email_address
 * 
 * @author Jimmy Nguyen
 * @since 10/23/2019
 *
 */

public class EmailAddress {
	
	private int id;
	private String emailAddress;
	
	public EmailAddress(int id, String emailAddress) {
		this.id = id;
		this.emailAddress = emailAddress;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	@Override
	public String toString() {
		return "EmailAddress [id=" + id + ", emailAddress=" + emailAddress + "]";
	}
	
}
