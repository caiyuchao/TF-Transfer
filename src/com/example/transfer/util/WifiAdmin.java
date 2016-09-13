package com.example.transfer.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.example.transfer.bean.TransferUser;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.widget.Toast;

/**
 * 管理wifi的工具类
 * @author yuehuang
 *
 */
public class WifiAdmin {

	private WifiManager wifiManager;
	private boolean flag = false;//标记是否打开热点
	private List<ScanResult> wifiList;  
    private List<String> passableHotsPot;
    private Context context;
    
    public interface WifiThreadListener{
    	public void onEvent(boolean flag);
    }
	
	public  WifiAdmin(Context context){
		this.context = context;
		//取得WifiManager对象
		wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	}
	
	//启动wifi并连接
	public void connect(final String wifi_name, final WifiThreadListener listener){
		if(!wifiManager.isWifiEnabled()){
			setHotspot(false);
			wifiManager.setWifiEnabled(true);
			System.out.println("开启WIFI");
		}
		wifiManager.startScan();
		System.out.println("已扫描");
		new Thread(){
			public void run() {
				while(wifiList == null || wifiList.size() == 0){
					wifiList = wifiManager.getScanResults(); 
				}
				System.out.println(wifiList.size());
	            if (wifiList == null || wifiList.size() == 0)  
	                return;  
	            boolean flag = onReceiveNewNetworks(wifiList, wifi_name);
	            listener.onEvent(flag);
			}
		}.start();
	}
	
	//设置热点
	public boolean setHotspot(boolean enabled){
		//打开热点需要关闭连接wifi的功能
		if(wifiManager.isWifiEnabled())
			wifiManager.setWifiEnabled(false);
		try {  
            //热点的配置类  
            WifiConfiguration apConfig = new WifiConfiguration();  
            //配置热点的名称(可以在名字后面加点随机数什么的)  
            apConfig.SSID = TransferUser.getInstance().getUsername();  
            //配置热点的密码  
            apConfig.preSharedKey="tftftftf"; 
            apConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            apConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            
            //通过反射调用设置热点  
            Method method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE); 
            //返回热点打开状态  
            return (Boolean) method.invoke(wifiManager, apConfig, enabled); 
        } catch (Exception e) {  
        	e.printStackTrace();
        	return false;
        }
	}
	
	public String getServerIp(){
		DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
		return Formatter.formatIpAddress(dhcpInfo.serverAddress);
	}
	
	/*当搜索到新的wifi热点时判断该热点是否符合规格*/  
    public boolean onReceiveNewNetworks(List<ScanResult> wifiList, String wifi_name){  
        passableHotsPot = new ArrayList<String>();  
        for(ScanResult result:wifiList){  
            System.out.println(result.SSID);  
            if((result.SSID).equals(wifi_name))  
                passableHotsPot.add(result.SSID);  
        }  
        return connectToHotpot();  
    }  
      
    /*连接到热点*/  
    public boolean connectToHotpot(){  
        if(passableHotsPot==null || passableHotsPot.size()==0)  
            return false;  
        WifiConfiguration wifiConfig = setWifiParams(passableHotsPot.get(0));  
        int wcgID = wifiManager.addNetwork(wifiConfig);  
        boolean flag = wifiManager.enableNetwork(wcgID, true);  
        System.out.println("connect success? "+flag);  
        return flag;
    }  
      
    /*设置要连接的热点的参数*/  
    public WifiConfiguration setWifiParams(String ssid){  
        WifiConfiguration apConfig=new WifiConfiguration();  
        apConfig.SSID = "\""+ssid+"\"";  
        apConfig.preSharedKey = "\"tftftftf\"";  
        apConfig.hiddenSSID = false;  
        apConfig.status = WifiConfiguration.Status.ENABLED;  
//        apConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);  
//        apConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);  
        apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);  
//        apConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);  
//        apConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);  
//        apConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);  
        return apConfig;  
    }
    
    public void closeWifiAp(){
    	try{
	    	Method method = wifiManager.getClass().getMethod("getWifiApConfiguration");    
	        method.setAccessible(true);    
	        WifiConfiguration config = (WifiConfiguration) method.invoke(wifiManager);    
	        Method method2 = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);    
	        method2.invoke(wifiManager, config, false);  
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }
}
