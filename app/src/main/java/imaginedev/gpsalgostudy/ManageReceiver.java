package imaginedev.gpsalgostudy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ManageReceiver extends BroadcastReceiver {
    public ManageReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("imaginedev.gpsalgostudy.STOP")) ;
        {
            context.stopService(new Intent(context, WorkerService.class));
        }
    }
}
