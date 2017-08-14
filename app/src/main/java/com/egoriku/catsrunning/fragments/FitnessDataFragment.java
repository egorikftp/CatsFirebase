package com.egoriku.catsrunning.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.egoriku.catsrunning.R;
import com.egoriku.catsrunning.activities.FitActivity;
import com.egoriku.catsrunning.adapters.FitnessDataHolder;
import com.egoriku.catsrunning.data.commons.TracksModel;
import com.egoriku.catsrunning.ui.activity.TrackMapActivity;
import com.egoriku.catsrunning.utils.FirebaseUtils;
import com.egoriku.catsrunning.utils.IntentBuilder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import timber.log.Timber;

import static com.egoriku.catsrunning.models.Constants.Extras.KEY_TYPE_FIT;
import static com.egoriku.catsrunning.models.Constants.FirebaseFields.TRACKS;
import static com.egoriku.catsrunning.models.Constants.FirebaseFields.TYPE_FIT;
import static com.egoriku.catsrunning.models.Constants.Tags.ARG_SECTION_NUMBER;

public class FitnessDataFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView textViewNoTracks;
    private ImageView imageViewNoTracks;
    private ProgressBar progressBar;

    private FirebaseRecyclerAdapter adapter;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseUtils firebaseUtils = FirebaseUtils.getInstance();

    public FitnessDataFragment() {
    }

    public static FitnessDataFragment newInstance(int sectionNumber) {
        FitnessDataFragment fragment = new FitnessDataFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fitness_data, container, false);

        int typeFit = getArguments().getInt(ARG_SECTION_NUMBER);

        progressBar = (ProgressBar) view.findViewById(R.id.fragment_fitness_data_progress_bar);
        recyclerView = (RecyclerView) view.findViewById(R.id.fitness_data_fragment_recycler_view);
        textViewNoTracks = (TextView) view.findViewById(R.id.fragment_fitness_data_text_no_tracks);
        imageViewNoTracks = (ImageView) view.findViewById(R.id.fragment_fitness_data_image_cats_no_track);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        hideNoTracks();
        showLoading(true);

        Query query = firebaseUtils.getFirebaseDatabase()
                .child(TRACKS)
                .child(user.getUid())
                .orderByChild(TYPE_FIT)
                .equalTo(typeFit);

        adapter = new FirebaseRecyclerAdapter<TracksModel, FitnessDataHolder>(TracksModel.class, R.layout.item_tracks_adapter, FitnessDataHolder.class, query) {
            @Override
            protected void populateViewHolder(final FitnessDataHolder viewHolder, TracksModel model, int position) {
                showLoading(false);
                viewHolder.setData(model, getContext());
            }

            @Override
            public FitnessDataHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                FitnessDataHolder holder = super.onCreateViewHolder(parent, viewType);

                holder.setOnClickListener(new FitnessDataHolder.ClickListener() {

                    @Override
                    public void onClickItem(int position) {
                        TracksModel tracksModel = (TracksModel) adapter.getItem(position);

                        if (tracksModel.getTime() == 0) {
                            startActivity(new IntentBuilder()
                                    .context(getActivity())
                                    .activity(FitActivity.class)
                                    .extra(KEY_TYPE_FIT, tracksModel.getTypeFit())
                                    .build());
                        } else {
                            TrackMapActivity.Companion.start(getActivity(), tracksModel);
                        }
                    }

                    @Override
                    public void onFavoriteClick(int position) {
                        TracksModel adapterItem = (TracksModel) adapter.getItem(position);
                        adapterItem.setFavorite(!adapterItem.isFavorite());
                        firebaseUtils.updateFavorite(adapterItem, getContext());
                    }

                    @Override
                    public void onLongClick(final int position) {
                        new AlertDialog.Builder(getActivity())
                                .setTitle(R.string.fitness_data_fragment_alert_title)
                                .setCancelable(true)
                                .setNegativeButton(R.string.fitness_data_fragment_alert_negative_btn, null)
                                .setPositiveButton(R.string.fitness_data_fragment_alert_positive_btn, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        firebaseUtils.removeTrack((TracksModel) adapter.getItem(position), getContext());

                                    }
                                })
                                .show();
                    }
                });
                return holder;
            }
        };

        firebaseUtils
                .getFirebaseDatabase()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (adapter.getItemCount() == 0) {
                            showNoTracks();
                            showLoading(false);
                        } else {
                            hideNoTracks();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Timber.d(databaseError.getMessage());
                    }
                });

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {

            public void onItemRangeRemoved(int positionStart, int itemCount) {
                if (adapter.getItemCount() == 0) {
                    showNoTracks();
                }
            }
        });

        recyclerView.setAdapter(adapter);
        return view;
    }

    private void showLoading(boolean isShowLoading) {
        progressBar.setVisibility(isShowLoading ? View.VISIBLE : View.GONE);
    }

    private void showNoTracks() {
        textViewNoTracks.setVisibility(View.VISIBLE);
        imageViewNoTracks.setVisibility(View.VISIBLE);
        textViewNoTracks.setText(R.string.no_tracks_text);
    }

    private void hideNoTracks() {
        textViewNoTracks.setVisibility(View.GONE);
        imageViewNoTracks.setVisibility(View.GONE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (adapter != null) {
            adapter.cleanup();
        }
    }
}
