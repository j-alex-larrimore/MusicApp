package com.android.larrimorea.musicapp;

import us.theappacademy.oauth.OAuthApplication;
import us.theappacademy.oauth.OAuthConnection;
import us.theappacademy.oauth.OAuthProvider;

/**
 * Created by Alex on 7/6/2015.
 */
public class SoundCloudConnection extends OAuthConnection {
    @Override
    protected OAuthProvider createOAuthProvider() {
       // new OAuthProvider()
        return new OAuthProvider("https://api.soundcloud.com",
                "https://soundcloud.com/connect",
                "https://api.soundcloud.com/oauth2/token",
                "https://127.0.0.1");
    }

    @Override
    protected OAuthApplication createOAuthApplication() {
        return new OAuthApplication("0e529480471dac5a7706e37eef82e1a7", "ddb3718a0ba1b087e4b00ced2cf659bf");
    }
}
