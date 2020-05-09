package com.acms.iexplore.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.acms.iexplore.customviews.LoadingDialog;
import com.acms.iexplore.R;
import com.acms.iexplore.retrofit.RetrofitClientInstance;
import com.acms.iexplore.retrofit.RetrofitInterface;
import com.acms.iexplore.models.BuildingModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class BuildingsFragment extends Fragment {

    private final String TAG = "BuildingsFragment";

    private ListView geofencesList;
    private GeofencesListAdapter geofencesListAdapter;

    private List <BuildingModel> buildings;

    private LoadingDialog loadingDialog;

    public BuildingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadingDialog = new LoadingDialog(getActivity());

        getBuildings();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_buildings, container, false);

        geofencesList = root.findViewById(R.id.geofencesList);
        geofencesListAdapter = new GeofencesListAdapter();

        return root;
    }

    private void getBuildings() {

        Retrofit retrofit = RetrofitClientInstance.getRetrofitInstance();
        RetrofitInterface retrofitInterface = retrofit.create(RetrofitInterface.class);

        loadingDialog.startLoadingDialog();

        Call<List<BuildingModel>> call = retrofitInterface.executeGetAllBuildingsInfo();

        call.enqueue(new Callback<List<BuildingModel>>() {
            @Override
            public void onResponse(Call<List<BuildingModel>> call, Response<List<BuildingModel>> response) {
                if (!response.isSuccessful()) {
                    loadingDialog.dismissLoadingDialog();
                    Toast.makeText(getContext(), "Failed to fetch buildings, failure code: " + response.code(), Toast.LENGTH_SHORT).show();
                    return;
                }
                buildings = response.body();
                loadingDialog.dismissLoadingDialog();
                geofencesList.setAdapter(geofencesListAdapter);
            }

            @Override
            public void onFailure(Call<List<BuildingModel>> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to fetch buildings from db: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                loadingDialog.dismissLoadingDialog();
            }
        });
    }

    class GeofencesListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return buildings.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.geofences_list_item, null);

            TextView buildingIdView = view.findViewById(R.id.buildingId);
            TextView buildingAddressView = view.findViewById(R.id.buildingAddress);

            buildingIdView.setText(buildings.get(position).getId());
            buildingAddressView.setText(buildings.get(position).getAddress());

            return view;
        }
    }
}
