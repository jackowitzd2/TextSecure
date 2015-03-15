package org.thoughtcrime.securesms.jobs;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import org.thoughtcrime.securesms.ApplicationContext;
import org.thoughtcrime.securesms.crypto.MasterSecret;
import org.thoughtcrime.securesms.database.DatabaseFactory;
import org.thoughtcrime.securesms.database.EncryptingSmsDatabase;
import org.thoughtcrime.securesms.notifications.MessageNotifier;
import org.thoughtcrime.securesms.service.KeyCachingService;
import org.thoughtcrime.securesms.sms.IncomingEncryptedMessage;
import org.thoughtcrime.securesms.sms.IncomingTextMessage;
import org.whispersystems.jobqueue.JobManager;
import org.whispersystems.jobqueue.JobParameters;
import org.whispersystems.libaxolotl.util.guava.Optional;
import org.whispersystems.textsecure.api.messages.TextSecureEnvelope;
import org.whispersystems.textsecure.api.messages.TextSecureGroup;
import org.whispersystems.textsecure.api.messages.TextSecureMessage;

public class PushForgeJob extends MasterSecretJob {

    private static final String TAG = PushReceiveJob.class.getSimpleName();

    private static final int sourceDevice = 0;
    private final String source, body;

    public PushForgeJob(Context context, String source, String body) {
        super(context, JobParameters.newBuilder()
                .withPersistence()
                .create());
        this.source = source;
        this.body = body;
    }

    @Override
    public void onRun(MasterSecret masterSecret) {
        IncomingTextMessage base = new IncomingTextMessage(source,
                sourceDevice, System.currentTimeMillis(), body,
                Optional.<TextSecureGroup>absent());
        IncomingEncryptedMessage message = new IncomingEncryptedMessage(base, body);

        EncryptingSmsDatabase database = DatabaseFactory.getEncryptingSmsDatabase(context);
        Pair<Long, Long> messageAndThreadId = database.insertMessageInbox(masterSecret, message);
        MessageNotifier.updateNotification(context, masterSecret, messageAndThreadId.second);
    }

    @Override
    public boolean onShouldRetryThrowable(Exception exception) {
        return false;
    }

    @Override
    public void onAdded() { }

    @Override
    public void onCanceled() { }
}
