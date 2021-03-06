package com.kelsos.mbrc.ui.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.inject.Inject;
import com.kelsos.mbrc.R;
import com.kelsos.mbrc.adapters.LyricsAdapter;
import com.kelsos.mbrc.data.model.TrackState;
import com.kelsos.mbrc.util.Logger;
import java.util.ArrayList;
import java.util.Arrays;
import roboguice.fragment.provided.RoboListFragment;
import rx.android.app.AppObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class LyricsFragment extends RoboListFragment {
  @Inject private TrackState model;

  @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.ui_fragment_lyrics, container, false);
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    AppObservable.bindFragment(this, model.getLyricsObservable())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::updateLyricsData, Logger::logThrowable);
  }

  @Override public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    getFragmentManager().beginTransaction()
        .replace(R.id.lyrics_mini_control, MiniControlFragment.newInstance())
        .commit();
  }

  public void updateLyricsData(String lyrics) {
    final ArrayList<String> lyricsList = new ArrayList<>(Arrays.asList(lyrics.split("\r\n")));
    final LyricsAdapter lyricsAdapter =
        new LyricsAdapter(getActivity(), R.layout.ui_list_lyrics_item, lyricsList);
    setListAdapter(lyricsAdapter);
  }
}
