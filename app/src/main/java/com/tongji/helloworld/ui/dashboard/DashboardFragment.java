package com.tongji.helloworld.ui.dashboard;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.tongji.helloworld.engine.FlightDetail;
import com.tongji.helloworld.util.FlightInfoReceiver;

import com.google.android.material.textfield.TextInputEditText;
import com.tongji.helloworld.R;
import com.tongji.helloworld.widget.FlightDetailAdapter;

import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class DashboardFragment extends Fragment {

    private DashboardViewModel dashboardViewModel;
    private Button confirm=null;
    private EditText flightText=null;
    private List<FlightDetail> detailsList=null;
    private ListView flightList=null;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            FlightDetailAdapter adapter=new FlightDetailAdapter(getContext(), R.layout.flight_item, detailsList);
            flightList.setAdapter(adapter);
        }
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d("hidayat", "Dash Fragment's view created");

        dashboardViewModel = ViewModelProviders.of(this).get(DashboardViewModel.class);
        View root = inflater.inflate(R.layout.fragment_search, container, false);

        confirm = root.findViewById(R.id.confirm);
        flightText = root.findViewById(R.id.flightInput);
        flightList = root.findViewById(R.id.flighList);

        confirm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final String text = flightText.getText().toString();
                flightText.clearFocus();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            detailsList= FlightInfoReceiver.getFlightDetail(text);
                            handler.sendEmptyMessage(0);
                        }catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
        return root;
    }


    @Override
    public void onPause() {
        super.onPause();
        Log.d("saturn", "Dash Fragment paused");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("saturn", "Dash Fragment resumed");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("saturn", "Dash Fragment destroyed");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("saturn", "Dash Fragment created");
    }

}