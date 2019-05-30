/*
 * Copyright (C) 2019 The Android Open Source Project
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
package com.google.android.exoplayer2.analytics;

import android.os.SystemClock;
import androidx.annotation.IntDef;
import android.util.Pair;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.analytics.AnalyticsListener.EventTime;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.List;

/** Statistics about playbacks. */
public final class PlaybackStats {

  /**
   * State of a playback. One of {@link #PLAYBACK_STATE_NOT_STARTED}, {@link
   * #PLAYBACK_STATE_JOINING_FOREGROUND}, {@link #PLAYBACK_STATE_JOINING_BACKGROUND}, {@link
   * #PLAYBACK_STATE_PLAYING}, {@link #PLAYBACK_STATE_PAUSED}, {@link #PLAYBACK_STATE_SEEKING},
   * {@link #PLAYBACK_STATE_BUFFERING}, {@link #PLAYBACK_STATE_PAUSED_BUFFERING}, {@link
   * #PLAYBACK_STATE_SEEK_BUFFERING}, {@link #PLAYBACK_STATE_ENDED}, {@link
   * #PLAYBACK_STATE_STOPPED}, {@link #PLAYBACK_STATE_FAILED} or {@link #PLAYBACK_STATE_SUSPENDED}.
   */
  @Documented
  @Retention(RetentionPolicy.SOURCE)
  @Target({ElementType.TYPE_PARAMETER, ElementType.TYPE_USE})
  @IntDef({
    PLAYBACK_STATE_NOT_STARTED,
    PLAYBACK_STATE_JOINING_BACKGROUND,
    PLAYBACK_STATE_JOINING_FOREGROUND,
    PLAYBACK_STATE_PLAYING,
    PLAYBACK_STATE_PAUSED,
    PLAYBACK_STATE_SEEKING,
    PLAYBACK_STATE_BUFFERING,
    PLAYBACK_STATE_PAUSED_BUFFERING,
    PLAYBACK_STATE_SEEK_BUFFERING,
    PLAYBACK_STATE_ENDED,
    PLAYBACK_STATE_STOPPED,
    PLAYBACK_STATE_FAILED,
    PLAYBACK_STATE_SUSPENDED
  })
  @interface PlaybackState {}
  /** Playback has not started (initial state). */
  public static final int PLAYBACK_STATE_NOT_STARTED = 0;
  /** Playback is buffering in the background for initial playback start. */
  public static final int PLAYBACK_STATE_JOINING_BACKGROUND = 1;
  /** Playback is buffering in the foreground for initial playback start. */
  public static final int PLAYBACK_STATE_JOINING_FOREGROUND = 2;
  /** Playback is actively playing. */
  public static final int PLAYBACK_STATE_PLAYING = 3;
  /** Playback is paused but ready to play. */
  public static final int PLAYBACK_STATE_PAUSED = 4;
  /** Playback is handling a seek. */
  public static final int PLAYBACK_STATE_SEEKING = 5;
  /** Playback is buffering to restart playback. */
  public static final int PLAYBACK_STATE_BUFFERING = 6;
  /** Playback is buffering while paused. */
  public static final int PLAYBACK_STATE_PAUSED_BUFFERING = 7;
  /** Playback is buffering after a seek. */
  public static final int PLAYBACK_STATE_SEEK_BUFFERING = 8;
  /** Playback has reached the end of the media. */
  public static final int PLAYBACK_STATE_ENDED = 9;
  /** Playback is stopped and can be resumed. */
  public static final int PLAYBACK_STATE_STOPPED = 10;
  /** Playback is stopped due a fatal error and can be retried. */
  public static final int PLAYBACK_STATE_FAILED = 11;
  /** Playback is suspended, e.g. because the user left or it is interrupted by another playback. */
  public static final int PLAYBACK_STATE_SUSPENDED = 12;
  /** Total number of playback states. */
  /* package */ static final int PLAYBACK_STATE_COUNT = 13;

  /** Empty playback stats. */
  public static final PlaybackStats EMPTY = merge(/* nothing */ );

