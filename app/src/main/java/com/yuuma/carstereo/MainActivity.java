package com.yuuma.carstereo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

public class MainActivity extends Activity {
    private static final String TAG = "YuumaCarStereo";
    private final int bg = Color.rgb(9, 14, 22);
    private final int panel = Color.rgb(20, 29, 42);
    private final int cyan = Color.rgb(0, 210, 230);
    
    private String deviceName = "YUUMA CAR STEREO";
    private TextView nameView, statusView, songView, artistView;
    private LinearLayout root;
    private BluetoothAdapter bluetoothAdapter;
    
    private BluetoothProfile a2dpSinkProfile;
    private boolean isSinkConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);
        
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        checkAndRequestPermissions();
        buildHome();
        initAdvancedBluetoothSink();
    }

    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= 31) {
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADMIN
                }, 20);
            }
        }
    }

    private TextView text(String s, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    private GradientDrawable bg(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(radius);
        return d;
    }

    private LinearLayout column() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    private LinearLayout row() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        return l;
    }

    private void pad(View v, int x, int y) {
        v.setPadding(x, y, x, y);
    }

    private Button button(String label, Runnable action) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextColor(Color.WHITE);
        b.setAllCaps(false);
        b.setBackground(bg(Color.rgb(30, 48, 66), 22));
        b.setOnClickListener(v -> action.run());
        return b;
    }

    private void buildHome() {
        root = column();
        root.setBackgroundColor(bg);
        pad(root, 20, 22);
        
        ScrollView scroll = new ScrollView(this);
        scroll.addView(root);
        setContentView(scroll);

        LinearLayout top = row();
        TextView logo = text("YUUMA", 22, cyan, true);
        top.addView(logo, new LinearLayout.LayoutParams(0, -2, 1));
        top.addView(button("⚙ Settings", this::showSettings));
        root.addView(top);

        nameView = text(deviceName, 13, Color.LTGRAY, true);
        nameView.setGravity(Gravity.CENTER);
        nameView.setPadding(0, 25, 0, 4);
        root.addView(nameView);

        TextView title = text("CAR AUDIO SYSTEM [PRO SINK]", 12, Color.GRAY, true);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView bt = text("◉  BLUETOOTH SINK RECEIVER", 14, cyan, true);
        bt.setGravity(Gravity.CENTER);
        bt.setPadding(0, 26, 0, 8);
        root.addView(bt);
        
        statusView = text("Initializing Bluetooth Stack...", 12, Color.LTGRAY, false);
        statusView.setGravity(Gravity.CENTER);
        root.addView(statusView);

        LinearLayout card = column();
        card.setBackground(bg(panel, 28));
        pad(card, 22, 24);
        
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, -2);
        cp.topMargin = 28;
        root.addView(card, cp);

        TextView disc = text("♫", 64, cyan, true);
        disc.setGravity(Gravity.CENTER);
        card.addView(disc);

        songView = text("Stream: Waiting for Audio...", 18, Color.WHITE, true);
        songView.setGravity(Gravity.CENTER);
        songView.setPadding(0, 12, 0, 4);
        card.addView(songView);

        artistView = text("Advanced Reflection Profile Active", 12, Color.LTGRAY, false);
        artistView.setGravity(Gravity.CENTER);
        card.addView(artistView);

        LinearLayout controls = row();
        controls.setGravity(Gravity.CENTER);
        controls.setPadding(0, 24, 0, 12);
        controls.addView(button("🔄 Scan Devices", this::checkPairedDevices));
        controls.addView(button("▶ / ⏸", this::toggleStreamPlayback));
        card.addView(controls);

        root.addView(button("Open Bluetooth settings", () -> startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS))),
                new LinearLayout.LayoutParams(-1, -2));

        TextView foot = text("YUUMA CAR STEREO  •  ADVANCED CORE", 10, Color.GRAY, true);
        foot.setGravity(Gravity.CENTER);
        foot.setPadding(0, 24, 0, 10);
        root.addView(foot);
    }

    private void initAdvancedBluetoothSink() {
        if (bluetoothAdapter == null) {
            statusView.setText("Bluetooth not supported");
            return;
        }
        
        statusView.setText("Binding A2DP Sink Proxy...");
        try {
            bluetoothAdapter.getProfileProxy(this, new BluetoothProfile.ServiceListener() {
                @Override
                public void onServiceConnected(int profile, BluetoothProfile proxy) {
                    a2dpSinkProfile = proxy;
                    isSinkConnected = true;
                    statusView.setText("Sink Proxy Connected");
                    songView.setText("Ready to Receive Stream");
                    Log.d(TAG, "A2DP Sink proxy connected successfully.");
                }

                @Override
                public void onServiceDisconnected(int profile) {
                    a2dpSinkProfile = null;
                    isSinkConnected = false;
                    statusView.setText("Sink Proxy Disconnected");
                    Log.d(TAG, "A2DP Sink proxy disconnected.");
                }
            }, 11);
        } catch (Exception e) {
            Log.e(TAG, "Exception during profile proxy binding", e);
            statusView.setText("Sink Restricted by System OS");
        }
    }

    private void checkPairedDevices() {
        if (bluetoothAdapter == null) return;
        if (Build.VERSION.SDK_INT >= 31 &&
            checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 20);
            return;
        }
        
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices != null && !pairedDevices.isEmpty()) {
            StringBuilder sb = new StringBuilder("Paired Devices:\n");
            for (BluetoothDevice device : pairedDevices) {
                sb.append("• ").append(device.getName()).append("\n");
            }
            songView.setText("Devices found: " + pairedDevices.size());
            info(sb.toString().trim());
        } else {
            info("No paired Bluetooth devices found.");
        }
    }

    private void toggleStreamPlayback() {
        if (!isSinkConnected || a2dpSinkProfile == null) {
            info("A2DP Sink profile is not connected or restricted.");
            return;
        }

        try {
            Method getConnectedDevices = a2dpSinkProfile.getClass().getMethod("getConnectedDevices");
            @SuppressWarnings("unchecked")
            List<BluetoothDevice> devices = (List<BluetoothDevice>) getConnectedDevices.invoke(a2dpSinkProfile);
            
            if (devices != null && !devices.isEmpty()) {
                BluetoothDevice device = devices.get(0);
                songView.setText("Streaming: " + device.getName());
                artistView.setText("Active Audio Connection");
                info("Connected Stream Device: " + device.getName());
            } else {
                songView.setText("No Active Stream");
                artistView.setText("No active stream device");
                info("No device is currently streaming audio via Sink.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Reflection invocation failed", e);
            info("Stream control blocked by Android security policy.");
        }
    }

    private void showSettings() {
        LinearLayout box = column();
        pad(box, 24, 8);
        
        final EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setText(deviceName);
        input.setHint("Stereo display name");
        box.addView(input);

        new AlertDialog.Builder(this)
            .setTitle("Car Stereo Settings")
            .setView(box)
            .setPositiveButton("Save", (dialog, which) -> {
                String v = input.getText().toString().trim();
                if (!v.isEmpty()) {
                    deviceName = v;
                    nameView.setText(deviceName);
                }
            })
            .setNeutralButton("Bluetooth settings", (dialog, which) -> 
                startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS)))
            .setNegativeButton("Close", null)
            .show();
    }

    private void info(String message) {
        new AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show();
    }
}
