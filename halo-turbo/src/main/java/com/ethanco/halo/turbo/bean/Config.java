package com.ethanco.halo.turbo.bean;

import android.content.Context;

import com.ethanco.halo.turbo.ads.IConvertor;
import com.ethanco.halo.turbo.ads.IHandler;
import com.ethanco.halo.turbo.type.Mode;

import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Created by EthanCo on 2016/9/16.
 */
public class Config {
    //模式
    public Mode mode;
    //目标IP
    public String targetIP;
    //目标端口
    public int targetPort;
    //源IP
    //public String sourceIP;
    //源端口
    public int sourcePort;
    //缓存大小
    public int bufferSize;
    //线程池
    public ExecutorService threadPool;

    public List<IHandler> handlers;

    public List<IConvertor> convertors;

    public Object codec;

    public Context context;

    public KeepAlive keepAlive;
}
