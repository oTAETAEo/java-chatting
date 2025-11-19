package woowacourse.chatting.util;

import java.util.UUID;

public class UUIDUtil {

    private UUIDUtil() {
    } // 인스턴스화 방지

    public static UUID toUUID(String uuidString) {
        try {
            return UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("잘못된 UUID 형식입니다: " + uuidString);
        }
    }
}