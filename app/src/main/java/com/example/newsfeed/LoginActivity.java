package com.example.newsfeed;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private Button bt1;
    private Button bt2;
    private EditText ed1;
    private EditText ed2;
    private String name;
    private String password;
    private Config config;
    final private OkHttpClient client = new OkHttpClient();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        config = new Config();

        bt1 = (Button) findViewById(R.id.button1);
        bt2 = (Button) findViewById(R.id.button2);
        ed1 = (EditText) findViewById(R.id.editText1);
        ed2 = (EditText) findViewById(R.id.editText2);

        Log.d("LoginActivity", "onCreate: ");

        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                name=ed1.getText().toString().trim();
                password=ed2.getText().toString().trim();
                if (isLeagal(name) && isLeagal(password)) {
                    if (name.length() >=3 && name.length() <= 16 && password.length() >= 6 && password.length() <= 16 ) {
                        Log.d("LoginActivity", "ed1: "+name);
                        //通过okhttp发起post请求
                        postLoginRequest(name,password);
                    } else {
                        showToast("require: name length 3~16 and password length 6~16");
                    }
                } else {
                    showToast("Illegal input!");
                }


            }
        });

        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                name=ed1.getText().toString().trim();
                password=ed2.getText().toString().trim();
                if (isLeagal(name) && isLeagal(password)) {
                    if (name.length() >=3 && name.length() <= 16 && password.length() >= 6 && password.length() <= 16 ) {
                        Log.d("LoginActivity", "ed1: "+name);
                        //通过okhttp发起post请求
                        postRegisterRequest(name,password);
                    } else {
                        showToast("require: name length 3~16 and password length 6~16");
                    }
                } else {
                    showToast("Illegal input!");
                }


            }
        });

    }

    //用户登录请求
    private void postLoginRequest(final String name,final String password)  {
        //建立请求表单，添加上传服务器的参数
        RequestBody formBody = new FormBody.Builder()
                .add("name",name)
                .add("password",md5(password))
                .build();
        //发起请求
        final Request request = new Request.Builder()
                .url(config.getScheme() + "://" + config.getHost() + ":" +config.getPort().toString() + "/users/check")
                .post(formBody)
                .build();
        //新建一个线程，用于得到服务器响应的参数
        new Thread(new Runnable() {
            @Override
            public void run() {
                Response response = null;
                try {
                    //回调
                    response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    Log.d("LoginActivity", "responseData: "+responseData);
                    if (responseData != "NOTFOUND") {
                        JSONObject jsonObject = new JSONObject(responseData);
                        Log.d("LoginActivity", "name: "+jsonObject.getString("name") + ",password:"+jsonObject.getString("password"));
                        if (name.equals(jsonObject.getString("name")) && md5(password).equals(jsonObject.getString("password"))) {
                            Intent intent = new Intent();
                            intent.putExtra("name", name);
                            intent.putExtra("password", md5(password));
                            intent.putExtra("id", jsonObject.getString("id"));
                            intent.putExtra("groups2", jsonObject.getString("groups2"));
                            Log.d("Login Activity", "run: Login success");
                            showToast("Login Successful");
                            intent.putExtra("ed1", name);
                            setResult(RESULT_OK, intent);
                            finish();
                        } else {
                            showToast("Login Failed");
                        }

                    } else {
                        Log.d("LoginActivity", "run: NOTFOUND");
                        showToast("Login Failed");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("LoginActivity", "run: Exception");
                    showToast("Login Failed");
                }
            }
        }).start();

    }


    //用户注册请求
    private void postRegisterRequest(final String name,final String password)  {
        //建立请求表单，添加上传服务器的参数
        RequestBody formBody = new FormBody.Builder()
                .add("name",name)
                .add("password",md5(password))
                .build();
        //发起请求
        final Request request = new Request.Builder()
                .url(config.getScheme() + "://" + config.getHost() + ":" +config.getPort().toString() + "/users/add")
                .post(formBody)
                .build();
        //新建一个线程，用于得到服务器响应的参数
        new Thread(new Runnable() {
            @Override
            public void run() {
                Response response = null;
                try {
                    //回调
                    response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    Log.d("LoginActivity", "responseData: "+responseData);
                    if (responseData != "FAILURE" && responseData != "EXISTED") {
                        JSONObject jsonObject = new JSONObject(responseData);
                        Log.d("LoginActivity", "name: "+jsonObject.getString("name") + ",password:"+jsonObject.getString("password"));
                        if (name.equals(jsonObject.getString("name")) && md5(password).equals(jsonObject.getString("password"))) {
                            Intent intent = new Intent();
                            intent.putExtra("name", name);
                            intent.putExtra("password", md5(password));
                            intent.putExtra("id", jsonObject.getString("id"));
                            intent.putExtra("groups2", jsonObject.getString("groups2"));
                            Log.d("Login Activity", "run: Login SUCCESS");
                            showToast("Register Successful");
                            intent.putExtra("ed1", name);
                            setResult(RESULT_OK, intent);
                            finish();
                        }

                    } else if(responseData.equals("EXISTED")){
                        Log.d("LoginActivity", "run: username EXISTED");
                        showToast("Username Existed!");
                    } else if(responseData.equals("FULL")){
                        Log.d("LoginActivity", "run: username EXISTED");
                        showToast("Registration channel is closed temporarily!");
                    } else {
                        Log.d("LoginActivity", "run: register FAILURE");
                        showToast("Register Failed");
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("LoginActivity", "run: Exception");
                    showToast("Register Failed");
                }
            }
        }).start();

    }

    private void showToast (final String toastText) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(LoginActivity.this, toastText, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static String md5(String string)
    {
        if (TextUtils.isEmpty(string))
        {
            return "";
        }
        MessageDigest md5 = null;
        try
        {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            String result = "";
            for (byte b : bytes)
            {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1)
                {
                    temp = "0" + temp;
                }
                result += temp;
            }
            return result;
        } catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        return "";
    }

    public static boolean isLeagal(String s) {
        Character[] characters = new Character[]{'\'', '\"', '(', ')', '[', ']', ' ','$', '%', ',', '|', '&', '?', '/'};
        for (Character character:characters) {
            if (s.indexOf(character) >= 0) {
                return false;
            }
        }
        return true;
    }
}
