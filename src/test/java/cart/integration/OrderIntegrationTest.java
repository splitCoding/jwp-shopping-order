package cart.integration;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import cart.dao.MemberDao;
import cart.domain.Member;
import cart.dto.request.CartItemRequest;
import cart.dto.request.OrderItemRequest;
import cart.dto.request.OrderRequest;
import cart.dto.request.ProductRequest;
import cart.dto.response.LoginResponse;
import cart.dto.response.OrderResponse;
import cart.dto.response.OrdersResponse;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.response.ResponseBodyExtractionOptions;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

public class OrderIntegrationTest extends IntegrationTest {

    @Autowired
    private MemberDao memberDao;
    private Long productId;
    private Long productId2;
    private Long productId3;
    private Member member1;
    private Member member2;
    private OrderRequest orderRequest1;
    private OrderRequest orderRequest2;

    @BeforeEach
    void setUp() {
        super.setUp();

        productId = createProduct(new ProductRequest("치킨", 10_000, "http://example.com/chicken.jpg"));
        productId2 = createProduct(new ProductRequest("피자", 15_000, "http://example.com/pizza.jpg"));
        productId3 = createProduct(new ProductRequest("셀러드", 20_000, "http://example.com/salad.jpg"));

        member1 = memberDao.getMemberByEmail("a@a.com").get();
        member2 = memberDao.getMemberByEmail("b@b.com").get();

        orderRequest1 = new OrderRequest(
            List.of(new OrderItemRequest(productId, 1), new OrderItemRequest(productId2, 1)),
            LocalDateTime.of(2023, 4, 4, 4, 4)
        );
        orderRequest2 = new OrderRequest(
            List.of(new OrderItemRequest(productId2, 1), new OrderItemRequest(productId3, 1)),
            LocalDateTime.of(2023, 4, 4, 4, 4)
        );
    }

