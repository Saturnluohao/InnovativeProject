package com.tongji.helloworld.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tongji.helloworld.engine.FlightDetail;
import com.tongji.helloworld.R;

import java.util.List;

public class FlightDetailAdapter extends ArrayAdapter {
    private int resourceId;

    public FlightDetailAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List objects){
        super(context, resource, objects);
        resourceId=resource;
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        View view;
        DetailHolder detailHolder;
        FlightDetail detail=(FlightDetail) getItem(position);
        if(convertView==null){
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            detailHolder=new DetailHolder();
            detailHolder.getView(view);
            view.setTag(detailHolder);
        }
        else{
            view=convertView;
            detailHolder=(DetailHolder) view.getTag();
        }
        detailHolder.setDetail(detail);
        return view;
    }

    class DetailHolder{
        TextView flightId, icao, airline, type, lat, lng, height, start, dest, direction;

        public void getView(View view){
            flightId=(TextView) view.findViewById(R.id.flightId);
            icao=(TextView) view.findViewById(R.id.ICAO);
            airline=(TextView) view.findViewById(R.id.airline);
            type=(TextView) view.findViewById(R.id.type);
            lat=(TextView) view.findViewById(R.id.lat);
            lng=(TextView) view.findViewById(R.id.lng);
            height=(TextView) view.findViewById(R.id.height);
            start=(TextView) view.findViewById(R.id.start);
            dest=(TextView) view.findViewById(R.id.dest);
            direction=(TextView) view.findViewById(R.id.direction);
        }

        public void setDetail(FlightDetail detail){
            flightId.setText("航班\n  "+detail.flight);
            icao.setText("ICAO\n  "+detail.icao);
            airline.setText("航线："+detail.airline);
            type.setText("机型："+detail.type);
            lat.setText("纬度："+""+detail.lat+"N");
            lng.setText("经度："+""+detail.lng+"E");
            height.setText("高度："+""+detail.height+"ft");
            start.setText("起飞机场："+detail.start);
            dest.setText("目的机场："+detail.end);
            direction.setText("方向："+""+detail.direction+"°");
        }
    }
}
