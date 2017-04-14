package com.example.administrator.terminal;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import utils.edittextvalidator.widget.FormEditText;

/**
 * Created by Lvmoy on 2017/3/28 0028.
 * Mode: - - !
 */

public class LoginActivity extends Activity {

    @BindView(R.id.tv_login)
    TextView tvLogin;
    @BindView(R.id.et_login_address)
    FormEditText etLoginAddress;
    @BindView(R.id.et_login_user)
    FormEditText etLoginUser;
    @BindView(R.id.et_login_psd)
    FormEditText etLoginPsd;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_login);
        ButterKnife.bind(this);
        String content = null;
        content = "172.22.1.22";
        etLoginAddress.setText(content);
        etLoginAddress.setSelection(content.length());
        content = "admin";
        etLoginUser.setText(content);
//        etLoginUser.setSelection(content.length());

//        // 获取编辑框焦点
//        etLoginPsd.setFocusable(true);
//        //打开软键盘
//        InputMethodManager imm = (InputMethodManager) this
//                .getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @OnClick(R.id.tv_login)
    public void onViewClicked() {
        FormEditText[] allFields = {etLoginAddress, etLoginUser, etLoginPsd};
        boolean allValid = true;
        boolean isIpValid = true;
        isIpValid = allFields[0].ztestValidity();
        for (FormEditText field : allFields) {
            allValid = field.ztestValidity() && allValid;
        }

        if (allValid) {
            Intent intent = new Intent(this, MainActivity.class);
            int successFlag = 1;
            intent.addFlags(successFlag);
            startActivity(intent);
            // YAY
        } else if (!isIpValid) {
            Crouton.makeText(LoginActivity.this, "请填写正确的登录IP地址！", Style.ALERT).show();
            // EditText are going to appear with an exclamation mark and an explicative message.
        } else {
            Crouton.makeText(LoginActivity.this, "请填写完整信息！", Style.ALERT).show();

        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Crouton.clearCroutonsForActivity(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
