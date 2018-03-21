package com.example.edu.serviceapp;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.widget.ArrayAdapter;

import static android.widget.Toast.LENGTH_LONG;

public class MainActivity extends AppCompatActivity {
    LocationManager locationManager;
    double longitudeBest, latitudeBest;
    double longitudeGPS, latitudeGPS;
    double longitudeNetwork, latitudeNetwork;
    Spinner spinner_pedido,spinner_cliente,spinner_ubicacion;
    TextView longitudeValueBest, latitudeValueBest;
    TextView longitudeValueGPS, latitudeValueGPS;
    TextView longitudeValueNetwork, latitudeValueNetwork, texto;
    RelativeLayout layout;

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1337;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        longitudeValueBest = findViewById(R.id.longitudeValueBest);
        latitudeValueBest = findViewById(R.id.latitudeValueBest);
        longitudeValueGPS = findViewById(R.id.longitudeValueGPS);
        latitudeValueGPS = findViewById(R.id.latitudeValueGPS);
        longitudeValueNetwork = findViewById(R.id.longitudeValueNetwork);
        latitudeValueNetwork = findViewById(R.id.latitudeValueNetwork);
        texto = findViewById(R.id.textView);
        layout = findViewById(R.id.relativeLayout);
        spinner_pedido = findViewById(R.id.spinner);
        spinner_cliente = findViewById(R.id.spinner2);
        spinner_ubicacion = findViewById(R.id.spinner3);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            layout.setVisibility(View.GONE);
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        } else {
            desarrollo();
        }
    }

    protected void desarrollo() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        return;
        }
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 5 * 20 * 1000, 10, locacion);
        new GetContacts().execute();

    }
    private class GetContacts extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(MainActivity.this,"Descargando datos del servidor",Toast.LENGTH_LONG).show();
        }
        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();
            // Making a request to url and getting response
            String url = "http://192.168.42.91:8000/pedidos.json";
            String jsonStr = sh.makeServiceCall(url);
            if (jsonStr != null) {
                try {
                    JSONArray jsonArray = new JSONArray(jsonStr);
                    String[]  pedido_array = new String[jsonArray.length()];
                    String[]  nombre_array = new String[jsonArray.length()];
                    String[]  ubicacion_array = new String[jsonArray.length()];
                    Log.e("valor", String.valueOf(jsonArray));
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        pedido_array[i] = jsonObject.getString("cliente_pedido");
                        nombre_array[i] = jsonObject.getString("cliente_nombre");
                        ubicacion_array[i] = jsonObject.getString("cliente_ubicacion");
                        //String var1 = jsonObject.getString("cliente_pedido");
                       // String var2 = jsonObject.getString("cliente_nombre");
                        Log.e("noise",pedido_array[i]);
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item,pedido_array);
                        spinner_pedido.setAdapter(adapter);
                        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item,nombre_array);
                        spinner_cliente.setAdapter(adapter1);
                        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item,ubicacion_array);
                        spinner_ubicacion.setAdapter(adapter2);
                    }
                } catch (final JSONException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("error",e.getMessage());
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    LENGTH_LONG).show();
                        }
                    });

                }

            } else {
                Log.e("falla server", "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                LENGTH_LONG).show();
                    }
                });
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);


        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    layout.setVisibility(View.VISIBLE);
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        return;
                    }

                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER, 2 * 20 * 1000, 10, locacion);

                } else {


                }

            }

        }
    }

    private final  LocationListener locacion = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d("status", "cahnge");

            longitudeGPS = location.getLongitude();
            latitudeGPS = location.getLatitude();
            Log.i("longitud", String.valueOf(longitudeGPS));
            longitudeValueGPS.setText(String.valueOf(longitudeGPS));
            latitudeValueGPS.setText(String.valueOf(latitudeGPS));
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            Log.d("status", "status");
        }

        @Override
        public void onProviderEnabled(String s) {
            Log.d("proveedor activado", "ativado");
        }

        @Override
        public void onProviderDisabled(String s) {
            Log.d("proveedor desctivado", "desativado");
            showAlert();
        }
    };
    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Active GPS")
                .setMessage("Su ubicación esta desactivada.\npor favor active su ubicación " +
                        "usa esta app")
                .setPositiveButton("Configuración de ubicación", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        dialog.show();
    }

}