package com.blessk.mlwparticipant;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

public class TabHomeFragment extends Fragment{
    String[] init_setting;
    String project_code;
    String server_ip;
    String auth_code;
    //interface via which we communicate to hosting Activity
    private ActivityCommunicator activityCommunicator;
    private DatabaseHelper dbHelper;
    private TextView project;
    private Spinner location_view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view= inflater.inflate(R.layout.tab_home_fragment, container, false);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View view=getView();
        //setup
        project=(TextView) view.findViewById(R.id.project_value);
        dbHelper=new DatabaseHelper(getActivity());
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run()
            {

                init_setting=dbHelper.getProjectSetting();
                project_code=init_setting[0];
                server_ip=init_setting[1];
                auth_code=init_setting[2];
                if(project_code!=null)
                    project.setText(project_code);
                else
                {
                    //start setting activity
                    startActivity(new Intent(getActivity(),ProjectManager.class));
                    return;
                }
            }
        });
        //setup

        //populate sites if available
        location_view= (Spinner) view.findViewById(R.id.location);
        // Spinner Drop down elements
        List<String> location_list = dbHelper.getParticipantLocation();
        SpinnerAdapter location_adapter = new SpinnerAdapter(getActivity(), android.R.layout.simple_list_item_1);
        location_adapter.add("All");
        location_adapter.addAll(location_list);
        location_adapter.add("All");//visible
        //location_adapter.add("Select location");
        location_view.setAdapter(location_adapter);
        location_view.setSelection(location_adapter.getCount());
        location_view.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                activityCommunicator.passDataToActivity(parent.getItemAtPosition(position).toString());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
            }
        });
location_view.setSelection(0);
        /**list view*/

    }
    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        context = getActivity();
        activityCommunicator =(ActivityCommunicator)context;
    }
}