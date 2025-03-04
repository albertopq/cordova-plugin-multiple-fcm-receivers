package cordova.plugin.multipleFCMReceivers;
import com.google.firebase.messaging.FirebaseMessagingService;
import by.chemerisuk.cordova.firebase.FirebaseMessagingPluginService;
import com.google.firebase.messaging.RemoteMessage;
import io.intercom.android.sdk.push.IntercomPushClient;

import java.lang.reflect.Field;
import java.util.Map;


public class Receiver extends FirebaseMessagingService {

    private final IntercomPushClient intercomPushClient = new IntercomPushClient();
    private final FirebaseMessagingPluginService firebaseClient = new FirebaseMessagingPluginService();
    public Receiver() {
    }

    @Override
    public void onCreate() {
        injectContext(firebaseClient);
        firebaseClient.onCreate();
    }

    @Override
    public void onNewToken(String refreshedToken) {
        intercomPushClient.sendTokenToIntercom(getApplication(), refreshedToken);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map message = remoteMessage.getData();
        if (intercomPushClient.isIntercomPush(message)) {
            intercomPushClient.handlePush(getApplication(), message);
        } else {
            injectContext(firebaseClient);
            firebaseClient.onMessageReceived(remoteMessage);
        }
    }

    private void injectContext(FirebaseMessagingService service) {
        setField(service, "mBase", this);
    }

    private boolean setField(Object targetObject, String fieldName, Object fieldValue) {
        Field field;
        try {
            field = targetObject.getClass().getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            field = null;
        }
        Class superClass = targetObject.getClass().getSuperclass();
        while (field == null && superClass != null) {
            try {
                field = superClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                superClass = superClass.getSuperclass();
            }
        }
        if (field == null) {
            return false;
        }
        field.setAccessible(true);
        try {
            field.set(targetObject, fieldValue);
            return true;
        } catch (IllegalAccessException e) {
            return false;
        }
    }

}
