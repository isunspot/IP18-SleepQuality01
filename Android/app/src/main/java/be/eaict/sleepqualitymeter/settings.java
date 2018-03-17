package be.eaict.sleepqualitymeter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mukesh.countrypicker.Country;
import com.mukesh.countrypicker.CountryPicker;
import com.mukesh.countrypicker.CountryPickerListener;

/**
 * Created by CarlV on 2/22/2018.
 */

public class settings extends AppCompatActivity {
    String firstName, lastName, password, newcountry, oldcountry, weight, email, userID;
    Boolean switchTemperature , switchLight, switchMeasurement;
    Switch temperature, light, measurement;
    EditText editFirstName, editLastName, editPassword, editWeight;
    TextView selectCountry;
    CountryPicker picker;
    //Firebase
    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    DatabaseReference databaseReference;
    ImageView imgCountry;
    User user;
    DatabaseReference dbFirstName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        switchTemperature = false;
        switchLight = false;
        switchMeasurement = false;
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("User");
        dbFirstName = FirebaseDatabase.getInstance().getReference().child("User").child("firstname");
        Load();
        selectCountry = findViewById(R.id.setTxtSelectCountry);
        Button savebutton = findViewById(R.id.setBtnSave);
        Button saveDiscard = findViewById(R.id.setBtnDiscard);
        temperature = findViewById(R.id.setSwitchTemp);
        light = findViewById(R.id.setSwitchLight);
        measurement = findViewById(R.id.setSwitchMeasurement);
        editFirstName = findViewById(R.id.setEditFirstName);
        editLastName = findViewById(R.id.setEditLastName);
        editPassword = findViewById(R.id.setEditPassw);
        editWeight = findViewById(R.id.setEditWeight);
        imgCountry = findViewById(R.id.setImgCountry);
        editWeight.setText(weight);
        selectCountry.setText(oldcountry);
        editFirstName.setText(firstName);
    //    Country tempcountry = Country.getCountryByName(oldcountry);
    //    imgCountry.setImageResource(tempcountry.getFlag());
        savebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(temperature.isChecked()) {
                    switchTemperature = true;
                }
                else {
                    switchTemperature = false;
                }
                if(light.isChecked()) {
                    switchLight = true;
                }
                else {
                    switchLight = false;
                }
                if(measurement.isChecked()) {
                    switchMeasurement = true;
                }
                else {
                    switchMeasurement = false;
                }
                Save();

                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        light.setChecked(switchLight);
        temperature.setChecked(switchTemperature);
        measurement.setChecked(switchMeasurement);
        selectCountry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                picker = CountryPicker.newInstance("Select Country");  // dialog title
                picker.setListener(new CountryPickerListener() {
                    @Override
                    public void onSelectCountry(String name, String code, String dialCode, int flagDrawableResID) {
                        newcountry = name;
                        selectCountry.setText(newcountry);
                        imgCountry.setImageResource(flagDrawableResID);
                        picker.dismiss();
                    }
                });
                picker.show(getSupportFragmentManager(), "COUNTRY_PICKER");
            }
        });
    saveDiscard.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getBaseContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    });
    }

    public void Save() {
        final String newpassword = editPassword.getText().toString();
        final String newfirstName = editFirstName.getText().toString();
        final String newlastName = editLastName.getText().toString();
        String newweight = editWeight.getText().toString();
        SharedPreferences sp = getSharedPreferences("DATA", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("light", switchLight);
        editor.putBoolean("measurement", switchMeasurement);
        editor.putBoolean("temp", switchTemperature);
        //Set password Firebase
                    if(!newpassword.isEmpty()){
                        if(password.length() < 6){
                            editPassword.setError("Minimum lenght of password should be 6");
                            editPassword.requestFocus();
                            return;
                        }
                        firebaseUser.updatePassword(newpassword);
                    }
                    if(newfirstName != firstName && !editFirstName.toString().isEmpty()) {
                        databaseReference.child(userID).child("firstname").setValue(newfirstName);
                    }
                    if(newlastName != lastName && !editLastName.toString().isEmpty()) {
                        databaseReference.child(userID).child("lastname").setValue(newlastName);
                    }
        if(newweight != weight && !editWeight.toString().isEmpty()) {
         //   int t = Integer.getInteger(editWeight.getText().toString().);
          /*  if(t  > 200) {
                editWeight.setError("Max weight is 200");
                editWeight.requestFocus();
                return;
            } */
            if (measurement.isChecked() == false) {
                databaseReference.child(userID).child("weight").setValue(newweight);
            }
            else {
                Double tempweight = Double.parseDouble(newweight) * 0.45359237;
                int tempt = tempweight.intValue();
                databaseReference.child(userID).child("weight").setValue(Integer.toString(tempt));

            }
        }
        if (newcountry != oldcountry && newcountry != null) {
            databaseReference.child(userID).child("country").setValue(newcountry);
        }
        editor.apply();
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT);
    }

    public void Load() {
        SharedPreferences sp = getSharedPreferences("DATA", MODE_PRIVATE);
        switchMeasurement  = sp.getBoolean("measurement", false);
        switchLight = sp.getBoolean("light", false);
        switchTemperature = sp.getBoolean("temp", false);
        email = mAuth.getCurrentUser().getEmail().toLowerCase();
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String rawdata_weight = null;

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    user = snapshot.getValue(User.class);
                    System.out.println(user.getEmail());

                    if(user.getEmail().equals(email)){
                        System.out.println("WOOOOHOOO");
                        userID = user.getId();
                        firstName = user.getFirstname();
                        lastName = user.getLastname();
                        oldcountry = user.getCountry();
                        rawdata_weight = user.getWeight();
                        selectCountry.setText(oldcountry);
                        editFirstName.setText(firstName);
                        editLastName.setText(lastName);
                    }
                }

                //Convert KG to pound
                if(measurement.isChecked() ==true) {
                    Double lbstokg = Double.parseDouble(rawdata_weight) / 0.45359237;
                    int t = lbstokg.intValue();
                    weight = Integer.toString(t);
                    editWeight.setText(weight);
                }
                else {
                    weight = rawdata_weight;
                    editWeight.setText(weight);
                }

                Country tempcountry = Country.getCountryByName(oldcountry);
                imgCountry.setImageResource(tempcountry.getFlag());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                throw databaseError.toException();
            }
        });
    }
    }