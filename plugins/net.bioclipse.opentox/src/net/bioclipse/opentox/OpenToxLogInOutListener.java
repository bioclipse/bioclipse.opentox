/* Copyright (c) 2011  Egon Willighagen <egonw@users.sf.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 */
package net.bioclipse.opentox;

import java.util.List;

import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.opentox.aa.opensso.OpenSSOToken;

import net.bioclipse.opentox.business.OpentoxManager;
import net.bioclipse.usermanager.IUserManagerListener;
import net.bioclipse.usermanager.UserManagerEvent;
import net.bioclipse.usermanager.business.IUserManager;

/**
 * 
 * @author Egon Willighagen, 
 * contribution by Klas Jšnsson (klas.joensson@gmail.com)
 *
 */
public class OpenToxLogInOutListener implements IUserManagerListener {

	private IUserManager userManager;
	public static String myAccountType = "";
	private OpenSSOToken token = null;
	private String authService = "";
	private static OpenToxLogInOutListener thisListener = null;
	private static final Logger logger = Logger.
	        getLogger(OpenToxLogInOutListener.class);
	
	/**
	 * Instantiate an OpenToxLogInOutListener. This method can only be used if 
	 * the OpenToxLogInOutListener has been instantiated with
	 *  getInstance(IUserManager userManager) first.
	 * 
	 * @return An OpenToxLogInOutListener
	 * @throws InstantiationException If it hasn't been instantiated before.
	 */
	public static OpenToxLogInOutListener getInstance() 
	        throws InstantiationException {
	    if (thisListener == null) 
	        throw new InstantiationException( "Are you sure it has been " +
	        		"instantiatedone before?" );
	    
	    return thisListener;
	}
	/**
	 * Instantiate an OpenToxLogInOutListener with help of the user manager.
	 * 
	 * @param userManager The user manger used by this session 
	 * @return An OpenToxLogInOutListener
	 */
	public static OpenToxLogInOutListener getInstance(IUserManager 
	                                                  userManager) {
        if (thisListener == null) 
            thisListener = new OpenToxLogInOutListener( userManager );
        
        return thisListener;
    }
	
	/**
	 * The (private) constructor.
	 * 
	 * @param userManager The user manger used by this session
	 */
	private OpenToxLogInOutListener(IUserManager userManager) {
		this.userManager = userManager;
	}
	
	/**
	 * The "normal" method for log-in/-out.
	 * 
	 * @param event An UserManagerEvent telling the listener what to do
	 * @return True if the event succeed
	 */
	@Override
	public boolean receiveUserManagerEvent(UserManagerEvent event) {
		System.out.println("Received event: " + event);
		boolean eventSucceeded = true;
		switch (event) {
		case LOGIN:
		    eventSucceeded = updateOnLogin();
			break;
		case LOGOUT:
			updateOnLogout();
			break;
		case UPDATE:
			eventSucceeded = update();
			break;
		default:
			break;
		}
		
		return eventSucceeded;
	}

	/**
	 * Do the actual login, with help of the properties in the openTox account
	 * in the usermanager.
	 * 
	 * @return True if the login succeed
	 */
	private boolean updateOnLogin() {
		logger.debug( "Logging in on OpenTox..." );
		logger.debug("Keyring User is logged in: " + userManager.isLoggedIn());
		boolean loginSucceeded = false;
		if (userManager.isLoggedIn() && getToken() == null) {
			try {
				System.out.println("Logging in on OpenTox (via the listener): ");
					String account = getAccount();
					loginSucceeded = login(
						userManager.getProperty( account, "username" ),
						userManager.getProperty( account, "password" ), 
						userManager.getProperty( account, "auth. service" )
					);
			} catch (Exception e) {
				logger.error("Error while logging in: " + e.getMessage());
				logger.error( e );
				loginSucceeded = false;
			}
		}
		return loginSucceeded;
	}
	
	private boolean update() {
		return updateOnLogin();
	}

	/**
	 * The method that does the actual logout.
	 *  
	 * @return True if the log out succeed 
	 */
	private boolean updateOnLogout() {
		System.out.println("Logging out of OpenTox...");
		System.out.println("Keyring User is logged in: " + userManager.isLoggedIn());
		if (!userManager.isLoggedIn() && getToken() != null) {
			try {
				System.out.println("Logging out of OpenTox...");
				return logout();
			} catch (Exception e) {
				logger.error( "Error while logging out: " + e.getMessage());
				logger.error( e );
				
			}
			return false;
		}
		return true; // Is already logged out
	}

	/**
	 * Returns the account type.
	 * 
	 * @return A <code>String</code> that represents the account type
	 */
	@Override
	public String getAccountType() {
	    if (myAccountType.isEmpty())
            // TODO When the variable is initiated here it should get its name 
            // from the extension-point some how...
            myAccountType = "OpenTox";
        
        return myAccountType;
	}
	
	/**
	 * A login method for that can login to specific authorization service. If 
	 * logged in to another service when calling this, it will logout from 
	 * that. 
	 * 
	 * @param user The user name for this authorization service
	 * @param pwd The password for this authorization service
	 * @param authService The specified authorization service
	 * @return True if the login succeeded
	 * @throws Exception If something goes wrong
	 */
	public boolean login(String user, String pwd, String authService) throws Exception {
        if (authService == null || authService.isEmpty())
            throw new LoginException( "You must select an authorization " +
                    "Service." );
        if (token == null) {
            token = new OpenSSOToken( authService );
        } else {
            token.logout();
            if (!authService.equals( authService )) 
                token = new OpenSSOToken( authService );                            
        }

        return token.login(user, pwd);
	}
	
	/**
	 * Logout from the openTox service.
	 *  
	 * @return True if the login succeeded
	 * @throws Exception If something goes wrong
	 */
    public boolean logout() throws Exception {
        boolean logoutOK = false;
        if (token == null) // already logged out
            return true;

        logoutOK = token.logout();
        token = null;
        return logoutOK;
    }
	
    /**
     * Returns a <code>String</code> with the authorization service.
     * 
     * @return The authorization service
     * @throws LoginException 
     */
	public String getAuthService() throws LoginException {
	    if (authService== null || authService.isEmpty())
	        authService = userManager.getProperty( getAccount(), "auth. service" );
	        
	    return authService;
	}
	
	/**
	 * Sets the the authorization service. Observe that this don't login to the 
	 * service.
	 * @param authService The authorization service to be used.
	 */
	public void setAuthService(String authService) {
	    this.authService = authService;
	}
	
	/**
	 * This method sets the authorization service to the one specified by the 
	 * openTox account properties and logins to it. If the login fails by some 
	 * reason (e.g. not logged in to a Bioclipse user account), the 
	 * authorization service will be set to an empty string.
	 */
	public void resetAuthService() {
	    if ( !updateOnLogin() )
	        this.authService = "";
	}
	
	/**
	 * A method to get the account name.
	 * 
	 * @return The account name.
	 * @throws LoginException
	 */
	private String getAccount() throws LoginException {
	    List<String> otssoAccounts = userManager
                .getAccountIdsByAccountTypeName( getAccountType() );
        if (otssoAccounts.size() > 0) {
            return otssoAccounts.get(0);
        }
        throw new LoginException( "Can't find this accountType" + 
                getAccountType() ); 
	}
	
	/**
	 * Returns the token.
	 * 
	 * @return The token
	 */
    public String getToken() {
        if (token == null) return null;
        
        return token.getToken();
    }
    
}
