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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Pattern;

import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.text.Html;
import android.text.format.Time;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RoteMemo extends Activity implements SensorEventListener {
//Activity fields -----------------------------------------------------------------------	
    EditText fromEdit;
    EditText toEdit;
    TextView wordText;
    TextView pathText;
    TextView statusText;
    FrameLayout studyLayout;
    Button browse;
    Button clear;
    PaintView paint;
    
    boolean filePicked;
	boolean saved = true;
    
    private String path = ""; 
    private int from = -1;
    private int to = -1;
    private int total = 0;
    private int current = -1;
    
	long startTime;
	//long totalTime;
    
    Random rnd;

    private ArrayList<WordEntry> words = new ArrayList<WordEntry>();

    
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    
    public final void onSensorChanged(SensorEvent event) {
      float distance = event.values[0];
      if (distance >= far) {
    	  RestoreState();
      } else {
    	  SaveState();
      }
    
    }
    
    private  SensorManager sensorManager;
    private  Sensor proximitySensor = null;
    private float far;
    
         

//Activity events -----------------------------------------------------------------------
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		 proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		 if (proximitySensor != null) far = proximitySensor.getMaximumRange();
        studyLayout = (FrameLayout) findViewById(R.id.studyLayout);
        //TextView text = (TextView) findViewById(R.id.);
//    	text.setText("tada");
        setContentView(R.layout.activity_main);
        LinearLayout responseLayout = (LinearLayout) findViewById(R.id.responseLayout);
        responseLayout.setVisibility(View.GONE);
        
        fromEdit = (EditText) findViewById(R.id.fromEditText);
        toEdit = (EditText) findViewById(R.id.toEditText);
        wordText = (TextView) findViewById(R.id.wordTextView);
        pathText = (TextView) findViewById(R.id.pathTextView);
        statusText = (TextView) findViewById(R.id.statusTextView);
        browse = (Button) findViewById(R.id.browseButton);
        clear = (Button) findViewById(R.id.clearButton);
        paint = (PaintView) findViewById(R.id.paintView1);
        rnd = new Random();
        
        startTime = System.currentTimeMillis();
        filePicked = false;
        
        final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        final BroadcastReceiver mReceiver = new ScreenReceiver();
        registerReceiver(mReceiver, filter);
    }
    @SuppressWarnings("unchecked")
	@Override
    public void onResume()
    {
    	super.onResume();
    	if (proximitySensor!= null) sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
    	RestoreState();
    }
    void RestoreState(){
    	if (!saved) return;
    	
    	try
    	{
    		saved = false;
    		if (filePicked)
    		{
    			filePicked = false;
    			return;
    		}
    		//File file = new File(getCacheDir(), "state");
    		FileInputStream stream = openFileInput("state");
    		ObjectInputStream objStream = new ObjectInputStream(stream);
    		path = (String) objStream.readObject();
    		from = objStream.readInt();
    		to = objStream.readInt();
    		startTime = System.currentTimeMillis() - objStream.readLong();
          	current = objStream.readInt();
          	boolean askMode = objStream.readBoolean();
    		words = (ArrayList<WordEntry>)objStream.readObject();
    		objStream.close();
    		
          	fromEdit.setText(Integer.toString(from));
          	toEdit.setText(Integer.toString(to));
          	pathText.setText(path);

            LinearLayout responseLayout = (LinearLayout) findViewById(R.id.responseLayout);
            LinearLayout requestLayout = (LinearLayout) findViewById(R.id.requestLayout);

            if (askMode)
          	{
                responseLayout.setVisibility(View.GONE);
                requestLayout.setVisibility(View.VISIBLE);
          		showKey();
          	}else{
                responseLayout.setVisibility(View.VISIBLE);
                requestLayout.setVisibility(View.GONE);
                showValue();
          	}

            updateStatus();
    	}catch(Exception ex)
    	{
    		wordText.setText(ex.toString());
    	}
    	
    }
    void SaveState()
    {
    	if (saved) return;
    	try
    	{
    		saved = true;
    		
    		//File file = new File(getCacheDir(), "state");
    		FileOutputStream stream = openFileOutput("state", Context.MODE_PRIVATE); 
    		ObjectOutputStream objStream = new ObjectOutputStream(stream);
    		objStream.writeObject(path);
    		objStream.writeInt(from);
    		objStream.writeInt(to);
    		objStream.writeLong(/*totalTime + */System.currentTimeMillis() - startTime);
    		objStream.writeInt(current);

    		LinearLayout requestLayout = (LinearLayout) findViewById(R.id.requestLayout);
    		objStream.writeBoolean(requestLayout.getVisibility() == View.VISIBLE);

    		objStream.writeObject(words);
    		objStream.close();
    		pathText.setText("waiting");
    	}catch(Exception ex)
    	{
    	
    	}
    }
    
    @Override
    public void onPause()
    {
    	super.onPause();
    	if (proximitySensor!=null) 	sensorManager.unregisterListener(this);
  		SaveState();
    }
    @Override
    public void onDestroy()
    {
    	if (!saved)
    	{
    		SaveState();
    		saved = true;
    	}
    	super.onDestroy();
    }