  /**
   * Returns the combined {@link PlaybackStats} for all input {@link PlaybackStats}.
   *
   * <p>Note that the full history of events is not kept as the history only makes sense in the
   * context of a single playback.
   *
   * @param playbackStats Array of {@link PlaybackStats} to combine.
   * @return The combined {@link PlaybackStats}.
   */
  public static PlaybackStats merge(PlaybackStats... playbackStats) {
    int playbackCount = 0;
    long[] playbackStateDurationsMs = new long[PLAYBACK_STATE_COUNT];
    long firstReportedTimeMs = C.TIME_UNSET;
    int foregroundPlaybackCount = 0;
    int abandonedBeforeReadyCount = 0;
    int endedCount = 0;
    int backgroundJoiningCount = 0;
    long totalValidJoinTimeMs = C.TIME_UNSET;
    int validJoinTimeCount = 0;
    int pauseCount = 0;
    int pauseBufferCount = 0;
    int seekCount = 0;
    int rebufferCount = 0;
    long maxRebufferTimeMs = C.TIME_UNSET;
    int adCount = 0;
    for (PlaybackStats stats : playbackStats) {
      playbackCount += stats.playbackCount;
      for (int i = 0; i < PLAYBACK_STATE_COUNT; i++) {
        playbackStateDurationsMs[i] += stats.playbackStateDurationsMs[i];
      }
      if (firstReportedTimeMs == C.TIME_UNSET) {
        firstReportedTimeMs = stats.firstReportedTimeMs;
      } else if (stats.firstReportedTimeMs != C.TIME_UNSET) {
        firstReportedTimeMs = Math.min(firstReportedTimeMs, stats.firstReportedTimeMs);
      }
      foregroundPlaybackCount += stats.foregroundPlaybackCount;
      abandonedBeforeReadyCount += stats.abandonedBeforeReadyCount;
      endedCount += stats.endedCount;
      backgroundJoiningCount += stats.backgroundJoiningCount;
      if (totalValidJoinTimeMs == C.TIME_UNSET) {
        totalValidJoinTimeMs = stats.totalValidJoinTimeMs;
      } else if (stats.totalValidJoinTimeMs != C.TIME_UNSET) {
        totalValidJoinTimeMs += stats.totalValidJoinTimeMs;
      }
      validJoinTimeCount += stats.validJoinTimeCount;
      pauseCount += stats.totalPauseCount;
      pauseBufferCount += stats.totalPauseBufferCount;
      seekCount += stats.totalSeekCount;
      rebufferCount += stats.totalRebufferCount;
      if (maxRebufferTimeMs == C.TIME_UNSET) {
        maxRebufferTimeMs = stats.maxRebufferTimeMs;
      } else if (stats.maxRebufferTimeMs != C.TIME_UNSET) {
        maxRebufferTimeMs = Math.max(maxRebufferTimeMs, stats.maxRebufferTimeMs);
      }
      adCount += stats.adPlaybackCount;
    }
    return new PlaybackStats(
        playbackCount,
        playbackStateDurationsMs,
        /* playbackStateHistory */ Collections.emptyList(),
        firstReportedTimeMs,
        foregroundPlaybackCount,
        abandonedBeforeReadyCount,
        endedCount,
        backgroundJoiningCount,
        totalValidJoinTimeMs,
        validJoinTimeCount,
        pauseCount,
        pauseBufferCount,
        seekCount,
        rebufferCount,
        maxRebufferTimeMs,
        adCount);
  }

  /** The number of individual playbacks for which these stats were collected. */
  public final int playbackCount;

  // Playback state stats.

