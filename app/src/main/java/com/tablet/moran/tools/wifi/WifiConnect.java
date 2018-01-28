package com.tablet.moran.tools.wifi;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import com.tablet.moran.tools.SLogger;

import java.util.List;

/**
 * Created by Stone on 2017/12/4.
 */

public class WifiConnect {

    WifiManager wifiManager;
    // 扫描出的网络连接列表
    private List<ScanResult> mWifiList;
    // 网络连接列表
    private List<WifiConfiguration> mWifiConfigurations;

    //定义几种加密方式，一种是WEP，一种是WPA，还有没有密码的情况
    public enum WifiCipherType
    {
        WIFICIPHER_WEP, WIFICIPHER_WPA, WIFICIPHER_NOPASS, WIFICIPHER_INVALID
    }

    //构造函数
    public WifiConnect(WifiManager wifiManager)
    {
        this.wifiManager = wifiManager;
    }

    //打开wifi功能
    private boolean openWifi()
    {
        boolean bRet = true;
        if (!wifiManager.isWifiEnabled())
        {
            bRet = wifiManager.setWifiEnabled(true);
        }
        return bRet;
    }

    public void startScan() {
        wifiManager.startScan();
        // 得到扫描结果
        mWifiList = wifiManager.getScanResults();
        // 得到配置好的网络连接
        mWifiConfigurations = wifiManager.getConfiguredNetworks();
    }



    //提供一个外部接口，传入要连接的无线网
    public boolean connect(String SSID, String Password, WifiCipherType Type)
    {
        if(!this.openWifi())
        {
            SLogger.d("<<", "--->开启wifi!!!!!!!!!失败了。。。。。。。。。");
            return false;
        }
        SLogger.d("<<", "--->开启wifi 进入等待");
        //开启wifi功能需要一段时间(我在手机上测试一般需要1-3秒左右)，所以要等到wifi
        //状态变成WIFI_STATE_ENABLED 的时候才能执行下面的语句
        //WifiManager.WIFI_STATE_ENABLING 2
        //WifiManager.WIFI_STATE_ENABLED 3
        SLogger.d("<<" , "wifi state is " + String.valueOf(wifiManager.getWifiState()));
        while(wifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED )
        {
            SLogger.d("<<" , "wifi state is------> " + String.valueOf(wifiManager.getWifiState()));
            try{
                //为了避免程序一直while循环，让它睡个100毫秒在检测……
                Thread.currentThread();
                Thread.sleep(100);
            }
            catch(InterruptedException ie){
            }
        }

        WifiConfiguration wifiConfig = this.createWifiInfo(SSID, Password, Type);
        //
        if(wifiConfig == null)
        {
            return false;
        }

        WifiConfiguration tempConfig = this.isExsits(SSID);

        if(tempConfig != null)
        {
            wifiManager.removeNetwork(tempConfig.networkId);
        }

        int netID = wifiManager.addNetwork(wifiConfig);
        boolean bRet = wifiManager.enableNetwork(netID, false);
        return bRet;
    }

    //查看以前是否也配置过这个网络
    public WifiConfiguration isExsits(String SSID)
    {
        List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs)
        {
            if (existingConfig.SSID.equals("\""+SSID+"\""))
            {
                return existingConfig;
            }
        }
        return null;
    }

    private WifiConfiguration createWifiInfo(String SSID, String Password, WifiCipherType Type)
    {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";
        if(Type == WifiCipherType.WIFICIPHER_NOPASS)
        {
            config.wepKeys[0] = "\"" + "\"";;
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;

            //或者下面两段代码也可以
            // config.hiddenSSID = true;
            // config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        }
        else if(Type == WifiCipherType.WIFICIPHER_WEP)
        {
            config.wepKeys[0] = "\""+Password+"\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        else if(Type == WifiCipherType.WIFICIPHER_WPA)
        {
            config.preSharedKey = "\""+Password+"\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            //config.allowedProtocols.set(WifiConfiguration.Protocol.WPA); //这句好像不行
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        else
        {
            return null;
        }
        return config;
    }


    public List<ScanResult> getmWifiList() {
        return mWifiList;
    }

    public void setmWifiList(List<ScanResult> mWifiList) {
        this.mWifiList = mWifiList;
    }

    public List<WifiConfiguration> getmWifiConfigurations() {
        return mWifiConfigurations;
    }

    public void setmWifiConfigurations(List<WifiConfiguration> mWifiConfigurations) {
        this.mWifiConfigurations = mWifiConfigurations;
    }

    public WifiManager getWifiManager() {
        return wifiManager;
    }

    public void setWifiManager(WifiManager wifiManager) {
        this.wifiManager = wifiManager;
    }
}
