package com.sony.smarteyeglass.extension.displaysetting;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by daiki on 2017/06/03.
 */

public class ListViewActivity extends Activity{

    ListView dataList;
    private BaseAdapter adapter;
    RoundReader roundReader;
    Round round;
    public String fileName;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listview_layout);
        System.out.println(android.os.Environment.getExternalStorageDirectory().getPath());
        dataList = (ListView) findViewById(R.id.data_list);
        ArrayList<String> list = new ArrayList<>();
        final String sdFile = android.os.Environment.getExternalStorageDirectory().getPath() + "/DataList/";

        final File[] files = new File(sdFile).listFiles();

        for(int i = 0; i < files.length; i++){
            if(files[i].isFile()){
                list.add(files[i].getName());
            }
        }
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, list);
        dataList.setAdapter(adapter);

        //リスト項目が選択された時のイベントを追加
        dataList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String msg = position + "番目のアイテムがクリックされました";
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                fileName = sdFile + files[position].getName();
                System.out.println(fileName);
                Intent intent = new Intent(ListViewActivity.this, ReadAndCalculateActivity.class);
                intent.putExtra("getFileName", fileName);
                startActivity(intent);
            }
        });

        // リストに取得したデータを表示
        // When button is clicked, run the SmartEyeglass app
        Button btnGlass = (Button) findViewById(R.id.new_create);
        btnGlass.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                Intent intent = new Intent(ListViewActivity.this, WriteOnlyActivity.class);
                startActivity(intent);
            }
        });
    }
}
