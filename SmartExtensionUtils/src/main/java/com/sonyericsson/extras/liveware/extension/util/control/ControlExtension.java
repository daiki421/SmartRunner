/*
Copyright (c) 2011, Sony Ericsson Mobile Communications AB
Copyright (c) 2012-2014 Sony Mobile Communications AB.

All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.

 * Neither the name of the Sony Ericsson Mobile Communications AB nor the names
  of its contributors may be used to endorse or promote products derived from
  this software without specific prior written permission.

* Neither the name of the Sony Mobile Communications AB nor the names
  of its contributors may be used to endorse or promote products derived from
  this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.sonyericsson.extras.liveware.extension.util.control;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

import com.sonyericsson.extras.liveware.aef.control.Control;
import com.sonyericsson.extras.liveware.aef.registration.Registration.Device;
import com.sonyericsson.extras.liveware.aef.registration.Registration.DeviceColumns;
import com.sonyericsson.extras.liveware.aef.registration.Registration.HostApp;
import com.sonyericsson.extras.liveware.aef.registration.Registration.HostAppColumns;
import com.sonyericsson.extras.liveware.extension.util.Dbg;
import com.sonyericsson.extras.liveware.extension.util.ExtensionUtils;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

/**
 * The control extension handles a control on an accessory.
 */
public abstract class ControlExtension {

    private static final int STATE_CREATED = 0;

    private static final int STATE_STARTED = 1;

    private static final int STATE_FOREGROUND = 2;

    /**
     * supported screen state.
     */
    private static final List<Integer> SUPPORTED_SCREEN_STATE = Arrays.asList(
            Control.Intents.SCREEN_STATE_OFF,
            Control.Intents.SCREEN_STATE_DIM,
            Control.Intents.SCREEN_STATE_ON,
            Control.Intents.SCREEN_STATE_AUTO
    );

    private int mState = STATE_CREATED;

    /**
     * The context of the extension service
     */
    protected final Context mContext;

    /**
     * Package name of the host application
     */
    protected final String mHostAppPackageName;

    /**
     * Default bitmap factory options that will be frequently used throughout
     * the extension to avoid any automatic scaling. Keep in mind that we are
     * not showing the images on the phone, but on the accessory.
     */
    protected final BitmapFactory.Options mBitmapOptions;

    /**
     * Create control extension.
     *
     * @param context The extension service context.
     * @param hostAppPackageName Package name of host application.
     */
    public ControlExtension(final Context context, final String hostAppPackageName) {
        if (context == null) {
            throw new IllegalArgumentException("context == null");
        }
        mContext = context;
        mHostAppPackageName = hostAppPackageName;

        mBitmapOptions = new BitmapFactory.Options();
        mBitmapOptions.inDensity = DisplayMetrics.DENSITY_DEFAULT;
        mBitmapOptions.inTargetDensity = DisplayMetrics.DENSITY_DEFAULT;
    }

    /**
     * Sets the control state to started. To request a control extension to start
     * you have to use {@link #startRequest()} method.
     */
    public final void start() {
        mState = STATE_STARTED;
        onStart();
    }

    /**
     * Sets the control state to resumed.
     */
    public final void resume() {
        mState = STATE_FOREGROUND;
        onResume();
    }

    /**
     * Sets the control state to paused.
     */
    public final void pause() {
        mState = STATE_STARTED;
        onPause();
    }

    /**
     * Sets the control state to stopped. To request a control extension to stop
     * you have to use {@link #stopRequest()} method.
     */
    public final void stop() {
        // If in foreground then pause it.
        if (mState == STATE_FOREGROUND) {
            pause();
        }

        mState = STATE_CREATED;
        onStop();
    }

    /**
     * Sets the control state to destroyed.
     */
    public final void destroy() {
        // If in foreground then pause it.
        if (mState == STATE_FOREGROUND) {
            pause();
        }
        // If started then stop it.
        if (mState == STATE_STARTED) {
            stop();
        }

        // No state for destroyed.
        onDestroy();
    }

    /**
     * Called when the control receives an action request.
     *
     * @see com.sonyericsson.extras.liveware.extension.util.ExtensionService#doActionOnAllControls(int,Bundle)
     * @param requestCode Code used to distinguish between different actions.
     * @param bundle Optional bundle with additional information.
     */
    public void onDoAction(int requestCode, Bundle bundle) {

    }