  /**
   * The playback state history as ordered pairs of the {@link EventTime} at which a state became
   * active and the {@link PlaybackState}.
   */
  public final List<Pair<EventTime, @PlaybackState Integer>> playbackStateHistory;
  /**
   * The elapsed real-time as returned by {@code SystemClock.elapsedRealtime()} of the first
   * reported playback event, or {@link C#TIME_UNSET} if no event has been reported.
   */
  public final long firstReportedTimeMs;
  /** The number of playbacks which were the active foreground playback at some point. */
  public final int foregroundPlaybackCount;
  /** The number of playbacks which were abandoned before they were ready to play. */
  public final int abandonedBeforeReadyCount;
  /** The number of playbacks which reached the ended state at least once. */
  public final int endedCount;
  /** The number of playbacks which were pre-buffered in the background. */
  public final int backgroundJoiningCount;
  /**
   * The total time spent joining the playback, in milliseconds, or {@link C#TIME_UNSET} if no valid
   * join time could be determined.
   *
   * <p>Note that this does not include background joining time. A join time may be invalid if the
   * playback never reached {@link #PLAYBACK_STATE_PLAYING} or {@link #PLAYBACK_STATE_PAUSED}, or
   * joining was interrupted by a seek, stop, or error state.
   */
  public final long totalValidJoinTimeMs;
  /**
   * The number of playbacks with a valid join time as documented in {@link #totalValidJoinTimeMs}.
   */
  public final int validJoinTimeCount;
  /** The total number of times a playback has been paused. */
  public final int totalPauseCount;
  /** The total number of times a playback has been paused while rebuffering. */
  public final int totalPauseBufferCount;
  /**
   * The total number of times a seek occurred. This includes seeks happening before playback
   * resumed after another seek.
   */
  public final int totalSeekCount;
  /**
   * The total number of times a rebuffer occurred. This excludes initial joining and buffering
   * after seek.
   */
  public final int totalRebufferCount;
  /**
   * The maximum time spent during a single rebuffer, in milliseconds, or {@link C#TIME_UNSET} if no
   * rebuffer occurred.
   */
  public final long maxRebufferTimeMs;
  /** The number of ad playbacks. */
  public final int adPlaybackCount;

  private final long[] playbackStateDurationsMs;

  /* package */ PlaybackStats(
      int playbackCount,
      long[] playbackStateDurationsMs,
      List<Pair<EventTime, @PlaybackState Integer>> playbackStateHistory,
      long firstReportedTimeMs,
      int foregroundPlaybackCount,
      int abandonedBeforeReadyCount,
      int endedCount,
      int backgroundJoiningCount,
      long totalValidJoinTimeMs,
      int validJoinTimeCount,
      int totalPauseCount,
      int totalPauseBufferCount,
      int totalSeekCount,
      int totalRebufferCount,
      long maxRebufferTimeMs,
      int adPlaybackCount) {
    this.playbackCount = playbackCount;
    this.playbackStateDurationsMs = playbackStateDurationsMs;
    this.playbackStateHistory = Collections.unmodifiableList(playbackStateHistory);
    this.firstReportedTimeMs = firstReportedTimeMs;
    this.foregroundPlaybackCount = foregroundPlaybackCount;
    this.abandonedBeforeReadyCount = abandonedBeforeReadyCount;
    this.endedCount = endedCount;
    this.backgroundJoiningCount = backgroundJoiningCount;
    this.totalValidJoinTimeMs = totalValidJoinTimeMs;
    this.validJoinTimeCount = validJoinTimeCount;
    this.totalPauseCount = totalPauseCount;
    this.totalPauseBufferCount = totalPauseBufferCount;
    this.totalSeekCount = totalSeekCount;
    this.totalRebufferCount = totalRebufferCount;
    this.maxRebufferTimeMs = maxRebufferTimeMs;
    this.adPlaybackCount = adPlaybackCount;
  }

  /**
   * Returns the total time spent in a given {@link PlaybackState}, in milliseconds.
   *
   * @param playbackState A {@link PlaybackState}.
   * @return Total spent in the given playback state, in milliseconds
   */
  public long getPlaybackStateDurationMs(@PlaybackState int playbackState) {
    return playbackStateDurationsMs[playbackState];
  }

  /**
   * Returns the {@link PlaybackState} at the given time.
   *
   * @param realtimeMs The time as returned by {@link SystemClock#elapsedRealtime()}.
   * @return The {@link PlaybackState} at that time, or {@link #PLAYBACK_STATE_NOT_STARTED} if the
   *     given time is before the first known playback state in the history.
   */
  @PlaybackState
  public int getPlaybackStateAtTime(long realtimeMs) {
    @PlaybackState int state = PLAYBACK_STATE_NOT_STARTED;
    for (Pair<EventTime, @PlaybackState Integer> timeAndState : playbackStateHistory) {
      if (timeAndState.first.realtimeMs > realtimeMs) {
        break;
      }
      state = timeAndState.second;
    }
    return state;
  }

