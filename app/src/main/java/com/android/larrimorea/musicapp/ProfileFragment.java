package com.android.larrimorea.musicapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.android.larrimorea.musicapp.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import us.theappacademy.oauth.OAuthParameters;
import us.theappacademy.oauth.task.GetRequestTask;
import us.theappacademy.oauth.util.JsonBuilder;
import us.theappacademy.oauth.util.UrlBuilder;
import us.theappacademy.oauth.view.OAuthFragment;


public class ProfileFragment extends OAuthFragment{
    private TextView profileName;
    private TextView userName;
    private ArrayList<Song> songList;
    private ListView songView;

    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound = false;


    @Override
    public void onTaskFinished(String responseString) {
        JSONObject jsonObject = JsonBuilder.jsonObjectFromString(responseString);
        setJsonObject(jsonObject);

        //profileName.setText(responseString);

        try {
            String url = getJsonObject().getString("stream_url"); // your URL here
            String title = getJsonObject().getString("title");
            String artist = getJsonObject().getString("username");

            profileName.setText(url + title + artist);
        }catch(JSONException e){
            Log.e("ProfFrag", "TaskFinished" + e);
        }


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

    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            musicSrv = binder.getService();
            musicSrv.setList(songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        if(playIntent == null){
            playIntent = new Intent(getActivity(), MusicService.class);
            getActivity().bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
        }
    }

    public void songPicked(View view){

    }
}