package com.juns.sdk.framework.xutils.image;

/**
 * 使已被LruCache移除, 但还在被ImageView使用的Drawable可以再次被回收使用.
 */
interface ReusableDrawable {

    MemCacheKey getMemCacheKey();

    void setMemCacheKey(MemCacheKey key);
}
