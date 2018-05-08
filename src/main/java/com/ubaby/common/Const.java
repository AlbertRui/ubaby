package com.ubaby.common;

/**
 * @author AlbertRui
 * @date 2018-05-04 23:26
 */
public class Const {

    public static final String CURRENT_USER = "currentUser";

    public static final String USERNAME = "username";

    public static final String EMAIL = "email";

    public interface Role {
        int ROLE_CUSTOMER = 0; //普通用户
        int ROLE_ADMIN = 1;//管理员
    }

    public enum ProductStatusEnum {

        ON_SALE(1, "在线");

        private String value;
        private int code;

        ProductStatusEnum(int code, String value) {
            this.value = value;
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }

    }

}
