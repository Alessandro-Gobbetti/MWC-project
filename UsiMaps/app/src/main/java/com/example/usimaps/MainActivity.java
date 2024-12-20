package com.example.usimaps;

import android.Manifest;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import com.example.usimaps.map.Graph;
import com.example.usimaps.map.Vertex;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.app.ActivityCompat;
import androidx.annotation.NonNull;

import com.example.usimaps.databinding.ActivityMainBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * MainActivity serves as the primary entry point of the application, managing navigation,
 * permissions, and the initialization of application data such as the USI map.
 *
 * This class integrates a bottom navigation bar and sets up navigation controllers
 * to manage different fragments within the app. It also handles runtime permission requests
 * and ensures that required permissions are granted before using certain features.
 */
public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    private static final int REQUEST_CODE_PERMISSIONS = 45;
    private boolean runningOorLater = android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;

    /**
     * Initializes the activity, setting up navigation, permissions, and application data.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down, this contains the most recent data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        // Setup navController
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .build();
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);

        // Setup bottom nav
        NavigationUI.setupWithNavController(binding.bottomNav, navController);

        // Permissions
        if (runningOorLater) {
            requestPermissionsIfNeeded();
        }


        SharedPreferences prefs = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
        if(!prefs.getBoolean("firstTime", false)) {
            // store the usi map in the database
            Graph graph = new Graph().generateUSIMap();
            for (Vertex vertex : graph.getVertices()) {
                String pictureName = vertex.getName()+".jpeg";
                if (vertex.getName().contains("-")) {
                    String[] parts = vertex.getName().split("-");
                    pictureName = parts[1] + ".jpeg";
                }

                InputStream inputStream = null;
                try {
                    try {
                        inputStream = getAssets().open("pictures_usi/" + pictureName);
                        Log.i("Main Activity","Picture found for vertex " + pictureName);
                    } catch (IOException e) {
                        inputStream = getAssets().open("pictures_usi/sectorA1.jpg");
                        Log.e("Main Activity", "Picture not found for vertex " + pictureName + ". Using default picture instead.");
                    }
                    // save the picture to the internal storage
                    File file = new File(getFilesDir(), pictureName);
                    // save the picture to the internal storage
                    FileOutputStream outputStream = new FileOutputStream(file);
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, read);
                    }
                    outputStream.close();
                    inputStream.close();
                    // update the vertex with the path to the picture
                    vertex.setImagePath(file.getAbsolutePath());
                    System.out.println("Picture saved for vertex " + vertex.getName() + " at " + file.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            DatabaseHelper dbHelper = new DatabaseHelper(this);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            String mapName = graph.getMapName();
            byte[] bytegraph = Graph.serialize(graph);
            values.put(DatabaseHelper.COLUMN_MAP_NAME, mapName);
            values.put(DatabaseHelper.COLUMN_MAP_OBJECT, bytegraph);

            // Check if the map already exists
            String selection = DatabaseHelper.COLUMN_MAP_NAME + " = ?";
            String[] selectionArgs = { mapName };

            int count = db.update(DatabaseHelper.TABLE_MAPS, values, selection, selectionArgs);

            // If the map does not exist, insert it
            if (count == 0) {
                db.insert(DatabaseHelper.TABLE_MAPS, null, values);
            }

            db.close();

            // set first time to false
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("firstTime", true);
            editor.commit();

            Log.i("Main Activity:", "USI map stored in the database for the first time");
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Handles navigation when the user selects the Up button.
     *
     * @return True if navigation was successful; false otherwise.
     */
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    /**
     * Requests necessary permissions (camera, location, microphone) at runtime if they
     * have not already been granted.
     */
    private void requestPermissionsIfNeeded() {
        String[] permissions = {
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.RECORD_AUDIO
        };

        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), REQUEST_CODE_PERMISSIONS);
        }
    }

    /**
     * Handles the results of runtime permission requests.
     *
     * @param requestCode  The request code passed in the permission request.
     * @param permissions  The requested permissions.
     * @param grantResults The grant results for the corresponding permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            boolean cameraGranted = false;
            boolean locationGranted = false;

            // Check each permission result
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.CAMERA)) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        cameraGranted = true;
                    } else {
                        Log.e("Main Activity", "Camera permission denied");
                    }
                } else if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        locationGranted = true;
                    } else {
                        Log.e("Main Activity", "Location permission denied");
                    }
                }
            }

            // Handle granted permissions
            if (cameraGranted) {
                Log.i("Main Activity", "Camera permission granted");
            }

            if (locationGranted) {
                Log.i("Main Activity", "Location permission granted");
            }
        }
    }

}