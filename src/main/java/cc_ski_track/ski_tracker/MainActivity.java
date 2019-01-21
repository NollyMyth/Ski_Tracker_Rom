package cc_ski_track.ski_tracker;

import android.content.pm.PackageManager;
import android.location.LocationProvider;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.opengl.GLSurfaceView;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements LocationListener {
    private GLSurfaceView mGLView;
    private static final int MY_PERMISSION_ACCESS_COARSE_LOCATION = 11;
    private LocationManager locationManager;
//    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
        mGLView = new MyGLSurfaceView(this);
        setContentView(mGLView);
    }

    @Override
    public void onResume() {
        super.onResume();

        //Obtention de la référence du service
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

        //Si le GPS est disponible, on s'y abonne
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            abonnementGPS();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        //On appelle la méthode pour se désabonner
        desabonnementGPS();
    }

    protected void afficheCoord(View v){
        ArrayList<LocationProvider> providers = new ArrayList<LocationProvider>();
        List<String> names = locationManager.getProviders(true);

        for(String name : names)
            providers.add(locationManager.getProvider(name));
        Criteria critere = new Criteria();
        critere.setAccuracy(Criteria.ACCURACY_FINE);
        critere.setBearingRequired(false);
        critere.setCostAllowed(false);
        critere.setPowerRequirement(Criteria.POWER_HIGH);
        critere.setSpeedRequired(true);
    }

    public void abonnementGPS() {

        //On s'abonne
        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  },
                    MY_PERMISSION_ACCESS_COARSE_LOCATION );
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, this);
    }

    public void desabonnementGPS() {
        //Si le GPS est disponible, on s'y abonne
        locationManager.removeUpdates(this);
    }

    @Override
    public void onStatusChanged (final String provider,final int status, final Bundle extras){

    }

    @Override
    public void onProviderEnabled (String provider){
        //Si le GPS est activé on s'abonne
        if("gps".equals(provider)) {
            abonnementGPS();
        }
    }

    @Override
    public void onProviderDisabled (String provider){
        //Si le GPS est désactivé on se désabonne
        if("gps".equals(provider)) {
            desabonnementGPS();
        }
    }

    @Override
    public void onLocationChanged (Location location){
        //On affiche dans un Toat la nouvelle Localisation
        final StringBuilder msg = new StringBuilder("lat : ");
        msg.append(location.getLatitude());
        msg.append( "; lng : ");
        msg.append(location.getLongitude());
        Toast.makeText(this, msg.toString(), Toast.LENGTH_SHORT).show();
        Log.d("location", msg.toString());
    }


}
