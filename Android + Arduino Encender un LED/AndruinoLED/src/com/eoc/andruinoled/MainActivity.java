package com.eoc.andruinoled;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.net.ParseException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity{

	protected static final int RESULT_SPEECH = 1;

	private ImageButton mBtnHablar;
	private TextView mTviSalida;
	private ArrayList<String> mTexto;
	private String aux;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mTviSalida = (TextView) findViewById(R.id.tviSalida);
		mBtnHablar = (ImageButton) findViewById(R.id.btnHablar);
		mBtnHablar.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				iniciarReconocimientoVoz();		
				
			}
		});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private void iniciarReconocimientoVoz() {
		Intent intent = new Intent(
				RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Diga Encender o Apagar");
		try {
			startActivityForResult(intent, RESULT_SPEECH);
			mTviSalida.setText("");
		} catch (ActivityNotFoundException a) {
			Toast.makeText(getApplicationContext(),
					"Tu dispositivo no es compatible con el \"Reconocimiento de Voz\"",
					Toast.LENGTH_SHORT).show();
			
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case RESULT_SPEECH: {
			if (resultCode == RESULT_OK && null != data) {

				mTexto = data
						.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
				aux = mTexto.get(0);
				if(aux.compareTo("encender")==0){
					mTviSalida.setText("LED encendido");
					aux = "1";
				}else if(aux.compareTo("apagar")==0){
					mTviSalida.setText("LED apagado");
					aux = "0";
				}else{
					mTviSalida.setText("Usted dijo : "+aux+". Diga \"encender\" o \"apagar\"");
				}
				
				new ComunicacionServidorTask(this).execute(aux);
			}
			break;
		}

		}
	}
	class ComunicacionServidorTask extends AsyncTask<String, Integer, Boolean>{
		// TAG
		private static final String TAG_COMUNICACION_SERVIDOR_TASK = "ComunicacionServidorTask";
		private static final String MSG_SOCKET_EXCEPTION = "SocketException";
		private static final String MSG_PARSE_EXCEPTION = "ParseException";
		private static final String MSG_IO_EXCEPTION = "IOException";
		
		private ProgressDialog mProgressDialog;
		
		
		public ComunicacionServidorTask(Context context) {
			this.context = context;
			mProgressDialog = new ProgressDialog(context);
		}
		@Override
		protected Boolean doInBackground(String... params) {
			
			return  ComunicacionServidor();
		}
		private Context context;

	    protected void onProgressUpdate(Integer... values) {
	         int progreso = values[0].intValue();
	         mProgressDialog.setProgress(progreso);
	     }
		 
		@Override
		protected void onPreExecute() {
			
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mProgressDialog.setMessage("Conectando. Espere unos momentos.");
			mProgressDialog.setCancelable(true);
			mProgressDialog.setMax(100);
			
			mProgressDialog.setOnCancelListener(new OnCancelListener() {
				
				@Override
				public void onCancel(DialogInterface dialog) {
					ComunicacionServidorTask.this.cancel(true);
					
				}
			});
			mProgressDialog.setProgress(0);
	        mProgressDialog.show();
			super.onPreExecute();
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if(result==true){
				mProgressDialog.dismiss();
				Toast.makeText(context, "Se conectó al servidor.", Toast.LENGTH_LONG).show();
			}else{
				mProgressDialog.dismiss();
				Toast.makeText(context, "No se pudo conectar al servidor.", Toast.LENGTH_LONG).show();
			}
			super.onPostExecute(result);
		}
		
		@Override
	    protected void onCancelled() {
	        Toast.makeText(context, "Se cancelo la conexión al servidor",
	            Toast.LENGTH_SHORT).show();
	    }
		private Boolean  ComunicacionServidor() {
			boolean resultado = false;
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://192.168.2.10:8080/arduino");
			
			try{
				ArrayList<NameValuePair> postParameters;
				postParameters = new ArrayList<NameValuePair>();
			    postParameters.add(new BasicNameValuePair("led", aux));
			    post.setEntity(new UrlEncodedFormEntity(postParameters));
				HttpResponse resp = httpClient.execute(post);
	        	String respStr = EntityUtils.toString(resp.getEntity());
				Log.i(TAG_COMUNICACION_SERVIDOR_TASK, respStr);
				if(respStr.equals("")){
					resultado = true;
				}
				
			} catch (SocketException sEx) {
				Log.e(TAG_COMUNICACION_SERVIDOR_TASK,
						MSG_SOCKET_EXCEPTION + ": " + sEx.getMessage());
			} catch (ParseException pEx) {
				Log.e(TAG_COMUNICACION_SERVIDOR_TASK,
						MSG_PARSE_EXCEPTION + ": " + pEx.getMessage());
			} catch (IOException ioEx) {
				Log.e(TAG_COMUNICACION_SERVIDOR_TASK,
						MSG_IO_EXCEPTION + ": " + ioEx.getMessage());
			}
			httpClient.getConnectionManager().shutdown();
			return resultado;
		}
	}


}
