package com.franpese.dogwalk.ui.mapa;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.franpese.dogwalk.R;
import com.franpese.dogwalk.servicios.EstadoServicio;
import com.franpese.dogwalk.servicios.gps.ServicioGPS;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static com.franpese.dogwalk.servicios.gps.ServicioGPS.ACCION_INICIAR_GPS;
import static com.franpese.dogwalk.servicios.gps.ServicioGPS.ACCION_PARAR_GPS;
import static com.franpese.dogwalk.servicios.gps.ServicioGPS.ACCION_PAUSAR_GPS;

public class FragmentMapa extends Fragment {

    private ViewModelMapa viewModel;
    private WebView mapaWeb;
    private FloatingActionButton botonPlayPauseGPS, botonStopGPS;
    private AppCompatImageButton peeButton, poopButton;
    private Location posicion_actual = null;
    private List<Location> paseo = new ArrayList<>();
    private float distance = 0.0f;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Timber.d("{onCreateView}");
        viewModel = new ViewModelProvider(this).get(ViewModelMapa.class);
        View root = inflater.inflate(R.layout.fragment_mapa, container, false);

        this.botonPlayPauseGPS = root.findViewById(R.id.botonPlayPauseGPS);
        this.botonPlayPauseGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ServicioGPS.estado.getValue() == EstadoServicio.INICIADO){
                    pausarServicioGPS();
                }else{
                    iniciarReanudarServicioGPS();
                }
            }
        });

        this.botonStopGPS = root.findViewById(R.id.botonStopGPS);
        this.botonStopGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pararServicioGPS();
            }
        });

        this.peeButton = root.findViewById(R.id.peeButton);
        this.peeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { mapaWeb.loadUrl("javascript:addPee();"); }
        });

        this.poopButton = root.findViewById(R.id.poopButton);
        this.poopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { mapaWeb.loadUrl("javascript:addPoop();"); }
        });

        this.mapaWeb = root.findViewById(R.id.mapa_web);
        this.mapaWeb.getSettings().setJavaScriptEnabled(true);
        this.mapaWeb.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        this.mapaWeb.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        this.mapaWeb.getSettings().setDomStorageEnabled(true);
        this.mapaWeb.setWebViewClient(new MapaWebviewClient());
        this.mapaWeb.getSettings().setBuiltInZoomControls(false);
        this.mapaWeb.setWebChromeClient(new MapaJavascriptClient());
        this.mapaWeb.addJavascriptInterface(new MapaJavaScriptCallbacks(getContext()), "Android");
        this.mapaWeb.loadUrl("file:///android_asset/index.html");

        viewModel.getLabel().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String label) {
                Timber.d("{getLabel.onChanged("+label+")}");
            }
        });

        ServicioGPS.estado.observe(getViewLifecycleOwner(), new Observer<EstadoServicio>() {
            @Override
            public void onChanged(EstadoServicio estado) {
                switch (estado){
                    case PARADO:
                        botonStopGPS.setVisibility(View.GONE);
                        botonPlayPauseGPS.setVisibility(View.VISIBLE);
                        botonPlayPauseGPS.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                        peeButton.setVisibility(View.GONE);
                        poopButton.setVisibility(View.GONE);
                        break;
                    case INICIADO:
                        botonStopGPS.setVisibility(View.VISIBLE);
                        botonPlayPauseGPS.setVisibility(View.VISIBLE);
                        botonPlayPauseGPS.setImageResource(R.drawable.ic_baseline_pause_24);
                        peeButton.setVisibility(View.VISIBLE);
                        poopButton.setVisibility(View.VISIBLE);
                        break;
                    case PAUSADO:
                        botonStopGPS.setVisibility(View.VISIBLE);
                        botonPlayPauseGPS.setVisibility(View.VISIBLE);
                        botonPlayPauseGPS.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                        break;
                }
            }
        });

        ServicioGPS.posicion.observe(getViewLifecycleOwner(), new Observer<Location>() {
            @Override
            public void onChanged(Location posicion) {
                if(posicion != null){
                    if(paseo.isEmpty()){
                        distance = 0.0f;
                        posicion_actual = posicion;
                        paseo.add(posicion);
                        Timber.d("{GPS.posicion.onChanged("+posicion_actual.getLongitude()+","+posicion_actual.getLatitude()+")}");
                        mapaWeb.loadUrl("javascript:onNewLocation("+posicion_actual.getLongitude()+", "+posicion_actual.getLatitude()+");");
                        return;
                    }else{
                        distance = posicion_actual.distanceTo(posicion);
                        if(distance > 1.0){
                            posicion_actual = posicion;
                            paseo.add(posicion);
                            Timber.d("{GPS.posicion.onChanged("+posicion_actual.getLongitude()+","+posicion_actual.getLatitude()+")}");
                            mapaWeb.loadUrl("javascript:onNewLocation("+posicion_actual.getLongitude()+", "+posicion_actual.getLatitude()+");");
                        }
                        return;
                    }
                }
            }
        });

        return root;
    }

    @Override
    public void onStart() {
        Timber.d("{onStart}");
        super.onStart();
    }

    @Override
    public void onStop() {
        Timber.d("{onStop}");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Timber.d("{onDestroy}");
        super.onDestroy();
        botonPlayPauseGPS.setVisibility(View.GONE);
    }

    private class MapaWebviewClient extends WebViewClient{
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Timber.d("{shouldOverrideUrlLoading}");
            return super.shouldOverrideUrlLoading(view, request);
        }
        @Override
        public void onPageFinished(WebView view, String url) {
            Timber.d("{onPageFinished}");
        }
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            Timber.d("{onReceivedError}");
            super.onReceivedError(view, request, error);
        }
    }

    private class MapaJavascriptClient extends WebChromeClient{
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            Timber.d("{onJsAlert}"+message);
            return true;
        }
        @Override
        public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
            Timber.d("{onJsConfirm}"+message);
            return true;
        }
        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
            Timber.d("{onJsPrompt}"+message);
            return true;
        }
    }

    private class MapaJavaScriptCallbacks {
        Context mContext;
        MapaJavaScriptCallbacks(Context c) { mContext = c; }
        @JavascriptInterface
        public void onMapReady() {
            Timber.d("{onMapReady}");
            botonPlayPauseGPS.setVisibility(View.VISIBLE);
        }
        @JavascriptInterface
        public void onMapLoaded() {
            Timber.d("{onMapLoaded}");
        }
        @JavascriptInterface
        public void onMarkerPosition(double lng, double lat) {
            Timber.d("{onMarkerPosition("+lng+","+lat+")}");
        }
    }

    private void iniciarReanudarServicioGPS(){
        Timber.d("{iniciarReanudarServicioGPS}");
        Intent intent = new Intent(requireContext(), ServicioGPS.class);
        intent.setAction(ACCION_INICIAR_GPS);
        requireContext().startService(intent);
    }

    private void pausarServicioGPS(){
        Timber.d("{pausarServicioGPS}");
        Intent intent = new Intent(requireContext(), ServicioGPS.class);
        intent.setAction(ACCION_PAUSAR_GPS);
        requireContext().startService(intent);
    }

    private void pararServicioGPS(){
        Timber.d("{pararServicioGPS}");
        Intent intent = new Intent(requireContext(), ServicioGPS.class);
        intent.setAction(ACCION_PARAR_GPS);
        requireContext().startService(intent);
    }

}