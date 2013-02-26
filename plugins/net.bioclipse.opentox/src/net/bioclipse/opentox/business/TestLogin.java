package net.bioclipse.opentox.business;

import java.util.HashMap;

import net.bioclipse.opentox.OpenToxLogInOutListener;
import net.bioclipse.usermanager.ITestAccountLogin;

import org.opentox.aa.opensso.OpenSSOToken;

public class TestLogin implements ITestAccountLogin {
    
    public TestLogin() {    }
    
    public boolean login(HashMap<String, String> myProperites) {
        String authService = myProperites.get( "auth. service" );
        String userName = myProperites.get( "username" );
        String pwd = myProperites.get( "password" );
        
        if (authService == null || userName == null || pwd == null)
            return false;
        
        boolean succsess = false;
        OpenSSOToken token = new OpenSSOToken( authService );
        try {
            succsess = token.login( userName, pwd );
            if (succsess)
                token.logout();
        } catch ( Exception e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return succsess;
    }

    @Override
    public String getAccountType() {
        try {
            OpenToxLogInOutListener listener = OpenToxLogInOutListener.getInstance();
            return listener.getAccountType();
        } catch ( InstantiationException e ) {
            /* If we end up here the listener hasn't been instantiated yet, 
             * but this is done when the plug in is registered in the usermanger
             * => if not done when calling this method then something is done 
             * wrong some where else....*/
            e.printStackTrace();
        }

        return null;
    }
}
