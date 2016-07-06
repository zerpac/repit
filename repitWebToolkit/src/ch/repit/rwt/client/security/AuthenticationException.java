package ch.repit.rwt.client.security;

/**
 *
 * @author tc149752
 */
public class AuthenticationException extends SecurityException {

    private String nickname;
    private String authDomain;

    private AuthenticationException() {
        super();
    }

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, String nickname, String authDomain) {
        super(message);
        this.nickname = nickname;
        this.authDomain = authDomain;
    }

    public String getAuthDomain() {
        return authDomain;
    }
    
    public String getNickname() {
        return nickname;
    }



}
