package com.example.words;

//import com.example.test01.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Pattern;

import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.text.Html;
import android.text.format.Time;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RoteMemo extends Activity implements SensorEventListener {
//Activity fields -----------------------------------------------------------------------	
    EditText fromEdit;
    EditText toEdit;
    TextView largeText;
    TextView wordText;
    TextView pathText;
    TextView statusText;
    
    Boolean filePicked;
    
    private String path = ""; 
    private int from = -1;
    private int to = -1;
    private int total = 0;
    private int current = -1;
    
	long startTime;
	long totalTime;
    
    Random rnd;
    private class Pair{
    	public String Key;
    	public String Value;
    	public boolean Answered;
    	public Pair(String key, String value)
    	{
    		this.Key = key;
    		this.Value = value;
    		Answered = false;
    	}
    }
    private ArrayList<Pair> words = new ArrayList<Pair>();

    
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
      // Do something here if sensor accuracy changes.
    }

    
    public final void onSensorChanged(SensorEvent event) {
      float distance = event.values[0];
      // Do something with this sensor data.
    }


//Activity events -----------------------------------------------------------------------
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout studyLayout = (FrameLayout) findViewById(R.id.studyLayout);
        //TextView text = (TextView) findViewById(R.id.);
//    	text.setText("tada");
        setContentView(R.layout.activity_main);
        LinearLayout responseLayout = (LinearLayout) findViewById(R.id.responseLayout);
        responseLayout.setVisibility(View.GONE);
        
        fromEdit = (EditText) findViewById(R.id.fromEditText);
        toEdit = (EditText) findViewById(R.id.toEditText);
        largeText = (TextView) findViewById(R.id.largeTextView);
        wordText = (TextView) findViewById(R.id.wordTextView);
        pathText = (TextView) findViewById(R.id.pathTextView);
        statusText = (TextView) findViewById(R.id.statusTextView);
        rnd = new Random();
        
        startTime = System.currentTimeMillis();
        filePicked = false;
        
        final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        final BroadcastReceiver mReceiver = new ScreenReceiver();
        registerReceiver(mReceiver, filter);

    }
    @Override
    public void onResume()
    {
    	super.onResume();
    	try
    	{
    		if (filePicked)
    		{
    			filePicked = false;
    			return;
    		}
    		//File file = new File(getCacheDir(), "state");
    		FileInputStream stream = new FileInputStream("state");
    		ObjectInputStream objStream = new ObjectInputStream(stream);
    		path = (String) objStream.readObject();
    		from = objStream.readInt();
    		to = objStream.readInt();
    		totalTime = objStream.readLong();
    		words = (ArrayList<Pair>) objStream.readObject();
    		objStream.close();
    	}catch(Exception ex)
    	{
    	
    	}
    }
    @Override
    public void onPause()
    {
    	super.onPause();
    	try
    	{
    		
    		//File file = new File(getCacheDir(), "state");
    		FileOutputStream stream = new FileOutputStream("state", false);
    		ObjectOutputStream objStream = new ObjectOutputStream(stream);
    		objStream.writeObject(path);
    		objStream.writeInt(from);
    		objStream.writeInt(to);
    		objStream.writeLong(totalTime + System.currentTimeMillis() - startTime);
    		objStream.writeObject(words);
    		objStream.close();
    	}catch(Exception ex)
    	{
    	
    	}
    }
//Button handlers ---------------------------------------------------------------------------
    public void showOnClick(View v)
    {
        LinearLayout responseLayout = (LinearLayout) findViewById(R.id.responseLayout);
        responseLayout.setVisibility(View.VISIBLE);
        LinearLayout requestLayout = (LinearLayout) findViewById(R.id.requestLayout);
        requestLayout.setVisibility(View.GONE);
        
    	if (current > -1)
    	{
    		largeText.setText(Html.fromHtml(words.get(current).Value));
    	}else{
    		largeText.setText("");
    	}
    	updateStatus();
    }
    
    public void rightOnClick(View v)
    {
    	if (current > -1)
    	{
    		words.get(current).Answered = true;
    	}
    	answer();
    }
    public void wrongOnClick(View v)
    {
    	answer();
    }
    
    public void rangeOnClick(View v)
    {
    	try
    	{
    		Integer fromI = Integer.parseInt(fromEdit.getText().toString());
    		Integer toI = Integer.parseInt(toEdit.getText().toString());
    		if (fromI < toI && fromI >=0 && toI < words.size())
    		{
    			Integer i;
    			for (i = 0; i < fromI; i++)
    				words.get(i).Answered = true;
    			for (i = fromI; i < toI; i++)
    				words.get(i).Answered = false;
    			for (i = toI + 1; i < words.size(); i++)
    				words.get(i).Answered = true;
    		}
    		from = fromI;
    		to = toI;
    		showNext();
    		updateStatus();
    	}catch(Exception ex)
    	{
    	
    	}
    }
    public void resetOnClick(View v)
    {
    	if (from < to && from >=0 && to < words.size())
		{
			Integer i;
			for (i = 0; i < words.size(); i++)
				words.get(i).Answered = false;
			from = 0;
			to = words.size() - 1;
    		showNext();
    		updateStatus();
		}
    }
    public void browseOnClick(View v)
    {
    	Intent intent = new Intent(this, Filelist.class);
    	intent.putExtra("path", path);
        startActivityForResult(intent, 1);
    }
    public void timerOnClick(View v)
    {
        startTime = System.currentTimeMillis();
    	totalTime = 0;
        updateStatus();
    }