    /**
     * Called to notify a control extension that it is no longer used and is
     * being removed. The control extension should clean up any resources it
     * holds (threads, registered receivers, etc) at this point.
     */
    public void onDestroy() {

    }

    /**
     * Called when the control extension is started by the host application.
     */
    public void onStart() {

    }

    /**
     * Called when the control extension is stopped by the host application.
     */
    public void onStop() {

    }

    /**
     * Called when the control extension is paused by the host application.
     */
    public void onPause() {
    }

    /**
     * Called when the control extension is resumed by the host application. The
     * extension is expected to send a new image each time it is resumed.
     */
    public void onResume() {
    }

    /**
     * Called when extension receives an error.
     *
     * @param code The reported error code.
     *            {@link Control.Intents#EXTRA_ERROR_CODE}
     */
    public void onError(final int code) {

    }

    /**
     * Called when a tap event has occurred.
     *
     * @param action
     *            Can be any tap actions defined in the
     *            {@link Control.TapActions} interface.
     *
     * @param timeStamp
     *            The time when the tap event occurred.
     */
    public void onTap(final int action, final long timeStamp) {

    }

    /**
     * Called when a key event has occurred.
     *
     * @param action The key action, one of
     *            <ul>
     *            <li> {@link Control.Intents#KEY_ACTION_PRESS}</li>
     *            <li> {@link Control.Intents#KEY_ACTION_RELEASE}</li>
     *            <li> {@link Control.Intents#KEY_ACTION_REPEAT}</li>
     *            </ul>
     * @param keyCode The key code.
     * @param timeStamp The time when the event occurred.
     */
    public void onKey(final int action, final int keyCode, final long timeStamp) {

    }

    /**
     * Called when a touch event has occurred.
     *
     * @param event The touch event.
     */
    public void onTouch(final ControlTouchEvent event) {

    }

    /**
     * Called when an object click event has occurred.
     *
     * @param event The object click event.
     */
    public void onObjectClick(final ControlObjectClickEvent event) {

    }

    /**
     * Called when a swipe event has occurred.
     *
     * @param direction The swipe direction, one of
     *            <ul>
     *            <li> {@link Control.Intents#SWIPE_DIRECTION_UP}</li>
     *            <li> {@link Control.Intents#SWIPE_DIRECTION_DOWN}</li>
     *            <li> {@link Control.Intents#SWIPE_DIRECTION_LEFT}</li>
     *            <li> {@link Control.Intents#SWIPE_DIRECTION_RIGHT}</li>
     *            </ul>
     */
    public void onSwipe(int direction) {

    }

    /**
     * Called when the host application requests content for a specific list
     * item.
     *
     * @param layoutReference The referenced list view
     * @param listItemPosition The requested list item position
     */
    public void onRequestListItem(final int layoutReference, final int listItemPosition) {

    }

    /**
     * Called when an item in a list has been clicked.
     *
     * @param listItem The list item that was clicked
     * @param clickType The type of click ({@link Control.Intents#CLICK_TYPE_SHORT},
     *        {@link Control.Intents#CLICK_TYPE_LONG})
     * @param itemLayoutReference The object within the list item that was
     *        clicked
     */
    public void onListItemClick(final ControlListItem listItem, final int clickType,
            final int itemLayoutReference) {

    }

    /**
     * Called when list refresh was requested.
     *
     * @param layoutReference The referenced list view
     */
    public void onListRefreshRequest(final int layoutReference) {

    }

    /**
     * Called when an item in a list has been selected.
     *
     * @param listItem The list item that was selected
     */
    public void onListItemSelected(final ControlListItem listItem) {

    }

    /**
     * Called when a menu item has been selected.
     *
     * @param menuItem The menu item that was selected
     */
    public void onMenuItemSelected(final int menuItem) {

    }

    /**
     * Called when the accessory screen's state leaves or enters Active Low
     * Power mode.
     *
     * @param lowPowerModeOn true when the low power mode is started, false when
     *            the accessory screen leaves Active Low power mode
     */
    public void onActiveLowPowerModeChange(final boolean lowPowerModeOn) {

    }

    /**
     * Send request to start control extension.
     */
    protected void startRequest() {
        if (Dbg.DEBUG) {
            Dbg.d("Sending start request");
        }
        Intent intent = new Intent(Control.Intents.CONTROL_START_REQUEST_INTENT);
        sendToHostApp(intent);
    }

