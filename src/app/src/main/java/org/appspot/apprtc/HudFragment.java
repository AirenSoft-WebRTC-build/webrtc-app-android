/*
 *  Copyright 2015 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package org.appspot.apprtc;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import org.webrtc.RTCStats;
import org.webrtc.RTCStatsReport;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Fragment for HUD statistics display.
 */
public class HudFragment extends Fragment {
  private TextView statView;
  private ImageButton toggleDebugButton;
  private boolean displayHud;
  private volatile boolean isRunning;
  private CpuMonitor cpuMonitor;

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View controlView = inflater.inflate(R.layout.fragment_hud, container, false);

    // Create UI controls.
    statView = controlView.findViewById(R.id.hud_stat_call);
      statView.setVisibility(View.VISIBLE);
    toggleDebugButton = controlView.findViewById(R.id.button_toggle_debug);

    toggleDebugButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (displayHud) {
          statView.setVisibility(
              statView.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
        }
      }
    });

    return controlView;
  }

  @Override
  public void onStart() {
    super.onStart();

    Bundle args = getArguments();
    if (args != null) {
      displayHud = args.getBoolean(CallActivity.EXTRA_DISPLAY_HUD, false);
    }
    int visibility = displayHud ? View.VISIBLE : View.INVISIBLE;
    statView.setVisibility(View.INVISIBLE);
    toggleDebugButton.setVisibility(visibility);
    isRunning = true;
  }

  @Override
  public void onStop() {
    isRunning = false;
    super.onStop();
  }

  public void setCpuMonitor(CpuMonitor cpuMonitor) {
    this.cpuMonitor = cpuMonitor;
  }

    public void updateEncoderStatistics(final RTCStatsReport report) {
        if (!isRunning || !displayHud) {
            return;
        }

        StringBuilder sb = new StringBuilder();

        if (cpuMonitor != null) {
            sb.append("CPU%: ")
                    .append(cpuMonitor.getCpuUsageCurrent())
                    .append("/")
                    .append(cpuMonitor.getCpuUsageAverage())
                    .append(". Freq: ")
                    .append(cpuMonitor.getFrequencyScaleAverage())
                    .append("\n");
        }

        sb.append("[INBOUND-RTP]").append("\n");
        for (RTCStats stat : report.getStatsMap().values()) {
            if (stat.getType().equalsIgnoreCase("inbound-rtp") && stat.getMembers().get("kind").toString().equalsIgnoreCase("video")) {
                sb.append("Size: ").append(stat.getMembers().get("frameWidth")).append("x").append(stat.getMembers().get("frameHeight")).append("\n");
                sb.append("FrameDecoded:").append(stat.getMembers().get("framesDecoded")).append("(").append(stat.getMembers().get("framesPerSecond")).append(")").append("\n");
                sb.append("keyFramesDecoded:").append(stat.getMembers().get("keyFramesDecoded")).append("\n");
                sb.append("Bitrate:").append( String.format("%.0f", ((Number)stat.getMembers().get("bytesReceived")).doubleValue() / ((Number)stat.getMembers().get("totalDecodeTime")).doubleValue() ) ).append("\n");
                sb.append("qpSum:").append( stat.getMembers().get("qpSum")).append("\n");
            }
        }
        sb.append("\n");

        sb.append("[OUTBOUND-RTP]").append("\n");
        for (RTCStats stat : report.getStatsMap().values()) {
            if (stat.getType().equalsIgnoreCase("outbound-rtp") && stat.getMembers().get("kind").toString().equalsIgnoreCase("video")) {
                sb.append("Size: ").append(stat.getMembers().get("frameWidth")).append("x").append(stat.getMembers().get("frameHeight")).append("\n");
                sb.append("FrameDecoded:").append(stat.getMembers().get("framesEncoded")).append("(").append(stat.getMembers().get("framesPerSecond")).append(")").append("\n");
                sb.append("keyFramesEncoded:").append(stat.getMembers().get("keyFramesEncoded")).append("\n");
                sb.append("Bitrate:").append( String.format("%.0f", ((Number)stat.getMembers().get("bytesSent")).doubleValue() / ((Number)stat.getMembers().get("totalEncodeTime")).doubleValue() ) ).append("\n");
                sb.append("qpSum:").append( stat.getMembers().get("qpSum")).append("\n");

            }
        }
        Log.d(getClass().getName(), sb.toString());

        statView.setText(sb.toString());
    }

    double objectToDouble(Object obj) {
      if( obj instanceof Number ) {
          Number number = (Number) obj;
          if (number instanceof Integer) {
              return (double) number.intValue();
          } else if (number instanceof Long) {
              return  (double) number.longValue();
          } else if (number instanceof Float) {
              return  (double)  number.floatValue();
          } else if (number instanceof Double) {
              return  (double) number.doubleValue();
          } else if (number instanceof Byte) {
              return  (double) number.byteValue();
          } else if (number instanceof Short) {
              return  (double) number.shortValue();
          }
      }
      else if(obj instanceof String) {
          return Double.parseDouble( (String)obj);
      }

      return 10000000.0f;
    }

}
