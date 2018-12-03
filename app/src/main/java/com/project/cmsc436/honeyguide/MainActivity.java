package com.project.cmsc436.honeyguide;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;

import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.util.Log;
import android.widget.EditText;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import android.content.Intent;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import android.content.SharedPreferences;



/** Note that here we are inheriting ListActivity class instead of Activity class **/
public class MainActivity extends AppCompatActivity {
    /** Items entered by the user is stored in this ArrayList variable */
    ArrayList<String> list = new ArrayList<String>();
   // ArrayList<String> selectList = new ArrayList<String>();
    private final int RESULT_REQUEST_RECORD_AUDIO = 1;
    private String TAG = "Honeyguide-Debug: ";
    private final String COLLECTION_NAME = "art_pieces ";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference docRef;
    private SharedPreferences sharedpreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.navigation_home:
                                FragmentTransaction homeTransaction = getSupportFragmentManager().beginTransaction();
                                homeTransaction.replace(R.id.frame_layout, HomeFragment.newInstance());
                                homeTransaction.commit();
                                break;
                            case R.id.navigation_saved:
                                FragmentTransaction savedTransaction = getSupportFragmentManager().beginTransaction();
                                savedTransaction.replace(R.id.frame_layout, SavedFragment.newInstance());
                                savedTransaction.commit();
                                break;
                            case R.id.navigation_settings:
                                FragmentTransaction settingsTransaction = getSupportFragmentManager().beginTransaction();
                                settingsTransaction.replace(R.id.frame_layout, new SettingsFragment());
                                settingsTransaction.commit();
                                break;
                        }
                        return true;
                    }
                });

        //fetching information from firebase once

        for(int i = 1; i <= 3; i++){
            docRef = db.collection(COLLECTION_NAME).document(Integer.toString(i));
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Map< String,Object> docFields = document.getData();
                            String title = docFields.get("Full title").toString();
                            Log.d(TAG, "Firebase Success: " + title);
                            sharedpreferences = getSharedPreferences(title, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedpreferences.edit();

                            for (Map.Entry<String,Object> field : docFields.entrySet()){
                                editor.putString(field.getKey(), field.getValue().toString());
                            }

                            editor.apply();
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });
        }

        //Manually displaying the first fragment - one time only
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, HomeFragment.newInstance());
        transaction.commit();

    }

    public void launchHomeScreen() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public void addItem() {
        Log.i("i", "entered addItem()");
        EditText edit = (EditText) findViewById(R.id.txtItem);
        list.add(edit.getText().toString());
        edit.setText("");
    }

   /* public void selectItem(String item) {
        selectList.add(item);
    }

    public ArrayList<String> getSelectList() {
        return selectList;
    }
*/
    //testing purpose
    public void launchDefaultPiece(){
        startActivity(new Intent(MainActivity.this,defaultPiece.class));
    }

    public ArrayList<String> getList() {
        return list;
    }

    public void clearData() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.clearApplicationUserData();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.RECORD_AUDIO}, RESULT_REQUEST_RECORD_AUDIO);
        }
        else {
            Intent i= new Intent(getApplicationContext(), ChirpService.class);
            startService(i);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RESULT_REQUEST_RECORD_AUDIO: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent i= new Intent(getApplicationContext(), ChirpService.class);
                    startService(i);                }
                return;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent i= new Intent(getApplicationContext(), ChirpService.class);
        stopService(i);
    }

    /*public void onGarbageAction(MenuItem m) {
        selectList.clear();
        SavedFragment.newInstance();
    }*/
}
