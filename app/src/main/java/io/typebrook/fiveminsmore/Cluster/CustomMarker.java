/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.typebrook.fiveminsmore.Cluster;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;

public class CustomMarker implements ClusterItem {
    private final LatLng mPosition;
    private String mTitle;
    private String mSnippet;
    private DateTime mTime;
    private int mElevation;

    public CustomMarker(LatLng latLng) {
        mPosition = latLng;
        mTitle = "no title";
        mSnippet = "";
    }

    public CustomMarker(LatLng latLng, String title, String snippet) {
        mPosition = latLng;
        mTitle = title;
        mSnippet = snippet;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public String getSnippet() {
        return mSnippet;
    }

    public void setDateTime(DateTime time){
        mTime = time;
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MMMM-dd日 a HH:mm")
                .withZone(DateTimeZone.forOffsetHours(8));
        mSnippet = "時間: " + time.toString(fmt) + "\n" + mSnippet;
    }

    public void setElevation(int ele){
        mElevation = ele;
        mSnippet = "高度: " + ele + "m\n" + mSnippet;
    }

    public static List<LatLng> toLatLng(List<CustomMarker> markers) {
        if (markers.isEmpty()) {
            throw new EmptyStackException();
        }

        List<LatLng> latLngs = new ArrayList<>();

        for (CustomMarker marker : markers) {
            latLngs.add(marker.getPosition());
        }
        return latLngs;
    }
}
