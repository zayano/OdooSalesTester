package com.example.odoosalestester;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

public class MainActivity extends AppCompatActivity {
    private OdooUtility odoo;
    private long loginTaskId;

    EditText editUsername;
    EditText editPassword;
    EditText editServerAddress;
    EditText editDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String database = SharedData.getKey(MainActivity.this, "database");
        String serverAddress = SharedData.getKey(MainActivity.this, "serverAddress");
        String username = SharedData.getKey(MainActivity.this, "username");
        String password = SharedData.getKey(MainActivity.this, "password");

        editServerAddress = findViewById(R.id.edt_server);
        editDatabase = findViewById(R.id.edt_database);
        editUsername = findViewById(R.id.edt_username);
        editPassword = findViewById(R.id.edt_password);

        editServerAddress.setText(serverAddress);
        editDatabase.setText(database);
        editUsername.setText(username);
        editPassword.setText(password);

        //Runtime permission request
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(getApplicationContext(), "<Explanation of permission request goes here>", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }

    }

    public void onLoginClick(View view){
        switch (view.getId()){
            case R.id.btn_login:
                String serverAddress = editServerAddress.getText().toString();
                String database = editDatabase.getText().toString();
                String username = editUsername.getText().toString();
                String password = editPassword.getText().toString();

                odoo = new OdooUtility(serverAddress,"common");
                loginTaskId = odoo.login(listener, database, username, password);

                SharedData.setKey(MainActivity.this, "serverAddress", serverAddress);
                SharedData.setKey(MainActivity.this, "database", database);
                SharedData.setKey(MainActivity.this, "username", username);
                SharedData.setKey(MainActivity.this, "password", password);
        }
    }

    XMLRPCCallback listener = new XMLRPCCallback() {
        @Override
        public void onResponse(long id, Object result) {
            Looper.prepare();
            if (id == loginTaskId){
                if (result instanceof Boolean && (Boolean) result == false){
                    odoo.MessageDialog(MainActivity.this, "Login Error");
                }
                else {
                    String uid = result.toString();
                    SharedData.setKey(MainActivity.this, "uid", uid);
//                    odoo.MessageDialog(MainActivity.this, "Login Succeeded. uid = "+uid);

                    Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                    MainActivity.this.startActivity(intent);
                }
            }
            Looper.loop();
        }

        @Override
        public void onError(long id, XMLRPCException error) {
            Looper.prepare();
            odoo.MessageDialog(MainActivity.this, "Login Error: " + error.getMessage());
            Looper.loop();
        }

        @Override
        public void onServerError(long id, XMLRPCServerException error) {
            Looper.prepare();
            odoo.MessageDialog(MainActivity.this, "Server Error: " + error.getMessage());
            Looper.loop();
        }
    };
}
