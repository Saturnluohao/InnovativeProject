package com.tongji.helloworld.ui.flight.Dialog;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;

import com.tongji.helloworld.R;
import com.tongji.helloworld.ui.flight.Interface.HeatMapOperation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class CityPickerDialog extends DialogFragment {

    private HeatMapOperation operation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.setting_heatmap, container, false);
        final Spinner citySpinner = root.findViewById(R.id.city_picker);
        final Spinner timespanSpinner = root.findViewById(R.id.timespan_picker);

        Button confirmButton = root.findViewById(R.id.HMSetting_confirm);
        Button cancelButton = root.findViewById(R.id.HMSetting_cancel);



        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(operation != null){
                    int pos = timespanSpinner.getSelectedItemPosition();
                    operation.showHeatMap(citySpinner.getSelectedItem().toString(),
                            timespanSpinner.getSelectedItemPosition() * 4 + 4);
                    dismiss();
                }
            }
        });
        return root;
    }

    public void setHeatMapOperation(HeatMapOperation operation){
        this.operation = operation;
    }
}
