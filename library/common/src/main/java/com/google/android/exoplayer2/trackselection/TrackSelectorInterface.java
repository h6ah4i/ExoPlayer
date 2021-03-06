/*
 * Copyright (C) 2020 The Android Open Source Project
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
package com.google.android.exoplayer2.trackselection;

// TODO(b/172315872) Replace @code by @link when Player has been migrated to common
/**
 * The component of a {@code Player} responsible for selecting tracks to be played.
 *
 * <p>No Player agnostic track selection is currently supported. Clients should downcast to the
 * implementation's track selection.
 */
// TODO(b/172315872) Define an interface for track selection.
public interface TrackSelectorInterface {}
