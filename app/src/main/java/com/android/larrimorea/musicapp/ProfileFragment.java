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
import android.widget.Toast;

import com.android.larrimorea.musicapp.R;
import com.parse.FindCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import us.theappacademy.oauth.OAuthParameters;
import us.theappacademy.oauth.task.GetRequestTask;
import us.theappacademy.oauth.util.JsonBuilder;
import us.theappacademy.oauth.util.UrlBuilder;
import us.theappacademy.oauth.view.OAuthFragment;


public class ProfileFragment extends OAuthFragment{
    private TextView profileName;
    private TextView userName;
    private ArrayList<Song> songList = new ArrayList<Song>();
    private ListView songView;
    OAuthParameters oAuthParameters;

    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound = false;
    private String mLoadingSong;


    @Override
    public void onTaskFinished(String responseString) {
        JSONObject jsonObject = JsonBuilder.jsonObjectFromString(responseString);
        setJsonObject(jsonObject);

        profileName.setText(responseString);

        try {
            String url = getJsonObject().getString("stream_url"); // your URL here
            String title = getJsonObject().getString("title");
            String artist = getJsonObject().getJSONObject("user").getString("username");
            Song s = new Song(url, title, artist);
            songList.add(s);
            addSongToParse(mLoadingSong, url, title, artist);
            musicSrv.setList(songList);
            musicSrv.playSong(oAuthParameters);
            profileName.setText(url + title + artist);
        }catch(JSONException e){
            Log.e("ProfFrag", "TaskFinished" + e);
        }


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        oAuthParameters = new OAuthParameters();
        oAuthParameters.addParameter("client_id", getOAuthConnection().getClientID());

        getSongList();
        //String song = "12505369";

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
            musicSrv.setClientID(oAuthParameters.toString());
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

    public void getSongList(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Song");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, com.parse.ParseException e) {
                if (e == null) {
                    setSongList(list);
                } else {
                    Log.e("ProfileFragment", "setSongListError: " + e.getMessage());
                }
            }
        });
    }

    public void setSongList(List<ParseObject> list){
        for(ParseObject song : list){
            String url = song.get("url").toString();
            String title = song.get("title").toString();
            String artist = song.get("artist").toString();
            Song s = new Song(url, title, artist);
            songList.add(s);
            Log.i("Frag", "setSongList" + title);
        }

        playSongList();
    }

    public void playSongList(){
        musicSrv.setList(songList);
        musicSrv.playSong(oAuthParameters);
    }

    public void addSong(String songID){
        String url = UrlBuilder.buildUrlWithParameters(getOAuthConnection().getApiUrl() + "/tracks/" + songID + ".json", oAuthParameters);
        mLoadingSong = songID;
        setUrlForApiCall(url);
        new GetRequestTask().execute(this);
    }

    public void songPicked(View view){

    }

    public void addSongToParse(String id, String url, String title, String artist){
        ParseObject song = new ParseObject("Song");
        song.put("songId", id);
        song.put("url", url);
        song.put("title", title);
        song.put("artist", artist);
        song.saveInBackground();
    }
}