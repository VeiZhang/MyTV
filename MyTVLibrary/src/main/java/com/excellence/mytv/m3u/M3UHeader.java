package com.excellence.mytv.m3u;

/**
 * This class describes a general m3u file head.
 *
 * @author Ke
 */
public class M3UHeader {

    /**
     * The human readable playlist name.
     */
    private String mName;

    /**
     * The default for playlist media type.
     */
    private String mType;

    /**
     * The default for playlist DLNA profile.
     */
    private String mDLNAExtras;

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

    public String getDLNAExtras() {
        return mDLNAExtras;
    }

    public void setDLNAExtras(String DLNAExtras) {
        mDLNAExtras = DLNAExtras;
    }

    @Override
    public String toString() {
        return "M3UHeader{" +
                "mName='" + mName + '\'' +
                ", mType='" + mType + '\'' +
                ", mDLNAExtras='" + mDLNAExtras + '\'' +
                '}';
    }
}
