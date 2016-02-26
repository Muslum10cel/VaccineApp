/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.muslumyusuf;

import com.initialize.GenerateVerificationCode;
import com.initialize.Initialize;
import com.mysql.jdbc.Connection;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author muslumoncel
 * @version 1.0
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
    private final SendMail sendMail = new SendMail();

    /**
     * Establish connection to database
     */
    private static void establishConnection() {
        try {
            Class.forName(Initialize.getCLASS_NAME());
            connection = (Connection) DriverManager.getConnection(Initialize.getDB_URL(), Initialize.getUSERNAME(), Initialize.getPASSWORD());
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
     * @param e_mail
     * @return Registration is successful or failed
     */
    public synchronized int register(String username, String fullname, String password, String e_mail) {
        int userAvailable = 0, registered = -1;
        try {
            establishConnection();
            preparedStatement = connection.prepareStatement(DbFunctions.CHECK_USER_FUNCTION);
            preparedStatement.setString(1, username);
            resultSet = preparedStatement.executeQuery();
            if (!Objects.equals(resultSet, null)) {
                while (resultSet.next()) {
                    userAvailable = resultSet.getInt(1);
                }
            }
            //!Objects.equals(userAvailable,1)
            if (!Objects.equals(userAvailable, 1)) {
                preparedStatement = connection.prepareStatement(DbFunctions.REGISTER);
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, passToHash(password));
                preparedStatement.setString(3, fullname);
                preparedStatement.setInt(4, Flags.USER_FLAG);
                preparedStatement.setString(5, e_mail);
                preparedStatement.executeQuery();
                registered = 2;
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
     * Returns an integer value. This method returns when user log-in to system.
     * Username and password must be specified.
     *
     * @param username Username of user
     * @param password Password of user
     * @return Log-in successful or failed
     */
    public synchronized int logIn(String username, String password) {
        int userAvailable = 0;
        try {
            establishConnection();
            preparedStatement = connection.prepareStatement(DbFunctions.CHECK_USER_FUNCTION);
            preparedStatement.setString(1, username);
            resultSet = preparedStatement.executeQuery();
            if (!Objects.equals(resultSet, null)) {
                while (resultSet.next()) {
                    userAvailable = resultSet.getInt(1);
                }
            }
            if (Objects.equals(userAvailable, 1)) {
                preparedStatement = connection.prepareStatement(DbFunctions.LOG_IN);
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, passToHash(password));
                resultSet = preparedStatement.executeQuery();
                if (!Objects.equals(resultSet, null)) {
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
            establishConnection();
            callableStatement = connection.prepareCall(DbStoredProcedures.ADD_BABY);
            callableStatement.setString(1, username);
            callableStatement.setString(2, baby_name);
            callableStatement.setString(3, date_of_birth);
            callableStatement.executeUpdate();

            callableStatement = connection.prepareCall(DbStoredProcedures.ADD_VACCINES);
            callableStatement.setString(1, calculateBcg(date_of_birth));
            callableStatement.setString(2, calculateVaricella(date_of_birth));
            callableStatement.executeUpdate();

            callableStatement = calculateDaBT_IPA_HIB(connection, date_of_birth);
            callableStatement.executeUpdate();

            callableStatement = calculateHepatit_A(connection, date_of_birth);
            callableStatement.executeUpdate();

            callableStatement = calculateHepatit_B(connection, date_of_birth);
            callableStatement.executeUpdate();

            callableStatement = calculateKKK(connection, date_of_birth);
            callableStatement.executeUpdate();

            callableStatement = calculateKPA(connection, date_of_birth);
            callableStatement.executeUpdate();

            callableStatement = calculateOPA(connection, date_of_birth);
            callableStatement.executeUpdate();

            callableStatement = calculateRVA(connection, date_of_birth);
            callableStatement.executeUpdate();

            callableStatement = connection.prepareCall(DbStoredProcedures.SET_FALSE_ALL_VACCINES_STATUS);
            return callableStatement.executeUpdate();
        } catch (SQLException | ParseException ex) {
            Logger.getLogger(DBOperations.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            closeEverything();
        }
        return -1;
    }

    /**
     * Updates DABT IPA HIB vaccine of baby
     *
     * @param baby_id id of baby
     * @param flag flag of vaccine
     * @return 1 updated, 0 not updated, -2 flag is not correct, -1 if catches
     * SQLException
     */
    public synchronized int update_DaBT_IPA_HIB(int baby_id, int flag) {
        try {
            establishConnection();
            callableStatement = connection.prepareCall(DbStoredProcedures.UPDATE_DaBT_IPA_HIB);
            callableStatement.setInt(1, baby_id);
            switch (flag) {
                case 1:
                    callableStatement.setInt(2, Flags.ONE_FLAG);
                    break;
                case 2:
                    callableStatement.setInt(2, Flags.TWO_FLAG);
                    break;
                case 3:
                    callableStatement.setInt(2, Flags.THREE_FLAG);
                    break;
                case 4:
                    callableStatement.setInt(2, Flags.FOUR_FLAG);
                    break;
                case 5:
                    callableStatement.setInt(2, Flags.FIVE_FLAG);
                    break;
                case 6:
                    callableStatement.setInt(2, Flags.SIX_FLAG);
                    break;
                default:
                    return -2;
            }
            return callableStatement.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(DBOperations.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            closeEverything();
        }

        return -1;
    }

    /**
     * Updates HEPATITIS A vaccine of baby
     *
     * @param baby_id id of baby
     * @param flag flag of vaccine
     * @return 1 updated, 0 not updated, -2 flag is not correct, -1 if catches
     * SQLException
     */
    public synchronized int update_Hepatit_A(int baby_id, int flag) {
        try {
            establishConnection();
            callableStatement = connection.prepareCall(DbStoredProcedures.UPDATE_HEPATIT_A);
            callableStatement.setInt(1, baby_id);
            switch (flag) {
                case 1:
                    callableStatement.setInt(2, Flags.ONE_FLAG);
                    break;
                case 2:
                    callableStatement.setInt(2, Flags.TWO_FLAG);
                    break;
                default:
                    return -2;
            }
            return callableStatement.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(DBOperations.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            closeEverything();
        }
        return -1;
    }

    /**
     * Updates HEPATITIS B vaccine of baby
     *
     * @param baby_id id of baby
     * @param flag flag of vaccine
     * @return 1 updated, 0 not updated, -2 flag is not correct, -1 if catches
     * SQLException
     */
    public synchronized int update_Hepatit_B(int baby_id, int flag) {
        try {
            establishConnection();
            callableStatement = connection.prepareCall(DbStoredProcedures.UPDATE_HEPATIT_B);
            callableStatement.setInt(1, baby_id);
            switch (flag) {
                case 1:
                    callableStatement.setInt(2, Flags.ONE_FLAG);
                    break;
                case 2:
                    callableStatement.setInt(2, Flags.TWO_FLAG);
                    break;
                case 3:
                    callableStatement.setInt(2, Flags.THREE_FLAG);
                    break;
                default:
                    return -2;
            }
            return callableStatement.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(DBOperations.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            closeEverything();
        }
        return -1;
    }

    /**
     * Updates KKK vaccine of baby
     *
     * @param baby_id id of baby
     * @param flag flag of vaccine
     * @return 1 updated, 0 not updated, -2 flag is not correct, -1 if catches
     * SQLException
     */
    public synchronized int update_KKK(int baby_id, int flag) {
        try {
            establishConnection();
            callableStatement = connection.prepareCall(DbStoredProcedures.UPDATE_KKK);
            callableStatement.setInt(1, baby_id);
            switch (flag) {
                case 1:
                    callableStatement.setInt(2, Flags.ONE_FLAG);
                    break;
                case 2:
                    callableStatement.setInt(2, Flags.TWO_FLAG);
                    break;
                default:
                    return -2;
            }
            return callableStatement.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(DBOperations.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            closeEverything();
        }
        return -1;
    }

    /**
     * Updates KPA vaccine of baby
     *
     * @param baby_id id of baby
     * @param flag flag of vaccine
     * @return 1 updated, 0 not updated, -2 flag is not correct, -1 if catches
     * SQLException
     */
    public synchronized int update_KPA(int baby_id, int flag) {
        try {
            establishConnection();
            callableStatement = connection.prepareCall(DbStoredProcedures.UPDATE_KPA);
            callableStatement.setInt(1, baby_id);
            switch (flag) {
                case 1:
                    callableStatement.setInt(2, Flags.ONE_FLAG);
                    break;
                case 2:
                    callableStatement.setInt(2, Flags.TWO_FLAG);
                    break;
                case 3:
                    callableStatement.setInt(2, Flags.THREE_FLAG);
                    break;
                case 4:
                    callableStatement.setInt(2, Flags.FOUR_FLAG);
                    break;
                default:
                    return -2;
            }
            return callableStatement.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(DBOperations.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            closeEverything();
        }
        return -1;
    }

    /**
     * Updates OPA vaccine of baby
     *
     * @param baby_id id of baby
     * @param flag flag of vaccine
     * @return 1 updated, 0 not updated, -2 flag is not correct, -1 if catches
     * SQLException
     */
    public synchronized int update_OPA(int baby_id, int flag) {
        try {
            establishConnection();
            callableStatement = connection.prepareCall(DbStoredProcedures.UPDATE_OPA);
            callableStatement.setInt(1, baby_id);
            switch (flag) {
                case 1:
                    callableStatement.setInt(2, Flags.ONE_FLAG);
                    break;
                case 2:
                    callableStatement.setInt(2, Flags.TWO_FLAG);
                    break;
                default:
                    return -2;
            }
            return callableStatement.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(DBOperations.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            closeEverything();
        }
        return -1;
    }

    /**
     * Updates RVA vaccine of baby
     *
     * @param baby_id id of baby
     * @param flag flag of vaccine
     * @return 1 updated, 0 not updated, -2 flag is not correct, -1 if catches
     * SQLException
     */
    public synchronized int update_RVA(int baby_id, int flag) {
        try {
            establishConnection();
            callableStatement = connection.prepareCall(DbStoredProcedures.UPDATE_RVA);
            callableStatement.setInt(1, baby_id);
            switch (flag) {
                case 1:
                    callableStatement.setInt(2, Flags.ONE_FLAG);
                    break;
                case 2:
                    callableStatement.setInt(2, Flags.TWO_FLAG);
                    break;
                case 3:
                    callableStatement.setInt(2, Flags.THREE_FLAG);
                    break;
                default:
                    return -2;
            }
            return callableStatement.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(DBOperations.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            closeEverything();
        }
        return -1;
    }

    /**
     * Updates baby vaccine such as BCG, VARICELLA
     *
     * @param baby_id id of baby
     * @param flag flag of related vaccine
     * @return 1 updated, 0 not updated, -2 flag is not correct, -1 if catches
     * SQLException
     */
    public synchronized int update_Vaccines(int baby_id, int flag) {
        try {
            establishConnection();
            callableStatement = connection.prepareCall(DbStoredProcedures.UPDATE_VACCINES);
            callableStatement.setInt(1, baby_id);
            switch (flag) {
                case 1:
                    callableStatement.setInt(2, Flags.ONE_FLAG);
                    break;
                case 2:
                    callableStatement.setInt(2, Flags.TWO_FLAG);
                    break;
                case 3:
                    callableStatement.setInt(2, Flags.THREE_FLAG);
                    break;
                case 4:
                    callableStatement.setInt(2, Flags.FOUR_FLAG);
                    break;
                case 5:
                    callableStatement.setInt(2, Flags.FIVE_FLAG);
                    break;
                case 6:
                    callableStatement.setInt(2, Flags.SIX_FLAG);
                    break;
                default:
                    return -2;
            }
            return callableStatement.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(DBOperations.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            closeEverything();
        }
        return -1;
    }

    /**
     * Adds comment written by user
     *
     * @param username of wrote comment
     * @param vaccine_name commented vaccine name
     * @param comment written comment
     * @return 1 updated, 0 not updated, -1 if catches SQLException
     */
    public synchronized int addComment(String username, String vaccine_name, String comment) {
        try {
            establishConnection();
            callableStatement = connection.prepareCall(DbStoredProcedures.ADD_COMMENT);
            callableStatement.setString(1, username.trim());
            callableStatement.setString(2, vaccine_name.trim());
            callableStatement.setString(3, comment);
            return callableStatement.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger((DBOperations.class.getName())).log(Level.SEVERE, null, ex);
        } finally {
            closeEverything();
        }
        return -1;
    }

    /**
     * Gets vaccine completion details of desired baby
     *
     * @param baby_id of desired baby
     * @return an object includes completion details of baby or null if catches
     * SQLException or JSONException
     */
    public synchronized JSONObject completedAndIncompletedVaccines(int baby_id) {
        JSONObject jSONObject = new JSONObject();
        try {
            establishConnection();
            callableStatement = connection.prepareCall(DbStoredProcedures.GET_COMPLETED_VACCINES);
            callableStatement.setInt(1, baby_id);
            resultSet = callableStatement.executeQuery();
            if (!Objects.equals(resultSet, null)) {
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
     * Updates password of user
     *
     * @param username wants to update password
     * @param newPassword of user
     * @return 1 updated, 0 not updated, -1 if catches SQLException, -2 if user
     * not available
     */
    public synchronized int forgottenPassword(String username, String newPassword) {
        try {
            int userAvailable = -2;
            establishConnection();
            preparedStatement = connection.prepareStatement(DbFunctions.CHECK_USER_FUNCTION);
            preparedStatement.setString(1, username);
            resultSet = preparedStatement.executeQuery();
            if (!Objects.equals(resultSet, null)) {
                while (resultSet.next()) {
                    userAvailable = resultSet.getInt(1);
                }
            }
            if (Objects.equals(userAvailable, 1)) {
                callableStatement = connection.prepareCall(DbStoredProcedures.FORGOTTEN_PASSWORD);
                callableStatement.setString(1, username);
                callableStatement.setString(2, passToHash(newPassword));
                return callableStatement.executeUpdate();
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
     * Gets comments written by users
     *
     * @param vaccine_name
     * @param beginning first index of comments
     * @param end last index of comments
     * @return an object includes comments or null if catches SQLException or
     * JSONException
     */
    public synchronized JSONObject getComments(String vaccine_name, int beginning, int end) {
        JSONObject object = new JSONObject();
        JSONArray jSONArray = new JSONArray();
        try {
            establishConnection();
            callableStatement = connection.prepareCall(DbStoredProcedures.GET_COMMENTS);
            callableStatement.setString(1, vaccine_name);
            callableStatement.setInt(2, beginning);
            callableStatement.setInt(3, end);
            resultSet = callableStatement.executeQuery();
            if (!Objects.equals(resultSet, null)) {
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

    /**
     * Gets babies of logged in user
     *
     * @param username of logged in
     * @return an object includes baby names of null if catches SQLException or
     * JSONException
     */
    public synchronized JSONObject getBabies(String username) {
        JSONObject jsonObject = new JSONObject();
        JSONArray jSONArray = new JSONArray();
        try {
            establishConnection();
            callableStatement = connection.prepareCall(DbStoredProcedures.GET_BABIES);
            callableStatement.setString(1, username);
            resultSet = callableStatement.executeQuery();
            if (!Objects.equals(resultSet, null)) {
                while (resultSet.next()) {
                    JSONObject jSONObject = new JSONObject();
                    jSONObject.put(Tags.BABY_ID, resultSet.getInt(1));
                    jSONObject.put(Tags.BABY_NAME, resultSet.getString(2));
                    jSONArray.put(jSONObject);
                }
            }
            return jsonObject.put(Tags.BABIES, jSONArray);
        } catch (SQLException | JSONException ex) {
            Logger.getLogger(DBOperations.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            closeEverything();
        }
        return null;
    }

    /**
     * Closes necessary objects
     */
    private void closeEverything() {
        try {
            if (!Objects.equals(connection, null)) {
                connection.close();
            }
            if (!Objects.equals(resultSet, null)) {
                resultSet.close();
            }
            if (!Objects.equals(preparedStatement, null)) {
                preparedStatement.close();
            }
            if (!Objects.equals(callableStatement, null)) {
                callableStatement.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns hashed string
     *
     * @param password user's password
     * @return SHA-512 encrypted password or null if catches
     * NoSuchAlgorithmException or UnsupportedEncodingException
     */
    private String passToHash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(password.getBytes("UTF-8"));
            byte[] digest = md.digest();
            return String.format("%064x", new java.math.BigInteger(1, digest));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            Logger.getLogger(DBOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Calculates VARICELLA vaccines dates
     *
     * @param date_of_birth of baby
     * @return date of VARICELLA
     * @throws ParseException
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
     * Calculates BCG vaccines dates
     *
     * @param date_of_birth of baby
     * @return date of BCG
     * @throws ParseException
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
     * Calculates DABT IPA HIB vaccines dates and create a callable statement
     *
     * @param connection current connection
     * @param date_of_birth of baby
     * @return a callableStatement included necessary informations or null if
     * catches SQLException or ParseException
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
     * Calculates HEPATITIS A vaccines dates and create a callable statement
     *
     * @param connection current connection
     * @param date_of_birth of baby
     * @return a callableStatement included necessary informations or null if
     * catches SQLException or ParseException
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
     * Calculates HEPATITIS B vaccines dates and create a callable statement
     *
     * @param connection current connection
     * @param date_of_birth of baby
     * @return a callableStatement included necessary informations or null if
     * catches SQLException or ParseException
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
     * Calculates KKK vaccines dates and create a callable statement
     *
     * @param connection current connection
     * @param date_of_birth of baby
     * @return a callableStatement included necessary informations or null if
     * catches SQLException or ParseException
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
     * Calculates KPA vaccines dates and create a callable statement
     *
     * @param connection current connection
     * @param date_of_birth of baby
     * @return a callableStatement included necessary informations or null if
     * catches SQLException or ParseException
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
     * Calculates OPA vaccines dates and create a callable statement
     *
     * @param connection current connection
     * @param date_of_birth of baby
     * @return a callableStatement included necessary informations or null if
     * catches SQLException or ParseException
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
     * Calculates RVA vaccines dates and create a callable statement
     *
     * @param connection current connection
     * @param date_of_birth of baby
     * @return a callableStatement included necessary informations or null if
     * catches SQLException or ParseException
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

    /**
     * Gets all vaccine names recorded to the database
     *
     * @return an object added vaccine names, null if catches SQLException or
     * JSONException
     */
    public JSONObject getAllVaccineNames() {
        JSONObject jSONObject = new JSONObject();
        JSONArray array = new JSONArray();
        try {
            establishConnection();
            callableStatement = connection.prepareCall(DbStoredProcedures.GET_ALL_VACCINE_NAMES);
            resultSet = callableStatement.executeQuery();
            if (!Objects.equals(resultSet, null)) {
                while (resultSet.next()) {
                    JSONObject temp = new JSONObject();
                    temp.put("Vaccine name", resultSet.getString(1));
                    array.put(temp);
                }
            }
            return jSONObject.put("Vaccines", array);
        } catch (SQLException | JSONException ex) {
            Logger.getLogger(DBOperations.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            closeEverything();
        }
        return null;
    }

    /**
     * Gets vaccine details of desired baby
     *
     * @param baby_id of desired baby for vaccination details
     * @return an object added baby's vaccine details, null if catches
     * SQLException or JSONException
     */
    public JSONObject getVaccinesDetailsOfBaby(int baby_id) {
        JSONObject jSONObject = new JSONObject();
        try {
            establishConnection();
            callableStatement = connection.prepareCall(DbStoredProcedures.GET_BABY_VACCINES);
            callableStatement.setInt(1, baby_id);
            resultSet = callableStatement.executeQuery();
            if (!Objects.equals(resultSet, null)) {
                while (resultSet.next()) {

                    jSONObject.put(Tags.BABY_NAME, resultSet.getString(1));
                    jSONObject.put(Tags.BCG, resultSet.getString(2));
                    jSONObject.put(Tags.VARICELLA, resultSet.getString(3));
                    jSONObject.put(Tags.HPA, resultSet.getString(4));
                    jSONObject.put(Tags.KMA4, resultSet.getString(5));
                    jSONObject.put(Tags.DaBT_IPA, resultSet.getString(6));
                    jSONObject.put(Tags.INFLUENZA, resultSet.getString(7));

                    jSONObject.put(Tags.FIRST_DaBT_IPA_HIB, resultSet.getString(8));
                    jSONObject.put(Tags.SECOND_DaBT_IPA_HIB, resultSet.getString(9));
                    jSONObject.put(Tags.THIRD_DaBT_IPA_HIB, resultSet.getString(10));
                    jSONObject.put(Tags.FOURTH_DaBT_IPA_HIB, resultSet.getString(11));
                    jSONObject.put(Tags.FIFTH_DaBT_IPA_HIB, resultSet.getString(12));
                    jSONObject.put(Tags.SIXTH_DaBT_IPA_HIB, resultSet.getString(13));

                    jSONObject.put(Tags.FIRST_HEPATIT_B, resultSet.getString(14));
                    jSONObject.put(Tags.SECOND_HEPATIT_B, resultSet.getString(15));
                    jSONObject.put(Tags.THIRD_HEPATIT_B, resultSet.getString(16));

                    jSONObject.put(Tags.FIRST_KPA, resultSet.getString(17));
                    jSONObject.put(Tags.SECOND_KPA, resultSet.getString(18));
                    jSONObject.put(Tags.THIRD_KPA, resultSet.getString(19));
                    jSONObject.put(Tags.FOURTH_KPA, resultSet.getString(20));

                    jSONObject.put(Tags.FIRST_KKK, resultSet.getString(21));
                    jSONObject.put(Tags.SECOND_KKK, resultSet.getString(22));

                    jSONObject.put(Tags.FIRST_RVA, resultSet.getString(23));
                    jSONObject.put(Tags.SECOND_RVA, resultSet.getString(24));
                    jSONObject.put(Tags.THIRD_RVA, resultSet.getString(25));

                    jSONObject.put(Tags.FIRST_HEPATIT_A, resultSet.getString(26));
                    jSONObject.put(Tags.SECOND_HEPATIT_A, resultSet.getString(27));

                    jSONObject.put(Tags.FIRST_OPA, resultSet.getString(28));
                    jSONObject.put(Tags.SECOND_OPA, resultSet.getString(29));
                }
                return jSONObject;
            }
        } catch (SQLException | JSONException e) {
            Logger.getLogger(DBOperations.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            closeEverything();
        }
        return null;
    }

    /**
     * If user forgets his/her password this method will sent a verification
     * code to the user's e-mail for changing password
     *
     * @param e_mail E-mail address of user
     * @return 10 sent successfully, -2 if catches MessagingException
     */
    public synchronized int sendMailToUser(String e_mail) {
        String verificationCode = GenerateVerificationCode.getVerificationCode();
        if (!Objects.equals(updateVerificationCodeInDB(e_mail, verificationCode), 0)) {
            return sendMail.sendMailTo(e_mail, verificationCode);
        }
        return -2;
    }

    /**
     * After generating verification code with this method code will be inserted
     * generated code to related database column of user
     *
     * @param e_mail E-mail address of user
     * @param code Generated code
     * @return 1 updated, 0 not updated, -1 if catches SQLException
     */
    private int updateVerificationCodeInDB(String e_mail, String code) {
        try {
            establishConnection();
            callableStatement = connection.prepareCall(DbStoredProcedures.UPDATE_VERIFICATION_CODE);
            callableStatement.setString(1, e_mail);
            callableStatement.setString(2, code);
            return callableStatement.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(DBOperations.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            closeEverything();
        }
        return -1;
    }

    /**
     * Checks verification code is valid or not sent by system to user's e-mail
     * address
     *
     * @param email E-mail address of user
     * @param code Verification code sent by system
     * @return 1 validated code, 0 not validated code, -2 if catches
     * SQLException
     */
    public synchronized int checkVerificationCode(String email, String code) {
        try {
            establishConnection();
            preparedStatement = connection.prepareStatement(DbFunctions.VALIDATE_VERIFICATION_CODE);
            preparedStatement.setString(1, code);
            preparedStatement.setString(2, email);
            resultSet = preparedStatement.executeQuery();
            if (!Objects.equals(resultSet, null)) {
                while (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(DBOperations.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            closeEverything();
        }
        return -2;
    }

    /**
     *
     * @param username
     * @param fileName
     * @param imageBytes
     * @return
     */
    public synchronized int uploadImage(String username, String fileName, byte[] imageBytes) {
        try {
            if (!uploadToFTP(username, fileName, new ByteArrayInputStream(imageBytes))) {
                return -2;
            }
            establishConnection();
            callableStatement = connection.prepareCall(DbStoredProcedures.UPDATE_IMAGE);
            callableStatement.setString(1, Initialize.imageFTPPath() + fileName);
            callableStatement.setString(2, username);
            return callableStatement.executeUpdate();
        } catch (SQLException e) {
            Logger.getLogger(DBOperations.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            closeEverything();
        }
        return -1;
    }

    private boolean uploadToFTP(String username, String fileName, InputStream inputStream) {
        FTPClient client = new FTPClient();
        try {
            client.connect(Initialize.FTPClient());
            if (client.login(Initialize.FTPUsername(), Initialize.getPASSWORD())) {
                client.setDefaultTimeout(10000);
                client.setFileType(FTPClient.BINARY_FILE_TYPE);
                if (client.storeFile(Tags.SITE + username + "/" + fileName, inputStream)) {
                    return true;
                }
            }
        } catch (IOException e) {
            Logger.getLogger(DBOperations.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            if (client.isConnected()) {
                try {
                    client.logout();
                    client.disconnect();
                } catch (IOException ex) {
                    Logger.getLogger(DBOperations.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return false;
    }
}