  /**
   * Returns the mean time spent joining the playback, in milliseconds, or {@link C#TIME_UNSET} if
   * no valid join time is available. Only includes playbacks with valid join times as documented in
   * {@link #totalValidJoinTimeMs}.
   */
  public long getMeanJoinTimeMs() {
    return validJoinTimeCount == 0 ? C.TIME_UNSET : totalValidJoinTimeMs / validJoinTimeCount;
  }

  /**
   * Returns the total time spent joining the playback in foreground, in milliseconds. This does
   * include invalid join times where the playback never reached {@link #PLAYBACK_STATE_PLAYING} or
   * {@link #PLAYBACK_STATE_PAUSED}, or joining was interrupted by a seek, stop, or error state.
   */
  public long getTotalJoinTimeMs() {
    return getPlaybackStateDurationMs(PLAYBACK_STATE_JOINING_FOREGROUND);
  }

  /** Returns the total time spent actively playing, in milliseconds. */
  public long getTotalPlayTimeMs() {
    return getPlaybackStateDurationMs(PLAYBACK_STATE_PLAYING);
  }

  /**
   * Returns the mean time spent actively playing per foreground playback, in milliseconds, or
   * {@link C#TIME_UNSET} if no playback has been in foreground.
   */
  public long getMeanPlayTimeMs() {
    return foregroundPlaybackCount == 0
        ? C.TIME_UNSET
        : getTotalPlayTimeMs() / foregroundPlaybackCount;
  }

  /** Returns the total time spent in a paused state, in milliseconds. */
  public long getTotalPausedTimeMs() {
    return getPlaybackStateDurationMs(PLAYBACK_STATE_PAUSED)
        + getPlaybackStateDurationMs(PLAYBACK_STATE_PAUSED_BUFFERING);
  }

  /**
   * Returns the mean time spent in a paused state per foreground playback, in milliseconds, or
   * {@link C#TIME_UNSET} if no playback has been in foreground.
   */
  public long getMeanPausedTimeMs() {
    return foregroundPlaybackCount == 0
        ? C.TIME_UNSET
        : getTotalPausedTimeMs() / foregroundPlaybackCount;
  }

  /**
   * Returns the total time spent rebuffering, in milliseconds. This excludes initial join times,
   * buffer times after a seek and buffering while paused.
   */
  public long getTotalRebufferTimeMs() {
    return getPlaybackStateDurationMs(PLAYBACK_STATE_BUFFERING);
  }

  /**
   * Returns the mean time spent rebuffering per foreground playback, in milliseconds, or {@link
   * C#TIME_UNSET} if no playback has been in foreground. This excludes initial join times, buffer
   * times after a seek and buffering while paused.
   */
  public long getMeanRebufferTimeMs() {
    return foregroundPlaybackCount == 0
        ? C.TIME_UNSET
        : getTotalRebufferTimeMs() / foregroundPlaybackCount;
  }

  /**
   * Returns the mean time spent during a single rebuffer, in milliseconds, or {@link C#TIME_UNSET}
   * if no rebuffer was recorded. This excludes initial join times and buffer times after a seek.
   */
  public long getMeanSingleRebufferTimeMs() {
    return totalRebufferCount == 0
        ? C.TIME_UNSET
        : (getPlaybackStateDurationMs(PLAYBACK_STATE_BUFFERING)
                + getPlaybackStateDurationMs(PLAYBACK_STATE_PAUSED_BUFFERING))
            / totalRebufferCount;
  }

  /**
   * Returns the total time spent from the start of a seek until playback is ready again, in
   * milliseconds.
   */
  public long getTotalSeekTimeMs() {
    return getPlaybackStateDurationMs(PLAYBACK_STATE_SEEKING)
        + getPlaybackStateDurationMs(PLAYBACK_STATE_SEEK_BUFFERING);
  }

