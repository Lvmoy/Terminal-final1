package com.example.administrator.terminal;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import adapter.HomeViewPagerAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import devlight.io.library.ntb.NavigationTabBar;
import pojo.order.DataItem;

import utils.NoDoubleClickListener;
import utils.OrderUtils;
import utils.edittextvalidator.widget.FormEditText;

/**
 * Created by Lvmoy on 2017/3/29 0029.
 * Mode: - - !
 */

public class HomeDetailActivity extends Activity {
    private static final String TAG = "HomeDetailActivity";
    @BindView(R.id.vp_horizontal_ntb)
    ViewPager vpHorizontalNtb;
    @BindView(R.id.ntb_horizontal)
    NavigationTabBar ntbHorizontal;
    @BindView(R.id.wrapper_ntb_horizontal)
    FrameLayout wrapperNtbHorizontal;


    private FormEditText etToCatIp;
    private FormEditText etToWanIp;
    private FormEditText etToLanIp;
    private TextView btCatPing;
    private TextView btWanPing;
    private TextView btLanPing;

    private EditText etSendHz;
    private EditText etSendBps;
    private EditText etReceiveHz;
    private EditText etReceiveBps;
    private EditText etPowerLevel;
    private Spinner spSendScrambler;

    private TextView btOk;
    private TextView btInfo;

    //spinner对应表
    private SparseIntArray scramMap = new SparseIntArray();

    private Map<String, Integer> scramValueToPostionMap = new HashMap<>();

    private View currentView = null;
    //    private HashMap<Integer, View > viewHashMap = new HashMap<>();
    private HomeViewPagerAdapter viewPagerAdapter;

    private List<View> viewList = new ArrayList<>();
    //已接收dataList
    private List<DataItem> dataItems  = new ArrayList<>();
    //修改判断edittext缓存List
    private List<DataItem> tempList = new ArrayList<>();
    //edittext 列表
    private List<View> conpenentList = new ArrayList<>();
    //详情页send data List
    private ArrayList<String> sendList = new ArrayList<>();

    //详情页receive data List
    private ArrayList<String> receiveList = new ArrayList<>();

    private boolean is570Connecting = false;
    private boolean isDataShowing = false;
    private boolean isChildThreadRunning = true;
    private static final String baseUri2 = "http://192.168.2.67:8080/test/";
    private String resultPing = null;

