/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.services.handlers;

import java.util.Properties;

import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wci.umls.server.User;
import com.wci.umls.server.helpers.ConfigUtility;

/**
 * Handles sending email to user to validate email address for registration.
 */
public class UserRegistrationHandler {

  /** Date format */
  public static final FastDateFormat df = FastDateFormat.getInstance("hh:mm:ss a");
  
  private static final Logger LOG = LoggerFactory.getLogger(UserRegistrationHandler.class);

  /**
   * Send an email to the user to verify their email address to complete registration.
   *
   * @param user User
   * @throws Exception the web application exception
   */
  public static void sendVerificationEmail(User user) throws Exception {

		try {
			final Properties config = ConfigUtility.getConfigProperties();
			
			if ("true".equals(config.getProperty("deploy.registration.required"))
					&& "true".equals(config.getProperty("mail.enabled"))) {
				
				final String subject = config.getProperty("registration.verificaton.email.subject");
				final String verificationLinkHost = config.getProperty("base.url");
				final String verificationLinkUrl = config.getProperty("registration.verificaton.email.url");
				final String from = config.getProperty("registration.verificaton.email.from");

				final String recipient = user.getEmail();

				// Bail if no recipient
				if (recipient == null || recipient.isEmpty()) {
					return;
				}

				final Properties props = new Properties();
				for (final Object prop : config.keySet()) {
					if (prop.toString().startsWith("mail.smtp")) {
						props.put(prop.toString(), config.getProperty(prop.toString()));
					}
				}

				final StringBuilder body = new StringBuilder();

				body.append("<html>");
				body.append("<head>");
				body.append("</head>");
				body.append("<body style='font-family: \"Open Sans\", sans-serif;font-size:1em;'>");
				body.append("<div style='padding-top: 30px; padding-bottom: 30px;'>");
				body.append("To complete registation for ");
				
				//application name
				body.append(config.getProperty("deploy.title"));
				body.append(" by ");
				body.append(config.getProperty("deploy.presented.by"));
				body.append("click the button below.<br />");
				body.append("</div>");
				body.append("<div style='padding-bottom: 30px; margin-left: 50px'>");
				body.append("<a href=\"");

				// add url to button
				body.append(verificationLinkHost).append("/").append(verificationLinkUrl).append("/").append(user.getUserToken());

				body.append("\" target=\"_blank\">");
				body.append("<div style='color: #fff; background-color: #337ab7; border-color:");
				body.append(
						"#2e6da4;display:inline-block;font-weight:600;text-align:center;white-space:nowrap;vertical-align:middle;border:1px solid transparent;padding:.75rem");
				body.append("1.5rem;font-size:1rem;line-height:1.5;border-radius:.55rem'>");
				body.append("Confirm Email");
				body.append("</div>");
				body.append("</a>");
				body.append("</div>");
				body.append("<div style='padding-bottom: 30px;'>");
				body.append(
						"******************************************************************************************************************************");
				body.append("</div>");
				body.append("<div style='padding-bottom: 30px;'>");
				body.append(
						"Please do not reply to this message as it is from an unattended mailbox. Any replies to this email will not be responded");
				body.append("to or forwarded. This service is used for outgoing emails only and cannot respond to inquiries.");
				body.append("</div>");
				body.append("</body>");
				body.append("</html>");

				ConfigUtility.sendEmail(subject, from, recipient, body.toString(), props);
				
				LOG.info("Registration for email confirmation sent to {}", user.getEmail());
			}
			else if ("true".equals(config.getProperty("deploy.registration.required"))
					&& !"true".equals(config.getProperty("mail.enabled"))) {
					throw new Exception("User registration is required but email is not configured.");
			}
		} catch (Exception ex) {
			LOG.error("Unable to handle exception", ex);
		}
  }
  
  /**
   * Send an email staff when a user validates their email address.
   *
   * @param user User
   * @throws Exception the web application exception
   */
  public static void sendRegistrationCompleteEmail(User user) throws Exception {

		try {
			final Properties config = ConfigUtility.getConfigProperties();
			
			if ("true".equals(config.getProperty("deploy.registration.required"))
					&& "true".equals(config.getProperty("mail.enabled"))) {
				
				final String subject = config.getProperty("registration.notification.email.subject");
				final String from = config.getProperty("registration.notification.email.from");
				final String recipients = config.getProperty("registration.notification.email.to");

				// exit if no recipients
				if (recipients == null || recipients.isEmpty()) {
					return;
				}

				final Properties props = new Properties();
				for (final Object prop : config.keySet()) {
					if (prop.toString().startsWith("mail.smtp")) {
						props.put(prop.toString(), config.getProperty(prop.toString()));
					}
				}

				final StringBuilder body = new StringBuilder();
				body.append("<html>");
				body.append("<head>");
				body.append("</head>");
				body.append("<body style='font-family: \"Open Sans\", sans-serif;font-size:1em;'>");
				body.append("<div style='padding-top: 30px; padding-bottom: 30px;'>");

				body.append("User ").append(user.getName()).append(" (").append(user.getEmail()).append(")")
						.append(" completed registration for ").append(config.getProperty("deploy.title"));

				body.append("</div>");
				body.append("<div style='padding-bottom: 30px;'>");
				body.append(
						"Please do not reply to this message as it is from an unattended mailbox. Any replies to this email will not be responded");
				body.append("to or forwarded. This service is used for outgoing emails only and cannot respond to inquiries.");
				body.append("</div>");
				body.append("</body>");
				body.append("</html>");

				ConfigUtility.sendEmail(subject, from, recipients, body.toString(), props);
				
				LOG.info("Registration notificaiton for email confirmation sent for {}", user.getEmail());
			}
			else if ("true".equals(config.getProperty("deploy.registration.required"))
					&& !"true".equals(config.getProperty("mail.enabled"))) {
					throw new Exception("User registration is required but email is not configured.");
			}
		} catch (Exception ex) {
			LOG.error("Unable to handle exception", ex);
		}
  }
}
