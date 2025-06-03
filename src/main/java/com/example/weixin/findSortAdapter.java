package com.example.weixin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

public class findSortAdapter extends BaseAdapter{

    private ViewHolder viewHolder;
    private List<Map<String, String>> data = null;
    private Context mContext;

    public findSortAdapter(Context mContext, List<Map<String, String>> data) {
        this.mContext = mContext;
        this.data = data;
    }

    public int getCount() {
        return this.data.size();
    }

    public Object getItem(int position) {
        return data.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View view, ViewGroup arg2) {

        if (view == null) {
            viewHolder = new ViewHolder();
            //获取listview对应的item布局
            view = LayoutInflater.from(mContext).inflate(R.layout.find_item, null);
            //初始化组件
            viewHolder.pic = (ImageView) view.findViewById(R.id.pic);
            viewHolder.title = (TextView) view.findViewById(R.id.title);
            viewHolder.pic1 = (ImageView) view.findViewById(R.id.pic1);
            viewHolder.divider = (View) view.findViewById(R.id.divider);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        
        Map<String, String> map = data.get(position);
        viewHolder.pic.setImageResource(Integer.parseInt(map.get("pic")));
        viewHolder.title.setText(map.get("title"));
        viewHolder.pic1.setImageResource(Integer.parseInt(map.get("pic1")));
        
        // 控制分隔线显示逻辑
        if (position == 0 || position == 2 || position == 4 || position == 6 || position == 8) {
            // 在这些位置后显示分隔线
            viewHolder.divider.setVisibility(View.VISIBLE);
        } else {
            viewHolder.divider.setVisibility(View.GONE);
        }
        
        return view;
    }

    final static class ViewHolder {
        ImageView pic;
        TextView title;
        ImageView pic1;
        View divider;
    }
}