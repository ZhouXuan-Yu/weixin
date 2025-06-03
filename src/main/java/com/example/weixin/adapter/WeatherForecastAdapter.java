package com.example.weixin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.weixin.R;
import com.example.weixin.model.WeatherData;
import java.util.List;

public class WeatherForecastAdapter extends RecyclerView.Adapter<WeatherForecastAdapter.ViewHolder> {
    private List<WeatherData> weatherDataList;

    public WeatherForecastAdapter(List<WeatherData> weatherDataList) {
        this.weatherDataList = weatherDataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_weather_forecast, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WeatherData weatherData = weatherDataList.get(position);
        
        holder.tvDate.setText(formatDate(weatherData.getFxDate()));
        holder.tvDayCondition.setText(weatherData.getTextDay());
        holder.tvNightCondition.setText(weatherData.getTextNight());
        holder.tvTempMax.setText(weatherData.getTempMax() + "°");
        holder.tvTempMin.setText(weatherData.getTempMin() + "°");
        holder.tvHumidity.setText("湿度: " + weatherData.getHumidity() + "%");
        holder.tvPrecip.setText("降水: " + weatherData.getPrecip() + "mm");
        holder.tvPressure.setText("气压: " + weatherData.getPressure() + "hPa");
        holder.tvWindDay.setText("风向: " + weatherData.getWindDirDay() + " " + weatherData.getWindScaleDay() + "级");
        holder.tvUvIndex.setText("紫外线: " + weatherData.getUvIndex());
        holder.tvSunrise.setText("日出: " + weatherData.getSunrise());
        holder.tvSunset.setText("日落: " + weatherData.getSunset());
    }

    @Override
    public int getItemCount() {
        return weatherDataList != null ? weatherDataList.size() : 0;
    }

    private String formatDate(String dateStr) {
        try {
            // 简单的日期格式化，可以根据需要进行更复杂的处理
            String[] parts = dateStr.split("-");
            if (parts.length == 3) {
                return parts[1] + "月" + parts[2] + "日";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dateStr;
    }

    public void updateData(List<WeatherData> newData) {
        this.weatherDataList = newData;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvDayCondition, tvNightCondition, tvTempMax, tvTempMin;
        TextView tvHumidity, tvPrecip, tvPressure, tvWindDay, tvUvIndex;
        TextView tvSunrise, tvSunset;

        ViewHolder(View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvDayCondition = itemView.findViewById(R.id.tvDayCondition);
            tvNightCondition = itemView.findViewById(R.id.tvNightCondition);
            tvTempMax = itemView.findViewById(R.id.tvTempMax);
            tvTempMin = itemView.findViewById(R.id.tvTempMin);
            tvHumidity = itemView.findViewById(R.id.tvHumidity);
            tvPrecip = itemView.findViewById(R.id.tvPrecip);
            tvPressure = itemView.findViewById(R.id.tvPressure);
            tvWindDay = itemView.findViewById(R.id.tvWindDay);
            tvUvIndex = itemView.findViewById(R.id.tvUvIndex);
            tvSunrise = itemView.findViewById(R.id.tvSunrise);
            tvSunset = itemView.findViewById(R.id.tvSunset);
        }
    }
}
