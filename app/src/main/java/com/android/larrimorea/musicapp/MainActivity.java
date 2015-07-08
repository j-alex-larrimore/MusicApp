package com.android.larrimorea.musicapp;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.parse.Parse;

import us.theappacademy.oauth.OAuthParameters;
import us.theappacademy.oauth.util.UrlBuilder;
import us.theappacademy.oauth.view.AuthorizeFragment;
import us.theappacademy.oauth.view.OAuthActivity;


public class MainActivity extends OAuthActivity {
    Fragment mFragment;

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
        mFragment = new ProfileFragment();

        Parse.enableLocalDatastore(this);

        Parse.initialize(this, "geQCZnYWdu5r9622GIzP5Cgz1NTKjAVLn3Dobgdn", "QovVsD8wtmEjfrSkGWxyZRbtvOn8afaIqIBHa0Se");

    }


    @Override
    public void setLayoutView() {

        replaceCurrentFragment(mFragment, false);
    }

    @Override
    public void replaceCurrentFragment(Fragment newFragment, boolean addToStack) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

        fragmentTransaction.replace(R.id.fragmentContainer, newFragment);
        if(addToStack){
            fragmentTransaction.addToBackStack(null);
        }
        fragmentTransaction.commit();
    }

    private void songPicked(View view){
        Log.i("Song Picked", "Song Picked");
    }
}
