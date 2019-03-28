```
package com.mealkey.mealtime.bi;

import ch.qos.logback.core.helpers.CyclicBuffer;
import ch.qos.logback.core.net.LoginAuthenticator;
import ch.qos.logback.core.util.ContentTypeUtil;
import ch.qos.logback.core.util.OptionHelper;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class SendMail {

    public static void main(String[] args) {
        SendMail ins = new SendMail();
        ins.sendBuffer();
    }

    public class LoginAuthenticator extends Authenticator {

        String username;
        String password;

        LoginAuthenticator(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(username, password);
        }
    }


    private Session buildSessionFromProperties() {
        String smtpHost = "smtp.mealkey.cn";
        Integer smtpPort = 25;
        String username = "support@mealkey.cn";
        String password = "Mealkey3333";
        Boolean isSTARTTLS = false;
        Boolean isSSL = true;

        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", Integer.toString(smtpPort));

        // 打印debug信息  注意，值必须是 String 类型的
        props.put("mail.debug", "true");

        // 设置超时时间  必须使用 javax.mail 1.5 开头的版本以上才可以
        props.put("mail.smtp.connectiontimeout", String.valueOf(5000L));
        props.put("mail.smtp.timeout", String.valueOf(5000L));



        LoginAuthenticator loginAuthenticator = null;
        if (username != null) {
            loginAuthenticator = new LoginAuthenticator(username, password);
            props.put("mail.smtp.auth", "true");
        }

        if (isSTARTTLS && isSSL) {
            System.out.println("Both SSL and StartTLS cannot be enabled simultaneously");
        } else {
            if (isSTARTTLS) {
                // see also http://jira.qos.ch/browse/LBCORE-225
                props.put("mail.smtp.starttls.enable", "true");
            }
            if (isSSL) {
                String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
                props.put("mail.smtp.socketFactory.port", Integer.toString(smtpPort));
                props.put("mail.smtp.socketFactory.class", SSL_FACTORY);
                props.put("mail.smtp.socketFactory.fallback", "true");
            }
        }

        return Session.getInstance(props, loginAuthenticator);
    }

    protected void sendBuffer() {
        try {
            MimeBodyPart part = new MimeBodyPart();

            StringBuffer sbuf = new StringBuffer();
            sbuf.append("con1");

            sbuf.append("con2");

            Session session = buildSessionFromProperties();
            MimeMessage mimeMsg = new MimeMessage(session);
            try {
                Address address = new InternetAddress("support@mealkey.cn");
                mimeMsg.setFrom(address);
            } catch (AddressException e) {
                e.printStackTrace();
            }

            mimeMsg.setSubject("test email", "UTF-8");

            try {
                Address toAddress = new InternetAddress("lw@mealkey.cn");
                mimeMsg.setRecipients(Message.RecipientType.TO, new Address[] {toAddress});
            } catch (AddressException e) {
                e.printStackTrace();
            }

            String contentType = "text/plain";
            part.setText(sbuf.toString(), "UTF-8", "plain");

            Multipart mp = new MimeMultipart();
            mp.addBodyPart(part);
            mimeMsg.setContent(mp);

            mimeMsg.setSentDate(new Date());
            Transport.send(mimeMsg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

```