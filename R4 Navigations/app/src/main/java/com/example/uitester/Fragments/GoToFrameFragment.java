package com.example.uitester.Fragments;

import android.graphics.PointF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aldebaran.qi.sdk.object.actuation.AttachedFrame;
import com.aldebaran.qi.sdk.object.actuation.Frame;
import com.aldebaran.qi.sdk.object.geometry.Transform;
import com.example.uitester.MainActivity;
import com.example.uitester.R;
import com.example.uitester.Utils.PointsOfInterestView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class GoToFrameFragment extends Fragment implements View.OnClickListener {

    private MainActivity ma;
    private Frame robotFrame;
    private Frame mapFrame;
    private List<PointF> poiPositions = null;

    /**
     * Inflates the layout associated with this fragment.
     * If an application theme is set, it will be applied to this fragment.
     */
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {

        int fragmentId = R.layout.fragment_mapview;
        this.ma = (MainActivity) getActivity();

        View view = inflater.inflate(fragmentId, container, false);
        // Retrieve the robot Frame
        robotFrame = (ma.qiContext.getActuationAsync()).getValue().async().robotFrame().getValue();

        // Retrieve the origin of the map Frame
        mapFrame = (ma.qiContext.getMappingAsync()).getValue().async().mapFrame().getValue();

        PointsOfInterestView explorationMapView = view.findViewById(R.id.explorationMapViewPopup);

        ma.robotHelper.localizeAndMapHelper.buildStreamableExplorationMap().andThenConsume(value -> {
            poiPositions = new ArrayList<>(ma.savedLocations.size() + 1);
            for (Map.Entry<String, AttachedFrame> stringAttachedFrameEntry : ma.savedLocations.entrySet()) {
                Transform transform = (((stringAttachedFrameEntry.getValue()).async().frame()).getValue().async().computeTransform(mapFrame)).getValue().getTransform();
                poiPositions.add(new PointF(((float) transform.getTranslation().getX()), (float) transform.getTranslation().getY()));
                //Log.d(TAG, "createGoToPopup: transform: "+(((stringAttachedFrameEntry.getValue()).async().frame()).getValue().async().computeTransform(mapFrame)).getValue().getTransform().getTranslation().toString());
            }
            explorationMapView.setExplorationMap(value.getTopGraphicalRepresentation());
            explorationMapView.setMapFramPosition();
            explorationMapView.setPoiPositions(poiPositions);
        }).andThenConsume(value -> {
            int delay = 0;
            int period = 500;  // repeat every sec.
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    // Compute the position of the robot relatively to the map Frame
                    Transform robotPos = robotFrame.computeTransform(mapFrame).getTransform();
                    // Set the position in the ExplorationMapView widget, it will be displayed as a red circle
                    explorationMapView.setRobotPosition(robotPos);
                }
            }, delay, period);
        });

        Button cancelButton = view.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.cancelButton) {
            v.setClickable(false);
            ma.cancelMoveToLocation();
            ma.currentChatBot.goToBookmarkSameTopic("cancelGoTo");
        }
    }
}
