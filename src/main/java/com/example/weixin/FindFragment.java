package com.example.weixin;

import androidx.fragment.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.weixin.findSortAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FindFragment extends Fragment {

    private ListView listView;
    private ImageView searchButton, addButton;
    private List<Map<String, String>> list = new ArrayList<>();
    // 只保留天气，计算器，指南针，图片分析，语音识别
    private int[] pic = new int[]{
            R.drawable.weather0, 
            R.drawable.tab_weixin_pressed,
            R.drawable.compass, 
            R.drawable.football1,
            R.drawable.audio_icon
    };
    private String[] data = new String[]{
            "天气", 
            "计算器", 
            "指南针", 
            "图片分析",
            "语音识别"
    };
    // 更新右侧箭头图标数量
    private int[] pic1 = new int[]{
            R.drawable.tab_img, R.drawable.tab_img, R.drawable.tab_img, 
            R.drawable.tab_img, R.drawable.tab_img
    };

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.find_fragment, container, false);

        listView = view.findViewById(R.id.listView);
        searchButton = view.findViewById(R.id.search);
        addButton = view.findViewById(R.id.plus);
        
        initData();
        findSortAdapter adapter = new findSortAdapter(getActivity().getApplicationContext(), list);
        listView.setAdapter(adapter);

        // 设置搜索和添加按钮的点击事件
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "搜索功能", Toast.LENGTH_SHORT).show();
            }
        });
        
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "添加功能", Toast.LENGTH_SHORT).show();
            }
        });
        
        // 设置列表项点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch(position) {
                    case 0: // 天气功能
                        Toast.makeText(getActivity(), "打开天气功能", Toast.LENGTH_SHORT).show();
                        Intent weatherIntent = new Intent(getActivity(), WeatherActivity.class);
                        startActivity(weatherIntent);
                        break;
                    case 1: // 计算器功能
                        Toast.makeText(getActivity(), "打开计算器", Toast.LENGTH_SHORT).show();
                        Intent calculatorIntent = new Intent(getActivity(), caclulator.class);
                        startActivity(calculatorIntent);
                        break;
                    case 2: // 指南针功能
                        Toast.makeText(getActivity(), "打开指南针", Toast.LENGTH_SHORT).show();
                        Intent compassIntent = new Intent(getActivity(), CompassActivity.class);
                        startActivity(compassIntent);
                        break;
                    case 3: // 图片分析功能
                        Toast.makeText(getActivity(), "打开图片分析", Toast.LENGTH_SHORT).show();
                        Intent imageAnalysisIntent = new Intent(getActivity(), ImageAnalysisActivity.class);
                        startActivity(imageAnalysisIntent);
                        break;
                    case 4: // 语音识别功能
                        Toast.makeText(getActivity(), "打开语音识别", Toast.LENGTH_SHORT).show();
                        Intent speechRecognitionIntent = new Intent(getActivity(), SpeechRecognitionActivity.class);
                        startActivity(speechRecognitionIntent);
                        break;
                    default:
                        Toast.makeText(getActivity(), "点击了: " + data[position], Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        return view;
    }

    private void initData() {
        for (int i = 0; i < data.length; i++) {
            Map<String, String> map = new HashMap<>();
            map.put("pic", String.valueOf(pic[i]));
            map.put("title", data[i]);
            map.put("pic1", String.valueOf(pic1[i]));
            list.add(map);
        }
    }
}
