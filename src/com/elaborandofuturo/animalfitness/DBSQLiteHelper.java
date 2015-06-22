package com.elaborandofuturo.animalfitness;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DBSQLiteHelper extends SQLiteOpenHelper {

   //Sentencia SQL para crear la tabla de Usuarios
   String tablapaseos = "CREATE TABLE Paseos (id INTEGER PRIMARY KEY AUTOINCREMENT, id_paseador TEXT NOT_NULL, hora_inicio DATETIME, hora_fin DATETIME, novedad TEXT)";
   String tablaposiciones = "CREATE TABLE Posiciones (id INTEGER PRIMARY KEY AUTOINCREMENT, id_paseos INTEGER NOT_NULL, longitud TEXT NOT_NULL, latitud TEXT NOT_NULL, tiempo DATETIME NOT_NULL, sync INTEGER NOT_NULL)";
   String recogidas = "CREATE TABLE Recogidas (id INTEGER PRIMARY KEY AUTOINCREMENT, id_mascota TEXT NOT_NULL, hora_recogida DATETIME, hora_dejada DATETIME, id_paseos INTEGER NOT_NULL)";
   
public DBSQLiteHelper(Context contexto, String nombre,
                              CursorFactory factory, int version) {
       super(contexto, nombre, factory, version);
   }

   @Override
   public void onCreate(SQLiteDatabase db) {
       db.execSQL("DROP TABLE IF EXISTS Paseos");
       db.execSQL("DROP TABLE IF EXISTS Posiciones");
       db.execSQL("DROP TABLE IF EXISTS Recogidas");
       //Se ejecuta la sentencia SQL de creación de la tabla
       db.execSQL(tablapaseos);
       db.execSQL(tablaposiciones);
       db.execSQL(recogidas);
   }

   @Override
   public void onUpgrade(SQLiteDatabase db, int versionAnterior, int versionNueva) {
       //NOTA: Por simplicidad del ejemplo aquí utilizamos directamente la opción de
       //      eliminar la tabla anterior y crearla de nuevo vacía con el nuevo formato.
       //      Sin embargo lo normal será que haya que migrar datos de la tabla antigua
       //      a la nueva, por lo que este método debería ser más elaborado.

       //Se elimina la versión anterior de la tabla
	   db.execSQL("DROP TABLE IF EXISTS Paseos");
       db.execSQL("DROP TABLE IF EXISTS Posiciones");
       db.execSQL("DROP TABLE IF EXISTS Recogidas");
       //Se crea la nueva versión de la tabla
       db.execSQL(tablapaseos);
       db.execSQL(tablaposiciones);
       db.execSQL(recogidas);
   }
}