package com.tongji.helloworld.util;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLngBounds;
import com.tongji.helloworld.R;
import com.tongji.helloworld.engine.FlightInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MarkerUpdater {
    public Map<String, Marker> markerMap;
    public List<OverlayOptions> options;

    private BitmapDescriptor bitmap;

    public MarkerUpdater(){
        markerMap = new HashMap<>();
        options = new ArrayList<>();
        bitmap = new BitmapDescriptorFactory().fromResource(R.drawable.airplane);
    }

    public void updateOverlayMap(List<FlightInfo> data, BaiduMap mBaiduMap){
        if(mBaiduMap.getMapStatus() == null)
            return;
        Marker temp;
        removeOutOfBound(mBaiduMap.getMapStatus().bound);
        for(FlightInfo flightInfo : data){
            if(markerMap.containsKey(flightInfo.icao)){
                temp = markerMap.get(flightInfo.icao);
                temp.setPosition(flightInfo.position);
                temp.setRotate((float)(flightInfo.direction));
            }
            else{
                markerMap.put(flightInfo.icao, (Marker) mBaiduMap.addOverlay(
                        new MarkerOptions().icon(bitmap)
                        .anchor(0.5f, 0.5f)
                        .scaleX(0.4f)
                        .scaleY(0.4f)
                        .position(flightInfo.position)
                        .rotate(flightInfo.direction)
                        .flat(true)
                        .title(flightInfo.icao)

                ));
            }
        }
    }

    private void removeOutOfBound(LatLngBounds bound){
        Iterator<Map.Entry<String, Marker>> it = markerMap.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<String, Marker> entry = it.next();
            if(!bound.contains(entry.getValue().getPosition())){
                entry.getValue().remove();
                it.remove();
            }
        }
    }
}
