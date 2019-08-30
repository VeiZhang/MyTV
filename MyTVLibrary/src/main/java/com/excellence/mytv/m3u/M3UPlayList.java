package com.excellence.mytv.m3u;

import java.util.List;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2019/8/30
 *     desc   :
 * </pre> 
 */
public class M3UPlayList {

    private M3UHeader mHeader;
    private List<M3UItem> mItems;

    public M3UHeader getHeader() {
        return mHeader;
    }

    public void setHeader(M3UHeader header) {
        mHeader = header;
    }

    public List<M3UItem> getItems() {
        return mItems;
    }

    public void setItems(List<M3UItem> items) {
        mItems = items;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (mHeader != null) {
            sb.append(mHeader.toString());
        } else {
            sb.append("No header");
        }
        sb.append('\n');
        for (M3UItem item : mItems) {
            sb.append(item.toString());
            sb.append('\n');
        }
        return sb.toString();
    }
}
