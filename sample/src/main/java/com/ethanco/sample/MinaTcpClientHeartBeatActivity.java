package com.ethanco.sample;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.ethanco.halo.turbo.Halo;
import com.ethanco.halo.turbo.ads.IHandlerAdapter;
import com.ethanco.halo.turbo.ads.IKeepAliveListener;
import com.ethanco.halo.turbo.ads.ISession;
import com.ethanco.halo.turbo.bean.KeepAlive;
import com.ethanco.halo.turbo.impl.handler.StringLogHandler;
import com.ethanco.halo.turbo.type.Mode;
import com.ethanco.json.convertor.convert.ObjectJsonConvertor;
import com.ethanco.sample.databinding.ActivityMinaTcpClientBinding;

import static com.ethanco.sample.MinaTcpServerHeartBeatActivity.HEART_BEAT;

public class MinaTcpClientHeartBeatActivity extends AppCompatActivity {

    private static final String TAG = "Z-MinaTcpClientActivity";
    private static final String TAG2 = "Z-KeepAlive";
    private ActivityMinaTcpClientBinding binding;
    private ScrollBottomTextWatcher watcher;
    private ISession session;
    private Halo halo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_mina_tcp_client);

        binding.btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String targetIP = binding.etTargetIp.getText().toString();
                if (TextUtils.isEmpty(targetIP)) {
                    Toast.makeText(MinaTcpClientHeartBeatActivity.this, "目标IP不能为空", Toast.LENGTH_SHORT).show();
                }

                new Thread() {
                    @Override
                    public void run() {
                        if (halo == null) {
                            halo = new Halo.Builder()
                                    .setMode(Mode.MINA_NIO_TCP_CLIENT)
                                    .setBufferSize(2048)
                                    .setTargetIP(targetIP)
                                    .setTargetPort(19701)
                                    .addHandler(new StringLogHandler(TAG))
                                    .addHandler(new DemoHandler())
                                    .addConvert(new ObjectJsonConvertor()) //增加转换器 -> write的时候自动转换为Json字符串
                                    .setKeepAlive(new KeepAlive(15, 60, keepAliveListener))
                                    .build();
                        }

                        if (halo.isRunning()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplication(), "已启动", Toast.LENGTH_SHORT).show();
                                }
                            });
                            return;
                        }

                        final boolean startSuccess = halo.start();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String connectResult = startSuccess ? "连接成功" : "连接失败";
                                binding.tvInfo.append(connectResult + "\r\n");
                            }
                        });
                    }
                }.start();
            }
        });

        binding.btnSendData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (session == null) {
                    Toast.makeText(MinaTcpClientHeartBeatActivity.this, "未建立连接", Toast.LENGTH_SHORT).show();
                } else {
                    //session.write("hello，这是从Client发送的数据");
                    session.write(new TestBean("aaa", "bbb"));
                }
            }
        });

        binding.btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (halo != null) {
                    halo.stop();
                    binding.tvInfo.append("停止连接" + "\r\n");
                }
            }
        });

        watcher = new ScrollBottomTextWatcher(binding.scrollView);
        binding.tvInfo.addTextChangedListener(watcher);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (watcher != null) {
            binding.tvInfo.removeTextChangedListener(watcher);
            watcher = null;
        }
    }

    class DemoHandler extends IHandlerAdapter {

        @Override
        public void sessionOpened(final ISession session) {
            Log.i(TAG2, "sessionOpened : ");
            MinaTcpClientHeartBeatActivity.this.session = session;
            //session.write("hello aaabbb");
        }

        @Override
        public void messageReceived(ISession session, Object message) {
            Log.i(TAG2, "messageReceived : ");
            String receive = String.valueOf(message);
            binding.tvInfo.append("接收:" + receive + "\r\n");
        }

        @Override
        public void messageSent(ISession session, Object message) {
            Log.i(TAG2, "messageSent : ");
            super.messageSent(session, message);
            binding.tvInfo.append("发送:" + message + "\r\n");
        }

        @Override
        public void sessionClosed(ISession session) {
            super.sessionClosed(session);
            Log.i(TAG2, "sessionClosed : ");
            MinaTcpClientHeartBeatActivity.this.session = null;
        }

        @Override
        public void sessionCreated(ISession session) {
            Log.i(TAG2, "sessionCreated : ");
            super.sessionCreated(session);
        }
    }

    private IKeepAliveListener keepAliveListener = new IKeepAliveListener() {
        @Override
        public void onKeepAliveRequestTimedOut(KeepAlive keepAlive, ISession iSession) {
            Log.i(TAG2, "Client >>> onKeepAliveRequestTimedOut...:" + iSession);
        }

        @Override
        public boolean isKeepAliveMessage(ISession ioSession, Object message) {
            if (message == null) return false;
            if (message instanceof String) {
                return message.equals(HEART_BEAT);
            }
            return false;
        }

        @Override
        public Object getKeepAliveMessage(ISession ioSession, Object o) {
            return HEART_BEAT;
        }
    };
}
