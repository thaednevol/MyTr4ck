package com.elaborandofuturo.animalfitness;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Inicio {
	
	Activity act;
	
	TextView tv_inicio_reloj_oficial;
	
	LinearLayout ll_inicio_fotos_animales;
	LinearLayout ll_inicio_datos_animales;
	ImageButton iv_inicio_cerrar;

	public Inicio(Activity _act) {
		this.act = _act;
		initcomponents();
	}

	private void initcomponents() {

		ll_inicio_fotos_animales = (LinearLayout) act.findViewById(R.id.ll_inicio_fotos_animales);
		ll_inicio_datos_animales = (LinearLayout) act.findViewById(R.id.ll_inicio_datos_animales);
		
		iv_inicio_cerrar = (ImageButton) act.findViewById(R.id.ib_inicio_cerrar);
		
		iv_inicio_cerrar.setVisibility(View.VISIBLE);
		
		iv_inicio_cerrar.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				cerrarsesion();
			}
		});
		Propiedades.PATH=act.getFilesDir().getParent();
		llenardatos();
		
	}
	
	protected void cerrarsesion() {
		Propiedades.mascotas_recogidas=getList("mascotas");
		
		if (Propiedades.mascotas_recogidas.isEmpty()){
			AlertDialog.Builder builder = new AlertDialog.Builder(act);
			builder.setNegativeButton("Cancelar",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									dialog.cancel();
								}
							})
					.setPositiveButton("Cerrar Sesión",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									sincronizar();
									dialog.cancel();
								}
								
							}).setTitle("Desea cerrar sesión?");
			AlertDialog alertDialog = builder.create();
			alertDialog.show();

		}
		else {
			AlertDialog.Builder builder = new AlertDialog.Builder(act);
			builder.setTitle("Debe entregar todas las mascotas primero")
					.setNegativeButton("Cancelar",
							new DialogInterface.OnClickListener(){
								public void onClick(DialogInterface dialog, int id) {
									dialog.cancel();
								}
							}).setPositiveButton("Entregar",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									dialog.cancel();
								}});
			AlertDialog alertDialog = builder.create();
			alertDialog.show();
		}
	}
	
	protected void sincronizar() {
		if (conexion()){
			DBSQLiteHelper lsqlh = new DBSQLiteHelper(act,
					Propiedades.dbname, null, Propiedades.db_version);
			SQLiteDatabase db = lsqlh.getWritableDatabase();
			
			try {
				if (db != null){
					SharedPreferences settings = act.getSharedPreferences("perfil",
							Context.MODE_PRIVATE);
					String id= settings.getString("id_paseo","-1");
					
					Cursor c = db.rawQuery("SELECT id,longitud,latitud,tiempo FROM Posiciones WHERE id_paseos='"+id+"' AND sync='0'", null);

					if (c.moveToFirst()) {
						do {
							
							Console.log("SINCRONIZAR "+c.getString(0));
							if(setposition(Double.parseDouble(c.getString(1)), Double.parseDouble(c.getString(2)), c.getString(3))=="-1"){
								Toast.makeText(act, "Hubo un problema con el envío de datos", Toast.LENGTH_SHORT).show();
								String str="UPDATE Posiciones SET sync='1' WHERE id='"+c.getString(0)+"'";
								db.execSQL(str);
								
							}
							
							
						} while (c.moveToNext());
					}
				}
				logout();
			}
			catch (Exception e){
				AlertDialog.Builder builder = new AlertDialog.Builder(act);
				builder.setTitle("Hubo un problema al salir de la sesión")
						.setNeutralButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										dialog.cancel();
									}
									
								});
				AlertDialog alertDialog = builder.create();
				alertDialog.show();
			}
			finally {
				db.close();
			}
			
			
			
		}
		else {
			AlertDialog.Builder builder = new AlertDialog.Builder(act);
			builder.setTitle("Debe tener acceso a internet para poder salir")
					.setNeutralButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									dialog.cancel();
								}
								
							});
			AlertDialog alertDialog = builder.create();
			alertDialog.show();
		}
}

	private void logout() {
		SharedPreferences settings = act.getSharedPreferences("perfil", Context.MODE_PRIVATE);
    	SharedPreferences.Editor editor = settings.edit();
    	editor.putString("id","-1");
    	editor.commit();
    	
    	Propiedades.mascotas_recogidas = new ArrayList<String>();
    	setList("mascotas");
    	Propiedades.ids_mascotas_recogidas = new ArrayList<String>();
    	setList("ids");
    	
    	ContenedorView con;
		con = (ContenedorView) act.findViewById(R.id.contenido);
    	con.removeAllViews();
		con.login(act);
		con.setTag("login");
		new Login(act);
				
    	
	}

	private boolean conexion() {
		ConnectivityManager conMgr = (ConnectivityManager) act
	            .getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo i = conMgr.getActiveNetworkInfo();
		  if (i == null)
		    return false;
		  if (!i.isConnected())
		    return false;
		  if (!i.isAvailable())
		    return false;
		  
		  return true;
	}

	private void llenardatos() {
		try {
			
			SharedPreferences settings = act.getSharedPreferences("perfil",
					Context.MODE_PRIVATE);
			String str=settings.getString("arreglo", "-1");
			
			JSONArray array = new JSONArray(str);
		
			for (int i=0; i<array.length(); i++){
				try{
				final JSONArray jamascota = array.getJSONArray(i);
				final JSONObject jomascota = jamascota.getJSONObject(0);
				final String id_mascota=jomascota.getString("userid");
				final String datos= jomascota.getString("datos");
				final String username = jomascota.getString("username");
				final String foto = jomascota.getString("avatar");
				final String nombre=jomascota.getString("name");
				final String correo=jomascota.getString("email");
				
				final String ruta= datos.split(",")[0].split(":")[1];
				
				final String jornada = datos.split(",")[1].split(":")[1];
				
				
				LayoutInflater inflater = LayoutInflater.from(act); // 1
				View mascota = inflater.inflate(R.layout.mascota, null);
				
				ImageView img_foto = (ImageView)mascota.findViewById(R.id.iv_mascota_foto);
				
				String fileName = foto.substring(foto.lastIndexOf('/')+1, foto.length());
				
				final Bitmap bm=BitmapFactory.decodeFile(Propiedades.PATH + "/files/"+fileName);
				img_foto.setImageBitmap(bm);
				
				img_foto.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Toast.makeText(act, username, Toast.LENGTH_SHORT).show();
					}
				});
				
				LinearLayout ll_datos_mascota = new LinearLayout(act);
				
				Propiedades.mascotas_recogidas=getList("mascotas");
				
				ImageView iv_mascota_check = (ImageView)mascota.findViewById(R.id.iv_mascota_check);
				if (!Propiedades.mascotas_recogidas.contains(username.trim())){
					iv_mascota_check.setBackgroundResource(R.drawable.sinrecoger);
	
				}
				else {
					iv_mascota_check.setBackgroundResource(R.drawable.paseando);
					
				}
				ll_inicio_fotos_animales.addView(mascota);
				
				int size=18;
				
				View datos_mascota = inflater.inflate(R.layout.datos_mascota, null);
				
				Propiedades.mascotas_recogidas=getList("mascotas");
				
				iv_mascota_check = (ImageView)datos_mascota.findViewById(R.id.iv_mascota_check);
				if (!Propiedades.mascotas_recogidas.contains(username.trim())){
					iv_mascota_check.setBackgroundResource(R.drawable.sinrecoger);
					
				}
				else {
					iv_mascota_check.setBackgroundResource(R.drawable.paseando);
					
				}
				
				TextView tv_nombre = (TextView)datos_mascota.findViewById(R.id.tv_nombre);
				tv_nombre.setText(nombre);
				
				TextView tv_jornada = (TextView)datos_mascota.findViewById(R.id.tv_jornada);
				tv_jornada.setText(jornada);
				
				LinearLayout ll_tiempo = (LinearLayout)datos_mascota.findViewById(R.id.ll_tiempo);
				
				String hora_recogida = getdatafromdb("hora_recogida", id_mascota);
				if (!hora_recogida.contentEquals("-1")){
					TextView tv_hora_recogida = new TextView(act);
					tv_hora_recogida.setTextSize(size);
					tv_hora_recogida.setText(hora_recogida+"\t");
					ll_tiempo.addView(tv_hora_recogida);
				}
				
				String hora_dejada = getdatafromdb("hora_dejada", id_mascota);
				if (!hora_dejada.contentEquals("-1")){
					TextView tv_hora_dejada = new TextView(act);
					tv_hora_dejada.setTextSize(size);
					tv_hora_dejada.setText(hora_dejada+"\t");
					ll_tiempo.addView(tv_hora_dejada);
				}

				String distancia = getdistancia(id_mascota);
				if (!distancia.contentEquals("-1")){
					TextView tv_distancia = new TextView(act);
					tv_distancia.setTextSize(size);
					tv_distancia.setText(distancia+"\t");
					ll_tiempo.addView(tv_distancia);
				}
				
				TextView tv_ruta = (TextView)datos_mascota.findViewById(R.id.tv_ruta);
				tv_ruta.setText(ruta);
				
				datos_mascota.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (checkgps()){
							leertag(username, nombre,bm,correo,id_mascota);
						}
		
					}
				});
		
				
				ll_inicio_datos_animales.addView(datos_mascota);
				}
				catch (Exception ex){
					Console.log(ex.toString());
					ex.printStackTrace();
					
				}
						
			}
		} catch (Exception e) {
			Console.log(e.toString());
			e.printStackTrace();
		}
	}
	
	protected boolean checkgps() {
		 final LocationManager manager = (LocationManager) act.getSystemService( Context.LOCATION_SERVICE );

		    if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
		    	final AlertDialog.Builder builder = new AlertDialog.Builder(act);
		        builder.setMessage("El GPS está desactivado, ¿desea habilitarlo?")
		               .setCancelable(false)
		               .setPositiveButton("Si", new DialogInterface.OnClickListener() {
		                   public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
		                       act.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
		                   }
		               })
		               .setNegativeButton("No", new DialogInterface.OnClickListener() {
		                   public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
		                        dialog.cancel();
		                   }
		               });
		        final AlertDialog alert = builder.create();
		        alert.show();
		        return false;
		    }
		    else {
		    	return true;
		    }
		
	}

	private String getdistancia(String username) {
		double distancia=0;
		
		DBSQLiteHelper lsqlh = new DBSQLiteHelper(act,
				Propiedades.dbname, null, Propiedades.db_version);
		SQLiteDatabase db = lsqlh.getWritableDatabase();
		
		SharedPreferences settings = act.getSharedPreferences("perfil",
				Context.MODE_PRIVATE);
		
		String id_paseo= settings.getString("id_paseo","-1");
		
		Cursor c = db.rawQuery("SELECT longitud,latitud,tiempo FROM Posiciones WHERE id_paseos='"+id_paseo+"'", null);

		Double x1=0.0;
		Double y1=0.0;
		if (c.moveToFirst()) {
			do {
				
				Double d1=x1-Double.parseDouble(c.getString(0));
				Double d2=y1-Double.parseDouble(c.getString(1));
				
				Double d1_2=Math.pow(d1, 2);
				Double d2_2=Math.pow(d2, 2);
				
				
				distancia=distancia+Math.sqrt(d1_2+d2_2);
				
				
			} while (c.moveToNext());
		}

		db.close();
		
		return "";
	}

	private String getdatafromdb(String string, String id_mascota) {
		DBSQLiteHelper lsqlh = new DBSQLiteHelper(act,
				Propiedades.dbname, null, Propiedades.db_version);
		SQLiteDatabase db = lsqlh.getWritableDatabase();
		
		SharedPreferences settings = act.getSharedPreferences("perfil",
				Context.MODE_PRIVATE);
		

		String result="-1";
		try {
			if (db != null){
				String id_paseo= settings.getString("id_paseo","-1");
				String str="SELECT "+string+" FROM Recogidas WHERE id_mascota='"+id_mascota+"' AND id_paseos='"+id_paseo+"'";
				
				Console.log(str);
				
				Cursor c = db.rawQuery(str, null);

				if (c.moveToFirst()) {
					do {
						if (c.getString(0)!=null){
							result= c.getString(0);
						}
						else {
							result="-1";
						}
						
					} while (c.moveToNext());
				}

			}
		}
		catch (Exception e){
			result="-1";
		}
		finally {
			db.close();
		}
		return result;
		
	}

	protected void leertag(final String username, String nombre, Bitmap bm, String correo, final String id_mascota) {
		try {
			Boolean continuar=false;
			
			Propiedades.mascotas_recogidas=getList("mascotas");
			
			Console.log("MASCOTAS RECOGIDAS" +Propiedades.mascotas_recogidas.toString());
		
			Iterator<String> iter = Propiedades.mascotas_recogidas.iterator();
			while (iter.hasNext()){
				if (iter.next().contains(username)){
					AlertDialog.Builder builder = new AlertDialog.Builder(act);
					builder.setTitle("Ya recogiste esta mascota ¬¬")
							.setNegativeButton("Cancelar",
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int id) {
											dialog.cancel();
										}
									}).setPositiveButton("Dejar mascota",
											new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int id) {
											dejarmascota(username, id_mascota);
											dialog.cancel();
										}
										
									});
					AlertDialog alertDialog = builder.create();
					alertDialog.show();
				continuar=true;
				break;
			}
		}
		
		if (!continuar){
			ContenedorView con;
			con = (ContenedorView) act.findViewById(R.id.contenido);
			con.removeAllViews();
			con.leertag(act);
			con.setTag("leertag");
			new LeerTag(act, username, nombre, bm, 0, correo, id_mascota);
		}

	} catch (Exception e) {
		Log.d(Propiedades.TAG,"Lista: NO OK "+e.toString());

		ContenedorView con;
		con = (ContenedorView) act.findViewById(R.id.contenido);
		con.removeAllViews();
		con.leertag(act);
		con.setTag("leertag");
		new LeerTag(act, username, nombre, bm, 0, correo, id_mascota);	
	}
	}

	protected void dejarmascota(String username, String id_mascota) {
		ContenedorView con;
		con = (ContenedorView) act.findViewById(R.id.contenido);
		con.removeAllViews();
		con.leertag(act);
		con.setTag("leertag");
		new LeerTag(act, username, "", null, 1, "",id_mascota);
	}

	
	public String loadJSONFromAsset(String jsfile) {
        String json = null;
        try {

            InputStream is = act.getAssets().open(jsfile);

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");
            

        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;

    }

	
	private void setList(String str) {
		try {
			SharedPreferences settings = act.getSharedPreferences("perfil",
					act.MODE_PRIVATE);
			SharedPreferences.Editor editor = settings.edit();
			String i = ObjectSerializer.serialize(Propiedades.mascotas_recogidas);
			editor.putString(str, i);
			editor.commit();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private ArrayList<String> getList(String str) {
		ArrayList<String> al;
		try {
			SharedPreferences settings = act.getSharedPreferences("perfil",
					act.MODE_PRIVATE);
			String mascotas = settings.getString(str, "");

			ObjectSerializer os = new ObjectSerializer();
			if (mascotas.contentEquals("")){
				Console.log("Vacio");
				al = new ArrayList<String>();
			}
			else {
				Console.log("Hay algo");
				al = (ArrayList<String>) os.deserialize(mascotas);
			}
			
			
		} catch (IOException e) {
			al = new ArrayList<String>();
			Console.log("Cacheado ");
		}

		return al;
	}


	@SuppressLint("NewApi") 
	private String setposition(double longitud, double latitud, String currentDateandTime) {
		Console.log(longitud+" "+latitud);
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

		StrictMode.setThreadPolicy(policy); 
		// Create a new HttpClient and Post Header
	    HttpClient httpclient = new DefaultHttpClient();
	    HttpPost httppost = new HttpPost("http://animalfitness.co/index.php?option=com_appfitnessmap");
	    
	    try {
	        // Add your data
	    	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        nameValuePairs.add(new BasicNameValuePair("id", Propiedades.id));
	        nameValuePairs.add(new BasicNameValuePair("latitude", latitud+""));
	        nameValuePairs.add(new BasicNameValuePair("longitud", longitud+""));
	        nameValuePairs.add(new BasicNameValuePair("type", "recoge"));
	        nameValuePairs.add(new BasicNameValuePair("mascotas", Propiedades.mascotas_recogidas.toString()));
	        nameValuePairs.add(new BasicNameValuePair("fechahora", currentDateandTime));
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
	    		return "-1";
	    	}
	}
}