  /**
   * Returns the mean time spent per foreground playback from the start of a seek until playback is
   * ready again, in milliseconds, or {@link C#TIME_UNSET} if no playback has been in foreground.
   */
  public long getMeanSeekTimeMs() {
    return foregroundPlaybackCount == 0
        ? C.TIME_UNSET
        : getTotalSeekTimeMs() / foregroundPlaybackCount;
  }

  /**
   * Returns the mean time spent from the start of a single seek until playback is ready again, in
   * milliseconds, or {@link C#TIME_UNSET} if no seek occurred.
   */
  public long getMeanSingleSeekTimeMs() {
    return totalSeekCount == 0 ? C.TIME_UNSET : getTotalSeekTimeMs() / totalSeekCount;
  }

  /**
   * Returns the total time spent actively waiting for playback, in milliseconds. This includes all
   * join times, rebuffer times and seek times, but excludes times without user intention to play,
   * e.g. all paused states.
   */
  public long getTotalWaitTimeMs() {
    return getPlaybackStateDurationMs(PLAYBACK_STATE_JOINING_FOREGROUND)
        + getPlaybackStateDurationMs(PLAYBACK_STATE_BUFFERING)
        + getPlaybackStateDurationMs(PLAYBACK_STATE_SEEKING)
        + getPlaybackStateDurationMs(PLAYBACK_STATE_SEEK_BUFFERING);
  }

  /**
   * Returns the mean time spent actively waiting for playback per foreground playback, in
   * milliseconds, or {@link C#TIME_UNSET} if no playback has been in foreground. This includes all
   * join times, rebuffer times and seek times, but excludes times without user intention to play,
   * e.g. all paused states.
   */
  public long getMeanWaitTimeMs() {
    return foregroundPlaybackCount == 0
        ? C.TIME_UNSET
        : getTotalWaitTimeMs() / foregroundPlaybackCount;
  }

  /** Returns the total time spent playing or actively waiting for playback, in milliseconds. */
  public long getTotalPlayAndWaitTimeMs() {
    return getTotalPlayTimeMs() + getTotalWaitTimeMs();
  }

  /**
   * Returns the mean time spent playing or actively waiting for playback per foreground playback,
   * in milliseconds, or {@link C#TIME_UNSET} if no playback has been in foreground.
   */
  public long getMeanPlayAndWaitTimeMs() {
    return foregroundPlaybackCount == 0
        ? C.TIME_UNSET
        : getTotalPlayAndWaitTimeMs() / foregroundPlaybackCount;
  }

  /** Returns the total time covered by any playback state, in milliseconds. */
  public long getTotalElapsedTimeMs() {
    long totalTimeMs = 0;
    for (int i = 0; i < PLAYBACK_STATE_COUNT; i++) {
      totalTimeMs += playbackStateDurationsMs[i];
    }
    return totalTimeMs;
  }

  /**
   * Returns the mean time covered by any playback state per playback, in milliseconds, or {@link
   * C#TIME_UNSET} if no playback was recorded.
   */
  public long getMeanElapsedTimeMs() {
    return playbackCount == 0 ? C.TIME_UNSET : getTotalElapsedTimeMs() / playbackCount;
  }

  /**
   * Returns the ratio of foreground playbacks which were abandoned before they were ready to play,
   * or {@code 0.0} if no playback has been in foreground.
   */
  public float getAbandonedBeforeReadyRatio() {
    int foregroundAbandonedBeforeReady =
        abandonedBeforeReadyCount - (playbackCount - foregroundPlaybackCount);
    return foregroundPlaybackCount == 0
        ? 0f
        : (float) foregroundAbandonedBeforeReady / foregroundPlaybackCount;
  }

  /**
   * Returns the ratio of foreground playbacks which reached the ended state at least once, or
   * {@code 0.0} if no playback has been in foreground.
   */
  public float getEndedRatio() {
    return foregroundPlaybackCount == 0 ? 0f : (float) endedCount / foregroundPlaybackCount;
  }

  /**
   * Returns the mean number of times a playback has been paused per foreground playback, or {@code
   * 0.0} if no playback has been in foreground.
   */
  public float getMeanPauseCount() {
    return foregroundPlaybackCount == 0 ? 0f : (float) totalPauseCount / foregroundPlaybackCount;
  }

