package edu.temple.templetag;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class CreateTagTest {

    @Rule
    public IntentsTestRule<HomeActivity> intentsTestRule =
            new IntentsTestRule<>(HomeActivity.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.ACCESS_FINE_LOCATION");

    @Before
    public void stubCameraIntent() {
        Instrumentation.ActivityResult result = createImageCaptureActivityResultStub();

        // Stub the Intent.
        intending(hasAction(MediaStore.ACTION_IMAGE_CAPTURE)).respondWith(result);
    }

    @Test
    public void createTagTest() throws InterruptedException {
        onView(withId(R.id.createTagBtn)).
                perform(click());
        onView(withId(R.id.tagLocationName))
                .perform(typeText("Esp"), closeSoftKeyboard());
        onView(withId(R.id.tagDescription))
                .perform(typeText("Espresso Testing"), closeSoftKeyboard());
        onView(withId(R.id.takeTagPictureBtn))
                .perform(click());
        onView(withId(R.id.createTagBtn))
                .perform(click());
    }

    private Instrumentation.ActivityResult createImageCaptureActivityResultStub() {
        // Put the drawable in a bundle.
        Bundle bundle = new Bundle();
        // This doesn't work.  I cannot get bitmap factory to return a bitmap.  This code is
        // straight from Google
        bundle.putParcelable("data", BitmapFactory.decodeResource(
                intentsTestRule.getActivity().getResources(), R.mipmap.ic_templetag));
        // Create the Intent that will include the bundle.
        Intent resultData = new Intent();
        resultData.putExtras(bundle);

        // Create the ActivityResult with the Intent.
        return new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
    }
}
