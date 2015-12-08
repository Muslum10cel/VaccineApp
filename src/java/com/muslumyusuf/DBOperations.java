/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.muslumyusuf;

import com.mysql.jdbc.Connection;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.CallableStatement;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author muslumoncel
 */
public class DBOperations {

	private static Connection connection = null;
	private static PreparedStatement preparedStatement = null;
	private static CallableStatement callableStatement = null;
	private static ResultSet resultSet = null;

	private static final Integer[] HEPATIT_B_DATES = {0, 30, 180};
	private static final Integer[] BCG_DATES = {60};
	private static final Integer[] DaBT_IPA_HIB_DATES = {60, 120, 180, 540};
	private static final Integer[] OPA_DATES = {180, 540};
	private static final Integer[] KPA_DATES = {60, 120, 180, 360};
	private static final Integer[] KKK_DATES = {360};
	private static final Integer[] VARICELLA_DATES = {360};
	private static final Integer[] HEPATIT_A_DATES = {540, 720};
	private static final Integer[] RVA_DATES = {60, 120, 180};

	private static void openConnection() {
		try {
			Class.forName(Initialize.CLASS_NAME);
			connection = (Connection) DriverManager.getConnection(Initialize.DB_URL, Initialize.USERNAME, Initialize.PASSWORD);
		} catch (ClassNotFoundException | SQLException ex) {
			Logger.getLogger(DBOperations.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/*
	 In register method we register user and check registration successful or not.
	 First of all, we have encrypt password to SHA-256.Callable statement is used for consuming stored procedures.
	 For registration, we use a stored procedure called "Register" with callableStatement. Then we set our data to callableStatement.
	 To check registration, we use 0,-1 and 1. If system fails, return -1, if username is available return 1, if registration is successfull
	 return 0.
	 */
	public synchronized int register(String username, String password) {
		int userAvailable = 0, registered = -1;
		try {
			openConnection();
			preparedStatement = connection.prepareCall(DbFunctions.CHECK_USER_FUNCTION);
			preparedStatement.setString(1, username);
			resultSet = preparedStatement.executeQuery();
			if (resultSet != null) {
				while (resultSet.next()) {
					userAvailable = resultSet.getInt(1);
				}
			}
			if (userAvailable != 1) {
				preparedStatement = connection.prepareCall(DbFunctions.REGISTER);
				preparedStatement.setString(1, username);
				preparedStatement.setString(2, passToHash(password));
				preparedStatement.setInt(3, Flags.USER_FLAG);
				preparedStatement.executeQuery();
				registered = 0;
			} else {
				return userAvailable;
			}
		} catch (SQLException ex) {
			Logger.getLogger(DBOperations.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			closeEverything();
		}
		return registered;
	}

	/*
	 In logIn method we take username and password of user as parameter.
	 First of all, We consume check user function to check user is available in database or not.
	 If user is already in database, method will return 1.
	 If not, we invoke log in function with username and password and if password is ok wil return 0.
	 If there is an exception it will return -1.
	 */
	public synchronized int logIn(String username, String password) {
		int userAvailable = 0;
		try {
			openConnection();
			preparedStatement = connection.prepareCall(DbFunctions.CHECK_USER_FUNCTION);
			preparedStatement.setString(1, username);
			resultSet = preparedStatement.executeQuery();
			if (resultSet != null) {
				while (resultSet.next()) {
					userAvailable = resultSet.getInt(1);
				}
			}
			if (userAvailable == 1) {
				preparedStatement = connection.prepareCall(DbFunctions.LOG_IN);
				preparedStatement.setString(1, username);
				preparedStatement.setString(2, passToHash(password));
				preparedStatement.executeQuery();
				return 0;
			} else {
				return userAvailable;
			}
		} catch (SQLException ex) {
			Logger.getLogger(DBOperations.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			closeEverything();
		}
		return -1;
	}

	public synchronized int addBaby(String username, String baby_name, Date date_of_birth) {
		List<Date> dates = new ArrayList<>();
		try {
			openConnection();
			callableStatement = connection.prepareCall(DbStoredProcedures.ADD_BABY);
			callableStatement.setString(1, username);
			callableStatement.setString(2, baby_name);
			callableStatement.setDate(3, (java.sql.Date) date_of_birth);
			callableStatement.executeQuery();

			Date date = new Date();
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);

			for (Integer HEPATIT_B_DATES1 : HEPATIT_B_DATES) {
				calendar.setTime(date);
				calendar.add(Calendar.DATE, HEPATIT_B_DATES1);
				dates.add(dateFormat.parse(dateFormat.format(calendar.getTime())));
			}

			callableStatement = connection.prepareCall(DbStoredProcedures.ADD_HEPATIT_B_VACCINES);
			callableStatement.setString(1, baby_name);
			for (int i = 0; i < dates.size(); i++) {
				callableStatement.setDate(i + 2, (java.sql.Date) dates.get(i));
			}
			callableStatement.executeQuery();
			dates.clear();
			calendar.setTime(date);

			for (Integer BCG_DATES1 : BCG_DATES) {
				calendar.setTime(date);
				calendar.add(Calendar.DATE, BCG_DATES1);
				dates.add(dateFormat.parse(dateFormat.format(calendar.getTime())));
			}
			
			
			
			calendar.setTime(date);

			for (Integer DaBT_IPA_HIB_DATES1 : DaBT_IPA_HIB_DATES) {
				calendar.setTime(date);
				calendar.add(Calendar.DATE, DaBT_IPA_HIB_DATES1);
				dates.add(dateFormat.parse(dateFormat.format(calendar.getTime())));
			}
			calendar.setTime(date);

			for (Integer OPA_DATES1 : OPA_DATES) {
				calendar.setTime(date);
				calendar.add(Calendar.DATE, OPA_DATES1);
				dates.add(dateFormat.parse(dateFormat.format(calendar.getTime())));
			}
			calendar.setTime(date);

			for (Integer KPA_DATES1 : KPA_DATES) {
				calendar.setTime(date);
				calendar.add(Calendar.DATE, KPA_DATES1);
				dates.add(dateFormat.parse(dateFormat.format(calendar.getTime())));
			}
			calendar.setTime(date);

			for (Integer KKK_DATES1 : KKK_DATES) {
				calendar.setTime(date);
				calendar.add(Calendar.DATE, KKK_DATES1);
				dates.add(dateFormat.parse(dateFormat.format(calendar.getTime())));
			}
			calendar.setTime(date);

			for (Integer VARICELLA_DATES1 : VARICELLA_DATES) {
				calendar.setTime(date);
				calendar.add(Calendar.DATE, VARICELLA_DATES1);
				dates.add(dateFormat.parse(dateFormat.format(calendar.getTime())));
			}
			calendar.setTime(date);

			for (Integer HEPATIT_A_DATES1 : HEPATIT_A_DATES) {
				calendar.setTime(date);
				calendar.add(Calendar.DATE, HEPATIT_A_DATES1);
				dates.add(dateFormat.parse(dateFormat.format(calendar.getTime())));
			}
			calendar.setTime(date);

			for (Integer RVA_DATES1 : RVA_DATES) {
				calendar.setTime(date);
				calendar.add(Calendar.DATE, RVA_DATES1);
				dates.add(dateFormat.parse(dateFormat.format(calendar.getTime())));
			}
		} catch (SQLException | ParseException ex) {
			Logger.getLogger(DBOperations.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			closeEverything();
		}
		return -1;
	}

	private void closeEverything() {
		try {
			connection.close();
			if (resultSet != null) {
				resultSet.close();
			}
			if (preparedStatement != null) {
				preparedStatement.close();
			}
			if (callableStatement != null) {
				callableStatement.close();
			}
		} catch (SQLException ex) {
			Logger.getLogger(DBOperations.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private String passToHash(String password) {
		String hashedPassword = "";
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-512");
			md.update(password.getBytes("UTF-8"));
			byte[] digest = md.digest();
			hashedPassword = String.format("%064x", new java.math.BigInteger(1, digest));
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
			Logger.getLogger(DBOperations.class.getName()).log(Level.SEVERE, null, ex);
		}
		return hashedPassword;
	}
}
