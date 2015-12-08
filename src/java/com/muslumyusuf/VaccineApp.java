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

	@WebMethod(operationName = "register")
	public boolean register(@WebParam(name = "username") String username, @WebParam(name = "password") String password, @WebParam(name = "log_in_level") int log_in_level) {
		return true;
	}

	@WebMethod(operationName = "checkConnection")
	public String checkConnection() {
		return DBOperations.openConnection();
	}
}
