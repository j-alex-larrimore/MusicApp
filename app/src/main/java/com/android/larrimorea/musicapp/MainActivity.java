package com.android.larrimorea.musicapp;

import android.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import us.theappacademy.oauth.OAuthParameters;
import us.theappacademy.oauth.util.UrlBuilder;
import us.theappacademy.oauth.view.AuthorizeFragment;
import us.theappacademy.oauth.view.OAuthActivity;


public class MainActivity extends OAuthActivity {

    @Override
    protected Fragment createFragment() {
        AuthorizeFragment authorizeFragment = new AuthorizeFragment();

        OAuthParameters oAuthParameters = new OAuthParameters();
        oAuthParameters.addParameter("client_id", oauthConnection.getClientID());
        oAuthParameters.addParameter("redirect_uri", oauthConnection.getRedirectUrl());
        oAuthParameters.addParameter("response_type", "code");
        oAuthParameters.addParameter("state", UrlBuilder.generateUniqueState(16));

        oauthConnection.state = oAuthParameters.getValueFromParameter("state");

        authorizeFragment.setOAuthParameters(oAuthParameters);

        return authorizeFragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        oauthConnection = new SoundCloudConnection();
        super.onCreate(savedInstanceState);
    }


    @Override
    public void setLayoutView() {

    }

    @Override
    public void replaceCurrentFragment(Fragment newFragment, boolean addToStack) {

    }
}
