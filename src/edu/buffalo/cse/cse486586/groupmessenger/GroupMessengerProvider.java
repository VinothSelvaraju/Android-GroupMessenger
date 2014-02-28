package edu.buffalo.cse.cse486586.groupmessenger;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

/**
 * GroupMessengerProvider is a key-value table. Once again, please note that we do not implement
 * full support for SQL as a usual ContentProvider does. We re-purpose ContentProvider's interface
 * to use it as a key-value table.
 * 
 * Please read:
 * 
 * http://developer.android.com/guide/topics/providers/content-providers.html
 * http://developer.android.com/reference/android/content/ContentProvider.html
 * 
 * before you start to get yourself familiarized with ContentProvider.
 * 
 * There are two methods you need to implement---insert() and query(). Others are optional and
 * will not be tested.
 * 
 * @author stevko
 *
 */
public class GroupMessengerProvider extends ContentProvider {

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // You do not need to implement this.
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        /*
         * TODO: You need to implement this method. Note that values will have two columns (a key
         * column and a value column) and one row that contains the actual (key, value) pair to be
         * inserted.
         * 
         * For actual storage, you can use any option. If you know how to use SQL, then you can use
         * SQLite. But this is not a requirement. You can use other storage options, such as the
         * internal storage option that I used in PA1. If you want to use that option, please
         * take a look at the code for PA1.
         */
    	
    	//@vino coding
    	//check if the file is existing
    	if(values!=null){
    		//Existing update
    	}
    	else{
    		//New add
    		String filePath = uri.getPath();
        	String columnValue = values.getAsString(filePath);
        	
        	FileOutputStream fos = null;
        	try {
    			fos = this.getContext().openFileOutput(filePath, Context.MODE_PRIVATE);
    			fos.write(columnValue.getBytes());
    		} catch (IOException e1) {
    			e1.printStackTrace();
    		}
        	finally{
        		try {
    				fos.close();
    			} catch (Exception e) {
    				e.printStackTrace();
    			}
        	}
        	
    	}
    	
    	
    	
        Log.v("insert", values.toString());
        return uri;
    }

    @Override
    public boolean onCreate() {
        // If you need to perform any one-time initialization task, please do it here.
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        /*
         * TODO: You need to implement this method. Note that you need to return a Cursor object
         * with the right format. If the formatting is not correct, then it is not going to work.
         * 
         * If you use SQLite, whatever is returned from SQLite is a Cursor object. However, you
         * still need to be careful because the formatting might still be incorrect.
         * 
         * If you use a file storage option, then it is your job to build a Cursor * object. I
         * recommend building a MatrixCursor described at:
         * http://developer.android.com/reference/android/database/MatrixCursor.html
         */
    	
    	//@author - vino coding begins
    	//build the cursor object
    	String[] columnNames = new String[2];
    	columnNames[0] = "key";
    	columnNames[1] = "value";
    	MatrixCursor cursor = new MatrixCursor(columnNames);
    	
    	FileInputStream fileInputStream = null;
    	int content;
		StringBuilder valueBuilder =  new StringBuilder();
		String value = "";
    	
		
		//file read and store the value read from the file as a String object
    	try {
    		fileInputStream = this.getContext().openFileInput(selection);
			while ((content = fileInputStream.read()) != -1) {
				valueBuilder.append(content);
				System.out.print((char) content);
			}
			value = valueBuilder.toString();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
    	finally{
    		try {
				fileInputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    	
   
    	//building the single row with key as in file name and value as the value read from the file
    	Object[] columnValues = new Object[2];
    	columnValues[0] = uri.getPath();
    	columnValues[1] = value;
    	cursor.addRow(columnValues);
        Log.v("query", selection);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }
}
