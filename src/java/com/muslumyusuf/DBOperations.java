/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.muslumyusuf;

import com.mysql.jdbc.Connection;
import java.sql.CallableStatement;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

	public static String openConnection() {
		try {
			Class.forName(Initialize.CLASS_NAME);
			connection = (Connection) DriverManager.getConnection(Initialize.DB_URL, Initialize.USERNAME, Initialize.PASSWORD);
			if (connection != null) {
				return "connected";
			}
		} catch (ClassNotFoundException | SQLException ex) {
			Logger.getLogger(DBOperations.class.getName()).log(Level.SEVERE, null, ex);
		}
		return "not Connected";
	}

}
