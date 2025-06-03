package com.example.weixin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendFragment extends Fragment {

    private ListView contactListView;
    private List<Map<String, Object>> contactList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend, container, false);
        
        initViews(view);
        loadContacts();
        
        return view;
    }
    
    private void initViews(View view) {
        contactListView = view.findViewById(R.id.contact_list);
    }
    
    private void loadContacts() {
        // 添加特殊条目
        addSpecialItem("新的朋友", R.drawable.new_friend);
        addSpecialItem("群聊", R.drawable.group_chat);
        addSpecialItem("标签", R.drawable.tag);
        addSpecialItem("公众号", R.drawable.official_accounts);
        
        // 添加常规联系人，按字母排序
        addContact("阿里巴巴", R.drawable.contact_avatar);
        addContact("曹操", R.drawable.contact_avatar);
        addContact("陈奕迅", R.drawable.contact_avatar);
        addContact("刘备", R.drawable.contact_avatar);
        addContact("孙权", R.drawable.contact_avatar);
        addContact("王菲", R.drawable.contact_avatar);
        addContact("小明", R.drawable.contact_avatar);
        addContact("张三", R.drawable.contact_avatar);
        addContact("赵云", R.drawable.contact_avatar);
        
        // 创建适配器
        SimpleAdapter adapter = new SimpleAdapter(
                getActivity(),
                contactList,
                R.layout.item_contact,
                new String[]{"avatar", "name"},
                new int[]{R.id.contact_avatar, R.id.contact_name}
        );
        
        contactListView.setAdapter(adapter);
    }
    
    private void addSpecialItem(String name, int avatarResId) {
        Map<String, Object> item = new HashMap<>();
        item.put("name", name);
        item.put("avatar", avatarResId);
        item.put("isSpecial", true);
        contactList.add(item);
    }
    
    private void addContact(String name, int avatarResId) {
        Map<String, Object> item = new HashMap<>();
        item.put("name", name);
        item.put("avatar", avatarResId);
        item.put("isSpecial", false);
        contactList.add(item);
    }
} 