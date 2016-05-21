package com.mate.music;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import com.mate.music.R;
import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class MainActivity extends ListActivity {

	private static final int UPDATE_FREQUENCY = 500;
	private static final int STEP_VALUE = 9000;
	
	private MediaCursorAdapter mediaAdapter = null;
	private TextView selectedFile = null;
	private SeekBar seekBar = null;
	private MediaPlayer player = null;
	private ImageButton playButton = null;
	private ImageButton prevButton = null;
	private ImageButton nextButton = null;
	private Cursor cursor;
	private boolean isStarted = true;
	private String currentFile ="";
	private boolean isMooveingSeekbar = false;
	//private ArrayList<File> songList;
	//private String[] items;
	
	private final Handler handler = new Handler();
	
	private final Runnable updatePositionRunable = new Runnable(){
		public void run(){
			updatePosition();
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		player = new MediaPlayer();

        selectedFile = (TextView) findViewById(R.id.selected_file);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        playButton = (ImageButton) findViewById(R.id.play_button);
        prevButton = (ImageButton) findViewById(R.id.prev_button);
        nextButton = (ImageButton) findViewById(R.id.next_button);
        
        
        
        player.setOnCompletionListener(onCompletion);
        player.setOnErrorListener(onError);
        seekBar.setOnSeekBarChangeListener(seekBarChanged);
        
        cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
        
        if(null != cursor){
        	cursor.moveToFirst();
        //songList = findSong();

        	mediaAdapter = new MediaCursorAdapter(this, R.layout.list_item, cursor);
        	setListAdapter(mediaAdapter);

        	playButton.setOnClickListener(onButtonClick);
        	nextButton.setOnClickListener(onButtonClick);
        	prevButton.setOnClickListener(onButtonClick);
        }
	}
	/*private ArrayList<File> findSong() {
		Cursor cursor;
        ArrayList<File> songList = new ArrayList<File>();
        Uri allSongsUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        //String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
       // if (isSdPresent()) {
            cursor = managedQuery(allSongsUri, null, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {

                        songList.addAll((Collection<? extends File>) cursor);
                    } while (cursor.moveToNext());
                    return songList;
                }
            return songList;
	}*/

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
	
			return true;
	}
	
	protected void onListItemClick(ListView list, View view, int position, long id) {
  	 super.onListItemClick(list, view, position, id);
   	 
   	 currentFile = (String) view.getTag();
   	 startPlay(currentFile);
   	
  }
   
   @Override
   protected void onDestroy() {
   	super.onDestroy();
   	handler.removeCallbacks(updatePositionRunable);
   	player.stop();
   	player.reset();
   	player.release();
   	
   	player = null;
   	
   }
   
   private void startPlay(String file){
   	Log.i("Selected: ",file);
   	
   	selectedFile.setText(file);
   	seekBar.setProgress(0);
   	player.stop();
   	player.reset();

   	try{
   		player.setDataSource(file);
   		player.prepare();
   		player.start();
   		
   	}catch (IllegalArgumentException e){
   		e.printStackTrace();
   	}catch (IllegalStateException e) {
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
   	
   	seekBar.setMax(player.getDuration());
   	playButton.setImageResource(R.drawable.ic_pause_button);
   	
   	updatePosition();
   	isStarted = true;
   	
   }
   
   private void stopPlay(){
   	player.stop();
   	player.reset();
   	playButton.setImageResource(R.drawable.ic_play_button);
   	handler.removeCallbacks(updatePositionRunable);
   	seekBar.setProgress(0);
   	isStarted = false;
   	
   }
   
   private void updatePosition(){
   	handler.removeCallbacks(updatePositionRunable);
   	seekBar.setProgress(player.getCurrentPosition());
   	handler.postDelayed(updatePositionRunable, UPDATE_FREQUENCY);
   }
   
   private View.OnClickListener onButtonClick = new View.OnClickListener(){
   	@Override
   	public void onClick(View v) {
   		switch (v.getId()){
   		case R.id.play_button: {
   			if(player.isPlaying()){
   				handler.removeCallbacks(updatePositionRunable);
   				player.pause();
   				playButton.setImageResource(R.drawable.ic_play_button);
   			}else{
   				if(isStarted){
   					player.start();
   					playButton.setImageResource(R.drawable.ic_pause_button);
   					updatePosition();
   				}else{
   					startPlay(currentFile);
   				}
   			}
   			break;
   		}
   		case R.id.next_button: {   			
   			player.seekTo(player.getCurrentPosition() + STEP_VALUE);
   			//player.selectTrack(cursor.getPosition()+1);
   			//player.getSelectedTrack(cursor.getPosition()+1);
   			
   			player.start();
     			break;
   			
 
   				
   		}
   			case R.id.prev_button :{
   				player.seekTo(player.getCurrentPosition() - STEP_VALUE);
   				/*int seekto = player.getCurrentPosition() - STEP_VALUE;
   				if(seekto < 0){
   					seekto = 0;
   					
   					player.pause();
   					player.seekTo(seekto);
   					player.start();
   					*/
   					break;
   					
   				}
   			}
   		}
   	};
   private MediaPlayer.OnCompletionListener onCompletion = new MediaPlayer.OnCompletionListener() {
		
		@Override
		public void onCompletion(MediaPlayer mp) {
			stopPlay();
			
		}
	};
	
	private MediaPlayer.OnErrorListener onError = new MediaPlayer.OnErrorListener() {
		
		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {
			// TODO Auto-generated method stub
			return false;
		}
	};
	
	private SeekBar.OnSeekBarChangeListener seekBarChanged = new SeekBar.OnSeekBarChangeListener() {
		
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			isMooveingSeekbar = false;
			
		}
		
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			isMooveingSeekbar = true;
			
		}
		
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			if(isMooveingSeekbar){
				player.seekTo(progress);
				
				Log.i("OnSeekBarChangeListener", "OnProgressChanged");
			}
			
		}
	};
	
}
