package com.elaborandofuturo.animalfitness;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class Login {
	
	Activity act;
	
	EditText et_login_usuario;
	EditText et_login_pass;
	Button btn_login_ok;
	
	String nombre;
	String pass;
	
	public Login(Activity _act) {
		this.act = _act;
		initcomponents();		
	}
	
	private void initcomponents() {
		et_login_usuario = (EditText)act.findViewById(R.id.et_login_usuario);
		et_login_pass = (EditText)act.findViewById(R.id.et_login_pass);
		btn_login_ok=(Button)act.findViewById(R.id.btn_login_ok);
		
		btn_login_ok.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ingresar();
			}
		});
		
		ImageButton iv_inicio_cerrar = (ImageButton) act.findViewById(R.id.ib_inicio_cerrar);
		
		iv_inicio_cerrar.setVisibility(View.GONE);
		
	}

	protected void ingresar() {
		nombre=et_login_usuario.getText().toString();
		pass=et_login_pass.getText().toString();
		if(nombre.contentEquals("")){
			Toast.makeText(act, "Ingrese por favor su usuario", Toast.LENGTH_SHORT).show();
		}
		else {
			if(pass.contentEquals("")){
				Toast.makeText(act, "Ingrese por favor su clave", Toast.LENGTH_SHORT).show();
			}
			else{
				login();
			}
		}
	}

	@SuppressLint("NewApi") 
	private void login() {
		if(android.os.Build.VERSION.SDK_INT>=10){
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy); 
			}
		
		
		// Create a new HttpClient and Post Header
	    HttpClient httpclient = new DefaultHttpClient();
	    HttpPost httppost = new HttpPost("http://www.animalfitness.co/index.php?option=com_appfitness");

	    try {
	        // Add your data
	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        
	        nameValuePairs.add(new BasicNameValuePair("username", nombre));
	        nameValuePairs.add(new BasicNameValuePair("password", pass));
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

	        // Execute HTTP Post Request
	        HttpResponse response = httpclient.execute(httppost);

            // According to the JAVA API, InputStream constructor do nothing. 
            //So we can't initialize InputStream although it is not an interface
            InputStream inputStream = response.getEntity().getContent();

            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuilder stringBuilder = new StringBuilder();

            String bufferedStrChunk = null;

            while((bufferedStrChunk = bufferedReader.readLine()) != null){
                stringBuilder.append(bufferedStrChunk);
            }
            
            JSONObject jo = new JSONObject(stringBuilder.toString());
		
            jo=jo.getJSONObject("return");
            
            if (jo.getString("success").contentEquals("0")){
            	JSONObject datos=jo.getJSONObject("datos");
            	Propiedades.id=datos.getString("id");
            	SharedPreferences settings = act.getSharedPreferences("perfil", Context.MODE_PRIVATE);
            	SharedPreferences.Editor editor = settings.edit();
            	editor.putString("id",Propiedades.id);
            	editor.commit();
            	
            	DBSQLiteHelper lsqlh = new DBSQLiteHelper(act,
						Propiedades.dbname, null, Propiedades.db_version);
				SQLiteDatabase db = lsqlh.getWritableDatabase();
				
				try {
					if (db != null){
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						String currentDateandTime = sdf.format(new Date());
						db.execSQL("INSERT INTO Paseos (id_paseador, hora_inicio) VALUES ('"+Propiedades.id+"','"+currentDateandTime+"')");
						
						
						Cursor c = db.rawQuery("SELECT last_insert_rowid()", null);

						if (c.moveToFirst()) {
							do {
								editor.putString("id_paseo", c.getString(0));
								editor.commit();
							} while (c.moveToNext());
						}
					}
				}
				catch (Exception e){
					
				}
				finally {
					db.close();
				}
            	
            	
            	
            	inicio();
            }
            else {
    			Toast.makeText(act, "Datos de ingreso incorrectos", Toast.LENGTH_SHORT).show();
            }

	    } catch (ClientProtocolException e) {
	        // TODO Auto-generated catch block
	    } catch (IOException e) {
	        // TODO Auto-generated catch block
	    }
	    catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void inicio() {
		
		Propiedades.PATH=act.getFilesDir().getParent();
		
		String str=getdata();
		
		SharedPreferences settings = act.getSharedPreferences("perfil",
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("arreglo", str);
		editor.commit();
		Console.log(str);
		
		try {
			JSONArray array = new JSONArray(str);
			
			for (int i=0; i<array.length(); i++){
				final JSONArray jamascota = array.getJSONArray(i);
				final JSONObject jomascota = jamascota.getJSONObject(0);
				final String foto = jomascota.getString("avatar");
				
				String fileName = foto.substring(foto.lastIndexOf('/')+1, foto.length() );
				
				File file = new File(Propiedades.PATH + "/files/", fileName);
				
				if (!file.exists()) {
					Bitmap bm=ImageDownloader.getBitmapFromURL(Propiedades.url+foto);
				
					FileOutputStream fOut = new FileOutputStream(Propiedades.PATH + "/files/"+fileName);
					Console.log("1: "+Propiedades.PATH + "/files/"+fileName);
					bm.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
					fOut.flush();
					fOut.close();
				}
			}
		} catch (Exception e) {
			Console.log(e.toString());
			e.printStackTrace();
		}
		
		
		ContenedorView con;
		con = (ContenedorView) act.findViewById(R.id.contenido);
		con.removeAllViews();
		con.inicio(act);
		con.setTag("inicio");
		new Inicio(act);
	}
	
	@SuppressLint("NewApi") 
	private String getdata() {
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

		StrictMode.setThreadPolicy(policy); 
		// Create a new HttpClient and Post Header
	    HttpClient httpclient = new DefaultHttpClient();
	    HttpPost httppost = new HttpPost("http://animalfitness.co/index.php?option=com_appfitnessusers");

	    try {
	        // Add your data
	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        nameValuePairs.add(new BasicNameValuePair("id", Propiedades.id));
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

	        // Execute HTTP Post Request
	        HttpResponse response = httpclient.execute(httppost);

            // According to the JAVA API, InputStream constructor do nothing. 
            //So we can't initialize InputStream although it is not an interface
            InputStream inputStream = response.getEntity().getContent();

            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuilder stringBuilder = new StringBuilder();

            String bufferedStrChunk = null;

            while((bufferedStrChunk = bufferedReader.readLine()) != null){
                stringBuilder.append(bufferedStrChunk);
            }
            return stringBuilder.toString();
            }
	    	catch (Exception e){
	    		Log.d(Propiedades.TAG, e.toString());
	    		return null;
	    	}
	}


}
