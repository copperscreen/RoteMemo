package com.example.words;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import java.util.Stack;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import java.io.File;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.support.v4.app.NavUtils;
import android.text.format.Time;

public class Filelist extends Activity {

	ArrayList<String> files;
	String path;
	ArrayAdapter<String> arrayAdapter;

	
	 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filelist);
        //getActionBar().setDisplayHomeAsUpEnabled(true);

        File dir;


        files = new ArrayList<String>();
        path = "";
        final ListView fileList=(ListView)findViewById(R.id.listView1);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
        	try
        	{
        		path = extras.getString("path");
        		
        		if (!"".equals(path))
        		{
	            	dir = new File(path);
	        		
	            	if (dir.isFile()) {
	            		dir = dir.getParentFile();
	            		path = dir.getPath();
	            	}
	            	files.addAll(Arrays.asList(dir.list()));
        		}
        	}catch(Exception ex)
        	{
        		files.clear();
        		path = "";
        	}
        } 
        if ("".equals(path))
        {
            dir = new File(Environment.getExternalStorageDirectory().toString());
            files.addAll(Arrays.asList(dir.list()));
        }


/*
        final Stack<Integer> colors = new Stack<Integer>();
        colors.push(Color.RED);
        colors.push(Color.GREEN);
        colors.push(Color.BLUE);
        colors.push(Color.YELLOW);
        colors.push(Color.CYAN);
  */      
        initFileList();
        
        arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, files){
        	
        	@Override
        	public View getView(int position, View convertView, ViewGroup parent) {
        	    View view = super.getView(position, convertView, parent);
        	    TextView text = (TextView) view.findViewById(android.R.id.text1);
                String fname = (String) fileList.getItemAtPosition(position);
        	    File file = new File(path + "/" + fname);
        	    if (file.isDirectory())
        	    {
        	    	text.setBackgroundColor(Color.rgb(225, 227, 188));
        	    }else
        	    {
        	    	text.setBackgroundColor(Color.TRANSPARENT);
        	    }
        	      //text.setTextColor(colors.pop());
        	    return view;
        	  }
        };
                // Set The Adapter
                fileList.setAdapter(arrayAdapter);
                
                fileList.setOnItemClickListener(new OnItemClickListener() {
                    //@Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	                    String fname = (String) fileList.getItemAtPosition(position);
	                    String fullpath = path + "/" + fname;
	                    File file = new File(fullpath);
	                    if (file.isFile())
	                    {
	                    	done(RESULT_OK, fullpath);
	                    }else if (file.isDirectory())
	                    {
	                    	path = fullpath;
		                    initFileList();
		        	    	arrayAdapter.notifyDataSetChanged();
	                    }
                    }
                });
    }
    
    private ArrayList<String> sortedFiles(String[] files)
    {
    	class CustomComparator implements Comparator<String> {
    	    public int compare(String filename1, String filename2) {
    	        Boolean isDir1 = (new File(path + "/" + filename1)).isDirectory();
    	        Boolean isDir2 = (new File(path + "/" + filename2)).isDirectory();
    	        if (isDir1 == isDir2) return filename1.compareTo(filename2);
    	        return isDir1 ? -1: 1;
    	    }
    	}
    	ArrayList<String> result= new ArrayList<String>(Arrays.asList(files));
    	Collections.sort(result, new CustomComparator());
    	return result;
    }
    private void initFileList()
    {
    	files.clear();
    	File dir;
    	try
    	{
    		if (path != "" )
    		{
            	dir = new File(path);
        		
            	if (dir.isFile()) dir = dir.getParentFile();
            	files.addAll(sortedFiles(dir.list()));
    		}
    	}catch(Exception ex)
    	{
    		files.clear();
    		path = "";
    	}
    	if (path == "")
    	{
    		dir = new File(Environment.getExternalStorageDirectory().toString());
    		path = dir.getPath();
    		files.addAll(sortedFiles(dir.list()));
    	}
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_filelist, menu);
        return true;
    }

    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case android.R.id.home:
//                NavUtils.navigateUpFromSameTask(this);
//                return true;
//        }
        return super.onOptionsItemSelected(item);
    }
    public void upOnClick(View v)
    {   
    	File dir = new File(path);
		
    	dir = dir.getParentFile();
    	if (dir != null)
    	{
	    	path = dir.getPath();
	    	initFileList();
	    	arrayAdapter.notifyDataSetChanged();
    	}
    }
    public void cancelOnClick(View v)
    {   
    	done(RESULT_CANCELED, "");
    }
    void done(int code, String result)
    {
    	Intent intent = new Intent();
   	    intent.putExtra("path", result);
   	    setResult(code, intent);
   	    finish();
    }
}
