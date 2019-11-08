package edu.oswego.websocket;

import java.io.IOException;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.oswego.database.Database;
import edu.oswego.mail.Mailer;
import edu.oswego.model.UserFavourites;
import edu.oswego.model.UserFolder;
import edu.oswego.props.Interval;
import edu.oswego.runnables.Handler;
import edu.oswego.runnables.ValidationRunnable;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

/**
 * Class handles all communication between gui and engine
 */
@ServerEndpoint("/engine")
public class Websocket {
	// this is to manage all current/last active threads for each unique sessions
	private static ConcurrentHashMap<String, StorageObject> sessionMapper = new ConcurrentHashMap<>();
	Messenger messenger = new Messenger();
	JSDecryptor jse = null;

	/**
	 * standard inclusive method that comes with websockets
	 * 
	 * @param session
	 */
	@OnOpen
	public void onOpen(Session session) {
		System.out.println("Session " + session.getId() + " been established");
		try {
			jse = new JSDecryptor();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}
		messenger.sendPublicKey(session, jse.getPublic());
	}

	// This method allows you to message a specific user.
	/*
	 * session.getBasicRemote().sendText(message);
	 */
	@OnMessage // method that communicates with clients
	public void onMessage(String message, Session session) {
		Thread.currentThread().setName("regular");
		JsonObject jsonMessage = new JsonParser().parse(message).getAsJsonObject();

		if (jsonMessage == null) {
			try {
				session.getBasicRemote().sendText("invalid_jsonObject: you've been disconnected");
				session.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		String messageType = jsonMessage.get("messagetype").getAsString();
		String decryptedEmail = null;
		try {
			String encryptedEmail = jsonMessage.get("email").getAsString();
			decryptedEmail = jse.decrypt(encryptedEmail);
		} catch (BadPaddingException e) {
			e.printStackTrace();
			messenger.sendErrorMessage(session,"failed to decrypt email");
			return;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			messenger.sendErrorMessage(session,"failed to decrypt email");
			return;
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
			messenger.sendErrorMessage(session,"failed to decrypt email");
			return;
		} catch (Exception e) {
			e.printStackTrace();
			messenger.sendErrorMessage(session,"failed to decrypt email");
			return;
		}
		StorageObject storageObject = sessionMapper.get(decryptedEmail);

		if (messageType.equals("filter")) {
			filter(session, storageObject.getDatabase(), decryptedEmail, jsonMessage.get("filter").getAsJsonObject());
		} else if (messageType.equals("login")) {
			String encryptedPass = jsonMessage.get("pass").getAsString();
			String decryptedPass = null;
			try {
				decryptedPass = jse.decrypt(encryptedPass);
			} catch (BadPaddingException e) {
				e.printStackTrace();
				messenger.sendErrorMessage(session,"failed to decrypt pass");
				return;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				messenger.sendErrorMessage(session,"failed to decrypt pass");
				return;
			} catch (IllegalBlockSizeException e) {
				e.printStackTrace();
				messenger.sendErrorMessage(session,"failed to decrypt pass");
				return;
			} catch(Exception e){
				e.printStackTrace();
				messenger.sendErrorMessage(session,"failed to decrypt pass");
				return;
			}
			login(session, decryptedEmail, decryptedPass, storageObject);
		} else if (messageType.equals("refresh")) {
			refresh(storageObject, decryptedEmail, storageObject.getMailer(), storageObject.getDatabase(), true,
					session);
		} else if (messageType.equals("addfavorite")) {
			addFavorite(session, storageObject.getDatabase(), jsonMessage.get("favoritename").getAsString(),
					jsonMessage.get("filter").getAsJsonObject());
		} else if (messageType.equals("callfavorite")) {
			callFavorite(session, storageObject.getDatabase(), jsonMessage.get("favoritename").getAsString(),
					decryptedEmail);
		} else if (messageType.equals("removefavorite")) {
			removeFavorite(session, storageObject.getDatabase(), jsonMessage.get("favoritename").getAsString());
		} else if (messageType.equals("logout")) {
			logout(session, decryptedEmail);
		} else {
			messenger.sendUpdateStatusMessage(session, "invalid messagetype\n" + "please send one of these options:\n"
					+ "login, filter, refresh, addfavorite, callfavorite, removefavorite or logout");

		}

	}

	/**
	 * Standard method that comes with websockets to close a connection
	 * 
	 * @param session
	 */
	@OnClose
	public void onClose(Session session) {
		System.out.println("session closed");
	}

	/**
	 * standard method that comes with websockets to handle errors
	 * 
	 * @param t
	 * @param session
	 */
	@OnError
	public void onError(Throwable t, Session session) {
		System.out.println("onError::");
		t.printStackTrace();
	}

	/*
	 * all private functional methods are below directly below are key methods for
	 * message digestion.
	 */

	private void login(Session session, String email, String pass, StorageObject storageObject) {
		// String pass = jsonMessage.get("pass").getAsString();

		Mailer mailer;
		Database database;

		if (storageObject == null) {
			storageObject = new StorageObject();
			mailer = new Mailer(email, pass);

			messenger.sendUpdateStatusMessage(session, "establising connection");

			boolean connectedToDatabase = mailer.isConnected();
			if (!connectedToDatabase) {
				messenger.sendUpdateStatusMessage(session, "failed to connect to email");
				return;
			}

			try {
				database = new Database(email, mailer);
			} catch (Throwable t) {
				messenger.sendErrorMessage(session, "error in db: " + t.getMessage());
				return;
			}
			messenger.sendUpdateStatusMessage(session, "established connection");

			storageObject.setDatabase(database);
			storageObject.setMailer(mailer);
			sessionMapper.put(email, storageObject);
		} else {
			mailer = storageObject.getMailer();
			database = storageObject.getDatabase();
			messenger.sendUpdateStatusMessage(session, "established connection");
		}

		boolean hasEmails;
		hasEmails = database.hasEmails();

		JsonObject js = new JsonObject();

		if (hasEmails) {
			List<UserFolder> folders;
			List<UserFavourites> favourites;
			try {
				folders = database.importFolders();
				favourites = database.getUserFavourites();
			} catch (Throwable t) {
				messenger.sendErrorMessage(session, "error in db: " + t.getMessage());
				return;
			}
			refresh(storageObject, email, mailer, database, true, session);

			js.addProperty("messagetype", "logininfo");
			JsonArray ja1 = new JsonArray();
			JsonArray ja2 = new JsonArray();
			for (int i = 0; i < folders.size(); i++) {
				ja1.add(folders.get(i).getFolder().getFullName());
			}
			for (int i = 0; i < favourites.size(); i++) {
				ja2.add(favourites.get(i).getName());
			}
			js.add("foldername", ja1);
			js.add("favoritename", ja2);
			messenger.sendMessageToClient(session, js);
		} else {
			messenger.sendUpdateStatusMessage(session, "nothing found in database, preforming fresh import");
			refresh(storageObject, email, mailer, database, false, session);
		}

	}

	// TODO Still needs testing
	private void filter(Session session, Database database, String email, JsonObject filter) {
		Handler handler = new Handler(session, database, email, filter);
		Thread thread = new Thread(handler);
		thread.start();
	}

	// TODO Still needs testing
	private void callFavorite(Session session, Database database, String favname, String email) {
		UserFavourites userFavourites;
		userFavourites = database.getUserFavourite(favname);
		Handler handler = new Handler(session, database, email, userFavourites);
		Thread thread = new Thread(handler);
		thread.start();
	}

	/**
	 * If a validationthread is not happening then refresh is called to make it so.
	 * 
	 * @param storageObject
	 * @param email
	 * @param mailer
	 * @param database
	 * @param validateOrPull
	 * @param session
	 */
	private void refresh(StorageObject storageObject, String email, Mailer mailer, Database database,
			boolean validateOrPull, Session session) {
		if (storageObject != null && storageObject.getValidationThread() != null
				&& storageObject.getValidationThread().isAlive()) {
			JsonObject js = new JsonObject();
			js.addProperty("messagetype", "statusupdate");
			js.addProperty("message", "validation already occuring");
			messenger.sendMessageToClient(session, js);
		}
		ValidationRunnable vr = new ValidationRunnable(mailer, database, validateOrPull, session);
		Thread thread = new Thread(vr);
		thread.start();
		storageObject.setValidationRunnable(vr);
		storageObject.setValidationThread(thread);
		sessionMapper.put(email, storageObject);
	}

	/**
	 * This method is the add a new favorite to database and return gui the new list
	 * of favorites
	 * 
	 * @param session
	 * @param database
	 * @param favoriteName
	 * @param filter
	 */
	private void addFavorite(Session session, Database database, String favoriteName, JsonObject filter) {
		String foldername = filter.get("foldername").getAsString();
		String sd = filter.get("date").getAsString();
		String interval = filter.get("interval").getAsString();

		DateTime startDate = getStartDate(sd);
		DateTime endDate = getEndDate(startDate, interval);
		if (startDate == null || endDate == null) {
			JsonObject js = new JsonObject();
			js.addProperty("messagetype", "statusupdate");
			js.addProperty("message", "invalid dateTime");
			messenger.sendMessageToClient(session, js);
			return;
		}

		boolean attachment = filter.get("attachment").getAsBoolean();
		boolean seen = filter.get("seen").getAsBoolean();
		boolean added;
		try {
			added = database.insertUserFavourites(favoriteName, startDate.toDate(), endDate.toDate(),
					Interval.parse(interval), attachment, seen, foldername);
		} catch (Throwable t) {
			messenger.sendErrorMessage(session, "error in db: " + t.getMessage());
			return;
		}
		if (added) {
			messenger.sendUpdateStatusMessage(session, "Favorite has been added");
		} else {
			messenger.sendUpdateStatusMessage(session, "No FolderName");
		}

		List<UserFavourites> favourites;
		try {
			favourites = database.getUserFavourites();
		} catch (Throwable t) {
			messenger.sendErrorMessage(session, "error in db: " + t.getMessage());
			return;
		}

		JsonArray ja = new JsonArray();
		for (int i = 0; i < favourites.size(); i++) {
			ja.add(favourites.get(i).getName());
		}

		JsonObject js = new JsonObject();
		js.addProperty("messagetype", "favoritename");
		js.add("favoritename", ja);
		messenger.sendMessageToClient(session, js);
	}

	/**
	 * method removes favorite from database and sends gui the new list of favorites
	 * 
	 * @param session
	 * @param database
	 * @param favoriteName
	 */
	private void removeFavorite(Session session, Database database, String favoriteName) {
		// dont return from these catches as they need UserFavourites
		try {
			database.removeUserFavourite(favoriteName);
		} catch (Throwable t) {
			messenger.sendErrorMessage(session, "error in db: " + t.getMessage());
			return;
		}
		messenger.sendUpdateStatusMessage(session, "Favorite has been removed");

		List<UserFavourites> favourites;
		favourites = database.getUserFavourites();

		JsonArray ja = new JsonArray();
		for (int i = 0; i < favourites.size(); i++) {
			ja.add(favourites.get(i).getName());
		}
		JsonObject js = new JsonObject();
		js.addProperty("messagetype", "favoritename");
		js.add("favoritename", ja);
		messenger.sendMessageToClient(session, js);
	}

	/**
	 * method to remove user instance from memory
	 * 
	 * @param session
	 * @param email
	 */
	private void logout(Session session, String email) {
		sessionMapper.remove(email);
		try {
			session.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * below are support methods
	 */

	/**
	 * Method to obtain end date
	 * 
	 * @param startDate
	 * @param interval
	 * @return DateTime
	 */
	private DateTime getEndDate(DateTime startDate, String interval) {
		if (startDate == null) {
			return null;
		}
		if (interval.equals("year")) {
			return startDate.plusYears(1);
		} else if (interval.equals("month")) {
			return startDate.plusMonths(1);
		} else {// week
			return startDate.plusWeeks(1);
		}
	}

	/**
	 * takes a string and attempts to convert it to a jodaDateTime
	 * 
	 * @param sd
	 * @return DateTime
	 */
	private DateTime getStartDate(String sd) {
		try {
			return new DateTime(DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss").parseMillis(sd));
		} catch (IllegalArgumentException iae) {
			return null;
		}
	}

}
