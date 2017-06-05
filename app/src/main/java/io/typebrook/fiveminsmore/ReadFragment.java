package io.typebrook.fiveminsmore;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.vipulasri.timelineview.TimelineView;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

import io.ticofab.androidgpxparser.parser.domain.WayPoint;
import io.typebrook.fiveminsmore.model.ClockDrawable;
import io.typebrook.fiveminsmore.model.TimeLineModel;

import static android.text.format.DateUtils.FORMAT_SHOW_TIME;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link ReadFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReadFragment extends Fragment {
    private static final String TAG = "ReadFragment";

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_GPXLIST = "mGpxFileList";

    private static List<WayPoint> wptList = new ArrayList<>();
    private List<TimeLineModel> mDataList = new ArrayList<>();

    private RecyclerView mRecyclerView;
    private TimeLineAdapter mTimeLineAdapter;

    private RelativeLayout superContainer;
    ImageView clockView;

    public ReadFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ReadFragment.
     */
    public static ReadFragment newInstance(List<WayPoint> wpts) {
        ReadFragment fragment = new ReadFragment();
        wptList = wpts;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        for (WayPoint wpt : wptList) {
            mDataList.add(new TimeLineModel(wpt.getName(), wpt.getTime().getMillis()));
        }

        superContainer = (RelativeLayout) getActivity().findViewById(R.id.layout_container);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_read, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity(), LinearLayoutManager.HORIZONTAL, false));
        mRecyclerView.setHasFixedSize(true);

        mTimeLineAdapter = new TimeLineAdapter(mDataList);
        mRecyclerView.setAdapter(mTimeLineAdapter);

        // 設置click listener
        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(),
                mRecyclerView, new ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                if (clockView == null)
                    createClock();

                LocalDateTime current = new LocalDateTime(
                        mDataList.get(position).getTime(), DateTimeZone.forOffsetHours(8));

                ((ClockDrawable) clockView.getDrawable()).start(current);
                fadeOutAndHideImage(clockView, false);
            }
        }));

        return rootView;
    }

    public interface ClickListener {
        void onClick(View view, int position);
    }

    public class TimeLineAdapter extends RecyclerView.Adapter<TimeLineViewHolder> {

        private List<TimeLineModel> mFeedList;
        private Context mContext;
        private LayoutInflater mLayoutInflater;

        public TimeLineAdapter(List<TimeLineModel> feedList) {
            mFeedList = feedList;
        }

        @Override
        public int getItemViewType(int position) {
            return TimelineView.getTimeLineViewType(position, getItemCount());
        }

        @Override
        public TimeLineViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            mContext = parent.getContext();
            mLayoutInflater = LayoutInflater.from(mContext);
            View view;

            view = mLayoutInflater.inflate(R.layout.item_timeline_horizontal_line_padding, parent, false);

            return new TimeLineViewHolder(view, viewType);
        }

        @Override
        public void onBindViewHolder(TimeLineViewHolder holder, int position) {

            TimeLineModel timeLineModel = mFeedList.get(position);

            holder.mTimelineView.setMarker(ContextCompat.getDrawable(mContext, R.drawable.ic_marker), ContextCompat.getColor(mContext, R.color.colorPrimary));

            holder.mDate.setVisibility(View.VISIBLE);
            holder.mDate.setText(DateUtils.formatDateTime(getActivity(), timeLineModel.getTime(), FORMAT_SHOW_TIME));

            holder.mMessage.setText(timeLineModel.getMessage());
        }

        @Override
        public int getItemCount() {
            return (mFeedList != null ? mFeedList.size() : 0);
        }

    }

    public class TimeLineViewHolder extends RecyclerView.ViewHolder {

        TextView mDate;
        TextView mMessage;
        TimelineView mTimelineView;

        public TimeLineViewHolder(View itemView, int viewType) {
            super(itemView);

            mDate = (TextView) itemView.findViewById(R.id.timeline_item_date);
            mMessage = (TextView) itemView.findViewById(R.id.timeline_item_text);
            mTimelineView = (TimelineView) itemView.findViewById(R.id.time_marker);

            mTimelineView.initLine(viewType);
        }
    }

    class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private ClickListener clicklistener;
        private GestureDetector gestureDetector;

        public RecyclerTouchListener(Context context, final RecyclerView recycleView, final ClickListener clicklistener) {

            this.clicklistener = clicklistener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clicklistener != null && gestureDetector.onTouchEvent(e)) {
                clicklistener.onClick(child, rv.getChildAdapterPosition(child));
            }

            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

    private void createClock() {
        // get the ClockImageView
        clockView = (ImageView) getActivity().getLayoutInflater().inflate(R.layout.view_clock, null);
        clockView.setLayoutParams(new ViewGroup.LayoutParams(400, 400));

        // Add into Screen
        this.superContainer.addView(clockView);

        // Set the position
        RelativeLayout.LayoutParams layoutParams =
                (RelativeLayout.LayoutParams) clockView.getLayoutParams();
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        layoutParams.setMargins(0, 0, 0, 100);
        clockView.setLayoutParams(layoutParams);

        ClockDrawable clockDrawable = new ClockDrawable(getResources());
        clockDrawable.setAnimateDays(false);
        clockView.setImageDrawable(clockDrawable);

        clockView.setVisibility(View.VISIBLE);
    }

    private void fadeOutAndHideImage(final ImageView img, final boolean really) {
        Animation fadeOut;
        if (really)
            fadeOut = new AlphaAnimation(1, 0);
        else
            fadeOut = new AlphaAnimation(1, 1);
        fadeOut.setInterpolator(new AccelerateInterpolator(4));
        fadeOut.setDuration(1200);

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation animation) {
                if (really)
                    img.setVisibility(View.GONE);
                else
                    fadeOutAndHideImage(img, true);
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationStart(Animation animation) {
            }
        });

        img.startAnimation(fadeOut);
    }
}