//Button handlers ---------------------------------------------------------------------------
    public void showOnClick(View v)
    {
        LinearLayout responseLayout = (LinearLayout) findViewById(R.id.responseLayout);
        responseLayout.setVisibility(View.VISIBLE);
        LinearLayout requestLayout = (LinearLayout) findViewById(R.id.requestLayout);
        requestLayout.setVisibility(View.GONE);

        showValue();
    	updateStatus();
    }
    private void showValue()
    {
    	if (current > -1)
    	{
    		wordText.setText(Html.fromHtml(words.get(current).Value));
    	}else{
    		wordText.setText("");
    	}
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
    		if (fromI < toI && fromI >0 && toI <= words.size())
    		{
    			Integer i;
    			for (i = 1; i < fromI; i++)
    				words.get(i - 1).Answered = true;
    			for (i = fromI; i <= toI; i++)
    				words.get(i - 1).Answered = false;
    			for (i = toI + 1; i <= words.size(); i++)
    				words.get(i - 1).Answered = true;
    		}
    		from = fromI;
    		to = toI;
    		showNext();
    		updateStatus();
    		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    		imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
    		//browse.requestFocus();
    		//studyLayout.requestFocus();
    	}catch(Exception ex)
    	{
    	
    	}
    }
    public void resetOnClick(View v)
    {
    	fromEdit.setText("1");
   	  	toEdit.setText(Integer.toString(words.size()));
   	  	this.rangeOnClick(v);
    }
    public void clearOnClick(View v)
    {
    	paint.Clear();
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
    	//totalTime = 0;
        updateStatus();
    }
    public void skipOnClick(View v)
    {
    	current = getNext(true);
    	if (current > -1)
    	{
    		wordText.setText(Html.fromHtml(words.get(current).Key));
    	}else{
    		wordText.setText("");
    	}
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
  	        		 words.add(new WordEntry(word, line)); 
  	        	  }
  	        	  
  	          }else if (line.indexOf("_") > -1)
  	          {
  	        	  String[] split = line.split("_");
  	        	  if (split.length>0)
  	        	  {
  	        		  //for(int i = split[0] == "" ? 1 : 0; i<split.length; i+=2)
  	        		  for(int i = 1; i<split.length; i+=2)
  	        		  {
  	        			  words.add(new WordEntry(split[i], line));
  	        		  }
  	        	  }
  	          }else if (line.indexOf("$word['") == 0)
  	          {
  	        	  String[] split = line.split("'");
  				  words.add(new WordEntry(split[1], split[3].replace("</td>", " </td>")));
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
    
    private int getNext(boolean next)
    {
    	paint.Clear();
    	ArrayList<Integer> unanswered = new ArrayList<Integer>();
    	int nextItem = -2;
    	for(int i =0; i < words.size(); i++)
    	{
    		if (!words.get(i).Answered)
    		{
    			unanswered.add(i);
    			if (nextItem == -1) nextItem = i;
    	    }
    		if (current == i) nextItem = -1;
    	}
    	if (unanswered.size() == 0) return -1;

    	if (next)
    	{
    		if (nextItem > -1) return nextItem;
    		return unanswered.get(0);
    	}
   		return unanswered.get(rnd.nextInt(unanswered.size()));
    }
    private void showNext()
    {
    	current = getNext(false);
    	showKey();
    }
    private void showKey()
    {
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