//---------------------------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    private void answer()
    {
        LinearLayout responseLayout = (LinearLayout) findViewById(R.id.responseLayout);
        responseLayout.setVisibility(View.GONE);
        LinearLayout requestLayout = (LinearLayout) findViewById(R.id.requestLayout);
        requestLayout.setVisibility(View.VISIBLE);
    	showNext();
    	updateStatus();
    }
    private void loadFile()
    {
        words.clear();
        try{
        	pathText.setText(path);
  	      LineNumberReader reader = new LineNumberReader(new FileReader(path));
  	      String line;
  	      while ((line = reader.readLine()) != null) {
  	          if (line.indexOf("\t") > -1)
  	          {
  	        	  for(String word : line.split("\t") )
  	        	  {
  	        		 words.add(new Pair(word, line)); 
  	        	  }
  	        	  
  	          }else if (line.indexOf("_") > -1)
  	          {
  	        	  String[] split = line.split("_");
  	        	  if (split.length>0)
  	        	  {
  	        		  //for(int i = split[0] == "" ? 1 : 0; i<split.length; i+=2)
  	        		  for(int i = 1; i<split.length; i+=2)
  	        		  {
  	        			  words.add(new Pair(split[i], line));
  	        		  }
  	        	  }
  	          }else if (line.indexOf("$word['") == 0)
  	          {
  	        	  String[] split = line.split("'");
  				  words.add(new Pair(split[1], split[3]));
  	          }
  	      }
        }catch(FileNotFoundException ex)
        {
        
        } catch (IOException e) {
  		// TODO Auto-generated catch block
  		
        
        }
    	
    }
    private void reset()
    {
        TextView status = (TextView) findViewById(R.id.pathTextView);
        status.setText(Integer.toString(words.size()) + ";" + path);
        
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      filePicked = true;
      if (data == null) {return;}
      if (resultCode != RESULT_OK) {return;}
      path = data.getStringExtra("path");
      loadFile();
      
      if (words.size()>0)
      {
      	  fromEdit.setText("1");
        	  toEdit.setText(Integer.toString(words.size()));
      }else
      {
      	fromEdit.setText("");
      	toEdit.setText("");
      }
      
      showNext();
      updateStatus();
      //BufferedReader reader = new BufferedReader(
      //tvName.setText("Your name is " + name);
    }
    
    private int getNext()
    {
    	ArrayList<Integer> unanswered = new ArrayList<Integer>();
    	for(int i =0; i < words.size(); i++)
    	{
    		if (!words.get(i).Answered)
    		{
    			unanswered.add(i);
    		}
    	}
    	if (unanswered.size() == 0) return -1;
    	return unanswered.get(rnd.nextInt(unanswered.size()));
    }
    private void showNext()
    {
    	current = getNext();
    	if (current > -1)
    	{
    		wordText.setText(Html.fromHtml(words.get(current).Key));
    	}else{
    		wordText.setText("");
    	}
    }
    private void updateStatus()
    {
    	int left = 0;
    	int size = words.size();
    	for(int i =0; i < size; i++)
    	{
    		if (!words.get(i).Answered) left++;
    	}
        Time period  = new Time(Time.TIMEZONE_UTC);
        period.set(System.currentTimeMillis() - startTime);
        
    	statusText.setText(period.format("%H:%M") + "    " +  String.valueOf(left) + "/" + String.valueOf(size));
    }
}

