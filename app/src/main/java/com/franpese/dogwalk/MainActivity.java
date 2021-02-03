package com.franpese.dogwalk;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    public static final String ACCION_ABRIR_FRAGMENT_MAPA = "ACCION_ABRIR_FRAGMENT_MAPA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Timber.d("{onCreate}");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        if((intent != null) && (intent.getAction().equals(ACCION_ABRIR_FRAGMENT_MAPA))){
            Navigation.findNavController(this, R.id.contenedor_fragmentos).navigate(R.id.abrir_fragment_mapa);
        }

        BottomNavigationView menu_navegacion = findViewById(R.id.menu_navegacion);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(R.id.fragmento_mascotas, R.id.fragmento_mapa).build();
        NavController navController = Navigation.findNavController(this, R.id.contenedor_fragmentos);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(menu_navegacion, navController);
    }

    @Override
    protected void onStart() {
        Timber.d("{onStart}");
        super.onStart();
    }

    @Override
    protected void onStop() {
        Timber.d("{onStop}");
        super.onStop();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Timber.d("{onNewIntent}");
        super.onNewIntent(intent);
        if(intent.getAction().equals(ACCION_ABRIR_FRAGMENT_MAPA)){
            Navigation.findNavController(this, R.id.contenedor_fragmentos).navigate(R.id.abrir_fragment_mapa);
        }
    }

}