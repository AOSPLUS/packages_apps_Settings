/*
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

package com.android.settings.rascarlo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.PreferenceCategory;
import android.preference.SeekBarPreference;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.Window;
import android.widget.Toast;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import java.io.File;
import java.io.IOException;

public class SystemLock extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "SystemLock";

    private static final int DLG_ENABLE_EIGHT_TARGETS = 0;

    private static final String KEY_SEE_TRHOUGH = "see_through";
    private static final String KEY_BLUR_RADIUS = "blur_radius";
    private static final String PREF_LOCKSCREEN_EIGHT_TARGETS = "lockscreen_eight_targets";

    private CheckBoxPreference mSeeThrough;
    private SeekBarPreference mBlurRadius;
    private CheckBoxPreference mLockscreenEightTargets;

    private Activity mActivity;
    private ContentResolver mResolver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();
        mActivity = getActivity();
        mResolver = mActivity.getContentResolver();

        addPreferencesFromResource(R.xml.system_lock);

        PreferenceScreen prefs = getPreferenceScreen();
        ContentResolver resolver = getContentResolver();

        // lockscreen see through
        mSeeThrough = (CheckBoxPreference) prefs.findPreference(KEY_SEE_TRHOUGH);
        mBlurRadius = (SeekBarPreference) prefs.findPreference(KEY_BLUR_RADIUS);
        mBlurRadius.setProgress(Settings.System.getInt(resolver,
            Settings.System.LOCKSCREEN_BLUR_RADIUS, 12));
        mBlurRadius.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        ContentResolver cr = getContentResolver();

        if (preference == mSeeThrough) {
              Settings.System.putInt(cr, Settings.System.LOCKSCREEN_SEE_THROUGH,
                      mSeeThrough.isChecked() ? 1 : 0);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        ContentResolver cr = getActivity().getContentResolver();

        if (preference == mBlurRadius) {
            Settings.System.putInt(cr, Settings.System.LOCKSCREEN_BLUR_RADIUS, (Integer)objValue);
            return true;
        } else if (preference == mLockscreenEightTargets) {
            showDialogInner(DLG_ENABLE_EIGHT_TARGETS, (Boolean) objValue);
            return true;
        }

        return false;
    }

    private void showDialogInner(int id, boolean state) {
        DialogFragment newFragment = MyAlertDialogFragment.newInstance(id, state);
        newFragment.setTargetFragment(this, 0);
        newFragment.show(getFragmentManager(), "dialog " + id);
    }

    public static class MyAlertDialogFragment extends DialogFragment {

        public static MyAlertDialogFragment newInstance(int id, boolean state) {
            MyAlertDialogFragment frag = new MyAlertDialogFragment();
            Bundle args = new Bundle();
            args.putInt("id", id);
            args.putBoolean("state", state);
            frag.setArguments(args);
            return frag;
        }

        SystemLock getOwner() {
            return (SystemLock) getTargetFragment();
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            int id = getArguments().getInt("id");
            boolean state = getArguments().getBoolean("state");
            switch (id) {
                case DLG_ENABLE_EIGHT_TARGETS:
                    getOwner().mLockscreenEightTargets.setChecked(!state);
                    break;
             }
        }
    }
}
