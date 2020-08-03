package chat.wewe.android.widget.message;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v13.view.inputmethod.InputContentInfoCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import chat.wewe.android.widget.AbsoluteUrl;
import chat.wewe.android.widget.R;
import chat.wewe.android.widget.helper.DebouncingOnClickListener;
import chat.wewe.android.widget.helper.FrescoHelper;
import chat.wewe.core.models.Attachment;
import chat.wewe.core.models.AttachmentTitle;
import chat.wewe.core.models.Message;

public class MessageFormLayout extends LinearLayout {

  protected ViewGroup composer;

  private ImageButton attachButton;
  private ImageButton sendButton;

  private RelativeLayout replyBar;
  private ImageView replyCancelButton;
  private SimpleDraweeView replyThumb;
  private TextView replyMessageText;
  private TextView replyUsernameText;
  private CheckBox checkbox;
  private  BlocingUsers blocingUsers;
  private Button buttonBlackList;
  private ConstraintLayout keyboard_container;

  private ExtraActionSelectionClickListener extraActionSelectionClickListener;
  private SubmitTextListener submitTextListener;
  private ImageKeyboardEditText.OnCommitContentListener listener;

  public MessageFormLayout(Context context) {
    super(context);
    init();
  }

  public MessageFormLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public MessageFormLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public MessageFormLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    init();
  }

  private void init() {
    composer = (ViewGroup) LayoutInflater.from(getContext())
        .inflate(R.layout.message_composer, this, false);

    attachButton = composer.findViewById(R.id.button_attach);

    attachButton.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View view) {
        onExtraActionSelectionClick();
      }
    });

    replyCancelButton = composer.findViewById(R.id.reply_cancel);
    replyMessageText = composer.findViewById(R.id.reply_message);
    replyUsernameText = composer.findViewById(R.id.reply_username);
    replyThumb = composer.findViewById(R.id.reply_thumb);
    replyBar = composer.findViewById(R.id.reply_bar);
    checkbox = composer.findViewById(R.id.checkbox2);
    sendButton = composer.findViewById(R.id.button_send);

    keyboard_container = composer.findViewById(R.id.keyboard_container);

    buttonBlackList = composer.findViewById(R.id.buttonBlackList);

    sendButton.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View v) {
        String messageText = getText();

        if (messageText.length() > 0 && submitTextListener != null) {
          if(checkbox.isChecked())
            submitTextListener.onSubmitText(messageText+"◫");
          else
            submitTextListener.onSubmitText(messageText);
          clearReplyContent();
        }
      }
    });

    buttonBlackList.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            switch (which){
              case DialogInterface.BUTTON_POSITIVE:
                blocingUsers.onSubmitText();
                keyboard_container.setVisibility(VISIBLE);
                buttonBlackList.setVisibility(GONE);
                break;

              case DialogInterface.BUTTON_NEGATIVE:
                //No button clicked
                break;
            }
          }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Внимание").setMessage("Розблокировать пользователя?").setPositiveButton("Да", dialogClickListener)
                .setNegativeButton("Нет", dialogClickListener).show();

      }
    });

    buttonBlackList.setVisibility(GONE);

    sendButton.setScaleX(0);
    sendButton.setScaleY(0);
    sendButton.setVisibility(GONE);
    ImageKeyboardEditText editText = composer.findViewById(R.id.editor);

    editText.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
      }

      @Override
      public void afterTextChanged(Editable s) {
        if (TextUtils.getTrimmedLength(s) > 0) {
          animateHide(attachButton);
          animateShow(sendButton);
        } else {
          animateShow(attachButton);
          animateHide(sendButton);
        }
      }
    });

    editText.setContentListener(new ImageKeyboardEditText.OnCommitContentListener() {
      @Override
      public boolean onCommitContent(InputContentInfoCompat inputContentInfo, int flags,
                                     Bundle opts, String[] supportedMimeTypes) {
        if (listener != null) {
          return listener.onCommitContent(inputContentInfo, flags, opts, supportedMimeTypes);
        }
        return false;
      }
    });

    addView(composer);
  }

  public void clearReplyContent() {
    replyBar.setVisibility(View.GONE);
    replyThumb.setVisibility(View.GONE);
    replyMessageText.setText("");
    replyUsernameText.setText("");
  }
  public void showReplyThumb() {
    replyThumb.setVisibility(View.VISIBLE);
  }

  public void setReplyCancelListener(OnClickListener onClickListener) {
    replyCancelButton.setOnClickListener(onClickListener);
  }

  public EditText getEditText() {
    return (EditText) composer.findViewById(R.id.editor);
  }

  public void setExtraActionSelectionClickListener(
      ExtraActionSelectionClickListener extraActionSelectionClickListener) {
    this.extraActionSelectionClickListener = extraActionSelectionClickListener;
  }

  public void setSubmitTextListener(SubmitTextListener submitTextListener) {
    this.submitTextListener = submitTextListener;
  }

  private void onExtraActionSelectionClick() {
    if (extraActionSelectionClickListener != null) {
      extraActionSelectionClickListener.onClick();
    }
  }

  private EditText getEditor() {
    return (EditText) composer.findViewById(R.id.editor);
  }

  public final String getText() {
    return getEditor().getText().toString().trim();
  }

  public final void setText(final CharSequence text) {
    final EditText editor = getEditor();
    editor.post(new Runnable() {
      @Override
      public void run() {
        editor.setText(text);
        if (text.length() > 0) {
          editor.setSelection(text.length());

          requestFocusAndShowKeyboard();
        }
      }
    });
  }

  public void setEnabled(boolean enabled) {
    sendButton.setEnabled(enabled);
    attachButton.setEnabled(enabled);
  }

  public void setEditTextCommitContentListener(
      ImageKeyboardEditText.OnCommitContentListener listener) {
    this.listener = listener;
  }

  public void setReplyContent(@NonNull AbsoluteUrl absoluteUrl, @NonNull Message message) {
    String text = message.getMessage();
    replyUsernameText.setText(message.getUser().getUsername());
    if (!TextUtils.isEmpty(text)) {
      replyMessageText.setText(text);
    } else {
      if (message.getAttachments() != null && message.getAttachments().size() > 0) {
        Attachment attachment = message.getAttachments().get(0);
        AttachmentTitle attachmentTitle = attachment.getAttachmentTitle();
        String imageUrl = null;
        if (attachment.getImageUrl() != null) {
          imageUrl = absoluteUrl.from(attachment.getImageUrl());
        }
        if (attachmentTitle != null) {
          text = attachmentTitle.getTitle();
        }
        if (TextUtils.isEmpty(text)) {
          text = "Unknown";
        }
        if (imageUrl != null) {
          FrescoHelper.INSTANCE.loadImageWithCustomization(replyThumb, imageUrl);
          showReplyThumb();
        }
        replyMessageText.setText(text);
      }
    }
    replyBar.setVisibility(View.VISIBLE);
    requestFocusAndShowKeyboard();
  }

  public void hideKeyboard() {
    final EditText editor = getEditor();
    editor.post(new Runnable() {
      @Override
      public void run() {
        InputMethodManager inputMethodManager = (InputMethodManager) editor.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(editor.getWindowToken(), 0);
      }
    });
  }

  private void requestFocusAndShowKeyboard() {
    final EditText editor = getEditor();
    editor.post(new Runnable() {
      @Override
      public void run() {
        InputMethodManager inputMethodManager = (InputMethodManager) editor.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        editor.requestFocus();
        inputMethodManager.showSoftInput(editor, 0);
      }
    });
  }

  private void animateHide(final View view) {
    view.animate().scaleX(0).scaleY(0).setDuration(150).withEndAction(new Runnable() {
      @Override
      public void run() {
        view.setVisibility(GONE);
      }
    });
  }

  private void animateShow(final View view) {
    view.animate().scaleX(1).scaleY(1).setDuration(150).withStartAction(new Runnable() {
      @Override
      public void run() {
        view.setVisibility(VISIBLE);
      }
    });
  }

  public void setBlocingUsers(BlocingUsers blocingUsers) {
    this.blocingUsers = blocingUsers;
  }



  public interface ExtraActionSelectionClickListener {
    void onClick();
  }

  public interface SubmitTextListener {
    void onSubmitText(String message);
  }

  public interface BlocingUsers {
    void onSubmitText();
  }

  public void setBlocing(boolean enabled) {


    if(enabled==true){
      buttonBlackList.setVisibility(VISIBLE);
      keyboard_container.setVisibility(GONE);}
    else {
      keyboard_container.setVisibility(VISIBLE);
      buttonBlackList.setVisibility(GONE);
    }
  }
}
