package chat.wewe.android.adapter;

public class AdapterTaskList {

    private String mImageNames;
    private String mPosition;
    private String mCreatedBy;
    private String mData;
    private int mNumberId;
    private Boolean mClosed;
    private String mRid;
    private String mPriority;

    public AdapterTaskList(String text1, String text2, String text3, String text4,int set,Boolean close, String rid,String priority) {
        mImageNames = text1;
        mPosition = text2;
        mCreatedBy = text3;
        mData = text4;
        mNumberId = set;
        mClosed = close;
        mRid = rid;
        mPriority = priority;
    }



    public String mImageNames() {
        return mImageNames;
    }
    public String mPosition() {
        return mPosition;
    }
    public String mCreatedBy() {
        return mCreatedBy;
    }
    public String mData() {
        return mData;
    }
    public int mNumberId() {
        return mNumberId;
    }
    public Boolean mClosed() {
        return mClosed;
    }
    public String mRid() {
        return mRid;
    }
    public String mPriority() {
        return mPriority;
    }
}