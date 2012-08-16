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

import net.bioclipse.usermanager.IUserManagerListener;
import net.bioclipse.usermanager.UserManagerEvent;
import net.bioclipse.usermanager.business.IUserManager;

public class OpenToxLogInOutListener implements IUserManagerListener {

	private IUserManager userManager;
	public static String myAccountType = "";
	
	public OpenToxLogInOutListener(IUserManager userManager) {
		this.userManager = userManager;
	}
	
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

	private boolean updateOnLogin() {
		System.out.println("Logging in on OpenTox...");
		System.out.println("Keyring User is logged in: " + userManager.isLoggedIn());
		boolean loginSucceeded = false;
		if (userManager.isLoggedIn() && Activator.getToken() == null) {
			try {
				System.out.println("Logging in on OpenTox: ");
				List<String> otssoAccounts = userManager
				        .getAccountIdsByAccountTypeName( getAccountType() );
				if (otssoAccounts.size() > 0) {
					String account = otssoAccounts.get(0);

					loginSucceeded = Activator.login(
						userManager.getProperty(account, "username"),
						userManager.getProperty(account, "password")	
					);
				}
			} catch (Exception e) {
				// FIXME: should do proper feedback
				System.out.println("Error while logging in: " + e.getMessage());
				e.printStackTrace();
				loginSucceeded = false;
			}
		}
		return loginSucceeded;
	}

	private boolean update() {
		return updateOnLogin();
	}

	private void updateOnLogout() {
		System.out.println("Logging out of OpenTox...");
		System.out.println("Keyring User is logged in: " + userManager.isLoggedIn());
		if (!userManager.isLoggedIn() && Activator.getToken() != null) {
			try {
				System.out.println("Logging out of OpenTox...");
				Activator.logout();
			} catch (Exception e) {
				// FIXME: should do proper feedback
				System.out.println("Error while logging out: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	@Override
	public String getAccountType() {
	    if (myAccountType.isEmpty())
            // TODO When the variable is initiated here it should get its name 
            // from the extension-point some how...
            myAccountType = "OpenTox OpenSSO Account";
        
        return myAccountType;
	}
}
