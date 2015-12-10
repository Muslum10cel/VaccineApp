/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.muslumyusuf;

/**
 *
 * @author muslumoncel
 */
public class DbStoredProcedures {

	public static final String ADD_BABY = "{call db_9c4db8_dprtmnt.addbaby(?,?,?)}";
	public static final String ADD_COMMENT = "{call db_9c4db8_dprtmnt.addComment(?,?,?)}";
	public static final String ADD_DaBT_IPA_HIB = "{call db_9c4db8_dprtmnt.addDabt_Ipa_HibVaccines(?,?,?,?)}";
	public static final String ADD_HEPATIT_A_VACCINES = "{call db_9c4db8_dprtmnt.addHepatitAVaccines(?,?)}";
	public static final String ADD_HEPATIT_B_VACCINES = "{call db_9c4db8_dprtmnt.addHepatitBVaccines(?,?,?)}";
	public static final String ADD_KKK_VACCINES = "{call db_9c4db8_dprtmnt.addKKKVaccines(?)}";
	public static final String ADD_KPA_VACCINES = "{call db_9c4db8_dprtmnt.addKPAVaccines(?,?,?,?)}";
	public static final String ADD_OPA_VACCINES = "{call db_9c4db8_dprtmnt.addOPAVaccines(?,?)}";
	public static final String ADD_RVA_VACCINES = "{call db_9c4db8_dprtmnt.addRVAVaccines(?,?,?)}";
	public static final String ADD_VACCINES = "{call db_9c4db8_dprtmnt.addVaccines(?,?)}";
	public static final String DELETE_COMMENT = "{call db_9c4db8_dprtmnt.deleteComment(?)}";
	public static final String FORGOTTEN_PASSWORD = "{call db_9c4db8_dprtmnt.forgottenPassword(?,?)}";
	public static final String GET_BABY_VACCINES = "{call db_9c4db8_dprtmnt.getBabyVaccines(?)}";
	public static final String GET_COMMENT = "{call db_9c4db8_dprtmnt.getComment(?)}";
	public static final String GET_COMPLETED_VACCINES = "{call db_9c4db8_dprtmnt.getCompletedVaccines(?)}";
	public static final String SET_FALSE_ALL_VACCINES_STATUS = "{call db_9c4db8_dprtmnt.setFalseAllVaccinesStatus()}";
	public static final String UPDATE_DaBT_IPA_HIB = "{call db_9c4db8_dprtmnt.update_DaBT_IPA_HIB(?,?)}";
	public static final String UPDATE_HEPATIT_A = "{call db_9c4db8_dprtmnt.update_Hepatit_A(?,?)}";
	public static final String UPDATE_HEPATIT_B = "{call db_9c4db8_dprtmnt.update_Hepatit_B(?,?)}";
	public static final String UPDATE_KKK = "{call db_9c4db8_dprtmnt.update_KKK(?,?)}";
	public static final String UPDATE_KPA = "{call db_9c4db8_dprtmnt.update_KPA(?,?)}";
	public static final String UPDATE_OPA = "{call db_9c4db8_dprtmnt.update_OPA(?,?)}";
	public static final String UPDATE_RVA = "{call db_9c4db8_dprtmnt.update_RVA(?,?)}";
	public static final String UPDATE_VACCINES = "{call db_9c4db8_dprtmnt.update_Vaccines(?,?)}";

}
