package cnnj.yuliangmax.rxbussample;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends FragmentActivity implements View.OnClickListener {

    private Button btn_R;
    private Button btn_U;

    private Button btn_Post_a;
    private Button btn_Post_b;

    private TextView tv_Message;

    private StringBuilder sb = new StringBuilder();

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            tv_Message.setText(sb.toString());
        }
    };

    TestEventListener listener = new TestEventListener() {
        @Override
        public void onLisTestEventAAA(TestEvent event) {
            sb.append("[onLisTestEventAAA] [" + Thread.currentThread().getId() + "] " + event.getMsg() + "\n");
            handler.sendEmptyMessage(0);
        }

        @Override
        public void onLisTestEventBBB(TestEvent event) {
            sb.append("[onLisTestEventBBB] [" + Thread.currentThread().getId() + "] " + event.getMsg() + "\n");
            handler.sendEmptyMessage(0);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

    }

    private void initView() {
        btn_R = (Button) findViewById(R.id.btn_R);
        btn_U = (Button) findViewById(R.id.btn_U);
        btn_Post_a = (Button) findViewById(R.id.btn_Post_a);
        btn_Post_b = (Button) findViewById(R.id.btn_Post_b);
        tv_Message = (TextView) findViewById(R.id.tv_Message);

        btn_R.setOnClickListener(this);
        btn_U.setOnClickListener(this);
        btn_Post_a.setOnClickListener(this);
        btn_Post_b.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_R:
                RxBus.getInstance().register(listener);
                break;
            case R.id.btn_U:
                RxBus.getInstance().unregister(listener);
                break;
            case R.id.btn_Post_a:
                RxBus.getInstance().post(new TestEvent("post aaa"), EventTags.TAG_AAA);
                break;
            case R.id.btn_Post_b:
                RxBus.getInstance().post(new TestEvent("post bbb"), EventTags.TAG_BBB);
                break;
            default:
                break;
        }
    }
}