  /**
   * Returns the mean number of times a playback has been paused while rebuffering per foreground
   * playback, or {@code 0.0} if no playback has been in foreground.
   */
  public float getMeanPauseBufferCount() {
    return foregroundPlaybackCount == 0
        ? 0f
        : (float) totalPauseBufferCount / foregroundPlaybackCount;
  }

  /**
   * Returns the mean number of times a seek occurred per foreground playback, or {@code 0.0} if no
   * playback has been in foreground. This includes seeks happening before playback resumed after
   * another seek.
   */
  public float getMeanSeekCount() {
    return foregroundPlaybackCount == 0 ? 0f : (float) totalSeekCount / foregroundPlaybackCount;
  }

  /**
   * Returns the mean number of times a rebuffer occurred per foreground playback, or {@code 0.0} if
   * no playback has been in foreground. This excludes initial joining and buffering after seek.
   */
  public float getMeanRebufferCount() {
    return foregroundPlaybackCount == 0 ? 0f : (float) totalRebufferCount / foregroundPlaybackCount;
  }

  /**
   * Returns the ratio of wait times to the total time spent playing and waiting, or {@code 0.0} if
   * no time was spend playing or waiting. This is equivalent to {@link #getTotalWaitTimeMs()} /
   * {@link #getTotalPlayAndWaitTimeMs()} and also to {@link #getJoinTimeRatio()} + {@link
   * #getRebufferTimeRatio()} + {@link #getSeekTimeRatio()}.
   */
  public float getWaitTimeRatio() {
    long playAndWaitTimeMs = getTotalPlayAndWaitTimeMs();
    return playAndWaitTimeMs == 0 ? 0f : (float) getTotalWaitTimeMs() / playAndWaitTimeMs;
  }

  /**
   * Returns the ratio of foreground join time to the total time spent playing and waiting, or
   * {@code 0.0} if no time was spend playing or waiting. This is equivalent to {@link
   * #getTotalJoinTimeMs()} / {@link #getTotalPlayAndWaitTimeMs()}.
   */
  public float getJoinTimeRatio() {
    long playAndWaitTimeMs = getTotalPlayAndWaitTimeMs();
    return playAndWaitTimeMs == 0 ? 0f : (float) getTotalJoinTimeMs() / playAndWaitTimeMs;
  }

  /**
   * Returns the ratio of rebuffer time to the total time spent playing and waiting, or {@code 0.0}
   * if no time was spend playing or waiting. This is equivalent to {@link
   * #getTotalRebufferTimeMs()} / {@link #getTotalPlayAndWaitTimeMs()}.
   */
  public float getRebufferTimeRatio() {
    long playAndWaitTimeMs = getTotalPlayAndWaitTimeMs();
    return playAndWaitTimeMs == 0 ? 0f : (float) getTotalRebufferTimeMs() / playAndWaitTimeMs;
  }

  /**
   * Returns the ratio of seek time to the total time spent playing and waiting, or {@code 0.0} if
   * no time was spend playing or waiting. This is equivalent to {@link #getTotalSeekTimeMs()} /
   * {@link #getTotalPlayAndWaitTimeMs()}.
   */
  public float getSeekTimeRatio() {
    long playAndWaitTimeMs = getTotalPlayAndWaitTimeMs();
    return playAndWaitTimeMs == 0 ? 0f : (float) getTotalSeekTimeMs() / playAndWaitTimeMs;
  }

  /**
   * Returns the rate of rebuffer events, in rebuffers per play time second, or {@code 0.0} if no
   * time was spend playing. This is equivalent to 1.0 / {@link #getMeanTimeBetweenRebuffers()}.
   */
  public float getRebufferRate() {
    long playTimeMs = getTotalPlayTimeMs();
    return playTimeMs == 0 ? 0f : 1000f * totalRebufferCount / playTimeMs;
  }

  /**
   * Returns the mean play time between rebuffer events, in seconds. This is equivalent to 1.0 /
   * {@link #getRebufferRate()}. Note that this may return {@link Float#POSITIVE_INFINITY}.
   */
  public float getMeanTimeBetweenRebuffers() {
    return 1f / getRebufferRate();
  }
}