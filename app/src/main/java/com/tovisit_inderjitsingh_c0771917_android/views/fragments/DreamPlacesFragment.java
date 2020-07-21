package com.tovisit_inderjitsingh_c0771917_android.views.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tovisit_inderjitsingh_c0771917_android.R;
import com.tovisit_inderjitsingh_c0771917_android.adapter.DreamPlacesAdapter;
import com.tovisit_inderjitsingh_c0771917_android.database.AppDatabase;
import com.tovisit_inderjitsingh_c0771917_android.database.AppExecutors;
import com.tovisit_inderjitsingh_c0771917_android.models.DreamPlacesModel;
import com.tovisit_inderjitsingh_c0771917_android.models.NotifyData;
import com.tovisit_inderjitsingh_c0771917_android.models.RefreshDataBroadcastReceiver;
import com.tovisit_inderjitsingh_c0771917_android.views.activities.MapDreamPlaceActivity;

import java.util.ArrayList;
import java.util.List;

public class DreamPlacesFragment extends Fragment implements NotifyData {
    private AppDatabase database;
    private RecyclerView recyclerViewPlaces;
    private ProgressBar progressBar;
    private TextView tvEmptyData;
    private List<DreamPlacesModel> places = new ArrayList<>();
    private DreamPlacesAdapter adapter;
    private View rootView;
    SharedPreferences sharedpreferences;
    public static final String MyPREFERENCES = "MyPrefs";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_dream_places, container, false);

        database = AppDatabase.getInstance(getActivity());
        sharedpreferences = getContext().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        subscribeRefreshDataReceiver();
        initViews();
        getPlacesFromDatabase();
        return rootView;
    }

    private void initViews() {
        progressBar = rootView.findViewById(R.id.progress_bar);
        recyclerViewPlaces = rootView.findViewById(R.id.recyclerViewPlaces);
        tvEmptyData = rootView.findViewById(R.id.tv_empty_data);
        swipeDeleteRecyclerView();
    }

    private void getPlacesFromDatabase() {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                places = database.dreamPlaces().getDreamPlacesList();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            if (places.size() > 0) {
                                setAdapter(places);
                                tvEmptyData.setVisibility(View.GONE);
                            } else {
                                tvEmptyData.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }
            }
        });
    }

    private void subscribeRefreshDataReceiver() {
        RefreshDataBroadcastReceiver refreshDataBroadcastReceiver = new RefreshDataBroadcastReceiver(this);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(refreshDataBroadcastReceiver,
                new IntentFilter("refreshData"));
    }

    private void setAdapter(List<DreamPlacesModel> places) {
        adapter = new DreamPlacesAdapter(this, places);
        recyclerViewPlaces.setHasFixedSize(true);
        recyclerViewPlaces.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerViewPlaces.setAdapter(adapter);
    }

    public void updateDreamPlace(final int id, final Double mlatitude, final Double mlongitude, final String name) {
        final DreamPlacesModel place = new DreamPlacesModel(mlatitude, mlongitude, name, true, id);
        showVisitDialog(place);
    }

    private void showVisitDialog(final DreamPlacesModel place) {
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_visit);
        dialog.setCancelable(false);

        final RadioGroup radioGroupVisit = dialog.findViewById(R.id.rdaioGroup_visit);
        final RadioButton rbNotVisit = dialog.findViewById(R.id.rb_not_visit);
        final RadioButton rbVisit = dialog.findViewById(R.id.rb_visit);
        TextView tvSelect = dialog.findViewById(R.id.tv_select);
        TextView tvCancel = dialog.findViewById(R.id.tv_cancel);


        tvSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rbVisit.isChecked()) {
                    AppExecutors.getInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            database.dreamPlaces().updateLocation(place);
                            getPlacesFromDatabase();
                        }
                    });
                }
                dialog.dismiss();
            }
        });
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }


    private void swipeDeleteRecyclerView() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        final int position = viewHolder.getLayoutPosition();
                        database.dreamPlaces().deleteLocation(places.get(position));
                        getPlacesFromDatabase();
                    }
                });
                Toast.makeText(getActivity(), "Place Deleted Successfully", Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(recyclerViewPlaces);
    }

    public void clickView(DreamPlacesModel dreamPlacesModel) {

        Intent intent = new Intent(getActivity(), MapDreamPlaceActivity.class);
        intent.putExtra("address", dreamPlacesModel.getName());
        intent.putExtra("latitude", dreamPlacesModel.getLatitude().toString());
        intent.putExtra("longitude", dreamPlacesModel.getLongitude().toString());
        intent.putExtra("id", String.valueOf(dreamPlacesModel.getId()));
        startActivity(intent);
    }

    @Override
    public void refreshData() {
        getPlacesFromDatabase();
    }
}