    /**
     * Send request to stop control extension.
     */
    protected void stopRequest() {
        if (Dbg.DEBUG) {
            Dbg.d("Sending stop request");
        }
        Intent intent = new Intent(Control.Intents.CONTROL_STOP_REQUEST_INTENT);
        sendToHostApp(intent);
    }

    /**
     * Show an image on the accessory.
     *
     * @param resourceId The image resource id.
     */
    protected void showImage(final int resourceId) {
        if (Dbg.DEBUG) {
            Dbg.d("showImage: " + resourceId);
        }

        Intent intent = new Intent();
        intent.setAction(Control.Intents.CONTROL_DISPLAY_DATA_INTENT);

        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), resourceId,
                mBitmapOptions);
        if (bitmap == null) {
            throw new IllegalArgumentException("bitmap is null.");
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream(256);
        bitmap.compress(CompressFormat.PNG, 100, os);
        byte[] buffer = os.toByteArray();
        intent.putExtra(Control.Intents.EXTRA_DATA, buffer);
        sendToHostApp(intent);
    }

    /**
     * Show a layout on the accessory.
     *
     * @param layoutId The layout resource id.
     * @param layoutData The layout data.
     * @see Control.Intents#EXTRA_LAYOUT_DATA
     */
    protected void showLayout(final int layoutId, final Bundle[] layoutData) {
        if (Dbg.DEBUG) {
            Dbg.d("showLayout");
        }

        Intent intent = new Intent(Control.Intents.CONTROL_PROCESS_LAYOUT_INTENT);
        intent.putExtra(Control.Intents.EXTRA_DATA_XML_LAYOUT, layoutId);
        if (layoutData != null && layoutData.length > 0) {
            intent.putExtra(Control.Intents.EXTRA_LAYOUT_DATA, layoutData);
        }

        sendToHostApp(intent);
    }

    /**
     * Update an image in a specific layout, on the accessory.
     *
     * @param layoutReference The referenced resource within the current layout.
     * @param resourceId The image resource id.
     */
    protected void sendImage(final int layoutReference, final int resourceId) {
        if (Dbg.DEBUG) {
            Dbg.d("sendImage");
        }

        Intent intent = new Intent(Control.Intents.CONTROL_SEND_IMAGE_INTENT);
        intent.putExtra(Control.Intents.EXTRA_LAYOUT_REFERENCE, layoutReference);
        intent.putExtra(Control.Intents.EXTRA_DATA_URI,
                ExtensionUtils.getUriString(mContext, resourceId));
        sendToHostApp(intent);
    }

    /**
     * Update an image in a specific layout, on the accessory.
     *
     * @param layoutReference The referenced resource within the current layout.
     * @param bitmap The bitmap to show.
     */
    protected void sendImage(final int layoutReference, final Bitmap bitmap) {
        if (Dbg.DEBUG) {
            Dbg.d("sendImage");
        }
        if (bitmap == null) {
            throw new IllegalArgumentException("bitmap is null.");
        }

        Intent intent = new Intent(Control.Intents.CONTROL_SEND_IMAGE_INTENT);
        intent.putExtra(Control.Intents.EXTRA_LAYOUT_REFERENCE, layoutReference);
        ByteArrayOutputStream os = new ByteArrayOutputStream(256);
        bitmap.compress(CompressFormat.PNG, 100, os);
        byte[] buffer = os.toByteArray();
        intent.putExtra(Control.Intents.EXTRA_DATA, buffer);
        sendToHostApp(intent);
    }

    /**
     * Update text in a specific layout, on the accessory.
     *
     * @param layoutReference The referenced resource within the current layout.
     * @param text The text to show.
     */
    protected void sendText(final int layoutReference, final String text) {
        if (Dbg.DEBUG) {
            Dbg.d("sendText: " + text);
        }
        Intent intent = new Intent(Control.Intents.CONTROL_SEND_TEXT_INTENT);
        intent.putExtra(Control.Intents.EXTRA_LAYOUT_REFERENCE, layoutReference);
        intent.putExtra(Control.Intents.EXTRA_TEXT, text);
        sendToHostApp(intent);
    }



    /**
     * Show bitmap on accessory.
     *
     * @param bitmap The bitmap to show.
     */
    protected void showBitmap(final Bitmap bitmap) {
        if (Dbg.DEBUG) {
            Dbg.d("showBitmap");
        }
        if (bitmap == null) {
            throw new IllegalArgumentException("bitmap is null.");
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(256);
        bitmap.compress(CompressFormat.PNG, 100, outputStream);

        Intent intent = new Intent(Control.Intents.CONTROL_DISPLAY_DATA_INTENT);
        intent.putExtra(Control.Intents.EXTRA_DATA, outputStream.toByteArray());
        sendToHostApp(intent);
    }

    /**
     * Show bitmap on accessory. Used when only updating part of the screen.
     *
     * @param bitmap The bitmap to show.
     * @param x The x position.
     * @param y The y position.
     */
    protected void showBitmap(final Bitmap bitmap, final int x, final int y) {
        if (Dbg.DEBUG) {
            Dbg.v("showBitmap x: " + x + " y: " + y);
        }
        if (bitmap == null) {
            throw new IllegalArgumentException("bitmap is null.");
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(256);
        bitmap.compress(CompressFormat.PNG, 100, outputStream);

        Intent intent = new Intent(Control.Intents.CONTROL_DISPLAY_DATA_INTENT);
        intent.putExtra(Control.Intents.EXTRA_X_OFFSET, x);
        intent.putExtra(Control.Intents.EXTRA_Y_OFFSET, y);
        intent.putExtra(Control.Intents.EXTRA_DATA, outputStream.toByteArray());
        sendToHostApp(intent);
    }

    /**
     * Set the accessory screens state.
     *
     * @see Control.Intents#SCREEN_STATE_AUTO
     * @see Control.Intents#SCREEN_STATE_DIM
     * @see Control.Intents#SCREEN_STATE_OFF
     * @see Control.Intents#SCREEN_STATE_ON
     * @param state The screen state.
     */
    protected void setScreenState(final int state) {
        if (Dbg.DEBUG) {
            Dbg.d("setScreenState: " + state);
        }
        if (!SUPPORTED_SCREEN_STATE.contains(state)) {
            throw new IllegalArgumentException("screen state(" + state + ") is not supported.");
        }
        Intent intent = new Intent(Control.Intents.CONTROL_SET_SCREEN_STATE_INTENT);
        intent.putExtra(Control.Intents.EXTRA_SCREEN_STATE, state);
        sendToHostApp(intent);
    }

    /**
     * Start repeating vibrator.
     *
     * @param onDuration On duration in milliseconds.
     * @param offDuration Off duration in milliseconds.
     * @param repeats The number of repeats of the on/off pattern. Use
     *            {@link Control.Intents#REPEAT_UNTIL_STOP_INTENT} to repeat
     *            until explicitly stopped.
     */
    protected void startVibrator(int onDuration, int offDuration, int repeats) {
        if (Dbg.DEBUG) {
            Dbg.v("startVibrator: onDuration: " + onDuration + ", offDuration: " + offDuration
                    + ", repeats: " + repeats);
        }
        Intent intent = new Intent(Control.Intents.CONTROL_VIBRATE_INTENT);
        intent.putExtra(Control.Intents.EXTRA_ON_DURATION, onDuration);
        intent.putExtra(Control.Intents.EXTRA_OFF_DURATION, offDuration);
        intent.putExtra(Control.Intents.EXTRA_REPEATS, repeats);
        sendToHostApp(intent);
    }

    /**
     * Stop vibrator.
     */
    protected void stopVibrator() {
        if (Dbg.DEBUG) {
            Dbg.v("Vibrator stop");
        }
        Intent intent = new Intent(Control.Intents.CONTROL_STOP_VIBRATE_INTENT);
        sendToHostApp(intent);
    }

    /**
     * Start a LED pattern.
     *
     * @param id Id of the LED to be controlled.
     * @param color Color you want the LED to blink with.
     * @param onDuration On duration in milliseconds.
     * @param offDuration Off duration in milliseconds.
     * @param repeats The number of repeats of the on/off pattern. Use
     *            {@link Control.Intents#REPEAT_UNTIL_STOP_INTENT} to repeat
     *            until explicitly stopped.
     */
    protected void startLedPattern(int id, int color, int onDuration, int offDuration, int repeats) {
        if (Dbg.DEBUG) {
            Dbg.v("startLedPattern: id: " + id + ", color: " + color + "onDuration: " + onDuration
                    + ", offDuration: " + offDuration + ", repeats: " + repeats);
        }
        Intent intent = new Intent(Control.Intents.CONTROL_LED_INTENT);
        intent.putExtra(Control.Intents.EXTRA_LED_ID, id);
        intent.putExtra(Control.Intents.EXTRA_LED_COLOR, color);
        intent.putExtra(Control.Intents.EXTRA_ON_DURATION, onDuration);
        intent.putExtra(Control.Intents.EXTRA_OFF_DURATION, offDuration);
        intent.putExtra(Control.Intents.EXTRA_REPEATS, repeats);
        sendToHostApp(intent);
    }

    /**
     * Turn led off.
     *
     * @param id Id of the LED to be controlled.
     */
    protected void stopLedPattern(int id) {
        if (Dbg.DEBUG) {
            Dbg.v("stopLedPattern: id: " + id);
        }
        Intent intent = new Intent(Control.Intents.CONTROL_LED_INTENT);
        intent.putExtra(Control.Intents.EXTRA_LED_ID, id);
        sendToHostApp(intent);
    }

    /**
     * Clear accessory display.
     */
    protected void clearDisplay() {
        if (Dbg.DEBUG) {
            Dbg.v("Clear display");
        }
        Intent intent = new Intent(Control.Intents.CONTROL_CLEAR_DISPLAY_INTENT);
        sendToHostApp(intent);
    }

    /**
     * Set the number of items in list. Used to specify the initial size of
     * the list and to update the size of the list.
     * @param layoutReference The referenced list view.
     * @param listCount The requested number of items in list.
     */
    protected void sendListCount(int layoutReference, int listCount) {
        if (listCount < 0) {
            throw new IllegalArgumentException("listCount(" + listCount + ") is invalid value.");
        }

        Intent intent = new Intent(Control.Intents.CONTROL_LIST_COUNT_INTENT);
        intent.putExtra(Control.Intents.EXTRA_LAYOUT_REFERENCE, layoutReference);
        intent.putExtra(Control.Intents.EXTRA_LIST_COUNT, listCount);
        sendToHostApp(intent);
    }

    /**
     * Set the number of items in list and populate it with content.
     * @param layoutReference The referenced list view.
     * @param listCount The requested number of items in list.
     * @param bundles List content. See {@link Control.Intents#EXTRA_LIST_CONTENT}.
     */
    protected void sendListCountWithContent(int layoutReference, int listCount, Bundle[] bundles) {
        if (listCount < 0) {
            throw new IllegalArgumentException("listCount(" + listCount + ") is invalid value.");
        }

        Intent intent = new Intent(Control.Intents.CONTROL_LIST_COUNT_INTENT);
        intent.putExtra(Control.Intents.EXTRA_LAYOUT_REFERENCE, layoutReference);
        intent.putExtra(Control.Intents.EXTRA_LIST_COUNT, listCount);
        intent.putExtra(Control.Intents.EXTRA_LIST_CONTENT, bundles);
        sendToHostApp(intent);
    }

    /**
     * Update a list item.
     * @param item List item.
     */
    protected void sendListItem(ControlListItem item) {
        if (item == null) {
            throw new IllegalArgumentException("item is null.");
        }

        Intent intent = new Intent(Control.Intents.CONTROL_LIST_ITEM_INTENT);
        intent.putExtra(Control.Intents.EXTRA_LAYOUT_REFERENCE, item.layoutReference);
        intent.putExtra(Control.Intents.EXTRA_DATA_XML_LAYOUT, item.dataXmlLayout);
        if (item.listItemId != -1) {
            intent.putExtra(Control.Intents.EXTRA_LIST_ITEM_ID, item.listItemId);
        }
        if (item.listItemPosition != -1) {
            intent.putExtra(Control.Intents.EXTRA_LIST_ITEM_POSITION, item.listItemPosition);
        }
        if (item.layoutData != null && item.layoutData.length > 0) {
            intent.putExtra(Control.Intents.EXTRA_LAYOUT_DATA, item.layoutData);
        }
        sendToHostApp(intent);
    }

    /**
     * Sends a request to move a list to a specified position.
     *
     * @param layoutReference The referenced list view.
     * @param position The referenced list item position.
     */
    protected void sendListPosition(int layoutReference, int position) {
        if (position < 0) {
            throw new IllegalArgumentException("position(" + position + ") is invalid value.");
        }

        Intent intent = new Intent(Control.Intents.CONTROL_LIST_MOVE_INTENT);
        intent.putExtra(Control.Intents.EXTRA_LAYOUT_REFERENCE, layoutReference);
        intent.putExtra(Control.Intents.EXTRA_LIST_ITEM_POSITION, position);
        sendToHostApp(intent);
    }

    /**
     * Show a menu.
     * @param menuItems Menu items. See {@link Control.Intents#EXTRA_MENU_ITEMS}.
     */
    protected void showMenu(Bundle[] menuItems) {
        Intent intent = new Intent(Control.Intents.CONTROL_MENU_SHOW);
        intent.putExtra(Control.Intents.EXTRA_MENU_ITEMS, menuItems);
        sendToHostApp(intent);
    }

    /**
     * Send intent to host application. Adds host application package name and
     * our package name.
     *
     * @param intent The intent to send.
     */
    protected void sendToHostApp(final Intent intent) {
        ExtensionUtils.sendToHostApp(mContext, mHostAppPackageName, intent);
    }

    /**
     * Get the host application id for this control.
     *
     * @return The host application id.
     */
    protected long getHostAppId() {
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(HostApp.URI,
                    new String[] {
                        HostAppColumns._ID
                    }, HostAppColumns.PACKAGE_NAME + " = ?",
                    new String[] {
                        mHostAppPackageName
                    }, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getLong(cursor.getColumnIndexOrThrow(HostAppColumns._ID));
            }
        } catch (SQLException e) {
            if (Dbg.DEBUG) {
                Dbg.w("Failed to query host apps", e);
            }
        } catch (SecurityException e) {
            if (Dbg.DEBUG) {
                Dbg.w("Failed to query host apps", e);
            }
        } catch (IllegalArgumentException e) {
            if (Dbg.DEBUG) {
                Dbg.w("Failed to query host apps", e);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return -1;
    }

    /**
     * Check if this host application has a vibrator.
     *
     * @return True if vibrator exists.
     */
    protected boolean hasVibrator() {
        long hostAppId = getHostAppId();

        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(
                    Device.URI,
                    new String[] {
                        DeviceColumns.VIBRATOR
                    },
                    DeviceColumns.HOST_APPLICATION_ID + " = " + hostAppId + " AND "
                            + DeviceColumns.VIBRATOR + " = 1", null, null);
            if (cursor != null) {
                return (cursor.getCount() > 0);
            }
        } catch (SQLException exception) {
            if (Dbg.DEBUG) {
                Dbg.e("Failed to query vibrator", exception);
            }
        } catch (SecurityException exception) {
            if (Dbg.DEBUG) {
                Dbg.e("Failed to query vibrator", exception);
            }
        } catch (IllegalArgumentException exception) {
            if (Dbg.DEBUG) {
                Dbg.e("Failed to query vibrator", exception);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return false;
    }

    /**
     * Process View and whole ViewGroup tree into ControlViewGroup used by
     * accessory.
     * @param v View to parse.
     * @return Group of processed views.
     */
    protected ControlViewGroup parseLayout(View v) {
        ControlViewGroup controlViewGroup = new ControlViewGroup();
        controlViewGroup.addView(new ControlView(v.getId(), v.isClickable(), v.isLongClickable()));
        if (v instanceof ViewGroup) {
            parseLayoutTraverse((ViewGroup) v, controlViewGroup);
        }
        return controlViewGroup;
    }

    /**
     * Helper method for {@link #parseLayout(View)}.
     * @param v View to parse.
     * @param controlViewGroup Group of processed views.
     */
    private void parseLayoutTraverse(ViewGroup v, ControlViewGroup controlViewGroup) {
        for (int i = 0; i < v.getChildCount(); i++) {
            View current = v.getChildAt(i);
            controlViewGroup.addView(new ControlView(current.getId(), current.isClickable(),
                    current
                    .isLongClickable()));
            if (current instanceof ViewGroup) {
                parseLayoutTraverse((ViewGroup) current, (ControlViewGroup) controlViewGroup);
            }
        }
    }

}
