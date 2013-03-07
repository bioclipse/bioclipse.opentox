package net.bioclipse.opentox.business;

import java.util.HashMap;

import net.bioclipse.opentox.OpenToxLogInOutListener;
import net.bioclipse.usermanager.ITestAccountLogin;

import org.apache.log4j.Logger;
import org.opentox.aa.opensso.OpenSSOToken;

/**
 * This class tests if its possibly to log in to OpenTox with the properties
 * provided in a hash map.
 * 
 * @author klasjonsson
 *
 */
public class TestLogin implements ITestAccountLogin {
    
    private Logger logger = Logger.getLogger( this.getClass() );
    
    public TestLogin() {    }
    
    /**
     * Tries to login and if is succeeded it logs out immediately.
     * @param A hash map containing the properties that are to be tested 
     */
    public boolean login(HashMap<String, String> myProperites) {
        /* TODO The key-values should be obtained from plugin.xml, if possibly*/
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
            logger.error( "Something went wrong when trying to into OpenTox:" +
                    e.getMessage() );
        }
        
        return succsess;
    }

    /**
     * A method for identify the test-class. I.e. it returns the name of the 
     * plug-in.
     * 
     * @return The name of the plug-in, i.e. "OpenTox"
     */
    @Override
    public String getAccountType() {
        try {
            OpenToxLogInOutListener listener = OpenToxLogInOutListener
                    .getInstance();
            return listener.getAccountType();
        } catch ( InstantiationException e ) {
            /* If we end up here the listener hasn't been instantiated yet, 
             * but this is done when the plug in is registered in the usermanger
             * => if not done when calling this method then something is done 
             * wrong some where else....*/
            logger.error( "Tried to log in to OpenTox before the listener" +
            		" was instantiated, should be impossibly: "+e.getMessage());
        }

        return null;
    }
}
