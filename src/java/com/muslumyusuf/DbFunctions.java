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
public class DbFunctions {

    public static final String CHECK_USER_FUNCTION = "select db_9c4db8_dprtmnt.checkUserFunc(?)";
    public static final String LOG_IN = "select db_9c4db8_dprtmnt.log_in(?,?)";
    public static final String REGISTER = "select db_9c4db8_dprtmnt.Register(?,?,?,?,?)";
    public static final String VALIDATE_VERIFICATION_CODE = "select db_9c4db8_dprtmnt.validateVerificationCode(?,?)";
}