    private Dialog dialog = null;
    private TextView dialgTextView = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_home_detail);
        ButterKnife.bind(this);
        initUI();
        initMap();
        setViews();
        doAllPing();
        doConnectAndQuery();
        doEditJudge();
    }

    private void initMap() {
        scramMap.append(1, 0);
        scramMap.append(0, 1);
        scramMap.append(2, 2);

        scramValueToPostionMap.put("0", 1);
        scramValueToPostionMap.put("1", 0);
        scramValueToPostionMap.put("2", 2);
    }

    //等待完成机制，统一处理
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    doOpenQuery();
                    break;
                case 2:
                    showPingInfo();
                    break;
                case 3:
                    Crouton.makeText(HomeDetailActivity.this, "修改成功！", Style.INFO).show();
                    doConnectAndQuery();
                    break;
                default:
                    break;
            }
        }
    };

    private void initUI() {
        viewList.add(View.inflate(getApplicationContext(), R.layout.layout_detail_item_common, null));
        viewList.add(View.inflate(getApplicationContext(), R.layout.layout_detail_item_3g, null));
        viewList.add(View.inflate(getApplicationContext(), R.layout.layout_detail_item_bd, null));
        viewList.add(View.inflate(getApplicationContext(), R.layout.layout_detail_item_570,null));
        viewList.add(View.inflate(getApplicationContext(), R.layout.layout_detail_item_voip, null));
        viewPagerAdapter = new HomeViewPagerAdapter(viewList);
        vpHorizontalNtb.setAdapter(viewPagerAdapter);


        final String[] colors = getResources().getStringArray(R.array.default_preview);

        final ArrayList<NavigationTabBar.Model> models = new ArrayList<>();
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.icon_home_detail_common),
                        Color.parseColor(colors[0]))
                        .title(getString(R.string.home_detail_common))
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.icon_home_detail_3g),
                        Color.parseColor(colors[1]))
                        .title(getString(R.string.home_detail_3g))
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.icon_home_detail_bd),
                        Color.parseColor(colors[2]))
                        .title(getString(R.string.home_detail_bd))
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.icon_home_detail_570),
                        Color.parseColor(colors[3]))
                        .title(getString(R.string.home_detail_570))
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.icon_home_detail_voip),
                        Color.parseColor(colors[4]))
                        .title(getString(R.string.home_detail_voip))
                        .build()
        );

        ntbHorizontal.setModels(models);
        ntbHorizontal.setViewPager(vpHorizontalNtb, 3);
        ntbHorizontal.setIconSizeFraction(0.5f);

        ntbHorizontal.post(new Runnable() {
            @Override
            public void run() {
                final View viewPager = findViewById(R.id.vp_horizontal_ntb);
                ((ViewGroup.MarginLayoutParams) viewPager.getLayoutParams()).topMargin =
                        (int) -ntbHorizontal.getBadgeMargin();
                viewPager.requestLayout();
            }
        });

        ntbHorizontal.setOnTabBarSelectedIndexListener(new NavigationTabBar.OnTabBarSelectedIndexListener() {
            @Override
            public void onStartTabSelected(final NavigationTabBar.Model model, final int index) {

            }

            @Override
            public void onEndTabSelected(final NavigationTabBar.Model model, final int index) {
                switch (index) {
                    case 3:

                        break;
                    default:
                        break;
                }
            }
        });

    }

    private void setViews() {
        if (viewList != null && viewList.get(3) != null) {

            View view = viewList.get(3);
            etSendHz = (EditText) view.findViewById(R.id.et_sendHz);
            etSendBps = (EditText) view.findViewById(R.id.et_sendbps);
            etReceiveHz = (EditText) view.findViewById(R.id.et_receivehz);
            etReceiveBps = (EditText) view.findViewById(R.id.et_receivebps);
            etPowerLevel = (EditText) view.findViewById(R.id.et_dbm);
            spSendScrambler = (Spinner) view.findViewById(R.id.sp_sendscrambler);

            conpenentList.add(etSendHz);
            conpenentList.add(etSendBps);
            conpenentList.add(etReceiveHz);
            conpenentList.add(etReceiveBps);
            conpenentList.add(etPowerLevel);
            conpenentList.add(spSendScrambler);

        }
    }


    private void doConnectAndQuery() {
        is570Connecting = getIntent().getBooleanExtra("570state", true);
        if(is570Connecting){
            Thread thread  = new Thread(new Runnable() {
                @Override
                public void run() {
                    if(doSetOrQuery(dataItems)){
                        handler.sendEmptyMessage(1);
                    }
                }
            });
            thread.start();
        }
        else {
            Crouton.makeText(this, "对不起，570设备没有连接，不支持此项服务。", Style.ALERT).show();
        }
    }



    private void doEditJudge() {
            if (viewList != null && viewList.get(3) != null) {
                View view = viewList.get(3);
                btOk = (TextView) view.findViewById(R.id.bt_ok);
                btInfo = (TextView) view.findViewById(R.id.bt_info);
                btOk.setOnClickListener(new NoDoubleClickListener() {
                    @Override
                    public void onNoDoubleClick(View v) {
                        if (dataItems != null && dataItems.size() > 0) {
                            tempList.clear();
                            for (int i = 0; i < dataItems.size(); i++) {
                                if(conpenentList.get(i) instanceof  EditText) {
                                    String temp =  ((EditText)conpenentList.get(i)).getText().toString();
                                    temp = dealEditString(i, temp);
                                    if (temp.equals(dataItems.get(i).getValue())) {
                                    } else {
                                        DataItem data = new DataItem();

                                        data.setId(dataItems.get(i).getId());
                                        data.setName("edit");
                                        data.setIp(dataItems.get(i).getIp());
                                        data.setIso(dataItems.get(i).getIso());
                                        data.setType(dataItems.get(i).getType());
                                        data.setValue(temp);
                                        tempList.add(data);
                                    }
                                }else if(conpenentList.get(i) instanceof Spinner){
                                    String transStr = ((Spinner)conpenentList.get(i)).getSelectedItemPosition() + "";
                                    transStr = dealEditString(i, transStr);
                                    if(doSpinnerJudge(conpenentList.get(i), dataItems.get(i).getValue())){
                                        DataItem mdata = new DataItem();

                                        mdata.setId(dataItems.get(i).getId());
                                        mdata.setName("edit");
                                        mdata.setIp(dataItems.get(i).getIp());
                                        mdata.setIso(dataItems.get(i).getIso());
                                        mdata.setType(dataItems.get(i).getType());
                                        mdata.setValue(transStr);
                                        tempList.add(mdata);
                                    }
                                }
                            }
                            if (tempList.size() > 0) {
                                doEdit(tempList);
                            }
                        }
                    }
                });

                btInfo.setOnClickListener(new View.OnClickListener() {
                    @Override
                        public void onClick(View v) {
                        Intent intent = new Intent(HomeDetailActivity.this, ShowActivity.class);
                        intent.putStringArrayListExtra("com.example.administrator.terminal.SendAll", sendList);
                        intent.putStringArrayListExtra("com.example.administrator.terminal.ReceiveAll", receiveList);
                        startActivity(intent);
                    }
                });
            }

    }

    private String dealEditString(int i, String string){
        StringBuilder stringBuffer = new StringBuilder(string);
        switch (i){
            case 0:
                stringBuffer.deleteCharAt(4);
                string = stringBuffer.toString();
                stringBuffer.setLength(0);
                break;
            case 1:
                stringBuffer.deleteCharAt(3);
                string = stringBuffer.toString();
                stringBuffer.setLength(0);
                break;
            case 2:
                stringBuffer.deleteCharAt(4);
                string = stringBuffer.toString();
                stringBuffer.setLength(0);
                break;
            case 3:
                stringBuffer.deleteCharAt(3);
                string = stringBuffer.toString();
                stringBuffer.setLength(0);
                break;
            case 4:
//                stringBuffer.deleteCharAt(2);
                string = "-" + stringBuffer.toString();
                stringBuffer.setLength(0);
                break;
            case 5:
                if(string.equals("0")){
                    string = "1";
                }else if(string.equals("1")){
                    string = "0";
                }else {
                    string = "2";
                }
                break;
            default:
                break;
        }
        return string;
    }
    private boolean doSpinnerJudge(View view, String string) {
        int position = ((Spinner) view).getSelectedItemPosition();
        String temp = scramMap.get(position) + "";
        if(temp.equals(string)){
            return false;
        }else {
            return true;
        }
    }

    private void doEdit(final List<DataItem> editList) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                OrderUtils.doQueueEdit(baseUri2, editList);
                handler.sendEmptyMessage(3);
            }
        });
        thread.start();

    }


    private void doOpenQuery() {
            if (dataItems != null) {
                if(sendList != null && sendList.size() >0 || (receiveList != null && receiveList.size() > 0)){
                    sendList.clear();
                    receiveList.clear();
                }
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append(dataItems.get(0).getValue());
                etSendHz.setText(stringBuffer.insert(4, "."));
                sendList.add(stringBuffer.toString());
                stringBuffer.setLength(0);
                stringBuffer.append(dataItems.get(1).getValue());
                etSendBps.setText(stringBuffer.insert(3, "."));
                sendList.add(stringBuffer.toString());
                stringBuffer.setLength(0);
                stringBuffer.append(dataItems.get(2).getValue());
                etReceiveHz.setText(stringBuffer.insert(4, "."));
                receiveList.add(stringBuffer.toString());
                stringBuffer.setLength(0);
                stringBuffer.append(dataItems.get(3).getValue());
                etReceiveBps.setText(stringBuffer.insert(3, "."));
                receiveList.add(stringBuffer.toString());
                int lenght = dataItems.get(4).getValue().length();
                String temp = dataItems.get(4).getValue().substring(1, lenght);
                stringBuffer.setLength(0);
                stringBuffer.append(temp);
                etPowerLevel.setText(stringBuffer);
                sendList.add(stringBuffer.toString());
//                etPowerLevel.setText(stringBuffer.insert(2, "."));
                String i = dataItems.get(5).getValue();
                spSendScrambler.setSelection(scramValueToPostionMap.get(i));
                switch (i){
                    case "0":
                        sendList.add("Off");
                        break;
                    case "1":
                        sendList.add("On(Normal)");
                        break;
                    case "2":
                        sendList.add("IESS-315");
                        break;
                    default:
                        break;
                }

            } else {
                Crouton.makeText(HomeDetailActivity.this, "连接570设备错误!", Style.ALERT).show();
            }
            isDataShowing = true;
    }

    private boolean doSetOrQuery(List<DataItem> dataItemList) {
        dataItemList.clear();
        return doAllQuery(dataItemList);
    }

    private boolean doAllQuery(List<DataItem> dataItemsList) {
        return OrderUtils.doAllQuery(baseUri2, dataItemsList);
    }



    private void doAllPing() {
        if (viewList != null && viewList.get(3) != null) {
            View view = viewList.get(3);
            etToCatIp = (FormEditText) view.findViewById(R.id.et_toCatIp);
            etToWanIp = (FormEditText) view.findViewById(R.id.et_toWanIp);
            etToLanIp = (FormEditText) view.findViewById(R.id.et_toLanIp);

            btCatPing = (TextView) view.findViewById(R.id.bt_catPing);
            btWanPing = (TextView) view.findViewById(R.id.bt_WanPing);
            btLanPing = (TextView) view.findViewById(R.id.bt_LanPing);

            etToCatIp.setFocusable(true);
            etToCatIp.requestFocus();
            etToCatIp.setTextIsSelectable(true);
            etToCatIp.setSelection(8);

            etToWanIp.setFocusable(true);
            etToWanIp.requestFocus();
            etToWanIp.setTextIsSelectable(true);
            etToWanIp.setSelection(8);

            etToLanIp.setFocusable(true);
            etToLanIp.requestFocus();
            etToLanIp.setTextIsSelectable(true);
            etToLanIp.setSelection(8);
            final InputMethodManager m=(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

            etToCatIp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    m.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                }
            });

            etToWanIp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    m.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                }
            });

            etToLanIp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    m.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                }
            });

            EditText editText = (EditText) view.findViewById(R.id.et_sendHz);
            editText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
            btCatPing.setOnClickListener(new NoDoubleClickListener() {
                @Override
                public void onNoDoubleClick(View v) {
                    if (!etToCatIp.ztestValidity()){

                    }else {
                        String catIp = etToCatIp.getText().toString();

                        doPing(catIp);
                    }

                }
            });
            btWanPing.setOnClickListener(new NoDoubleClickListener() {
                @Override
                public void onNoDoubleClick(View v) {
                    if (!etToWanIp.ztestValidity()){

                    }else{
                        String wanIp = etToWanIp.getText().toString();

                        doPing(wanIp);
                    }
                }
            });
            btLanPing.setOnClickListener(new NoDoubleClickListener() {
                @Override
                public void onNoDoubleClick(View v) {
                    if (!etToLanIp.ztestValidity()){

                    }else{
                        String lanIp = etToLanIp.getText().toString();

                        doPing(lanIp);
                    }
                }
            });

        } else {
            Crouton.makeText(HomeDetailActivity.this, "viewHashMap = null", Style.ALERT).show();

        }


    }

    private void doPing(String ipStr) {

        if(OrderUtils.doCatPing(ipStr)){
            resultPing = OrderUtils.doPingForInfo(ipStr);
            handler.sendEmptyMessage(2);
                Crouton.makeText(HomeDetailActivity.this, "恭喜，网络通畅!", Style.INFO).show();
            }else{
                resultPing = OrderUtils.doPingForInfo(ipStr);
                handler.sendEmptyMessage(2);
                Crouton.makeText(HomeDetailActivity.this, "对不起，网络不通!请检查地址", Style.ALERT).show();
            }
    }

    private void showPingInfo() {
        if(dialog == null || dialgTextView == null){
            dialog = new Dialog(HomeDetailActivity.this, R.style.UserAvatarDialog);
            dialgTextView = new TextView(HomeDetailActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialgTextView.setTextColor(getResources().getColor(R.color.colorOrange));
            dialgTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
            dialog.setContentView(dialgTextView);
        }
        dialgTextView.setText(resultPing);
        dialog.show();
    }
    private boolean is570Connected() {
        return is570Connecting;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Crouton.clearCroutonsForActivity(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
