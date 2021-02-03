package com.franpese.dogwalk.servicios.gps;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.MutableLiveData;

import com.franpese.dogwalk.MainActivity;
import com.franpese.dogwalk.R;
import com.franpese.dogwalk.servicios.EstadoServicio;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import timber.log.Timber;

;import static com.franpese.dogwalk.MainActivity.ACCION_ABRIR_FRAGMENT_MAPA;

public class ServicioGPS extends LifecycleService {

    public static final String ACCION_INICIAR_GPS = "ACCION_INICIAR_GPS";
    public static final String ACCION_PAUSAR_GPS = "ACCION_PAUSAR_GPS";
    public static final String ACCION_PARAR_GPS = "ACCION_PARAR_GPS";
    public static final String ID_CANAL_NOTIFICACIONES = "notificaciones_servicio_gps";
    public static final String CANAL_NOTIFICACIONES = "ServicioGPS";
    public static final int ID_NOTIFICACION = 1;


    private boolean iniciado = false;
    private LocationCallback callbackGPS = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            Timber.d("{onLocationResult}");
            if(locationResult != null){
                for(Location location : locationResult.getLocations()){
                    location.setLatitude(Math.round(location.getLatitude()*100000)/100000.0d);
                    location.setLongitude(Math.round(location.getLongitude()*100000)/100000.0d);
                    posicion.postValue(location);
                }
            }
        }
    };

    public static final MutableLiveData<EstadoServicio> estado = new MutableLiveData<>(EstadoServicio.PARADO);
    public static final MutableLiveData<Location> posicion = new MutableLiveData<>(null);

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        if(intent != null) {
            switch (intent.getAction()) {
                case ACCION_INICIAR_GPS:
                    if (!iniciado) {
                        Timber.d("{Acción Iniciar}");
                        iniciar();
                        iniciado = true;
                    } else {
                        Timber.d("{Acción Reanudar}");
                    }
                    iniciarActualizacionesGPS();
                    estado.postValue(EstadoServicio.INICIADO);
                    break;
                case ACCION_PAUSAR_GPS:
                    Timber.d("{Acción Pausar}");
                    pausar();
                    break;
                case ACCION_PARAR_GPS:
                    Timber.d("{Acción Parar}");
                    terminar();
                    break;
                default:
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void iniciar() {
        Timber.d("{iniciar}");
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            crearCanalNotificaciones(manager);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, ID_CANAL_NOTIFICACIONES).setAutoCancel(false).setOngoing(true).setSmallIcon(R.drawable.ic_baseline_pets_24).setContentTitle(getString(R.string.app_name)).setContentText(getString(R.string.notificacion_gps_en_marcha));
        Intent abrir_fragment_mapa = new Intent(this, MainActivity.class);
        abrir_fragment_mapa.setAction(ACCION_ABRIR_FRAGMENT_MAPA);
        PendingIntent pending_intent = PendingIntent.getActivity(this, 0, abrir_fragment_mapa, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pending_intent);
        startForeground(ID_NOTIFICACION, builder.build());
    }

    private void pausar(){
        pararActualizacionesGPS();
        estado.postValue(EstadoServicio.PAUSADO);
    }

    private void terminar(){
        pararActualizacionesGPS();
        estado.postValue(EstadoServicio.PARADO);
        stopForeground(true);
        stopSelf();
    }

    private void iniciarActualizacionesGPS(){
        LocationRequest requestGPS = new LocationRequest();
        requestGPS.setInterval(4000);
        requestGPS.setFastestInterval(2000);
        requestGPS.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(requestGPS, callbackGPS, Looper.getMainLooper());
    }

    private void pararActualizacionesGPS(){
        LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(callbackGPS);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void crearCanalNotificaciones(NotificationManager manager){
        NotificationChannel canalNotificaciones = new NotificationChannel(ID_CANAL_NOTIFICACIONES, CANAL_NOTIFICACIONES, NotificationManager.IMPORTANCE_LOW);
        manager.createNotificationChannel(canalNotificaciones);
    }
}
