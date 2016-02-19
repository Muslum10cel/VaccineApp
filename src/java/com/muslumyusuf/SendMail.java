/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.muslumyusuf;

import com.initialize.InitProperties;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author muslumoncel
 */
public class SendMail {

    private final String SENDER;
    private final String PASS;
    private Properties properties;
    private Session session;
    private Message message;

    public SendMail() {
        SENDER = InitProperties.getUSERNAME();
        PASS = InitProperties.getPASS();
        init();
    }

    private void init() {
        properties = InitProperties.getProperties();
    }

    public int sendMailTo(String receiver, String code) {
        session = Session.getInstance(properties, new javax.mail.Authenticator() {
            @Override
            protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                return new javax.mail.PasswordAuthentication(SENDER, PASS);
            }
        });

        try {
            message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(receiver));
            message.setSubject(Tags.VERIFY);
            message.setText(Tags.VERIFICATION_CODE + code);
            Transport.send(message);
            return 10;
        } catch (MessagingException e) {
            Logger.getLogger(DBOperations.class.getName()).log(Level.SEVERE, null, e);
        }
        return -2;
    }
}
