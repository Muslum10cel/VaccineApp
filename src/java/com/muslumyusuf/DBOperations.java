/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.muslumyusuf;

import initialize.Initialize;
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
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author muslumoncel
 */
public class DBOperations {

	private static Connection connection = null;
	private static PreparedStatement preparedStatement = null;
	private static CallableStatement callableStatement = null;
	private static ResultSet resultSet = null;

	private final Integer[] HEPATIT_B_DATES = {0, 30, 180};
	private final Integer[] BCG_DATES = {60};
	private final Integer[] DaBT_IPA_HIB_DATES = {60, 120, 180, 540};
	private final Integer[] OPA_DATES = {180, 540};
	private final Integer[] KPA_DATES = {60, 120, 180, 360};
	private final Integer[] KKK_DATES = {360};
	private final Integer[] VARICELLA_DATES = {360};
	private final Integer[] HEPATIT_A_DATES = {540, 720};
	private final Integer[] RVA_DATES = {60, 120, 180};

	private static void openConnection() {
		try {
			Class.forName(Initialize.CLASS_NAME);
			connection = (Connection) DriverManager.getConnection(Initialize.DB_URL, Initialize.USERNAME, Initialize.PASSWORD);
		} catch (ClassNotFoundException | SQLException ex) {
			Logger.getLogger(DBOperations.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Returns an integer value. This method returns when user wants to
	 * register. Username, full name and password must be specified.
	 *
	 * @param username Username for registration
	 * @param fullname Name and surname of user
	 * @param password Password of user
	 * @return Registration is successful or failed
	 */
	public synchronized int register(String username, String fullname, String password) {
		int userAvailable = 0, registered = -1;
		try {
			openConnection();
			preparedStatement = connection.prepareStatement(DbFunctions.CHECK_USER_FUNCTION);
			preparedStatement.setString(1, username);
			resultSet = preparedStatement.executeQuery();
			if (resultSet != null) {
				while (resultSet.next()) {
					userAvailable = resultSet.getInt(1);
				}
			}
			if (userAvailable != 1) {
				preparedStatement = connection.prepareStatement(DbFunctions.REGISTER);
				preparedStatement.setString(1, username);
				preparedStatement.setString(2, passToHash(password));
				preparedStatement.setString(3, fullname);
				preparedStatement.setInt(4, Flags.USER_FLAG);
				preparedStatement.executeQuery();
				registered = 1;
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

	/**
	 * Returns an integer value. This method returns when user log-in to
	 * system. Username and password must be specified.
	 *
	 * @param username Username of user
	 * @param password Password of user
	 * @return Log-in successful or failed
	 */
	public synchronized int logIn(String username, String password) {
		int userAvailable = 0;
		try {
			openConnection();
			preparedStatement = connection.prepareStatement(DbFunctions.CHECK_USER_FUNCTION);
			preparedStatement.setString(1, username);
			resultSet = preparedStatement.executeQuery();
			if (resultSet != null) {
				while (resultSet.next()) {
					userAvailable = resultSet.getInt(1);
				}
			}
			if (userAvailable == 1) {
				preparedStatement = connection.prepareStatement(DbFunctions.LOG_IN);
				preparedStatement.setString(1, username);
				preparedStatement.setString(2, passToHash(password));
				resultSet = preparedStatement.executeQuery();
				if (resultSet != null) {
					while (resultSet.next()) {
						return resultSet.getInt(1);
					}
				}
			} else {
				return -2;
			}
		} catch (SQLException ex) {
			Logger.getLogger(DBOperations.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			closeEverything();
		}
		return -1;
	}

	/**
	 * Returns an integer value.
	 *
	 * @param username username of current user
	 * @param baby_name baby name
	 * @param date_of_birth birth date of baby
	 * @return 1 for successfully
	 *
	 */
	public synchronized int addBaby(String username, String baby_name, String date_of_birth) {
		try {
			openConnection();
			callableStatement = connection.prepareCall(DbStoredProcedures.ADD_BABY);
			callableStatement.setString(1, username);
			callableStatement.setString(2, baby_name);
			callableStatement.setString(3, date_of_birth);
			callableStatement.executeQuery();

			callableStatement = connection.prepareCall(DbStoredProcedures.ADD_VACCINES);
			callableStatement.setString(1, calculateBcg(date_of_birth));
			callableStatement.setString(2, calculateVaricella(date_of_birth));
			callableStatement.executeQuery();

			callableStatement = calculateDaBT_IPA_HIB(connection, date_of_birth);
			callableStatement.executeQuery();

			callableStatement = calculateHepatit_A(connection, date_of_birth);
			callableStatement.executeQuery();

			callableStatement = calculateHepatit_B(connection, date_of_birth);
			callableStatement.executeQuery();

			callableStatement = calculateKKK(connection, date_of_birth);
			callableStatement.executeQuery();

			callableStatement = calculateKPA(connection, date_of_birth);
			callableStatement.executeQuery();

			callableStatement = calculateOPA(connection, date_of_birth);
			callableStatement.executeQuery();

			callableStatement = calculateRVA(connection, date_of_birth);
			callableStatement.executeQuery();

			callableStatement = connection.prepareCall(DbStoredProcedures.SET_FALSE_ALL_VACCINES_STATUS);
			callableStatement.executeQuery();

			return 1;
		} catch (SQLException | ParseException ex) {
			Logger.getLogger(DBOperations.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			closeEverything();
		}
		return -1;
	}

	/**
	 * @param baby_id
	 * @param flag
	 * @return *
	 */
	public synchronized int update_DaBT_IPA_HIB(int baby_id, int flag) {
		try {
			openConnection();
			callableStatement = connection.prepareCall(DbStoredProcedures.UPDATE_DaBT_IPA_HIB);
			callableStatement.setInt(1, baby_id);
			if (flag == 1) {
				callableStatement.setInt(2, Flags.ONE_FLAG);
			} else if (flag == 2) {
				callableStatement.setInt(2, Flags.TWO_FLAG);
			} else if (flag == 3) {
				callableStatement.setInt(2, Flags.THREE_FLAG);
			} else if (flag == 4) {
				callableStatement.setInt(2, Flags.FOUR_FLAG);
			} else if (flag == 5) {
				callableStatement.setInt(2, Flags.FIVE_FLAG);
			} else if (flag == 6) {
				callableStatement.setInt(2, Flags.SIX_FLAG);
			} else {
				return 0;
			}
			callableStatement.executeQuery();
			return 1;
		} catch (SQLException ex) {
			Logger.getLogger(DBOperations.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			closeEverything();
		}

		return -1;
	}

	/**
	 * @param baby_id
	 * @param flag
	 * @return
	 */
	public synchronized int update_Hepatit_A(int baby_id, int flag) {
		try {
			openConnection();
			callableStatement = connection.prepareCall(DbStoredProcedures.UPDATE_HEPATIT_A);
			callableStatement.setInt(1, baby_id);
			if (flag == 1) {
				callableStatement.setInt(2, Flags.ONE_FLAG);
			} else if (flag == 2) {
				callableStatement.setInt(2, Flags.TWO_FLAG);
			} else {
				return 0;
			}
			callableStatement.executeQuery();
			return 1;
		} catch (SQLException ex) {
			Logger.getLogger(DBOperations.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			closeEverything();
		}

		return -1;
	}

	/**
	 * @param baby_id
	 * @param flag
	 * @return
	 */
	public synchronized int update_Hepatit_B(int baby_id, int flag) {
		try {
			openConnection();
			callableStatement = connection.prepareCall(DbStoredProcedures.UPDATE_HEPATIT_B);
			callableStatement.setInt(1, baby_id);
			if (flag == 1) {
				callableStatement.setInt(2, Flags.ONE_FLAG);
			} else if (flag == 2) {
				callableStatement.setInt(2, Flags.TWO_FLAG);
			} else if (flag == 3) {
				callableStatement.setInt(2, Flags.THREE_FLAG);
			} else {
				return 0;
			}
			callableStatement.executeQuery();
			return 1;
		} catch (SQLException ex) {
			Logger.getLogger(DBOperations.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			closeEverything();
		}

		return -1;
	}

	/**
	 * @param baby_id
	 * @param flag
	 * @return
	 */
	public synchronized int update_KKK(int baby_id, int flag) {
		try {
			openConnection();
			callableStatement = connection.prepareCall(DbStoredProcedures.UPDATE_KKK);
			callableStatement.setInt(1, baby_id);
			if (flag == 1) {
				callableStatement.setInt(2, Flags.ONE_FLAG);
			} else if (flag == 2) {
				callableStatement.setInt(2, Flags.TWO_FLAG);
			} else {
				return 0;
			}
			callableStatement.executeQuery();
			return 1;
		} catch (SQLException ex) {
			Logger.getLogger(DBOperations.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			closeEverything();
		}

		return -1;
	}

	/**
	 * @param baby_id
	 *
	 * @param flag
	 * @return
	 */
	public synchronized int update_KPA(int baby_id, int flag) {
		try {
			openConnection();
			callableStatement = connection.prepareCall(DbStoredProcedures.UPDATE_KPA);
			callableStatement.setInt(1, baby_id);
			if (flag == 1) {
				callableStatement.setInt(2, Flags.ONE_FLAG);
			} else if (flag == 2) {
				callableStatement.setInt(2, Flags.TWO_FLAG);
			} else if (flag == 3) {
				callableStatement.setInt(2, Flags.THREE_FLAG);
			} else if (flag == 4) {
				callableStatement.setInt(2, Flags.FOUR_FLAG);
			} else {
				return 0;
			}
			callableStatement.executeQuery();
			return 1;
		} catch (SQLException ex) {
			Logger.getLogger(DBOperations.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			closeEverything();
		}

		return -1;
	}

	/**
	 * @param baby_id
	 *
	 * @param flag
	 * @return
	 */
	public synchronized int update_OPA(int baby_id, int flag) {
		try {
			openConnection();
			callableStatement = connection.prepareCall(DbStoredProcedures.UPDATE_OPA);
			callableStatement.setInt(1, baby_id);
			if (flag == 1) {
				callableStatement.setInt(2, Flags.ONE_FLAG);
			} else if (flag == 2) {
				callableStatement.setInt(2, Flags.TWO_FLAG);
			} else {
				return 0;
			}
			callableStatement.executeQuery();
			return 1;
		} catch (SQLException ex) {
			Logger.getLogger(DBOperations.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			closeEverything();
		}

		return -1;
	}

	/**
	 * @param baby_id
	 *
	 * @param flag
	 * @return
	 */
	public synchronized int update_RVA(int baby_id, int flag) {
		try {
			openConnection();
			callableStatement = connection.prepareCall(DbStoredProcedures.UPDATE_RVA);
			callableStatement.setInt(1, baby_id);
			if (flag == 1) {
				callableStatement.setInt(2, Flags.ONE_FLAG);
			} else if (flag == 2) {
				callableStatement.setInt(2, Flags.TWO_FLAG);
			} else if (flag == 3) {
				callableStatement.setInt(2, Flags.THREE_FLAG);
			} else {
				return 0;
			}
			callableStatement.executeQuery();
			return 1;
		} catch (SQLException ex) {
			Logger.getLogger(DBOperations.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			closeEverything();
		}

		return -1;
	}

	/**
	 * @param baby_id
	 *
	 * @param flag
	 * @return
	 */
	public synchronized int update_Vaccines(int baby_id, int flag) {
		try {
			openConnection();
			callableStatement = connection.prepareCall(DbStoredProcedures.UPDATE_VACCINES);
			callableStatement.setInt(1, baby_id);
			if (flag == 1) {
				callableStatement.setInt(2, Flags.ONE_FLAG);
			} else if (flag == 2) {
				callableStatement.setInt(2, Flags.TWO_FLAG);
			} else if (flag == 3) {
				callableStatement.setInt(2, Flags.THREE_FLAG);
			} else if (flag == 4) {
				callableStatement.setInt(2, Flags.FOUR_FLAG);
			} else if (flag == 5) {
				callableStatement.setInt(2, Flags.FIVE_FLAG);
			} else if (flag == 6) {
				callableStatement.setInt(2, Flags.SIX_FLAG);
			} else {
				return 0;
			}
			callableStatement.executeQuery();
			return 1;
		} catch (SQLException ex) {
			Logger.getLogger(DBOperations.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			closeEverything();
		}

		return -1;
	}

	/**
	 * @param username
	 *
	 * @param vaccine_name
	 * @param comment
	 * @return
	 */
	public synchronized int addComment(String username, String vaccine_name, String comment) {
		try {
			openConnection();
			callableStatement = connection.prepareCall(DbStoredProcedures.ADD_COMMENT);
			callableStatement.setString(1, username.trim());
			callableStatement.setString(2, vaccine_name.trim());
			callableStatement.setString(3, comment);
			callableStatement.executeQuery();
			return 1;
		} catch (SQLException ex) {
			Logger.getLogger((DBOperations.class.getName())).log(Level.SEVERE, null, ex);
		} finally {
			closeEverything();
		}
		return -1;
	}

	/**
	 * @param baby_id
	 *
	 * @return
	 */
	public synchronized JSONObject completedAndIncompletedVaccines(int baby_id) {
		JSONObject jSONObject = new JSONObject();
		try {
			openConnection();
			callableStatement = connection.prepareCall(DbStoredProcedures.GET_COMPLETED_VACCINES);
			callableStatement.setInt(1, baby_id);
			resultSet = callableStatement.executeQuery();
			if (resultSet != null) {
				while (resultSet.next()) {
					jSONObject.put(Tags.BCG, resultSet.getInt(1));
					jSONObject.put(Tags.DaBT_IPA, resultSet.getInt(2));
					jSONObject.put(Tags.VARICELLA, resultSet.getInt(3));
					jSONObject.put(Tags.KMA4, resultSet.getInt(4));
					jSONObject.put(Tags.HPA, resultSet.getInt(5));
					jSONObject.put(Tags.INFLUENZA, resultSet.getInt(6));
					jSONObject.put(Tags.FIRST_RVA, resultSet.getInt(7));
					jSONObject.put(Tags.SECOND_RVA, resultSet.getInt(8));
					jSONObject.put(Tags.THIRD_RVA, resultSet.getInt(9));
					jSONObject.put(Tags.FIRST_OPA, resultSet.getInt(10));
					jSONObject.put(Tags.SECOND_OPA, resultSet.getInt(11));
					jSONObject.put(Tags.FIRST_HEPATIT_A, resultSet.getInt(12));
					jSONObject.put(Tags.SECOND_HEPATIT_A, resultSet.getInt(13));
					jSONObject.put(Tags.FIRST_HEPATIT_B, resultSet.getInt(14));
					jSONObject.put(Tags.SECOND_HEPATIT_B, resultSet.getInt(15));
					jSONObject.put(Tags.THIRD_HEPATIT_B, resultSet.getInt(16));
					jSONObject.put(Tags.FIRST_KKK, resultSet.getInt(17));
					jSONObject.put(Tags.SECOND_KKK, resultSet.getInt(18));
					jSONObject.put(Tags.FIRST_KPA, resultSet.getInt(19));
					jSONObject.put(Tags.SECOND_KPA, resultSet.getInt(20));
					jSONObject.put(Tags.THIRD_KPA, resultSet.getInt(21));
					jSONObject.put(Tags.FOURTH_KPA, resultSet.getInt(22));
					jSONObject.put(Tags.FIRST_DaBT_IPA_HIB, resultSet.getInt(23));
					jSONObject.put(Tags.SECOND_DaBT_IPA_HIB, resultSet.getInt(24));
					jSONObject.put(Tags.THIRD_DaBT_IPA_HIB, resultSet.getInt(25));
					jSONObject.put(Tags.FOURTH_DaBT_IPA_HIB, resultSet.getInt(26));
					jSONObject.put(Tags.FIFTH_DaBT_IPA_HIB, resultSet.getInt(27));
					jSONObject.put(Tags.SIXTH_DaBT_IPA_HIB, resultSet.getInt(28));
					return jSONObject;
				}
			}
		} catch (SQLException | JSONException ex) {
			Logger.getLogger(DBOperations.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			closeEverything();
		}
		return null;
	}

	/**
	 * @param username
	 *
	 * @param newPassword
	 * @return
	 */
	public synchronized int forgottenPassword(String username, String newPassword) {
		try {
			int userAvailable = 0;
			openConnection();
			preparedStatement = connection.prepareStatement(DbFunctions.CHECK_USER_FUNCTION);
			preparedStatement.setString(1, username);
			resultSet = preparedStatement.executeQuery();
			if (resultSet != null) {
				while (resultSet.next()) {
					userAvailable = resultSet.getInt(1);
				}
			}
			if (userAvailable == 1) {
				callableStatement = connection.prepareCall(DbStoredProcedures.FORGOTTEN_PASSWORD);
				callableStatement.setString(1, username);
				callableStatement.setString(2, passToHash(newPassword));
				return 1;
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

	/**
	 * @param username
	 *
	 * @param vaccine_name
	 * @param beginning
	 * @param end
	 * @return
	 */
	public synchronized JSONObject getComments(String username, String vaccine_name, int beginning, int end) {
		JSONObject object = new JSONObject();
		JSONArray jSONArray = new JSONArray();
		try {
			openConnection();
			callableStatement = connection.prepareCall(DbStoredProcedures.GET_COMMENTS);
			callableStatement.setString(1, username);
			callableStatement.setString(2, vaccine_name);
			callableStatement.setInt(3, beginning);
			callableStatement.setInt(4, end);
			resultSet = callableStatement.executeQuery();
			if (resultSet != null) {
				while (resultSet.next()) {
					JSONObject jSONObject = new JSONObject();
					jSONObject.put(Tags.USERNAME, resultSet.getString(1));
					jSONObject.put(Tags.VACCINE_NAME, resultSet.getString(2));
					jSONObject.put(Tags.COMMENT, resultSet.getString(3));
					jSONObject.put(Tags.COMMENT_DATE, resultSet.getDate(4));
					jSONArray.put(jSONObject);
				}
			}
			return object.put(Tags.COMMENTS, jSONArray);
		} catch (SQLException | JSONException ex) {
			Logger.getLogger(DBOperations.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			closeEverything();
		}
		return null;
	}

	public synchronized JSONObject getBabies(String username) {
		JSONObject jsonObject = new JSONObject();
		JSONArray jSONArray = new JSONArray();
		try {
			openConnection();
			callableStatement = connection.prepareCall(DbStoredProcedures.GET_BABIES);
			callableStatement.setString(1, username);
			resultSet = callableStatement.executeQuery();
			if (resultSet != null) {
				while (resultSet.next()) {
					JSONObject jSONObject = new JSONObject();
					jSONObject.put(Tags.BABY_ID, resultSet.getInt(1));
					jSONObject.put(Tags.BABY_NAME, resultSet.getString(2));
					jSONArray.put(jSONObject);
				}
				return jsonObject.put(Tags.BABIES, jSONArray);
			}
			return jsonObject.put("fail", "null");
		} catch (SQLException | JSONException ex) {
			Logger.getLogger(DBOperations.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			closeEverything();
		}
		return null;
	}

	/**
	 * *
	 */
	private void closeEverything() {
		try {
			if (connection != null) {
				connection.close();
			}
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

	/**
	 * Returns hashed string
	 *
	 * @return SHA-512 encrypted password
	 */
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

	/**
	 *
	 */
	private String calculateVaricella(String dateTemp) throws ParseException {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = dateFormat.parse(dateFormat.format(dateFormat.parse(dateTemp)));
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DATE, VARICELLA_DATES[0]);
		return dateFormat.format(calendar.getTime());
	}

	/**
	 *
	 */
	private String calculateBcg(String dateTemp) throws ParseException {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = dateFormat.parse(dateFormat.format(dateFormat.parse(dateTemp)));
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DATE, BCG_DATES[0]);
		return dateFormat.format(calendar.getTime());
	}

	/**
	 *
	 */
	private CallableStatement calculateDaBT_IPA_HIB(Connection connection, String date_of_birth) {
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date date = dateFormat.parse(dateFormat.format(dateFormat.parse(date_of_birth)));
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			CallableStatement tempCall = connection.prepareCall(DbStoredProcedures.ADD_DaBT_IPA_HIB);
			for (int i = 0; i < DaBT_IPA_HIB_DATES.length; i++) {
				calendar.add(Calendar.DATE, DaBT_IPA_HIB_DATES[i]);
				tempCall.setString(i + 1, dateFormat.format(calendar.getTime()));
				calendar.setTime(date);
			}
			return tempCall;
		} catch (SQLException | ParseException ex) {
			Logger.getLogger(DBOperations.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	/**
	 *
	 */
	private CallableStatement calculateHepatit_A(Connection connection, String date_of_birth) {
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date date = dateFormat.parse(dateFormat.format(dateFormat.parse(date_of_birth)));
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			CallableStatement tempCall = connection.prepareCall(DbStoredProcedures.ADD_HEPATIT_A_VACCINES);
			for (int i = 0; i < HEPATIT_A_DATES.length; i++) {
				calendar.add(Calendar.DATE, HEPATIT_A_DATES[i]);
				tempCall.setString(i + 1, dateFormat.format(calendar.getTime()));
				calendar.setTime(date);
			}
			return tempCall;
		} catch (SQLException | ParseException ex) {
			Logger.getLogger(DBOperations.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	/**
	 *
	 */
	private CallableStatement calculateHepatit_B(Connection connection, String date_of_birth) {
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date date = dateFormat.parse(dateFormat.format(dateFormat.parse(date_of_birth)));
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			CallableStatement tempCall = connection.prepareCall(DbStoredProcedures.ADD_HEPATIT_B_VACCINES);
			for (int i = 0; i < HEPATIT_B_DATES.length; i++) {
				calendar.add(Calendar.DATE, HEPATIT_B_DATES[i]);
				tempCall.setString(i + 1, dateFormat.format(calendar.getTime()));
				calendar.setTime(date);
			}
			return tempCall;
		} catch (SQLException | ParseException ex) {
			Logger.getLogger(DBOperations.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	/**
	 *
	 */
	private CallableStatement calculateKKK(Connection connection, String date_of_birth) {
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date date = dateFormat.parse(dateFormat.format(dateFormat.parse(date_of_birth)));
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			CallableStatement tempCall = connection.prepareCall(DbStoredProcedures.ADD_KKK_VACCINES);
			for (int i = 0; i < KKK_DATES.length; i++) {
				calendar.add(Calendar.DATE, KKK_DATES[i]);
				tempCall.setString(i + 1, dateFormat.format(calendar.getTime()));
				calendar.setTime(date);
			}
			return tempCall;
		} catch (SQLException | ParseException ex) {
			Logger.getLogger(DBOperations.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	/**
	 *
	 */
	private CallableStatement calculateKPA(Connection connection, String date_of_birth) {
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date date = dateFormat.parse(dateFormat.format(dateFormat.parse(date_of_birth)));
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			CallableStatement tempCall = connection.prepareCall(DbStoredProcedures.ADD_KPA_VACCINES);
			for (int i = 0; i < KPA_DATES.length; i++) {
				calendar.add(Calendar.DATE, KPA_DATES[i]);
				tempCall.setString(i + 1, dateFormat.format(calendar.getTime()));
				calendar.setTime(date);
			}
			return tempCall;
		} catch (SQLException | ParseException ex) {
			Logger.getLogger(DBOperations.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	/**
	 *
	 */
	private CallableStatement calculateOPA(Connection connection, String date_of_birth) {
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date date = dateFormat.parse(dateFormat.format(dateFormat.parse(date_of_birth)));
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			CallableStatement tempCall = connection.prepareCall(DbStoredProcedures.ADD_OPA_VACCINES);
			for (int i = 0; i < OPA_DATES.length; i++) {
				calendar.add(Calendar.DATE, OPA_DATES[i]);
				tempCall.setString(i + 1, dateFormat.format(calendar.getTime()));
				calendar.setTime(date);
			}
			return tempCall;
		} catch (SQLException | ParseException ex) {
			Logger.getLogger(DBOperations.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	/**
	 *
	 */
	private CallableStatement calculateRVA(Connection connection, String date_of_birth) {
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date date = dateFormat.parse(dateFormat.format(dateFormat.parse(date_of_birth)));
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			CallableStatement tempCall = connection.prepareCall(DbStoredProcedures.ADD_RVA_VACCINES);
			for (int i = 0; i < RVA_DATES.length; i++) {
				calendar.add(Calendar.DATE, RVA_DATES[i]);
				tempCall.setString(i + 1, dateFormat.format(calendar.getTime()));
				calendar.setTime(date);
			}
			return tempCall;
		} catch (SQLException | ParseException ex) {
			Logger.getLogger(DBOperations.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}
}
