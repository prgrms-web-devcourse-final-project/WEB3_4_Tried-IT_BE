package com.dementor.chat.service;

import com.dementor.domain.admin.entity.Admin;

public class TestAdminFactory {

    public static Admin create(Long id) {
        Admin admin = new Admin();
        admin.setId(id); // 롬복 @Setter 또는 직접 setter 추가 필요
        return admin;
    }

    public static Admin createDefault() {
        return create(1L);
    }
}
