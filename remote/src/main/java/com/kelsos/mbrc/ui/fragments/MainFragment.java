package com.kelsos.mbrc.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.*;
import android.view.animation.*;
import android.widget.*;
import com.google.inject.Singleton;
import com.kelsos.mbrc.BuildConfig;
import com.kelsos.mbrc.R;
import com.kelsos.mbrc.constants.UserInputEventType;
import com.kelsos.mbrc.enums.ConnectionStatus;
import com.kelsos.mbrc.events.Events;
import com.kelsos.mbrc.events.MessageEvent;
import com.kelsos.mbrc.events.ui.*;
import com.kelsos.mbrc.net.Notification;
import com.kelsos.mbrc.rest.RemoteApi;
import com.kelsos.mbrc.util.RemoteUtils;
import com.squareup.picasso.Picasso;
import roboguice.fragment.provided.RoboFragment;
import roboguice.inject.InjectView;
import roboguice.util.Ln;
import rx.android.observables.AndroidObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import javax.inject.Inject;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Singleton
public class MainFragment extends RoboFragment {
    /**
     * Total milliseconds in a second (1000)
     */
    public static final int MILLISECONDS = 1000;
    /**
     * Total seconds in a minute (60)
     */
    public static final int SECONDS = 60;
    public static final int TIME_PERIOD = 1;
    private final ScheduledExecutorService progressScheduler = Executors.newScheduledThreadPool(1);
    @InjectView(R.id.main_track_progress_current)
    private TextView trackProgressCurrent;
    @InjectView(R.id.main_track_duration_total)
    private TextView trackDuration;
    @InjectView(R.id.main_track_progress_seeker)
    private SeekBar trackProgressSlider;
    @InjectView(R.id.main_album_cover_image_view)
    private ImageView albumCover;
    @InjectView(R.id.ratingWrapper)
    private LinearLayout ratingWrapper;
    @InjectView(R.id.track_rating_bar)
    private RatingBar trackRating;
    @Inject
    private RemoteApi api;

