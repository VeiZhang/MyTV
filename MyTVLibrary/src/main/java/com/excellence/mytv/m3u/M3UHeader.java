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

    /**
     * The default for playlist media plugin (handler).
     */
    private String mPlugin;

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

    public String getPlugin() {
        return mPlugin;
    }

    public void setPlugin(String plugin) {
        mPlugin = plugin;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[Head]");
        if (mName != null) {
            sb.append("\nName: " + mName);
        }
        if (mType != null) {
            sb.append("\nType: " + mType);
        }
        if (mDLNAExtras != null) {
            sb.append("\nDLNA Extras: " + mDLNAExtras);
        }
        if (mPlugin != null) {
            sb.append("\nPlugin: " + mPlugin);
        }
        return sb.toString();
    }
}
