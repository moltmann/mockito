/*
 * Copyright (c) 2018 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */

package org.mockito.android.test;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.widget.FrameLayout;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.spy;

@RunWith(AndroidJUnit4.class)
public class BlacklistedApisTest {
    /**
     * Check if the application is marked as {@code android:debuggable} in the manifest
     *
     * @return {@code true} iff it is marked as such
     */
    private boolean isDebuggable() throws PackageManager.NameNotFoundException {
        Context context = InstrumentationRegistry.getTargetContext();
        PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);

        return (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }

    @Test
    public void callBlacklistedPublicMethodRealMethod() throws Exception {
        Context targetContext = InstrumentationRegistry.getTargetContext();

        FrameLayout child = new FrameLayout(targetContext);
        FrameLayout parent = spy(new FrameLayout(targetContext));

        if (isDebuggable()) {
            // This calls a blacklisted public method.
            // Since Android P these methods are not callable from outside of the Android framework
            // anymore:
            //
            // https://android-developers.googleblog.com/2018/02/
            // improving-stability-by-reducing-usage.html
            //
            // Hence if we use a subclass mock this will fail. Inline mocking does not have this
            // problem as the mock class is the same as the mocked class.
            parent.addView(child);
        } else {
            try {
                parent.addView(child);
                fail();
            } catch (NoSuchMethodError expected) {
                // expected
            }
        }
    }

    @Test
    public void copyBlacklistedFields() throws Exception {
        assumeTrue(isDebuggable());

        Context targetContext = InstrumentationRegistry.getTargetContext();

        FrameLayout child = new FrameLayout(targetContext);
        FrameLayout parent = spy(new FrameLayout(targetContext));

        parent.addView(child);

        // During cloning of the parent spy, all fields are copied. This accesses a blacklisted
        // fields. Since Android P these fields are not visible from outside of the Android
        // framework anymore:
        //
        // https://android-developers.googleblog.com/2018/02/
        // improving-stability-by-reducing-usage.html
        //
        // As 'measure' requires the fields to be initialized, this fails if the fields are not
        // copied.
        parent.measure(100, 100);
    }
}
