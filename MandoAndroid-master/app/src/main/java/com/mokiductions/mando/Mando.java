package com.mokiductions.mando;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.erz.joysticklibrary.JoyStick;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Mando extends AppCompatActivity {

    private SharedPreferences prefs;

    private String serverAddress;

    private Connection conn;

    private Button btnConn;
    private TextView connState;
    private ListView drawerList;
    private DrawerLayout drawerLayout;
    private JoyStick joyStick;

    private String[] menuList = {"Pausar/Reactivar", "Activar/Quitar turbo",
            "Activar/Desactivar debug", "Establecer IP del servidor", "Salir del juego"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mando);

        // Carga la IP guardada en las preferencias de la aplicación
        prefs = getSharedPreferences("data", 0);
        serverAddress = prefs.getString("svIp", "");

        joyStick = (JoyStick) findViewById(R.id.joyStick);
        joyStick.setType(JoyStick.TYPE_8_AXIS);
        joyStick.setListener(new JoyStick.JoyStickListener() {
            @Override
            public void onMove(JoyStick joyStick, double angle, double power, int direction) {
                // Aquí gestionará en qué dirección se está moviendo.
                conn.sendData("MOVE," + angle + "," + power);
                /*
                switch (direction) {
                    case JoyStick.DIRECTION_CENTER:
                        // Cuando el JoyStick está en el centro no se hace nada

                        break;
                    case JoyStick.DIRECTION_LEFT_UP:
                        if (conn != null) {
                            conn.sendData("UL");
                        }
                        break;
                    case JoyStick.DIRECTION_UP:
                        if (conn != null) {
                            conn.sendData("U");
                        }
                        break;
                    case JoyStick.DIRECTION_UP_RIGHT:
                        if (conn != null) {
                            conn.sendData("UR");
                        }
                        break;
                    case JoyStick.DIRECTION_LEFT:
                        if (conn != null) {
                            conn.sendData("L");
                        }
                        break;
                    case JoyStick.DIRECTION_RIGHT:
                        if (conn != null) {
                            conn.sendData("R");
                        }
                        break;
                    case JoyStick.DIRECTION_DOWN_LEFT:
                        if (conn != null) {
                            conn.sendData("BL");
                        }
                        break;
                    case JoyStick.DIRECTION_DOWN:
                        if (conn != null) {
                            conn.sendData("B");
                        }
                        break;
                    case JoyStick.DIRECTION_RIGHT_DOWN:
                        if (conn != null) {
                            conn.sendData("BR");
                        }
                        break;
                    default:
                        // Nunca debería darse este caso
                        break;
                }
                */
            }

            @Override
            public void onTap() {
                // Nada
            }

            @Override
            public void onDoubleTap() {
                // Nada
            }
        });

        btnConn = (Button) findViewById(R.id.buttonConn);
        connState = (TextView) findViewById(R.id.connectStateText);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        drawerList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, menuList));
        drawerList.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                itemClicked(position);
            }
        });

        conn = null;

        loadActionBar();
    }

    private void itemClicked(int pos) {
        switch (pos) {
            case 0:
                conn.sendData("PAUSE");
                drawerLayout.closeDrawer(drawerList);
                break;
            case 1:
                conn.sendData("TURBO");
                drawerLayout.closeDrawer(drawerList);
                break;
            case 2:
                conn.sendData("DEBUG");
                drawerLayout.closeDrawer(drawerList);
                break;
            case 3:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Dirección del servidor");
                final EditText input = new EditText(this);
                input.setText(serverAddress);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
                builder.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        serverAddress = input.getText().toString();
                        SharedPreferences.Editor prefEdit = prefs.edit();
                        prefEdit.putString("svIp", serverAddress).apply();
                    }
                });
                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
                drawerLayout.closeDrawer(drawerList);
                break;
            case 4:
                conn.sendData("BYE");
                System.exit(0);
                break;
            default:
                // Nothing
                break;
        }
    }

    private void loadActionBar() {
        LayoutInflater inflator = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.actionbar, null);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setCustomView(v);
    }

    public void openMenu(View view) {
        if (drawerLayout.isDrawerOpen(drawerList)) {
            drawerLayout.closeDrawer(drawerList);
        } else {
            drawerLayout.openDrawer(drawerList);
        }
    }

    public void disableBtnConn() {
        btnConn.setEnabled(false);
    }

    public void setConnState(String s) {
        connState.setText(s);
    }

    public void connect(View view) {
        if (checkIp(serverAddress)) {
            System.out.println("llega");
            conn = new Connection(this, serverAddress);
            conn.run();
        } else {
            Toast.makeText(this, "Primero debe establecer una IP válida", Toast.LENGTH_SHORT);
        }
    }

    private boolean checkIp(String Value) {
        Pattern pattern = Pattern.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
        Matcher matcher = pattern.matcher(Value);
        return matcher.matches();
    }
}
