package com.biotag.opencv4androiddemo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class ChooseActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btn_hunman_reco;
    private Button btn_dog_reco;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);
        initView();
        initAuthority(this);
    }

    private void initAuthority(Context context) {
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //申请了两种权限：WRITE_EXTERNAL_STORAGE与 CAMERA 权限
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE}, 10);
            return;
        }
    }

    private void initView() {
        btn_hunman_reco = (Button) findViewById(R.id.btn_hunman_reco);
        btn_dog_reco = (Button) findViewById(R.id.btn_dog_reco);

        btn_hunman_reco.setOnClickListener(this);
        btn_dog_reco.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        intent= new Intent(ChooseActivity.this,MainActivity.class);
        switch (v.getId()) {
            case R.id.btn_hunman_reco:
                intent.putExtra("id","human");
                startActivity(intent);
                break;
            case R.id.btn_dog_reco:
                intent.putExtra("id","dog");
                startActivity(intent);
                break;
        }
    }
}