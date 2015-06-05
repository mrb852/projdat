package com.eosol.surftownprototype;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.*;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;

import java.net.URLEncoder;


//api_id = 476b254276b246dd0687
//api_key = 010928a35be31506aef3
public class Surftown {
	public final static int unexpectedServerResponseException = 1;
	public final static int iOExecption = 2;
	public final static int badCredentialsException = 3;
	public Callback callback = null;
	
	public int userID;
	
	public Surftown(){
		this.userID = -1; 
	}
	
	public Surftown(String mail, String password) throws IOException, UnexpectedServerResponseExeption, BadCredentialsException{
		this.Login(mail, password);
	}
	
	public void Login(String mail, String password) throws IOException, UnexpectedServerResponseExeption, BadCredentialsException{
		class AsyncLogin extends AsyncTask<String, Void, Integer>{
			private int exceptionType = 0;
			private Exception exception = null;
			
			protected Integer doInBackground(String... param){
				SurftownConnection con;
				JSONObject json;
				String buffer = "";
				String line;
				
				try{
					con = new SurftownConnection("verifyClientLogin&email=" + param[0] + "&password=" + param[1]);
					while((line = con.in.readLine()) != null)
						buffer += line;
				}catch(IOException e){
					this.exception = e;
					this.exceptionType = Surftown.iOExecption;
					return -1;
				}
				
				try{
					json = new JSONObject(buffer);
				}catch(JSONException e){
					this.exception =  new UnexpectedServerResponseExeption("Login failed, due to malformed JSON response from server");
					this.exceptionType = Surftown.unexpectedServerResponseException;
					return -1;
				}
				
				try{
					if(json.getBoolean("success")){
						return json.getInt("client_id");
					}else{
						this.exception =  new BadCredentialsException("Wrong E-mail or password");
						this.exceptionType = Surftown.badCredentialsException;
						return -1;
					}
				}catch(JSONException e){
					//this should not happen, so print stack trace
					e.printStackTrace();
					return -1;
				}
			}
			
			protected void onPostExecute(Integer userID){
				super.onPostExecute(userID);
				if(this.exception == null){
					Surftown.this.userID = userID;
					if(Surftown.this.callback != null)
						Surftown.this.callback.successCallback(Surftown.this);
				}else if(Surftown.this.callback != null){
					Surftown.this.callback.errorCallback(exception, exceptionType);
				}
			}
		}
		(new AsyncLogin()).execute(URLEncoder.encode(mail, "UTF-8"), URLEncoder.encode(password, "UTF-8"));
	}
	
	private class SurftownConnection {
		private String apiID = "476b254276b246dd0687";
		private String apiKey = "010928a35be31506aef3";
		private String urlString = "http://hbdevdhk.yoga.surftown.net/surf-admin/api.php?api_id=" + apiID + "&api_key=" + apiKey + "&call=";
		private URL url;
		private HttpURLConnection con;
		public BufferedReader in;
		
		public SurftownConnection(String callFunction) throws IOException{
			try{
				this.url = new URL(this.urlString + callFunction);
				this.con = (HttpURLConnection) this.url.openConnection();
				con.setRequestMethod("GET");
				this.in = new BufferedReader(new InputStreamReader(this.con.getInputStream()));
			}catch(MalformedURLException e){
				//This should really not happen
				e.printStackTrace();
			}
		}
		public void close(){
			this.con.disconnect();
		}
	}
	
	public class UnexpectedServerResponseExeption extends Exception {
	    public UnexpectedServerResponseExeption(String message) {
	        super(message);
	    }
	}
	
	public class BadCredentialsException extends Exception {
	    public BadCredentialsException(String message) {
	        super(message);
	    }
	}
}