    private int previousVol;
    private SeekBar.OnSeekBarChangeListener durationSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser && progress != previousVol) {
                previousVol = progress;
            }
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
        }

    };
    private ScheduledFuture mProgressUpdateHandler;

    private boolean isTablet;
    private RatingBar.OnRatingBarChangeListener ratingChangeListener = (ratingBar, v, b) -> {
        if (b) {

        }
    };
    private ImageView.OnClickListener coverOnClick = new ImageView.OnClickListener() {

        private boolean isActive = false;

        @Override
        public void onClick(View view) {

            if (!isActive) {
                animateRatingBar();
            }
        }

        private void animateRatingBar() {
            final int fadeInDuration = 300;
            final int timeBetween = 3000;
            final int fadeOutDuration = 800;

            Animation fadeIn = new AlphaAnimation(0, 1);
            fadeIn.setInterpolator(new DecelerateInterpolator());
            fadeIn.setDuration(fadeInDuration);

            Animation fadeOut = new AlphaAnimation(1, 0);
            fadeOut.setInterpolator(new AccelerateInterpolator());
            fadeOut.setStartOffset(fadeInDuration + timeBetween);
            fadeOut.setDuration(fadeOutDuration);

            AnimationSet animation = new AnimationSet(false);
            animation.addAnimation(fadeIn);
            animation.addAnimation(fadeOut);
            animation.setRepeatCount(1);

            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    isActive = true;
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    isActive = false;
                    ratingWrapper.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            ratingWrapper.setVisibility(View.VISIBLE);
            ratingWrapper.startAnimation(animation);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        isTablet = getResources().getBoolean(R.bool.isTablet);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.ui_fragment_main, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        setTextViewTypeface();
        registerListeners();

        Picasso.with(getActivity())
                .load(String.format("%s?t=%s", RemoteApi.COVER_URL, RemoteUtils.getTimeStamp()))
                .placeholder(R.drawable.ic_image_no_cover)
                .fit()
                .into(albumCover);

        AndroidObservable.bindFragment(this, Events.Messages)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(err -> Ln.d("Error %s", err.getMessage()))
                .filter(msg -> msg.getType().equals(Notification.PLAY_STATUS_CHANGED))
                .flatMap(resp -> api.getPlaystate())
                .subscribe(resp -> handlePlayStateChange(resp.getValue()));

        AndroidObservable.bindFragment(this, api.getCurrentPosition())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnError(err -> Ln.d("Error %s", err.getMessage()))
                .subscribe(update -> handlePositionUpdate(update.getPosition(), update.getDuration()));


    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * Registers the listeners for the interface elements used for interaction.
     */
    private void registerListeners() {
        try {
            ratingWrapper.setVisibility(View.INVISIBLE);
            trackRating.setOnRatingBarChangeListener(ratingChangeListener);
            trackProgressSlider.setOnSeekBarChangeListener(durationSeekBarChangeListener);
            albumCover.setOnClickListener(coverOnClick);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Ln.e(e, "listener registration");
            }
        }

    }

    /**
     * Sets the typeface of the text views in the main activity to roboto.
     */
    private void setTextViewTypeface() {
        try {
            Typeface robotoRegular = Typeface.createFromAsset(getActivity().getAssets(), "fonts/roboto_regular.ttf");
            trackProgressCurrent.setTypeface(robotoRegular);
            trackDuration.setTypeface(robotoRegular);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Ln.e(e, "setting typeface");
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.share, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionbar_share:
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                //shareIntent.putExtra(Intent.EXTRA_TEXT, "Now Playing: " + artistLabel.getText() + " - " + titleLabel.getText());
                setShareIntent(shareIntent);
                return true;
            default:
                return false;
        }
    }

    private void setShareIntent(Intent shareIntent) {

    }

    public void handleRatingChange(final RatingChanged event) {
        if (trackRating != null) {
            trackRating.setRating(event.getRating());
        }
    }

    public void handleCoverEvent(final CoverAvailable cevent) {
        if (albumCover == null) {
            return;
        }

        Picasso.with(getActivity().getBaseContext())
                .load(cevent.getCoverUrl())
                .placeholder(R.drawable.ic_image_no_cover)
                .fit()
                .into(albumCover);
        Ln.d("cover event received");
    }

    public void handlePlayStateChange(final String change) {
        switch (change.toUpperCase()) {
            case "PLAYING":
                trackProgressAnimation();
                break;
            case "PAUSED":
                stopTrackProgressAnimation();
                break;
            case "STOPPED":
                stopTrackProgressAnimation();
                activateStoppedState();
                break;
            default:
                stopTrackProgressAnimation();
        }
    }

    /**
     * If the track progress animation is running the the function stops it.
     */
    private void stopTrackProgressAnimation() {
        if (mProgressUpdateHandler != null) {
            mProgressUpdateHandler.cancel(true);
        }
    }

    /**
     * Starts the progress animation when called. If It was previously running then it restarts it.
     */
    private void trackProgressAnimation() {
        if (!isVisible()) {
            return;
        }
        /* If the scheduled tasks is not null then cancel it and clear it along with the timer to create them anew */
        stopTrackProgressAnimation();

        final Runnable updateProgress = () -> {

            int currentProgress = trackProgressSlider.getProgress() / MILLISECONDS;
            final int currentMinutes = currentProgress / SECONDS;
            final int currentSeconds = currentProgress % SECONDS;

            if (getActivity() == null) {
                return;
            }

            getActivity().runOnUiThread(() -> {
                try {
                    if (trackProgressSlider == null) {
                        return;
                    }
                    trackProgressSlider.setProgress(trackProgressSlider.getProgress() + MILLISECONDS);
                    trackProgressCurrent.setText(String.format("%02d:%02d", currentMinutes, currentSeconds));
                } catch (Exception ex) {

                    if (BuildConfig.DEBUG) {
                        Log.d("mbrc-log:", "animation timer", ex);
                    }
                }
            });
        };

        mProgressUpdateHandler = progressScheduler.scheduleAtFixedRate(updateProgress, 0,
                TIME_PERIOD, TimeUnit.SECONDS);

    }

    private void activateStoppedState() {
        if (trackProgressCurrent == null || trackProgressSlider == null) {
            return;
        }
        trackProgressSlider.setProgress(0);
        trackProgressCurrent.setText("00:00");
    }

    public void handleTrackInfoChange(final TrackInfoChange change) {
        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle(change.getTitle());
        actionBar.setSubtitle(change.getAlbum());
        actionBar.setDisplayShowTitleEnabled(true);
    }


    public void handleConnectionStatusChange(final ConnectionStatusChange change) {
        if (change.getStatus() == ConnectionStatus.CONNECTION_OFF) {
            stopTrackProgressAnimation();
            activateStoppedState();
        }
    }

    /**
     * Responsible for updating the displays and seekbar responsible for the display of the track duration and the
     * current progress of playback
     */

    public void handlePositionUpdate(int current, int total) {
        if (trackProgressCurrent == null || trackProgressSlider == null || trackDuration == null) {
            return;
        }
        if (total == 0) {
            new MessageEvent(UserInputEventType.REQUEST_POSITION);
            return;
        }
        int currentSeconds = current / MILLISECONDS;
        int totalSeconds = total / MILLISECONDS;

        final int currentMinutes = currentSeconds / SECONDS;
        final int totalMinutes = totalSeconds / SECONDS;

        currentSeconds %= SECONDS;
        totalSeconds %= SECONDS;
        final int finalTotalSeconds = totalSeconds;
        final int finalCurrentSeconds = currentSeconds;

        trackDuration.setText(String.format("%02d:%02d", totalMinutes, finalTotalSeconds));
        trackProgressCurrent.setText(String.format("%02d:%02d", currentMinutes, finalCurrentSeconds));

        trackProgressSlider.setMax(total);
        trackProgressSlider.setProgress(current);

        trackProgressAnimation();
    }

    /**
     * Returns true if the orientation is landscape and false if it is horizontal.
     *
     * @return true or false
     */
    public boolean isLandscape() {
        boolean result;
        final Display display = ((WindowManager) getActivity()
                .getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay();

        int orientation = display.getRotation();
        result = orientation == Surface.ROTATION_90 || orientation == Surface.ROTATION_270;

        return result;
    }
}
