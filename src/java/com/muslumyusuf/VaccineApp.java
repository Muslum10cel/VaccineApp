/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.muslumyusuf;

import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.ws.rs.Produces;

/**
 *
 * @author muslumoncel
 */
@WebService(serviceName = "VaccineApp")
public class VaccineApp {

	private final static DBOperations dBOperations = new DBOperations();

	@WebMethod(operationName = "register")
	public int register(@WebParam(name = "username") String username, @WebParam(name = "fullname") String fullname, @WebParam(name = "password") String password) {
		return dBOperations.register(username, fullname, password);
	}

	@WebMethod(operationName = "log_in")
	public int logIn(@WebParam(name = "username") String username, @WebParam(name = "password") String password) {
		return dBOperations.logIn(username, password);
	}

	@WebMethod(operationName = "add_Baby")
	public int addBaby(@WebParam(name = "username") String username, @WebParam(name = "baby_name") String baby_name, @WebParam(name = "date_of_birth") String date_of_birth) {
		return dBOperations.addBaby(username, baby_name, date_of_birth);
	}

	@WebMethod(operationName = "update_DaBT_IPA_HIB")
	public int update_DaBT_IPA_HIB(@WebParam(name = "baby_id") int baby_id, @WebParam(name = "flag") int flag) {
		return dBOperations.update_DaBT_IPA_HIB(baby_id, flag);
	}

	@WebMethod(operationName = "update_Hepatit_A")
	public int update_Hepatit_A(@WebParam(name = "baby_id") int baby_id, @WebParam(name = "flag") int flag) {
		return dBOperations.update_Hepatit_A(baby_id, flag);
	}

	@WebMethod(operationName = "update_Hepatit_B")
	public int update_Hepatit_B(@WebParam(name = "baby_id") int baby_id, @WebParam(name = "flag") int flag) {
		return dBOperations.update_Hepatit_B(baby_id, flag);
	}

	@WebMethod(operationName = "update_KKK")
	public int update_KKK(@WebParam(name = "baby_id") int baby_id, @WebParam(name = "flag") int flag) {
		return dBOperations.update_KKK(baby_id, flag);
	}

	@WebMethod(operationName = "update_KPA")
	public int update_KPA(@WebParam(name = "baby_id") int baby_id, @WebParam(name = "flag") int flag) {
		return dBOperations.update_KPA(baby_id, flag);
	}

	@WebMethod(operationName = "update_OPA")
	public int update_OPA(@WebParam(name = "baby_id") int baby_id, @WebParam(name = "flag") int flag) {
		return dBOperations.update_OPA(baby_id, flag);
	}

	@WebMethod(operationName = "update_RVA")
	public int update_RVA(@WebParam(name = "baby_id") int baby_id, @WebParam(name = "flag") int flag) {
		return dBOperations.update_RVA(baby_id, flag);
	}

	@WebMethod(operationName = "update_Vaccines")
	public int update_Vaccines(@WebParam(name = "baby_id") int baby_id, @WebParam(name = "flag") int flag) {
		return dBOperations.update_Vaccines(baby_id, flag);
	}

	@Produces("application/json")
	@WebMethod(operationName = "get_Completed_Vaccines")
	public String getCompletedVaccines(@WebParam(name = "baby_id") int baby_id) {
		return dBOperations.completedAndIncompletedVaccines(baby_id).toString();
	}

	@WebMethod(operationName = "addComment")
	public int addComment(@WebParam(name = "username") String username, @WebParam(name = "vaccine_name") String vaccine_name, @WebParam(name = "comment") String comment) {
		return dBOperations.addComment(username, vaccine_name, comment);
	}

	@WebMethod(operationName = "forgottenPassword")
	public int forgottenPassword(@WebParam(name = "username")String username,@WebParam(name = "newPassword") String newPassword) {
		return dBOperations.forgottenPassword(username, newPassword);
	}

	@Produces("application/json")
	@WebMethod(operationName = "getComment")
	public String comments(@WebParam(name = "vaccine_name") String vaccine_name, @WebParam(name = "beginning") int beginning,@WebParam(name = "end") int end) {
		return dBOperations.getComments(vaccine_name, beginning, end).toString();
	}

	@Produces("application/json")
	@WebMethod(operationName = "getBabies")
	public String getBabies(@WebParam(name = "username") String username) {
		return dBOperations.getBabies(username).toString();
	}
}
