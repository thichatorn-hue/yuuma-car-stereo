package com.yuuma.carstereo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

public class MainActivity extends Activity {
    private final int bg = Color.rgb(9, 14, 22);
    private final int panel = Color.rgb(20, 29, 42);
    private final int cyan = Color.rgb(0, 210, 230);
    private String deviceName = "YUUMA CAR STEREO";
    private TextView nameView, statusView, songView, artistView;
    private LinearLayout root;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);
        if (Build.VERSION.SDK_INT >= 31 &&
            checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 20);
        }
        buildHome();
    }

    private TextView text(String s, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s); t.setTextSize(size); t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }
    private GradientDrawable bg(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color); d.setCornerRadius(radius); return d;
    }
    private LinearLayout column() {
        LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); return l;
    }
    private LinearLayout row() {
        LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL); return l;
    }
    private void pad(View v, int x, int y) { v.setPadding(x,y,x,y); }
    private Button button(String label, Runnable action) {
        Button b = new Button(this); b.setText(label); b.setTextColor(Color.WHITE);
        b.setAllCaps(false); b.setBackground(bg(Color.rgb(30,48,66), 22));
        b.setOnClickListener(v -> action.run()); return b;
    }
    private void buildHome() {
        root = column(); root.setBackgroundColor(bg); pad(root, 20, 22);
        ScrollView scroll = new ScrollView(this); scroll.addView(root);
        setContentView(scroll);

        LinearLayout top = row();
        TextView logo = text("YUUMA", 22, cyan, true);
        top.addView(logo, new LinearLayout.LayoutParams(0,-2,1));
        top.addView(button("⚙ Settings", this::showSettings));
        root.addView(top);

        nameView = text(deviceName, 13, Color.LTGRAY, true);
        nameView.setGravity(Gravity.CENTER); nameView.setPadding(0,25,0,4);
        root.addView(nameView);
        TextView title = text("CAR AUDIO SYSTEM", 12, Color.GRAY, true);
        title.setGravity(Gravity.CENTER); root.addView(title);

        TextView bt = text("◉  BLUETOOTH", 14, cyan, true);
        bt.setGravity(Gravity.CENTER); bt.setPadding(0,26,0,8); root.addView(bt);
        statusView = text("Receiver mode depends on Android device support", 12, Color.LTGRAY, false);
        statusView.setGravity(Gravity.CENTER); root.addView(statusView);

        LinearLayout card = column(); card.setBackground(bg(panel,28)); pad(card,22,24);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1,-2); cp.topMargin=28;
        root.addView(card,cp);
        TextView disc = text("♫", 64, cyan, true); disc.setGravity(Gravity.CENTER);
        card.addView(disc);
        songView = text("No track information", 20, Color.WHITE, true);
        songView.setGravity(Gravity.CENTER); songView.setPadding(0,12,0,4); card.addView(songView);
        artistView = text("Connect a supported Bluetooth audio source", 12, Color.LTGRAY, false);
        artistView.setGravity(Gravity.CENTER); card.addView(artistView);

        LinearLayout controls = row(); controls.setGravity(Gravity.CENTER);
        controls.setPadding(0,24,0,12);
        controls.addView(button("⏮", () -> info("Media controls will be available when a supported audio session is connected.")));
        controls.addView(button("▶ / ⏸", () -> info("This app is currently the receiver interface; it cannot create A2DP Sink support by itself.")));
        controls.addView(button("⏭", () -> info("Media controls will be available when a supported audio session is connected.")));
        card.addView(controls);

        root.addView(button("Open Bluetooth settings", () -> startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS))),
                new LinearLayout.LayoutParams(-1,-2));
        TextView foot = text("YUUMA CAR STEREO  •  ANDROID 14", 10, Color.GRAY, true);
        foot.setGravity(Gravity.CENTER); foot.setPadding(0,24,0,10); root.addView(foot);
    }
    private void showSettings() {
        LinearLayout box = column(); pad(box,24,8);
        EditText input = new EditText(this); input.setSingleLine(true); input.setText(deviceName);
        input.setHint("Stereo display name"); box.addView(input);
        new AlertDialog.Builder(this).setTitle("Car Stereo Settings")
            .setView(box)
            .setPositiveButton("Save", (d,w) -> {
                String v=input.getText().toString().trim();
                if (!v.isEmpty()) { deviceName=v; nameView.setText(deviceName); }
            })
            .setNeutralButton("Bluetooth settings", (d,w) -> startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS)))
            .setNegativeButton("Close", null).show();
    }
    private void info(String s) { new AlertDialog.Builder(this).setMessage(s).setPositiveButton("OK",null).show(); }
}
