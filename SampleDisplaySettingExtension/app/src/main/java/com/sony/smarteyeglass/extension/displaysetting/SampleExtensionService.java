/*
Copyright (c) 2011, Sony Mobile Communications Inc.
Copyright (c) 2014, Sony Corporation

 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 * Neither the name of the Sony Mobile Communications Inc.
 nor the names of its contributors may be used to endorse or promote
 products derived from this software without specific prior written permission.

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
package com.sony.smarteyeglass.extension.displaysetting;

import android.content.Intent;
import android.util.Log;

import com.sonyericsson.extras.liveware.aef.control.Control;
import com.sonyericsson.extras.liveware.extension.util.ExtensionService;
import com.sonyericsson.extras.liveware.extension.util.ExtensionUtils;
import com.sonyericsson.extras.liveware.extension.util.control.ControlExtension;
import com.sonyericsson.extras.liveware.extension.util.registration.DeviceInfoHelper;
import com.sonyericsson.extras.liveware.extension.util.registration.RegistrationInformation;

/**
 * The Sample Extension Service handles registration and keeps track of all
 * accessories.
 */
public final class SampleExtensionService extends ExtensionService {

    // ActivityからControlExtensionに送信するための変数
    public static SampleExtensionService Object;
    ControlExtension controlExtension;
    private static String Message = null;
    public static SampleDisplaySettingControl SmartEyeglassControl;


    /** Creates a new instance. */
    public SampleExtensionService() {
        super(Constants.EXTENSION_KEY);
        Object = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(Constants.LOG_TAG, "onCreate");
    }

    public void sendMessageToExtension(final String message) {
        Message = message;
        if (SmartEyeglassControl == null) {
            startSmartEyeglassExtension();
        } else {
            SmartEyeglassControl.requestExtensionStart();
            System.out.println("HWES/sendMessageToExtension");
        }
    }

    public void startSmartEyeglassExtension() {
        Intent intent = new Intent(Control.Intents
                .CONTROL_START_REQUEST_INTENT);
        ExtensionUtils.sendToHostApp(getApplicationContext(),
                "com.sony.smarteyeglass", intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(Constants.LOG_TAG, "onDestroy");
    }

    @Override
    protected RegistrationInformation getRegistrationInformation() {
        return new SampleRegistrationInformation(this);
    }

    @Override
    protected boolean keepRunningWhenConnected() {
        return false;
    }

    @Override
    public ControlExtension createControlExtension(
            final String hostAppPackageName) {
        boolean isApiSupported = DeviceInfoHelper
            .isSmartEyeglassScreenSupported(this, hostAppPackageName);
        if (isApiSupported) {
            return new SampleDisplaySettingControl(this, hostAppPackageName);
        } else {
            Log.d(Constants.LOG_TAG, "Service: not supported, exiting");
            throw new IllegalArgumentException(
                "No control for: " + hostAppPackageName);
        }
    }
}
