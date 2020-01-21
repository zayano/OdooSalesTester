package com.example.odoosalestester;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

public class CustomerFormActivity extends AppCompatActivity implements LocationListener {

    private OdooUtility odoo;
    private Partner partner;
    private String uid;
    private String password;
    private String database;
    private String serverAddress;

    private long searchTaskId;
    private long updateTaskId;
    private long createTaskId;
    private long deleteTaskId;

    EditText editName;
    EditText editStreet;
    EditText editStreet2;
    EditText editCity;
    EditText editState;
    EditText editCountry;
    EditText editEmail;
    EditText editMobile;
    EditText editGeo;

    private LocationManager locationManager;
    private String provider;
    private Double latitude;
    private Double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_form);

        initField();

        uid = SharedData.getKey(CustomerFormActivity.this, "uid");
        password = SharedData.getKey(CustomerFormActivity.this, "password");
        serverAddress = SharedData.getKey(CustomerFormActivity.this, "serverAddress");
        database = SharedData.getKey(CustomerFormActivity.this, "database");

        odoo = new OdooUtility(serverAddress, "object");

        partner = new Partner();

        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        if (name != null) {
            searchPartnerByName(name);
        }

        enableGPS();
    }

    private void enableGPS() {

        //Runtime permission request
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(getApplicationContext(), "<Explanation of permission request goes here>", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        locationManager.requestLocationUpdates(provider, 400, 1, this);
        Location location = locationManager.getLastKnownLocation(provider);

        if (location != null) {
            onLocationChanged(location);
        } else {
            Log.v("GPS", "Location not available");
        }
    }

    private void searchPartnerByName(String name) {

        // [ [["name","=",name]] ]
        List condition = Arrays.asList(
                Arrays.asList(
                        Arrays.asList("name", "=", name)
                )
        );

        // { "fields": ["id","name","email","city"]}
        Map fields = new HashMap() {{
            put("fields", Arrays.asList("id", "name", "street", "street2", "city", "state_id",
                    "country_id", "mobile", "email"));
            put("limit", 1);
        }};


        searchTaskId = odoo.search_read(listener, database, uid, password,
                "res.partner", condition, fields);
    }

    private void fillPartnerForm() {
        editName.setText(partner.getName());
        editStreet.setText(partner.getStreet());
        editStreet2.setText(partner.getStreet2());
        editCity.setText(partner.getCity());
        editCountry.setText(partner.getCountry());
        editState.setText(partner.getState());
        editMobile.setText(partner.getMobile());
        editEmail.setText(partner.getEmail());
    }

    public void updatePartnerModel() {
        partner.setName(editName.getText().toString());
        partner.setStreet(editStreet.getText().toString());
        partner.setStreet2(editStreet2.getText().toString());
        partner.setCity(editCity.getText().toString());
        partner.setMobile(editMobile.getText().toString());
        partner.setEmail(editEmail.getText().toString());
//        partner.setCountry(editCountry.getText().toString());
//        partner.setState(editState.getText().toString());
    }

    XMLRPCCallback listener = new XMLRPCCallback() {
        @Override
        public void onResponse(long id, Object result) {
            Looper.prepare();
            if (id == searchTaskId) {
                Object[] classObjs = (Object[]) result;
                int length = classObjs.length;

                for (int i = 0; i < length; i++) {
                    Map<String, Object> classObj = (Map<String, Object>) classObjs[i];
                    partner.setData(classObj);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fillPartnerForm();
                    }
                });

            } else if (id == updateTaskId) {
                Boolean res = (Boolean) result;
                if (res) {
                    odoo.MessageDialog(CustomerFormActivity.this, "Update Success");
                } else {
                    odoo.MessageDialog(CustomerFormActivity.this, "Update Failed");
                }
            } else if (id == createTaskId) {
                String res = result.toString();
                if (res != null) {
                    odoo.MessageDialog(CustomerFormActivity.this, "Create Success, Record ID = " + res);
                } else {
                    odoo.MessageDialog(CustomerFormActivity.this, "Create Failed");
                }
            } else if (id == deleteTaskId) {
                Boolean res = (Boolean) result;
                if (res != null) {
                    odoo.MessageDialog(CustomerFormActivity.this, "Delete Success");
                } else {
                    odoo.MessageDialog(CustomerFormActivity.this, "Delete Failed");
                }
            }
            Looper.loop();
        }

        @Override
        public void onError(long id, XMLRPCException error) {
            Looper.prepare();
            odoo.MessageDialog(CustomerFormActivity.this, error.getMessage());
            Looper.loop();
        }

        @Override
        public void onServerError(long id, XMLRPCServerException error) {
            Looper.prepare();
            odoo.MessageDialog(CustomerFormActivity.this, error.getMessage());
            Looper.loop();
        }
    };

    private void initField() {
        editName = findViewById(R.id.edt_name);
        editStreet = findViewById(R.id.edt_address);
        editStreet2 = findViewById(R.id.edt_street);
        editCity = findViewById(R.id.edt_city);
        editState = findViewById(R.id.edt_state);
        editCountry = findViewById(R.id.edt_country);
        editEmail = findViewById(R.id.edt_email);
        editMobile = findViewById(R.id.edt_mobile);
        editGeo = findViewById(R.id.edt_gps);
    }

    public void sendToOdoo() {

        // [ [id], {
        //      'name': "Newer partner",
        //      'email' : "email@daio.com"
        //}]

        List data = Arrays.asList(
                Arrays.asList(partner.getId()),
                new HashMap() {{
                    put("name", partner.getName());
                    put("street", partner.getStreet());
                    put("street2", partner.getStreet2());
                    put("city", partner.getCity());
                    put("mobile", partner.getMobile());
                    put("email", partner.getEmail());
                }}
        );
        updateTaskId = odoo.update(listener, database, uid, password,
                "res.partner", data);
    }

    public void onClickSave(View view) {
        updatePartnerModel();

        if (partner.getId() != null) {
            sendToOdoo();
        } else {
            createPartnerToOdoo();
        }


        Intent intent = new Intent(CustomerFormActivity.this, CustomerListActivity.class);
        startActivity(intent);
    }

    public void createPartnerToOdoo() {
        // { {"name":"abc", "street": "sudirman"} }
        List data = Arrays.asList(
                new HashMap() {{
                    put("name", partner.getName());
                    put("street", partner.getStreet());
                    put("street2", partner.getStreet2());
                    put("city", partner.getCity());
                    put("mobile", partner.getMobile());
                    put("email", partner.getEmail());
                }}
        );

        createTaskId = odoo.create(listener, database, uid, password,
                "res.partner", data);
    }

    public void onDeletePartner(View view) {
        String msg = "Are you sure to delete partner?";
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CustomerFormActivity.this);
        alertDialogBuilder.setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        deletePartnerOdoo();
                        Intent intent = new Intent(CustomerFormActivity.this, CustomerListActivity.class);
                        CustomerFormActivity.this.startActivity(intent);
                    }
                }).create().show();
    }

    private void deletePartnerOdoo() {
        // {{ id }}
        List condition = Arrays.asList(
                Arrays.asList(partner.getId())

        );

        deleteTaskId = odoo.delete(listener, database, uid,
                password, "res.partner", condition);

    }

    @Override
    public void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(provider, 400, 1, this);
    }

    public void onClickGpsPartner(View view) {
        if (latitude != null && longitude != null){

            List data = Arrays.asList(
                    Arrays.asList(partner.getId()),
                    new HashMap(){{
                        put("partner_longitude", longitude.toString());
                        put("partner_latitude", latitude.toString());
                    }}
            );
            updateTaskId = odoo.update(listener, database, uid, password,
                    "res.partner", data);
        } else {
            odoo.MessageDialog(CustomerFormActivity.this, "GPS not found!");
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        editGeo.setText(latitude.toString()+", "+longitude.toString());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Enable new provider " + provider ,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Disable provider " + provider ,
                Toast.LENGTH_SHORT).show();
    }
}
