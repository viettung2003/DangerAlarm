package com.tungvuong.dangeralarm;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;



import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.nio.channels.Channel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import com.facebook.FacebookSdk;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.ShareOpenGraphAction;
import com.facebook.share.model.ShareOpenGraphContent;
import com.facebook.share.model.ShareOpenGraphObject;
import com.facebook.share.widget.ShareDialog;
import com.zing.zalo.zalosdk.oauth.FeedData;
import com.zing.zalo.zalosdk.oauth.OpenAPIService;
import com.zing.zalo.zalosdk.oauth.ZaloOpenAPICallback;
import com.zing.zalo.zalosdk.oauth.ZaloPluginCallback;
import com.zing.zalo.zalosdk.oauth.ZaloSDKApplication;

public class MainActivity extends Activity implements LocationListener {
    static boolean active = false;
    @Override
    protected void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        active=false;
    }
    boolean dabam=false;
    public File file = new File("/sdcard/datacuaapp.txt");
    public String[] dong = {"", "", "", ""};
    public TextView tv1;
    public TextView tv2;
    public TextView tv3;
    public TextView tv4;
    public LocationManager locationManager;
    public LocationListener locationListener;
    public TextToSpeech tts;
    private boolean tu=false;
    public double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkQuyen();
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        startService(new Intent(MainActivity.this, receive.class));
        ZaloSDKApplication.wrap(getApplication());
        tv1 = (TextView) findViewById(R.id.so1);
        tv2 = (TextView) findViewById(R.id.so2);
        tv3 = (TextView) findViewById(R.id.so3);
        tv4 = (TextView) findViewById(R.id.so4);
        gps();

        tu=false;
        if (haveNetworkConnection() == false) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Hãy bật mạng để chương trình hoạt động hiệu quả")
                    .setCancelable(false)
                    .setPositiveButton("Bật", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                            wifi.setWifiEnabled(true);

                        }
                    })
                    .setNegativeButton("Không bật", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        }
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListenerGPS = new LocationListener() {
            @Override
            public void onLocationChanged(android.location.Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        int checkVal = this.checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        int checkVal2 = this.checkCallingOrSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
        if ((checkVal == PackageManager.PERMISSION_GRANTED) & (checkVal2 == PackageManager.PERMISSION_GRANTED)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerGPS); //fix cái này
        } else {
            if (!(checkVal == PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
            if (checkVal2 != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
        }


        if (!file.exists()) {
            try {
                FileWriter writer = new FileWriter(file);
                writer.append("" + "\n" + "" + "\n" + "" + "\n" + "" + "\n");
                writer.flush();
                writer.close();
            } catch (IOException io) {
                io.printStackTrace();

            }
        } else {
            doc();
            tv1.setText(dong[0]);
            tv2.setText(dong[1]);
            tv3.setText(dong[2]);
            tv4.setText(dong[3]);
        }
        super.onCreate(savedInstanceState);
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.dialog);
        dialog.setTitle("Thêm số");
        final EditText so = (EditText) dialog.findViewById(R.id.sdt);
        final Button them = (Button) dialog.findViewById(R.id.button19);
        final Button btnThem = (Button) findViewById(R.id.them1);
        final Button btnThem2 = (Button) findViewById(R.id.them2);
        final Button btnThem3 = (Button) findViewById(R.id.them3);
        final Button btnThem4 = (Button) findViewById(R.id.them4);
        final Button chat1=(Button) findViewById(R.id.chat1);
        final Button sms = (Button) findViewById(R.id.sms);
        sms.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                dabam=true;
                guitin();
            }

        });
        if (tv1.getText().toString().length() != 0) {
            btnThem.setText("Xóa");
        }
        if (tv2.getText().toString().length() != 0) {
            btnThem2.setText("Xóa");
        }
        if (tv3.getText().toString().length() != 0) {
            btnThem3.setText("Xóa");
        }
        if (tv4.getText().toString().length() != 0) {
            btnThem4.setText("Xóa");
        }
        btnThem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tv1.getText().toString().length() == 0) {
                    dialog.show();
                    them.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (checkText(so.getText().toString())) {

                                TextView so1 = (TextView) findViewById(R.id.so1);
                                so1.setText(so.getText().toString());
                                dialog.dismiss();
                                ghira(so.getText().toString(), 0);
                                btnThem.setText("Xóa");
                            }
                        }
                    });
                } else {
                    tv1.setText("");
                    ghira("", 0);
                    btnThem.setText("Thêm");
                }
            }
        });
        btnThem2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tv2.getText().toString().length() == 0) {
                    dialog.show();
                    them.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (checkText(so.getText().toString())) {
                                TextView so2 = (TextView) findViewById(R.id.so2);
                                so2.setText(so.getText().toString());
                                dialog.dismiss();
                                ghira(so.getText().toString(), 1);
                                btnThem2.setText("Xóa");
                            }
                        }
                    });
                } else {
                    tv2.setText("");
                    ghira("", 1);
                    btnThem2.setText("Thêm");
                }
            }
        });
        btnThem3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tv3.getText().toString().length() == 0) {
                    dialog.show();
                    them.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (checkText(so.getText().toString())) {
                                TextView so3 = (TextView) findViewById(R.id.so3);
                                so3.setText(so.getText().toString());
                                dialog.dismiss();
                                ghira(so.getText().toString(), 2);
                                btnThem3.setText("Xóa");
                            }
                        }
                    });
                } else {
                    tv3.setText("");
                    ghira("", 2);
                    btnThem3.setText("Thêm");
                }
            }
        });
        btnThem4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tv4.getText().toString().length() == 0) {
                    dialog.show();
                    them.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (checkText(so.getText().toString())) {
                                TextView so4 = (TextView) findViewById(R.id.so4);
                                so4.setText(so.getText().toString());
                                dialog.dismiss();
                                ghira(so.getText().toString(), 3);
                                btnThem4.setText("Xóa");
                            }
                        }
                    });
                } else {
                    tv4.setText("");
                    ghira("", 3);
                    btnThem4.setText("Thêm");
                }
            }
        });
        final Button call1 = (Button) findViewById(R.id.goi1);
        final Button call2 = (Button) findViewById(R.id.goi2);
        final Button call3 = (Button) findViewById(R.id.goi3);
        final Button call4 = (Button) findViewById(R.id.goi4);
        call1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goi(tv1.getText().toString());
            }
        });
        call2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goi(tv2.getText().toString());
            }
        });
        call3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goi(tv3.getText().toString());
            }
        });
        call4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goi(tv4.getText().toString());
            }
        });
        final Button call113 = (Button) findViewById(R.id.goi113);
        final Button call114 = (Button) findViewById(R.id.goi114);
        final Button call115 = (Button) findViewById(R.id.goi115);
        call113.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goi("113");
            }
        });
        call114.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goi("114");
            }
        });
        call115.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goi("115");
            }
        });
        final Button cam = (Button) findViewById(R.id.cam);
        cam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chup();
            }
        });
        final Button fb = (Button) findViewById(R.id.fb);
        final Button tw = (Button) findViewById(R.id.tw);

        fb.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                dabam=true;
                guifb();

            }
        });
        tw.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                dabam=true;
                guitw();
            }
        });
        final Button zl = (Button) findViewById(R.id.zl);
        zl.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                guizl();
            }
        });
        final Button bao = (Button) findViewById(R.id.bao);
        bao.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                    play();
            }
        });
        final Button chat2 = (Button) findViewById(R.id.chat2);
        chat2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                finish();
                Intent myIntent = new Intent(view.getContext(), chat.class);
                myIntent.putExtra("message","");
                myIntent.putExtra("co",false);
                startActivityForResult(myIntent, 0);

            }
        });
        chat1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                finish();
                mo();
                tu=true;
                active=false;
            }
        });
    }


    public boolean checkText(String a) {
        if (a.equals("")) {
            Toast toast = Toast.makeText(MainActivity.this, "Bạn chưa nhập gì hết!", Toast.LENGTH_SHORT);
            toast.show();
            return false;
        } else {
            return true;
        }
    }


    public void doc() {
        int checkVal = this.checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if ((checkVal == PackageManager.PERMISSION_GRANTED)) {
            Integer a = 0;
            try {
                String curLine = null;
                BufferedReader reader = new BufferedReader(new FileReader("/sdcard/datacuaapp.txt"));
                while (a < 4) {
                    curLine = reader.readLine();
                    if (curLine == null) {
                        dong[a] = "";
                    } else {
                        dong[a] = curLine;
                    }
                    a++;

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }


    }

    public void ghira(String a, Integer b) {
        int checkVal = this.checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if ((checkVal == PackageManager.PERMISSION_GRANTED)) {
            dong[b] = a;
            try {
                FileWriter fw = new FileWriter(file);

                for (int i = 0; i < 4; i++) {
                    fw.write(dong[i] + "\n");
                }

                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

    }
    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
    public void checkQuyen() {
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.INTERNET,
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.CALL_PHONE,
                android.Manifest.permission.SEND_SMS,
        };

        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
    }

    // thêm phương thức chống hành động chạy khi chưa cấp quyền
    public void goi(String a) {
        int checkVal = this.checkCallingOrSelfPermission(Manifest.permission.CALL_PHONE);
        if ((checkVal == PackageManager.PERMISSION_GRANTED)) {
            if (!TextUtils.isEmpty(a)) {
                String dial = "tel:" + a;
                startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));

            } else {
                Toast.makeText(MainActivity.this, "Bạn chưa nhập số vào", Toast.LENGTH_SHORT).show();
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 1);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    // chỉnh lại để lấy tọa độ chính xác
    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    public boolean chon = true;

    public void guitin() {
        int checkVal = this.checkCallingOrSelfPermission(Manifest.permission.SEND_SMS);
        int checkVal2 = this.checkCallingOrSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
        if ((checkVal == PackageManager.PERMISSION_GRANTED) && (checkVal2 == PackageManager.PERMISSION_GRANTED) && (gps() == true)) {
            String them = "smsto:";

            if (chon == true) {
                boolean sodau = false;
                if (tv1.getText().toString().length() != 0) {
                    them = them + tv1.getText().toString();
                    sodau = true;
                }
                if (tv2.getText().toString().length() != 0) {
                    if (sodau == false) {
                        them = them + tv2.getText().toString();
                        sodau = true;
                    } else {
                        them = them + ";" + tv2.getText().toString();
                    }
                }
                if (tv3.getText().toString().length() != 0) {
                    if (sodau == false) {
                        them = them + tv3.getText().toString();
                        sodau = true;
                    } else {
                        them = them + ";" + tv3.getText().toString();
                    }
                }
                if (tv4.getText().toString().length() != 0) {
                    if (sodau == false) {
                        them = them + tv4.getText().toString();
                        sodau = true;
                    } else {
                        them = them + ";" + tv4.getText().toString();
                    }
                }
                if (sodau == false) {
                    Toast toast = Toast.makeText(MainActivity.this, "Bạn không có bất cứ số khẩn cấp nào cả!", Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    Intent smsIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse(them));
                    String nd = "Tôi đang gặp nguy hiểm! Hãy đến cứu tôi";
                    if (haveNetworkConnection() == true) {
                        nd = nd + " tại nơi có tọa độ là " + latitude + " " + longitude + ". Có thể dùng  Google Maps để tìm ra vị trí này";
                    }
                    smsIntent.putExtra("sms_body", nd);
                    startActivity(smsIntent);
                }
            }
        } else {
            if (!(checkVal == PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 1);
            }
            if (checkVal2 != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
            if (gps() == false) {
                gps();
            }
        }
    }


    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    public boolean gps() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Hãy bật GPS để chương trình hoạt động hiệu quả")
                    .setCancelable(false)
                    .setPositiveButton("Bật", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            chon = true;

                        }
                    })
                    .setNegativeButton("Không bật", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            dialog.cancel();
                            chon = false;
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        }
        return chon;
    }

    int TAKE_PHOTO_CODE = 0;
    public String ten = null;

    public void chup() {
        int checkVal = this.checkCallingOrSelfPermission(Manifest.permission.CAMERA);
        if ((checkVal == PackageManager.PERMISSION_GRANTED)) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
            StrictMode.setVmPolicy(builder.build());
            ten = "/sdcard/IMG_" + timeStamp + ".png";
            File newfile = new File(ten);
            try {
                newfile.createNewFile();
            } catch (IOException e) {
            }

            Uri outputFileUri = Uri.fromFile(newfile);

            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

            startActivityForResult(cameraIntent, TAKE_PHOTO_CODE);

        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {


        }
    }

    public void guifb() {
        int checkVal = this.checkCallingOrSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
        if ((checkVal == PackageManager.PERMISSION_GRANTED) && (gps() == true) && (haveNetworkConnection() == true)) {
            String tin = "Tôi đang gặp nguy hiểm! Hãy đến cứu tôi tại nơi có tọa độ là " + latitude + " " + longitude + ". Có thể dùng  Google Maps để tìm ra vị trí này";
            ShareDialog shareDialog=new ShareDialog(this);
            ShareLinkContent content = new ShareLinkContent.Builder()
                    .setContentUrl(Uri.parse("https://www.google.com/maps/search/?api=1&query="+latitude+","+longitude))
                    .setQuote(tin+"\n"+"Thông báo này được tạo bởi ứng dụng DangerAlarm")
                    .build();
            shareDialog.show(content);
        } else {
            if (gps() == false) {
                gps();
            }
            if (checkVal != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
            if (haveNetworkConnection() == false) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Hãy bật mạng để chương trình hoạt động hiệu quả")
                        .setCancelable(false)
                        .setPositiveButton("Bật", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                                wifi.setWifiEnabled(true);

                            }
                        })
                        .setNegativeButton("Không bật", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                dialog.cancel();
                            }
                        });
                final AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }


    public void guitw() {
        int checkVal = this.checkCallingOrSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
        if ((checkVal == PackageManager.PERMISSION_GRANTED) && (gps() == true) && (haveNetworkConnection() == true)) {
            String tin = "Tôi đang gặp nguy hiểm! Hãy đến cứu tôi tại nơi có tọa độ là " + latitude + " " + longitude + ". Có thể dùng  Google Maps để tìm ra vị trí này";
            String tweetUrl = "https://twitter.com/intent/tweet?text=" + tin + " &url=";
            Uri uri = Uri.parse(tweetUrl);
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } else {
            if (gps() == false) {
                gps();
            }
            if (checkVal != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
            if (haveNetworkConnection() == false) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Hãy bật mạng để chương trình hoạt động hiệu quả")
                        .setCancelable(false)
                        .setPositiveButton("Bật", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                                wifi.setWifiEnabled(true);

                            }
                        })
                        .setNegativeButton("Không bật", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                dialog.cancel();
                            }
                        });
                final AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }


    MediaPlayer mediaPlayer=new MediaPlayer();

    public void play()
    {
        AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 100, 0);
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.a);
        new bao().execute();


    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer.stop();
        mediaPlayer.release();

    }
    public void mo()
    {
        int checkVal = this.checkCallingOrSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
        if ((checkVal == PackageManager.PERMISSION_GRANTED) && (gps() == true) && (haveNetworkConnection() == true)) {
            tu=true;
            String tin = "Tôi đang gặp nguy hiểm! Hãy đến cứu tôi tại nơi có tọa độ là " + latitude + " " + longitude + ". Có thể dùng  Google Maps để tìm ra vị trí này";
            Intent intent = new Intent(MainActivity.this, chat.class);
            intent.putExtra("message", tin);
            intent.putExtra("co", tu);
            startActivity(intent);


        } else {
            if (gps() == false) {
                gps();
            }
            if (checkVal != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
            if (haveNetworkConnection() == false) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Hãy bật mạng để chương trình hoạt động hiệu quả")
                        .setCancelable(false)
                        .setPositiveButton("Bật", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                                wifi.setWifiEnabled(true);

                            }
                        })
                        .setNegativeButton("Không bật", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                dialog.cancel();
                            }
                        });
                final AlertDialog alert = builder.create();
                alert.show();
            }
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (dabam==false) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
        }
        else if (dabam==true)
        {
            dabam=false;
            Intent myIntent = new Intent(this, MainActivity.class);
            startActivityForResult(myIntent, 0);
        }

    }

    private class bao extends AsyncTask<URL, Integer, Long> {
        protected Long doInBackground(URL... urls) {
            for (int i=0; i<30; i++)
            {
                mediaPlayer.start();
                try
                {
                    Thread.sleep(4000);
                }
                catch(InterruptedException ex)
                {
                    Thread.currentThread().interrupt();
                }
            }
            return null;
        }
        protected void onProgressUpdate(Integer... progress) {
            //Yet to code
        }
        protected void onPostExecute(Long result) {

        }
    }

    public void guizl() {
        int checkVal = this.checkCallingOrSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
        if ((checkVal == PackageManager.PERMISSION_GRANTED) && (gps() == true) && (haveNetworkConnection() == true)) {
            PackageManager pm = this.getPackageManager();
            boolean isInstalled = isPackageInstalled("com.zing.zalo", pm);
            if (isInstalled==true) {
                String tin = "Tôi đang gặp nguy hiểm! Hãy đến cứu tôi tại nơi có tọa độ là " + latitude + " " + longitude + ". Có thể dùng  Google Maps để tìm ra vị trí này";

                ZaloOpenAPICallback mCallBack = new ZaloOpenAPICallback() {

                    @Override
                    public void onResult(JSONObject jSONObject) {
                        Toast toast = Toast.makeText(MainActivity.this, "Đã đăng lên Zalo!", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                };
                OpenAPIService.getInstance().postToWall(this, "", tin, mCallBack);
                Toast toast = Toast.makeText(MainActivity.this, "Đã đăng lên Zalo!", Toast.LENGTH_SHORT);
                toast.show();
            }
            else
            {
                Toast toast = Toast.makeText(MainActivity.this, "Điện thoại của bạn không có Zalo!", Toast.LENGTH_SHORT);
                toast.show();
            }

        } else {
            if (gps() == false) {
                gps();
            }
            if (checkVal != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
            if (haveNetworkConnection() == false) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Hãy bật mạng để chương trình hoạt động hiệu quả")
                        .setCancelable(false)
                        .setPositiveButton("Bật", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                                wifi.setWifiEnabled(true);

                            }
                        })
                        .setNegativeButton("Không bật", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                dialog.cancel();
                            }
                        });
                final AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }

    private boolean isPackageInstalled(String packageName, PackageManager packageManager) {

        boolean found = true;

        try {

            packageManager.getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {

            found = false;
        }

        return found;
    }

// bây h là làm phương thức để nó nhận tin nhắn trong nền và đẩy thông báo
}

