package etn.app.danghoc.eatitt_shipper.services;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;

import etn.app.danghoc.eatitt_shipper.common.Common;

public class MyFCMServices   extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Map<String,String> dataRecv=remoteMessage.getData();
        if(dataRecv!=null)
        {
            Common.showNotifiCation(this,new Random().nextInt(),
                    dataRecv.get(Common.NOTI_TITILE),
                    dataRecv.get(Common.NOTI_CONTENT),
                    null);
        }
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Common.updateToken(this,s,false,true); /// dang la shipper app nen isShipper =true

    }
}
