/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.muslumyusuf;

import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;

/**
 *
 * @author muslumoncel
 */
@WebService(serviceName = "VaccineApp")
public class VaccineApp {

	private final static DBOperations dBOperations = new DBOperations();

	@WebMethod(operationName = "register")
	public int register(@WebParam(name = "username") String username, @WebParam(name = "password") String password) {
		return dBOperations.register(username, password);
	}

	@WebMethod(operationName = "log-in")
	public int logIn(@WebParam(name = "username") String username, @WebParam(name = "password") String password) {
		return dBOperations.logIn(username, password);
	}
}
