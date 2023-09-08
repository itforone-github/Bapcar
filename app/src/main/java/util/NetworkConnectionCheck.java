package util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;

import androidx.annotation.NonNull;

public class NetworkConnectionCheck extends ConnectivityManager.NetworkCallback {
    private Context context;
    private NetworkRequest networkRequest;
    private ConnectivityManager connectivityManager;
    public static boolean isNetwork= true;
    public NetworkConnectionCheck(Context context){
        this.context=context;
        networkRequest = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build();
        this.connectivityManager = (ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }
    public void register() {
        this.connectivityManager.registerNetworkCallback(networkRequest,this);
    }
    public void unregister(){
        this.connectivityManager.unregisterNetworkCallback(this);
    }

    @Override
    public void onAvailable(@NonNull Network network) {
        super.onAvailable(network);

       /* ((MainActivity)this.context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MainActivity.notNetworkImg.setVisibility(View.GONE);
                MainActivity.mainWebView.setVisibility(View.VISIBLE);
            }
        });*/


        //Toast.makeText(this.context,"네트워크 연결됨",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLost(@NonNull Network network) {
        super.onLost(network);

        /*((MainActivity)this.context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MainActivity.notNetworkImg.setVisibility(View.VISIBLE);
                MainActivity.mainWebView.setVisibility(View.GONE);
            }
        });*/

        //Toast.makeText(this.context,"네트워크 연결이 끊김",Toast.LENGTH_SHORT).show();
    }
}