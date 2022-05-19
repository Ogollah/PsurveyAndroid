package com.mhealthkenya.psurvey.activities.auth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.fxn.stash.Stash;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.mhealthkenya.psurvey.R;
import com.mhealthkenya.psurvey.activities.MainActivity;
import com.mhealthkenya.psurvey.depedancies.Constants;

import com.mhealthkenya.psurvey.models.auth;

import org.json.JSONException;
import org.json.JSONObject;

import static com.mhealthkenya.psurvey.depedancies.AppController.TAG;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

public class LoginActivity extends AppCompatActivity {


    private Button btn_login;
    private TextView sign_up;
    private TextView forgot_password;
    private TextInputEditText phoneNumber;
    private TextInputEditText password;
    private ProgressDialog pDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LoginActivity loginActivity =new LoginActivity();
        //loginActivity.
        setContentView(R.layout.activity_login);
        ///ssl








        ///ssl


        SSLContext context = null;
        try {
            context = SSLContext.getInstance("TLS 1.2");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            context.init(null, null, null);
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
       /*SSLSocketFactory noSSLv3Factory = null;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            noSSLv3Factory = new TLSSocketFactory(sslContext.getSocketFactory());
        } else {
            noSSLv3Factory = sslContext.getSocketFactory();
        }
        connection.setSSLSocketFactory(noSSLv3Factory);*/



        ///ssl


        Stash.init(this);
        setContentView(R.layout.activity_login);

        initialise();

        pDialog = new ProgressDialog(LoginActivity.this);
        pDialog.setTitle("Signing In...");
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);


        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            pDialog.show();
                            loginRequest();

                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });

                //pDialog.show();
                //loginRequest();

            }
        });

       /* forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(LoginActivity.this, "Forgot Password clicked!", Toast.LENGTH_SHORT).show();

            }
        });*/

        sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent mint = new Intent(LoginActivity.this, SignUpActivity.class);
                mint.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mint);
            }
        });


    }

    private void initialise(){

//        initialization of components

        btn_login = findViewById(R.id.btn_login);
        sign_up = (TextView) findViewById(R.id.tv_sign_up);
//        forgot_password = findViewById(R.id.tv_forgot_password);

        phoneNumber = (TextInputEditText) findViewById(R.id.edtxt_phone_no);
        password = (TextInputEditText) findViewById(R.id.edtxt_pass);

    }

    private void loginRequest() {


        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("msisdn", phoneNumber.getText().toString());
            jsonObject.put("password", password.getText().toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        AndroidNetworking.post(Constants.ENDPOINT+Constants.LOGIN)
                .addHeaders("Content-Type", "application.json")
                .addHeaders("Accept", "*/*")
                .addHeaders("Accept", "gzip, deflate, br")
                .addHeaders("Connection","keep-alive")
                .addJSONObjectBody(jsonObject) // posting json
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response

//                        Log.e(TAG, response.toString());

                        if (pDialog != null && pDialog.isShowing()) {
                            pDialog.hide();
                            pDialog.cancel();
                        }

                        try {
                            String auth_token = response.has("auth_token") ? response.getString("auth_token") : "";
                            auth newUser = new auth(auth_token);

                            Stash.put(Constants.AUTH_TOKEN, newUser);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                        if (response.has("auth_token")){

                            Intent mint = new Intent(LoginActivity.this, MainActivity.class);
                            mint.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(mint);


                            Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            if (pDialog != null && pDialog.isShowing()) {
                                pDialog.hide();
                                pDialog.cancel();
                            }

                            Toast.makeText(LoginActivity.this, "Please Try again later!", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                        Log.e(TAG, String.valueOf(error.getErrorCode()));

                        if (pDialog != null && pDialog.isShowing()) {
                            pDialog.hide();
                            pDialog.cancel();
                        }

                        if (error.getErrorBody().contains("Unable to log in with provided credentials.")){

                            Snackbar.make(findViewById(R.id.login_lyt), "Invalid phone number or password." , Snackbar.LENGTH_LONG).show();


                        }
                        else {

                            Snackbar.make(findViewById(R.id.login_lyt), "" + error.getErrorBody(), Snackbar.LENGTH_LONG).show();


                        }


                    }
                });
    }

}