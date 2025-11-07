package woowacourse.chatting.jwt;

public enum JwtRole {

    GRANT_TYPE("Bearer")
    ;

    private String role;

    JwtRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}
