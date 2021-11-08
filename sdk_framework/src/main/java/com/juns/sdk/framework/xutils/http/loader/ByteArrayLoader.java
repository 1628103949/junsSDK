package com.juns.sdk.framework.xutils.http.loader;

import com.juns.sdk.framework.xutils.cache.DiskCacheEntity;
import com.juns.sdk.framework.xutils.common.util.IOUtil;
import com.juns.sdk.framework.xutils.http.request.UriRequest;

import java.io.InputStream;

class ByteArrayLoader extends Loader<byte[]> {

    @Override
    public Loader<byte[]> newInstance() {
        return new ByteArrayLoader();
    }

    @Override
    public byte[] load(final InputStream in) throws Throwable {
        return IOUtil.readBytes(in);
    }

    @Override
    public byte[] load(final UriRequest request) throws Throwable {
        request.sendRequest();
        return this.load(request.getInputStream());
    }

    @Override
    public byte[] loadFromCache(final DiskCacheEntity cacheEntity) throws Throwable {
        return null;
    }

    @Override
    public void save2Cache(final UriRequest request) {
    }
}
