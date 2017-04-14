package com.example.administrator.terminal;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Lvmoy on 2017/3/30 0030.
 * Mode: - - !
 */

public class ShowActivity extends Activity {

    @BindView(R.id.rl_sendData)
    RecyclerView rlSendData;
    @BindView(R.id.rl_receiveData)
    RecyclerView rlReceiveData;
    @BindView(R.id.bt_return)
    TextView btReturn;

    private ArrayList<String> sendList = new ArrayList<>();
    private ArrayList<String> receiveList = new ArrayList<>();
    private ArrayList<String> sendName = new ArrayList<>();
    private ArrayList<String> receiveName = new ArrayList<>();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_detail_item_570_info);
        ButterKnife.bind(this);
        sendList = getIntent().getStringArrayListExtra("com.example.administrator.terminal.SendAll");
        receiveList = getIntent().getStringArrayListExtra("com.example.administrator.terminal.ReceiveAll");
        showInfo(sendList, receiveList);

    }

    private void showInfo(ArrayList<String> sendList, ArrayList<String> receiveList) {

        sendName.add(getResources().getString(R.string.home_detail_item_send_hz) + ":\n");
        sendName.add(getResources().getString(R.string.home_detail_item_bps) + ":\n");
        sendName.add(getResources().getString(R.string.home_detail_item_dbm) + ":\n");
        sendName.add(getResources().getString(R.string.home_detail_item_scrambler) + ":\n");
        rlSendData.setLayoutManager(new LinearLayoutManager(this));
        rlSendData.setAdapter(new RelistAdapter(sendList, sendName));
        receiveName.add("\t:\t" + getResources().getString(R.string.home_detail_item_receive_hz) + "\n");
        receiveName.add("\t:\t" + getResources().getString(R.string.home_detail_item_receive_bps));
        rlReceiveData.setLayoutManager(new LinearLayoutManager(this));
        rlReceiveData.setAdapter(new RelistAdapter(receiveName, receiveList));
    }


    @OnClick(R.id.bt_return)
    public void onViewClicked() {
        ShowActivity.this.finish();
    }

    public class RelistAdapter extends RecyclerView.Adapter<RelistAdapter.VH>{

        ArrayList<String> stringList = new ArrayList<>();
        ArrayList<String> nameList = new ArrayList<>();
        public RelistAdapter(ArrayList<String> stringList, ArrayList<String> nameList) {
            this.stringList = stringList;
            this.nameList = nameList;
        }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_detail_item_570_info_item, parent, false);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            holder.value.setText(stringList.get(position));
            holder.name.setText(nameList.get(position));
        }

        @Override
        public int getItemCount() {
            return stringList.size();
        }

        public class VH extends RecyclerView.ViewHolder{
            public final TextView name;
            public final TextView value;
            public VH(View itemView) {
                super(itemView);
                name = (TextView) itemView.findViewById(R.id.tv_item_name);
                value = (TextView) itemView.findViewById(R.id.tv_item_value);
            }
        }
    }
}
