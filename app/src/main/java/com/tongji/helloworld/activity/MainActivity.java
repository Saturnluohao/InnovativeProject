package com.tongji.helloworld.activity;

import android.Manifest;
import android.os.Bundle;
import android.os.Handler;

import com.baidu.mapapi.SDKInitializer;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.ar.core.ArCoreApk;
import com.tongji.helloworld.R;
import com.tongji.helloworld.ui.dashboard.DashboardFragment;
import com.tongji.helloworld.ui.home.HomeFragment;
import com.tongji.helloworld.ui.notifications.NotificationsFragment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        navView = findViewById(R.id.nav_view);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 1);
        //initView();
    }

    /*public void initView() {
        homeFragment = new HomeFragment();
        dashFragment = new DashboardFragment();
        notiFragment = new NotificationsFragment();
        bottomNavigationView = findViewById(R.id.nav_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);//设置导航栏监听器
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.nav_host_fragment, homeFragment);
        transaction.add(R.id.nav_host_fragment, dashFragment);
        transaction.add(R.id.nav_host_fragment, notiFragment);
        transaction.hide(dashFragment);
        transaction.hide(notiFragment);
        transaction.commit();
    }

    @Override
    //处理导航栏子项的点击事件
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int itemId = menuItem.getItemId();//获取点击的位置以及对应的id
        switch (itemId) {
            case R.id.navigation_home:
                getSupportFragmentManager().beginTransaction().hide(dashFragment)
                        .hide(notiFragment).show(homeFragment).commit();
                menuItem.setChecked(true);
                break;
            case R.id.navigation_dashboard:
                getSupportFragmentManager().beginTransaction().hide(homeFragment)
                        .hide(notiFragment).show(dashFragment).commit();
                menuItem.setChecked(true);
                break;
            case R.id.navigation_notifications:
                getSupportFragmentManager().beginTransaction().hide(homeFragment)
                        .hide(dashFragment).show(notiFragment).commit();
                menuItem.setChecked(true);
                break;
        }
        return false;
    }*/
}
