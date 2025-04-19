package com.lb.im.platform.group.appliication.cache;

import com.lb.im.platform.group.domain.event.IMGroupEvent;

public interface GroupCacheService {

    /**
     * 更新群组缓存
     */
    void updateGroupCache(IMGroupEvent imGroupEvent);
}
