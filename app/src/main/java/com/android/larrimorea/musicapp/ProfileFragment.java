package com.android.larrimorea.musicapp;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.larrimorea.musicapp.R;

import java.io.IOException;

import us.theappacademy.oauth.OAuthParameters;
import us.theappacademy.oauth.task.GetRequestTask;
import us.theappacademy.oauth.util.UrlBuilder;
import us.theappacademy.oauth.view.OAuthFragment;

/**
 * Created by Alex on 7/6/2015.
 */
public class ProfileFragment extends OAuthFragment {
    private TextView profileName;
    private TextView userName;


    @Override
    public void onTaskFinished(String responseString) {
        profileName.setText(responseString);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        OAuthParameters oAuthParameters = new OAuthParameters();
        oAuthParameters.addParameter("client_id", getOAuthConnection().getClientID());


        String song = "12505369";

        String url = UrlBuilder.buildUrlWithParameters(getOAuthConnection().getApiUrl() + "/tracks/" + song + ".json", oAuthParameters);
        setUrlForApiCall(url);
        new GetRequestTask().execute(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_profile, container, false);

        profileName = (TextView)fragmentView.findViewById(R.id.profileName);
        userName = (TextView)fragmentView.findViewById(R.id.userName);

        return fragmentView;
    }
}
