package chat.wewe.persistence.realm.helpers;

import bolts.Continuation;
import bolts.Task;
import chat.wewe.android.log.RCLog;

/**
 * Bolts-Task continuation for just logging if error occurred.
 */
public class LogcatIfError implements Continuation {
  @Override
  public Object then(Task task) throws Exception {
    if (task.isFaulted()) {
      RCLog.w(task.getError());
    }
    return task;
  }
}
