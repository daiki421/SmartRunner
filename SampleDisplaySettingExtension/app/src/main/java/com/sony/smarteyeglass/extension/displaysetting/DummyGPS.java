package com.sony.smarteyeglass.extension.displaysetting;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by daiki on 2017/06/08.
 */

public class DummyGPS extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_layout);
        Button setGPSButton = (Button) findViewById(R.id.start_button);
        setGPSButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                System.out.println("Get GPS");



            }
        });

    }
}
