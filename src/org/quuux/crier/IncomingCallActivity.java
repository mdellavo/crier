package org.quuux.crier;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import android.telephony.TelephonyManager;
import android.support.v4.content.LocalBroadcastManager;

import org.quuux.crier.R;

public class IncomingCallActivity extends Activity {

    private static final String TAG ="IncomingCallActivity";
    protected BroadcastReceiver mReceiver;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);

        setContentView(R.layout.incoming_call);

        String number = getIntent().getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
        TextView text = (TextView)findViewById(R.id.text);
        text.setText("Incoming call from " + number);

        mReceiver = new BroadcastReceiver() {
                @Override 
                public void onReceive(Context context, Intent intent) {
                    Log.d(TAG, "got update: " + intent);
                }
            };
    }

    @Override
    protected void onResume() {
        LocalBroadcastManager local_broadcast_manager = LocalBroadcastManager.getInstance(this);
        local_broadcast_manager.registerReceiver(mReceiver,
                                                 new IntentFilter(CrierService.ACTION_CALL_UPDATE));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager local_broadcast_manager = LocalBroadcastManager.getInstance(this);
        local_broadcast_manager.unregisterReceiver(mReceiver);
    }
}
