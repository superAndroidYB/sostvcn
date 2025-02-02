package com.sostvcn.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.sostvcn.R;
import com.sostvcn.model.SosAudioList;

import java.util.HashMap;

/**
 * Created by Administrator on 2017/6/25.
 */
public class MultiselectListAdapter extends BaseAdapter {
    private Context context;
    private SosAudioList audioList;
    private HashMap<Integer, Boolean> selectedMap;

    public MultiselectListAdapter(Context context, SosAudioList audioList) {
        this.audioList = audioList;
        this.context = context;
        selectedMap = new HashMap<>();
    }

    @Override
    public int getCount() {
        return audioList.getVoice_list().size();
    }

    @Override
    public Object getItem(int i) {
        return audioList.getVoice_list().get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        final int index = i;
        ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = LayoutInflater.from(context).inflate(R.layout.multiselect_list_item, null);
            holder.selectCheckBox = (CheckBox) view.findViewById(R.id.selected_checkbox);
            holder.titleText = (TextView) view.findViewById(R.id.audio_titile);
            holder.downloadStatusView = (ImageView) view.findViewById(R.id.download_image_view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        SosAudioList.Voice_list item = audioList.getVoice_list().get(index);
        holder.titleText.setText(item.getTitle());
        holder.downloadStatusView.setImageResource(R.mipmap.audio_album_multiselect_waiting);
        if (selectedMap.containsKey(index)) {
            holder.selectCheckBox.setChecked(selectedMap.get(index));
        } else {
            holder.selectCheckBox.setChecked(false);
        }
        return view;
    }


    public void setIsSelected(Integer index) {
        if (selectedMap.containsKey(index)) {
            selectedMap.put(index, !selectedMap.get(index));
        } else {
            selectedMap.put(index, true);
        }

        this.notifyDataSetChanged();
    }

    public void allSelected(boolean oper) {
        for (int i = 0; i < audioList.getVoice_list().size(); i++) {
            selectedMap.put(i, oper);
        }
        this.notifyDataSetChanged();
    }

    static class ViewHolder {
        CheckBox selectCheckBox;
        TextView titleText;
        ImageView downloadStatusView;
    }
}
