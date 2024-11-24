package edu.ewubd.cse489_23_2_2019360046;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignupActivity extends Activity {

    private TextView btnToggle, tvToggleLabel, tvTitle;
    private TableRow rowName, rowEmail, rowPhone, rowRePass;
    private boolean isLoginPage = false;

    String errMessage = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        SharedPreferences sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);

        tvToggleLabel = findViewById(R.id.tvToggleLabel);
        tvTitle = findViewById(R.id.tvTitle);
        btnToggle = findViewById(R.id.btnToggle);
        rowName = findViewById(R.id.rowName);
        rowEmail = findViewById(R.id.rowEmail);
        rowPhone = findViewById(R.id.rowPhone);
        rowRePass = findViewById(R.id.rowRePass);
        EditText etUserId = findViewById(R.id.etUserId);

        try {
            boolean isRemLoginChecked = sharedPreferences.getBoolean("isRemLoginChecked", false);
            boolean isRemUsedIdChecked = sharedPreferences.getBoolean("isRemUsedIdChecked", false);
            Log.d("Debug", "isRemUsedIdChecked: " + isRemUsedIdChecked);
            if (isRemLoginChecked) {
                startActivity(new Intent(SignupActivity.this, MainActivity.class));
                finish();
            }
            if (isRemUsedIdChecked) {
                String id = sharedPreferences.getString("userID", "");
                etUserId.setText(id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.changeView();

        btnToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLoginPage = !isLoginPage;   // One click true another false
                changeView();
            }
        });

        findViewById(R.id.btnGo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLoginPage) {
                    String userId = ((EditText) findViewById(R.id.etUserId)).getText().toString().trim();
                    String password = ((EditText) findViewById(R.id.etPassword)).getText().toString().trim();
                    boolean isRemUsedIdChecked = ((CheckBox) findViewById(R.id.chkRemUsedId)).isChecked();
                    boolean isRemLoginChecked = ((CheckBox) findViewById(R.id.chkRemLogin)).isChecked();

                    boolean isValidData = validateLoginData(userId, password);

                    if (isValidData) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("isRemUsedIdChecked", isRemUsedIdChecked);

                        String storedUserId = sharedPreferences.getString("userID", "");
                        String storedPassword = sharedPreferences.getString("password", "");

                        if (userId.equals(storedUserId) && password.equals(storedPassword)) {
                            editor.putBoolean("isRemLoginChecked", isRemLoginChecked);
                            editor.apply();
                            startActivity(new Intent(SignupActivity.this, MainActivity.class));
                            finish();
                        } else {
                            showErrorMessage("Invalid credentials.\n");
                        }
                    } else {
                        showErrorMessage(errMessage);
                    }
                } else {
                    String name = ((EditText) findViewById(R.id.etName)).getText().toString().trim();
                    String email = ((EditText) findViewById(R.id.etEmail)).getText().toString().trim();
                    String phone = ((EditText) findViewById(R.id.etPhone)).getText().toString().trim();
                    String userID = ((EditText) findViewById(R.id.etUserId)).getText().toString().trim();
                    String password = ((EditText) findViewById(R.id.etPassword)).getText().toString().trim();
                    String rePassword = ((EditText) findViewById(R.id.etRePassword)).getText().toString().trim();
                    boolean isRemUsedIdChecked =((CheckBox) findViewById(R.id.chkRemUsedId)).isChecked();
                    boolean isRemLoginChecked = ((CheckBox) findViewById(R.id.chkRemLogin)).isChecked();

                    boolean isValidData = validateSignupData(name, email, phone, userID, password, rePassword);

                    if (isValidData) {
                        if (password.equals(rePassword)) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("name", name);
                            editor.putString("email", email);
                            editor.putString("phone", phone);
                            editor.putString("userID", userID);
                            editor.putString("password", password);
                            editor.putBoolean("isRemUsedIdChecked", isRemUsedIdChecked);
                            editor.putBoolean("isRemLoginChecked", isRemLoginChecked);
                            editor.apply();
                            startActivity(new Intent(SignupActivity.this, MainActivity.class));
                            finish();
                        } else {
                            showErrorMessage("Passwords do not match.\n");
                        }
                    } else {
                        showErrorMessage(errMessage);
                    }
                }
            }
        });

        findViewById(R.id.btnExit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    private void changeView(){
        if(isLoginPage) {
            rowName.setVisibility(View.GONE);
            rowEmail.setVisibility(View.GONE);
            rowPhone.setVisibility(View.GONE);
            rowRePass.setVisibility(View.GONE);
            tvTitle.setText("Login");
            tvToggleLabel.setText("Don't have an account?");
            btnToggle.setText("Signup");
        } else{
            rowName.setVisibility(View.VISIBLE);
            rowEmail.setVisibility(View.VISIBLE);
            rowPhone.setVisibility(View.VISIBLE);
            rowRePass.setVisibility(View.VISIBLE);
            tvTitle.setText("Signup");
            tvToggleLabel.setText("Already have an account?");
            btnToggle.setText("Login");
        }
    }

    private boolean validateSignupData(String name, String email, String phone, String userID, String password, String rePassword) {
        errMessage = "";
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || userID.isEmpty() || password.isEmpty() || rePassword.isEmpty()) {
            errMessage += "All fields are required.\n---------------------\n";
        }

        if (name.length() < 4 || name.length() > 16 || !name.matches("[a-zA-Z]+")) {
            errMessage += "Name should be 4-16 characters.\n";
        }

        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        if (!email.matches(emailPattern)) {
            errMessage += "Invalid email format.\n";
        }

        String regex = "^(\\+)?(88)?01[0-9]{9}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(phone);
        if (!matcher.matches()) {
            errMessage += "Invalid Phone.\n";
        }

        if (userID.length() < 4 || userID.length() > 6) {
            errMessage += "User ID should be 4-8 characters long.\n";
        }

        if (password.length() < 4 || password.length() > 6) {
            errMessage += "Password should be 4-6 characters long.\n";
        }

        if (!password.equals(rePassword)) {
            errMessage += "Passwords do not match.\n";
        }

        if (errMessage.isEmpty()){
            return true;
        } else {
            return false;
        }
    }

    private boolean validateLoginData(String userId, String password) {
        errMessage = "";
        if (userId.isEmpty() || password.isEmpty()) {
            errMessage += "Please enter both User ID and Password.\n";
        }

        if (password.length() < 4 || password.length() > 6) {
            errMessage += "Invalid password.\n";
        }

        if (errMessage.isEmpty()){
            return true;
        } else {
            return false;
        }
    }

    private void showErrorMessage(String receivedErrMessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(receivedErrMessage);
        builder.setTitle("Error");
        builder.setCancelable(true);

        builder.setPositiveButton("Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}