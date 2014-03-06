package edu.buffalo.cse.cse486586.groupmessenger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.TextView;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 * 
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {
	
	static final int SERVER_PORT = 10000;
	static final String REMOTE_PORT0 = "11108";
    static final String REMOTE_PORT1 = "11112";
    static final String REMOTE_PORT2 = "11116";
    static final String REMOTE_PORT3 = "11120";
    static final String REMOTE_PORT4 = "11124";
    static String myPort = "";
    int localCounter = -1;
	static final String TAG = GroupMessengerActivity.class.getSimpleName();
	private final Uri mUri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger.provider");;
	
	private Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);
        

        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */
        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());
        
        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));
        
      
        
        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        
        
        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            Log.e(TAG, "Can't create a ServerSocket");
            return;
        }	
        
        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs in a total-causal order.
         */
        final EditText editText = (EditText) findViewById(R.id.editText1);
        
        findViewById(R.id.button4).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				String msg = editText.getText().toString() + "\n";
                editText.setText(""); // This is one way to reset the input box.
                TextView localTextView = (TextView) findViewById(R.id.textView1);
                localTextView.append("\t" + msg); // This is one way to display a string.
                TextView remoteTextView = (TextView) findViewById(R.id.textView1);
                remoteTextView.append("\n");
                
               
                localCounter++;

                /*
                 * Note that the following AsyncTask uses AsyncTask.SERIAL_EXECUTOR, not
                 * AsyncTask.THREAD_POOL_EXECUTOR as the above ServerTask does. To understand
                 * the difference, please take a look at
                 * http://developer.android.com/reference/android/os/AsyncTask.html
                 */
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);
      	
			}
        });         
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }
    
    
    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {
    	int sg=0;
        int rg=0;
    	private String readFromSocket(ServerSocket serverSocket){
    		String messageText = "";
			try {
				Socket clientSocket = serverSocket.accept();
				BufferedReader messageIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				messageText = messageIn.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
        	return messageText;
    	}
    	
    	
    	private void writeToSocket(Socket socket, String message){
    		
			try {
				OutputStream outputStream = socket.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
                bufferedWriter.write(message);
                bufferedWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
        	finally{
        		try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
    	}
    	
    	private synchronized void multicast(String messsage){
    		
    		for(int port=11108;port<=11124;port+=4){
            	Socket socket=null;
				try {
					socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),Integer.parseInt(String.valueOf(port)));
				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
            	writeToSocket(socket,messsage);
    		}
    	
    	}
    	
        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];
            
            
            
            ArrayList<String> SequencerMessageList = new ArrayList<String>();
            ArrayList<String> messageList = new ArrayList<String>();
           
            
            String myPort = GroupMessengerActivity.myPort;
            String messageText = "";
            System.out.println("PRINT PORT NO: "+myPort);
            System.out.println("BEFORE WHILE LOOP");
            while(true){
            	System.out.println("TEST BEFORE READFROMSOCKET");
            	
    			try {
    				Socket clientSocket = serverSocket.accept();
    				BufferedReader messageIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    				messageText = messageIn.readLine();
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
            	//String messageText = readFromSocket(serverSocket);
            	Log.e(messageText, "Message received");
            	System.out.println("MY PORT NUMBER: "+myPort);
            	
            	ContentValues cv = new ContentValues();
            	System.out.println("KEY TO BE INSERTED: "+sg);
				cv.put("key",String.valueOf(sg));
				cv.put("value", messageText);
				Log.e("insert", String.valueOf(sg));
				getBaseContext().getContentResolver().insert(mUri, cv);
				sg++;
            	
//				if(myPort.equals("11108")){
//					System.out.println("I M SEQUENCER");
//					//receive Normal message and -> multicast the message to the group
//					if(!messageText.contains("order")){
//						SequencerMessageList.add(messageText);
//						StringBuilder orderMessageBuilder = new StringBuilder();
//						orderMessageBuilder.append("order_");
//						orderMessageBuilder.append(messageText);
//						orderMessageBuilder.append("_"+sg);
//						String orderMessage = orderMessageBuilder.toString();
//					
//						//Multicast
//						multicast(orderMessage);
//						
//						//new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, orderMessage, myPort);
//						sg++;
//					}
//					// receive order message -> deliver it
//					else{
//						String[] orderMessFrag = messageText.split("_");
//						if(SequencerMessageList.contains(orderMessFrag[1])){
//							System.out.println("DELIVER - "+ orderMessFrag[1]);
//							
//							// normal message they receive over the socket - Need to deliver it - insert into content provider	
//							ContentValues cv = new ContentValues();
//							cv.put("key",String.valueOf(sg));
//							cv.put("value", orderMessFrag[1]);
//							Log.e("insert", String.valueOf(sg));
//							getBaseContext().getContentResolver().insert(mUri, cv);
//						}
//					}
//				}
//				else{
//					System.out.println("I M GROUP MEMBER");
//					//receive order message
//					if(messageText.contains("order")){
//						System.out.println("GROUP MEMBER - RECEIVED ORDER MESSAGE");
//						String[] orderMessFrag = messageText.split("_");
//						System.out.println("PRINT rg and sg AT GROUP MEMBER SIDE: rg: "+rg+" sg: "+sg);
//						if(messageList.contains(orderMessFrag[1]) && rg == sg){
//							System.out.println("DELIVER - "+ orderMessFrag[1]);
//							
//							ContentValues cv = new ContentValues();
//							cv.put("key",String.valueOf(sg));
//							cv.put("value", orderMessFrag[1]);
//							Log.e("insert", String.valueOf(sg));
//							getBaseContext().getContentResolver().insert(mUri, cv);
//							
//							rg=sg+1;
//						}
//					}
//					else{
//						//receive normal message - put it in a list
//						System.out.println("GROUP MEMBER - RECEIVED NORMAL MESSAGE AND ADDED TO LIST: "+ messageText);
//						messageList.add(messageText);
//					
//					}
//					
//
//					
//				}
				

				//System.out.println("Input Message: "+ messageText);
				publishProgress(messageText);
            }
        }

        protected void onProgressUpdate(String...strings) {
            /*
             * The following code displays what is received in doInBackground().
             */
            String strReceived = strings[0].trim();
            TextView remoteTextView = (TextView) findViewById(R.id.textView1);
            remoteTextView.append(strReceived + "\t\n");
            TextView localTextView = (TextView) findViewById(R.id.textView1);
            localTextView.append("\n");
            
            /*
             * The following code creates a file in the AVD's internal storage and stores a file.
             * 
             * For more information on file I/O on Android, please take a look at
             * http://developer.android.com/training/basics/data-storage/files.html
             */
            
            String filename = "SimpleMessengerOutput";
            String string = strReceived + "\n";
            FileOutputStream outputStream;

            try {
                outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(string.getBytes());
                outputStream.close();
            } catch (Exception e) {
                Log.e(TAG, "File write failed");
            }

            return;
        }
    }
    
    
    private class ClientTask extends AsyncTask<String, Void, Void> {
    	
    	private synchronized void writeToSocket(Socket socket, String message){
			try {
				OutputStream outputStream = socket.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
                bufferedWriter.write(message);
                bufferedWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
        	finally{
        		try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
    	}
    	
    	private ArrayList<String> remotePortComp(String inputMessage){
    		ArrayList<String> remotePortList = new ArrayList<String>();
    		remotePortList.add(REMOTE_PORT0);
          	remotePortList.add(REMOTE_PORT1);
          	remotePortList.add(REMOTE_PORT2);
          	remotePortList.add(REMOTE_PORT3);
          	remotePortList.add(REMOTE_PORT4);
          	return remotePortList;
    	}
    	
    	
        @Override
        protected Void doInBackground(String... msgs) {
            try {
                
            	//ArrayList<String> remotePortList = remotePortComp(msgs[1]);
                Log.e(msgs[1], "Port no: ");
                               
                //for(String itr:remotePortList){
                for(int port=11108;port<=11124;port+=4){
                	Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),Integer.parseInt(String.valueOf(port)));

            		StringBuffer sb = new StringBuffer();
                    sb.append(msgs[0].trim());
                    //sb.append(" ");
                    //sb.append(msgs[1]);
                   // sb.append("_");
                    //sb.append("AVD");
                    //sb.append(i);
                    //sb.append("_");
                    //sb.append(localCounter);
                    String finalMessage = sb.toString();
                    System.out.println("MESSAGE FROM CLIENT SOCKET: "+ finalMessage);
                   // try {
    				OutputStream outputStream = socket.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
                    bufferedWriter.write(finalMessage);
                    bufferedWriter.flush();
                    outputStream.close();
                    socket.close();
        			//} catch (IOException e) {
        			//	e.printStackTrace();
        			//}
//                	finally{
//                		try {
//        					socket.close();
//        				} catch (IOException e) {
//        					e.printStackTrace();
//        				}
//                	}
                   //writeToSocket(socket,finalMessage);  

                }
               
            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
            	e.printStackTrace();
                Log.e(TAG, "ClientTask socket IOException");
            }
            return null;
        }
    }	
}
