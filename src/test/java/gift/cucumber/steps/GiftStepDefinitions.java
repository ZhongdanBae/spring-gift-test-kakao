package gift.cucumber.steps;

import gift.model.*;
import io.cucumber.java.Before;
import io.cucumber.java.ko.그러면;
import io.cucumber.java.ko.만일;
import io.cucumber.java.ko.조건;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class GiftStepDefinitions {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long senderId;
    private Long receiverId;
    private Long currentOptionId;
    private Response lastResponse;

    @Before
    public void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 28080;
        // 새 엔티티 추가 시 이 목록도 업데이트 필요 (JPA 엔티티: Wish, Option, Product, Category, Member)
        jdbcTemplate.execute(
                "TRUNCATE TABLE wish, option, product, category, member RESTART IDENTITY CASCADE"
        );
    }

    @조건("회원 {string}이 존재한다")
    public void 회원이_존재한다(String name) {
        String email = name + "@test.com";
        jdbcTemplate.update("INSERT INTO member (name, email) VALUES (?, ?)", name, email);
        Long id = jdbcTemplate.queryForObject(
                "SELECT id FROM member WHERE email = ?", Long.class, email);
        if ("보내는사람".equals(name)) {
            senderId = id;
        } else {
            receiverId = id;
        }
    }

    @조건("재고가 {int}인 옵션이 존재한다")
    public void 재고가_n인_옵션이_존재한다(int stock) {
        jdbcTemplate.update("INSERT INTO category (name) VALUES (?)", "테스트 카테고리");
        Long categoryId = jdbcTemplate.queryForObject(
                "SELECT id FROM category WHERE name = ?", Long.class, "테스트 카테고리");
        jdbcTemplate.update(
                "INSERT INTO product (name, price, image_url, category_id) VALUES (?, ?, ?, ?)",
                "테스트 상품", 10000, "http://test.jpg", categoryId);
        Long productId = jdbcTemplate.queryForObject(
                "SELECT id FROM product WHERE name = ?", Long.class, "테스트 상품");
        jdbcTemplate.update(
                "INSERT INTO option (name, quantity, product_id) VALUES (?, ?, ?)",
                "테스트 옵션", stock, productId);
        currentOptionId = jdbcTemplate.queryForObject(
                "SELECT id FROM option WHERE product_id = ?", Long.class, productId);
    }

    @만일("{int}개를 선물한다")
    public void n개를_선물한다(int quantity) {
        lastResponse = given()
                .contentType("application/json")
                .header("Member-Id", String.valueOf(senderId))
                .body("""
                        {
                            "optionId": %d,
                            "quantity": %d,
                            "receiverId": %d,
                            "message": "선물입니다"
                        }
                        """.formatted(currentOptionId, quantity, receiverId))
                .when()
                .post("/api/gifts");
    }

    @만일("옵션ID {long}로 {int}개를 선물한다")
    public void 옵션ID로_n개를_선물한다(long optionId, int quantity) {
        lastResponse = given()
                .contentType("application/json")
                .header("Member-Id", String.valueOf(senderId))
                .body("""
                        {
                            "optionId": %d,
                            "quantity": %d,
                            "receiverId": %d,
                            "message": "선물입니다"
                        }
                        """.formatted(optionId, quantity, receiverId))
                .when()
                .post("/api/gifts");
    }

    @그러면("응답 상태코드는 {int}이다")
    public void 응답_상태코드는_n이다(int statusCode) {
        assertThat(lastResponse.statusCode()).isEqualTo(statusCode);
    }

    @그러면("재고는 {int}이다")
    public void 재고는_n이다(int expectedStock) {
        // JdbcTemplate으로 직접 조회 (Hibernate 캐시 우회)
        Integer actual = jdbcTemplate.queryForObject(
                "SELECT quantity FROM option WHERE id = ?", Integer.class, currentOptionId);
        assertThat(actual).isEqualTo(expectedStock);
    }
}
