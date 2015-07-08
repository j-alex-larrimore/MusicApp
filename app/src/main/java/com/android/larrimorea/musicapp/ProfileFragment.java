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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.MediaController;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import us.theappacademy.oauth.OAuthParameters;
import us.theappacademy.oauth.task.GetRequestTask;
import us.theappacademy.oauth.util.JsonBuilder;
import us.theappacademy.oauth.util.UrlBuilder;
import us.theappacademy.oauth.view.OAuthFragment;


public class ProfileFragment extends OAuthFragment implements MediaController.MediaPlayerControl, View.OnClickListener {
    private ArrayList<Song> songList = new ArrayList<Song>();
    private ListView songView;
    OAuthParameters oAuthParameters;

    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound = false;
    private String mLoadingSong;
    private View fragmentView;

    private MusicController mController;
    private boolean paused = false;
    private boolean playbackPaused = false;

    @Override
    public void onTaskFinished(String responseString) {
        JSONObject jsonObject = JsonBuilder.jsonObjectFromString(responseString);
        setJsonObject(jsonObject);

        try {
            String url = getJsonObject().getString("stream_url"); // your URL here
            String title = getJsonObject().getString("title");
            String artist = getJsonObject().getJSONObject("user").getString("username");
            Song s = new Song(url, title, artist);
            songList.add(s);
            addSongToParse(mLoadingSong, url, title, artist);
            musicSrv.setList(songList);
            musicSrv.playSong();
        }catch(JSONException e){
            Log.e("ProfFrag", "TaskFinished" + e);
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        oAuthParameters = new OAuthParameters();
        oAuthParameters.addParameter("client_id", getOAuthConnection().getClientID());


        //String song = "12505369";

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_profile, container, false);

        songView = (ListView)fragmentView.findViewById(R.id.song_list);
        songView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View clickView,
                                    int position, long id) {
                songPicked(clickView);
            }
        });

        setController();

        getSongList();

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
            musicSrv.setoAuthParameters(oAuthParameters);
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

    private void getSongList(){
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

    private void setSongList(List<ParseObject> list){
        for(ParseObject song : list){
            String url = song.get("url").toString();
            String title = song.get("title").toString();
            String artist = song.get("artist").toString();
            Song s = new Song(url, title, artist);
            songList.add(s);
            Log.i("Frag", "setSongList " + title);
        }
//        addSong("");
//        addSong("");
//        addSong("");
        displaySongs();
        playSongList();
    }

    private void displaySongs(){
        SongAdapter songAdt = new SongAdapter(getActivity(), songList);
        songView.setAdapter(songAdt);
    }

    private void playSongList(){
        musicSrv.setList(songList);
        musicSrv.playSong();
    }

    private void addSong(String songID){
        String url = UrlBuilder.buildUrlWithParameters(getOAuthConnection().getApiUrl() + "/tracks/" + songID + ".json", oAuthParameters);
        mLoadingSong = songID;
        setUrlForApiCall(url);
        new GetRequestTask().execute(this);
    }

    public void songPicked(View view){
        musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
        musicSrv.playSong();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        mController.show(0);
    }

    private void addSongToParse(String id, String url, String title, String artist){
        ParseObject song = new ParseObject("Song");
        song.put("songId", id);
        song.put("url", url);
        song.put("title", title);
        song.put("artist", artist);
        song.saveInBackground();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_shuffle:
                //shuffle
                break;
            case R.id.action_end:
                musicSrv.stopService(playIntent);
                musicSrv=null;
                System.exit(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        musicSrv.stopService(playIntent);
        musicSrv=null;
        super.onDestroy();
    }

    @Override
    public void start() {
        musicSrv.go();
    }

    @Override
    public void pause() {
        playbackPaused=true;
        musicSrv.pausePlayer();
    }

    @Override
    public int getDuration() {
        if(musicSrv!=null && musicBound && musicSrv.isPng()){
            return musicSrv.getDur();
        }
        else return 0;
    }

    @Override
    public int getCurrentPosition() {
        if(musicSrv!=null && musicBound && musicSrv.isPng()){
            return musicSrv.getPosn();
        }
        else return 0;
    }

    @Override
    public void seekTo(int pos) {
        musicSrv.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        if(musicSrv!=null && musicBound){
            return musicSrv.isPng();
        }
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    private void setController(){
        mController = new MusicController(getActivity());
        mController.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });

        mController.setMediaPlayer(this);
        mController.setAnchorView(fragmentView.findViewById(R.id.song_list));
        mController.setEnabled(true);
    }

    //play next
    private void playNext(){
        musicSrv.playNext();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        mController.show(0);
    }

    //play previous
    private void playPrev(){
        musicSrv.playPrev();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        mController.show(0);
    }

    @Override
    public void onPause() {
        super.onPause();
        paused=true;
    }

    @Override
    public void onStop() {
            mController.hide();
            super.onStop();

    }

    @Override
    public void onResume() {
        super.onResume();
        if(paused){
            setController();
            paused=false;
        }
    }

    @Override
    public void onClick(View v) {
        songPicked(v);
    }
}