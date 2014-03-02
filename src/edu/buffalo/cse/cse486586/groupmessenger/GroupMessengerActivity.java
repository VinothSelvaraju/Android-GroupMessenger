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

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
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
	static final String TAG = GroupMessengerActivity.class.getSimpleName();
	
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
        
        System.out.println("TEST");
        
        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        
        
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
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
       
                String msg = editText.getText().toString() + "\n";
                editText.setText(""); // This is one way to reset the input box.
                TextView localTextView = (TextView) findViewById(R.id.textView1);
                localTextView.append("\t" + msg); // This is one way to display a string.
                TextView remoteTextView = (TextView) findViewById(R.id.textView1);
                remoteTextView.append("\n");

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

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];
            
            /*
             * TODO: Fill in your server code that receives messages and passes them
             * to onProgressUpdate().
             */
            //Vino code
            while(true){
            	try {
                	Socket clientSocket = serverSocket.accept();
                	BufferedReader messageIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                	String messageText = messageIn.readLine();
                	Log.e(messageText, "Message received");
                	//System.out.println("Input Message: "+ messageText);
                	publishProgress(messageText);
                } catch (IOException e) {
    				e.printStackTrace();
    			}
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

        @Override
        protected Void doInBackground(String... msgs) {
            try {
                //String remotePort = REMOTE_PORT0;
            	
                ArrayList<String> remotePortList = new ArrayList<String>();
                Log.e(msgs[1], "Port no: ");
                System.out.println("TEST<><><><><><"+msgs[1]);
                if (msgs[1].equals(REMOTE_PORT0)){
                	
                	remotePortList.add(REMOTE_PORT1);
                	remotePortList.add(REMOTE_PORT2);
                	remotePortList.add(REMOTE_PORT3);
                	remotePortList.add(REMOTE_PORT4);
                }
                   
                else if (msgs[1].equals(REMOTE_PORT1)){
                	remotePortList.add(REMOTE_PORT0);
                	remotePortList.add(REMOTE_PORT2);
                	remotePortList.add(REMOTE_PORT3);
                	remotePortList.add(REMOTE_PORT4);
                }
                    
                else if (msgs[1].equals(REMOTE_PORT2)){
                	remotePortList.add(REMOTE_PORT0);
                	remotePortList.add(REMOTE_PORT1);
                	remotePortList.add(REMOTE_PORT3);
                	remotePortList.add(REMOTE_PORT4);
                }
                	
                else if (msgs[1].equals(REMOTE_PORT3)){
                	remotePortList.add(REMOTE_PORT0);
                	remotePortList.add(REMOTE_PORT1);
                	remotePortList.add(REMOTE_PORT2);
                	remotePortList.add(REMOTE_PORT4);
                }
                	
                else if (msgs[1].equals(REMOTE_PORT4)){
                	remotePortList.add(REMOTE_PORT0);
                	remotePortList.add(REMOTE_PORT1);
                	remotePortList.add(REMOTE_PORT2);
                	remotePortList.add(REMOTE_PORT3);
                }
                
                for(String itr:remotePortList){
                	Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),Integer.parseInt(itr));
    	            String msgToSend = msgs[0];
    	           
    	            //vino code
                    OutputStream outputStream = socket.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
                    bufferedWriter.write(msgToSend);
                    bufferedWriter.flush();
                    socket.close();
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