    @DisplayName("장바구니에 담긴 상품을 주문하고 주문내역을 저장한다.")
    @Test
    public void saveOrder() {
        //given
        createCartItem(member1, new CartItemRequest(productId));
        createCartItem(member1, new CartItemRequest(productId2));

        //when
        final ExtractableResponse<Response> response = given().log().all()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .auth().oauth2(getAccessToken(member1.getEmail(), member1.getPassword()))
            .body(orderRequest1)
            .when()
            .post("/orders")
            .then().log().all()
            .extract();

        final OrderResponse orderResponse = response.body().as(OrderResponse.class);

        //then
        assertAll(
            () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value()),
            () -> assertThat(response.header("Location")).isNotNull(),
            () -> assertThat(orderResponse.getItems()).hasSize(2),
            () -> assertThat(orderResponse.getProductPrice()).isEqualTo(25_000),
            () -> assertThat(orderResponse.getDiscountPrice()).isEqualTo(0),
            () -> assertThat(orderResponse.getDeliveryFee()).isEqualTo(3000),
            () -> assertThat(orderResponse.getTotalPrice()).isEqualTo(28_000)
        );
    }

    @DisplayName("장바구니에 없는 상품을 주문시 400 상태코드를 응답한다.")
    @Test
    public void saveOrder_notInCartItem() {
        //given
        createCartItem(member1, new CartItemRequest(productId));

        //when
        final ExtractableResponse<Response> response = given().log().all()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .auth().oauth2(getAccessToken(member1.getEmail(), member1.getPassword()))
            .body(orderRequest1)
            .when()
            .post("/orders")
            .then().log().all()
            .extract();

        //then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @DisplayName("주문내역의 ID를 통해 단일 주문내역을 조회한다.")
    @Test
    public void findOrderById() {
        //given
        final Long orderId = createOrder(member1, orderRequest1);

        //when
        final ExtractableResponse<Response> response = given().log().all()
            .auth().oauth2(getAccessToken(member1.getEmail(), member1.getPassword()))
            .when()
            .get("/orders/{orderId}", orderId)
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract();

        final OrderResponse orderResponse = response.body().as(OrderResponse.class);

        //then
        assertAll(
            () -> assertThat(orderResponse.getOrderId()).isEqualTo(orderId),
            () -> assertThat(orderResponse.getItems()).hasSize(2),
            () -> assertThat(orderResponse.getProductPrice()).isEqualTo(25_000),
            () -> assertThat(orderResponse.getDiscountPrice()).isEqualTo(0),
            () -> assertThat(orderResponse.getDeliveryFee()).isEqualTo(3000),
            () -> assertThat(orderResponse.getTotalPrice()).isEqualTo(28_000)
        );
    }

    @DisplayName("다른 멤버의 주문내역을 조회시 401 상태코드를 응답한다.")
    @Test
    public void findOrderById_anotherMembersOrder() {
        //given
        final Long orderId = createOrder(member2, orderRequest1);

        //when
        //then
        RestAssured.given().log().all()
            .auth().oauth2(getAccessToken(member1.getEmail(), member1.getPassword()))
            .when()
            .get("/orders/{orderId}", orderId)
            .then().log().all()
            .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @DisplayName("멤버의 전체 주문내역을 조회한다.")
    @Test
    public void findOrders() {
        //given
        createOrder(member1, orderRequest1);
        createOrder(member1, orderRequest2);

        //when
        final ExtractableResponse<Response> response = given().log().all()
            .auth().oauth2(getAccessToken(member1.getEmail(), member1.getPassword()))
            .when()
            .get("/orders")
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract();

        final OrdersResponse ordersResponse = response.body().as(OrdersResponse.class);

        //then
        assertAll(
            () -> assertThat(ordersResponse.getOrders()).hasSize(2),

            () -> assertThat(ordersResponse.getOrders().get(0).getItems()).hasSize(2),
            () -> assertThat(ordersResponse.getOrders().get(0).getProductPrice()).isEqualTo(25_000),
            () -> assertThat(ordersResponse.getOrders().get(0).getDiscountPrice()).isEqualTo(0),
            () -> assertThat(ordersResponse.getOrders().get(0).getDeliveryFee()).isEqualTo(3000),
            () -> assertThat(ordersResponse.getOrders().get(0).getTotalPrice()).isEqualTo(28_000),

            () -> assertThat(ordersResponse.getOrders().get(1).getItems()).hasSize(2),
            () -> assertThat(ordersResponse.getOrders().get(1).getProductPrice()).isEqualTo(35_000),
            () -> assertThat(ordersResponse.getOrders().get(1).getDiscountPrice()).isEqualTo(0),
            () -> assertThat(ordersResponse.getOrders().get(1).getDeliveryFee()).isEqualTo(3000),
            () -> assertThat(ordersResponse.getOrders().get(1).getTotalPrice()).isEqualTo(38_000)
        );
    }

    private Long createProduct(ProductRequest productRequest) {
        ExtractableResponse<Response> response = given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(productRequest)
            .when()
            .post("/products")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract();

        return getIdFromCreatedResponse(response);
    }

    private Long createCartItem(Member member, CartItemRequest cartItemRequest) {
        ExtractableResponse<Response> response = given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .auth().oauth2(getAccessToken(member.getEmail(), member.getPassword()))
            .body(cartItemRequest)
            .when()
            .post("/cart-items")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract();

        return getIdFromCreatedResponse(response);
    }

    private long getIdFromCreatedResponse(ExtractableResponse<Response> response) {
        return Long.parseLong(response.header("Location").split("/")[2]);
    }

    public Long createOrder(Member member, OrderRequest orderRequest) {
        orderRequest.getOrderItems()
            .stream()
            .map(OrderItemRequest::getId)
            .forEach((productId) -> createCartItem(member, new CartItemRequest(productId)));

        final ExtractableResponse<Response> response = given().log().all()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .auth().oauth2(getAccessToken(member.getEmail(), member.getPassword()))
            .body(orderRequest)
            .when()
            .post("/orders")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract();

        return Long.parseLong(response.header("Location").split("/")[2]);
    }

    public String getAccessToken(final String email, final String password) {
        final ResponseBodyExtractionOptions body = given().log().all()
            .formParam("email", email)
            .formParam("password", password)
            .when()
            .post("/login")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .body();

        final LoginResponse loginResponse = body.as(LoginResponse.class);
        return loginResponse.getAccessToken();
    }
}
