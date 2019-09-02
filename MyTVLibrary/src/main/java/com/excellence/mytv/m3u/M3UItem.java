package com.excellence.mytv.m3u;

/**
 * This class describes a general m3u item.
 *
 * @author Ke
 */
public class M3UItem {

    /**
     * 格式说明：
     * https://github.com/dholroyd/m3u8parser
     * https://tools.ietf.org/html/draft-pantos-http-live-streaming-02#page-4
     *
     *
     * #EXTM3U
     * #EXTINF:-1 tvg-id="Ned1.nl" tvg-name="||NL|| NPO 1 HD" tvg-logo="http://tv.trexiptv.com:8000/picons/logos/npo1hd.png" group-title="NEDERLAND HD",||NL|| NPO 1 HD
     * http://line.protv.cc:8000/JNbDvoT2eT/yY7KS0F8t4/976
     * #EXTINF:-1,||NL|| SBS 6 HD
     * http://line.protv.cc:8000/JNbDvoT2eT/yY7KS0F8t4/961
     */

    /**
     * The channel id.
     */
    private String mId;

    /**
     * The channel name, maybe empty, use {@link #mTitle}.
     */
    private String mName;

    /**
     * The stream duration time, it's unit is second.
     */
    private String mDuration;

    /**
     * The stream url.
     */
    private String mUrl;

    /**
     * The url to the logo icon.
     */
    private String mLogo;

    /**
     * The group name.
     */
    private String mGroupTitle;

    /**
     * the title -> the name
     */
    private String mTitle;

    /**
     * The media type. It can be one of the following types: avi, asf, wmv, mp4,
     * mpeg, mpeg1, mpeg2, ts, mp2t, mp2p, mov, mkv, 3gp, flv, aac, ac3, mp3,
     * ogg, wma.
     */
    private String mType;

    /**
     * The DLNA profile. It can be set as none, mpeg_ps_pal, mpeg_ps_pal_ac3,
     * mpeg_ps_ntsc, mpeg_ps_ntsc_ac3, mpeg1, mpeg_ts_sd, mpeg_ts_hd, avchd,
     * wmv_med_base, wmv_med_full, wmv_med_pro, wmv_high_full, wmv_high_pro,
     * asf_mpeg4_sp, asf_mpeg4_asp_l4, asf_mpeg4_asp_l5, asf_vc1_l1,
     * mp4_avc_sd_mp3, mp4_avc_sd_ac3, mp4_avc_hd_ac3, mp4_avc_sd_aac,
     * mpeg_ts_hd_mp3, mpeg_ts_hd_ac3, mpeg_ts_mpeg4_asp_mp3,
     * mpeg_ts_mpeg4_asp_ac3, avi, divx5, mp3, ac3, wma_base, wma_full, wma_pro.
     */
    private String mDLNAExtras;

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getDuration() {
        return mDuration;
    }

    public void setDuration(String duration) {
        mDuration = duration;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public String getLogo() {
        return mLogo;
    }

    public void setLogo(String logo) {
        mLogo = logo;
    }

    public String getGroupTitle() {
        return mGroupTitle;
    }

    public void setGroupTitle(String groupTitle) {
        mGroupTitle = groupTitle;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
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
        StringBuilder sb = new StringBuilder();
        sb.append("[Item]");
        if (mName != null) {
            sb.append("\nName: " + mName);
        }
        sb.append("\nDuration: " + mDuration);
        if (mUrl != null) {
            sb.append("\nURL: " + mUrl);
        }
        if (mGroupTitle != null) {
            sb.append("\nGroup: " + mGroupTitle);
        }
        if (mTitle != null) {
            sb.append("\nTitle: " + mTitle);
        }
        if (mLogo != null) {
            sb.append("\nLogo: " + mLogo);
        }
        if (mType != null) {
            sb.append("\nType: " + mType);
        }
        if (mDLNAExtras != null) {
            sb.append("\nDLNA Extras: " + mDLNAExtras);
        }
        return sb.toString();
    }
}
