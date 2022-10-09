# Spring Data JPA $N + 1$ Problem

### Tags
<p align="left">
    <img alt="Spring Data JPA" src="https://img.shields.io/badge/Spring%20Data%20JPA-blue">
    <img alt="N + 1 Problem" src="https://img.shields.io/badge/N+1%20Problem-blue">
</p>


## What is $N + 1$ Problem

在 Systems design 中，TABLE 之間「一對多」、「多對多」的關係十分常見，常常會有需要同時抓取多張 TABLE 的需求。

例如在「商品類別」與「商品」之間「一對多」的情境下，若要同時抓取 $5$ 筆商品類別及各類別中的所有商品。如果是直接下 SQL，我們可以很直覺地使用 1 條 `JOIN` statement 就達成需求。但是在 ORM 框架中，框架可能會幫我們產生並執行 6 次 query statements！也就是 1 條 SQL 可以解決的問題，ORM 卻需要 6 條 SQL，這勢必會帶來效能問題！

而本文將會模擬 Spring Data JPA 的 $N + 1$ Problem 並示範 Solution！

## Demo

Demo 將模擬 `product_type` 與 `product` 兩張 TABLE `@OneToMany` `@ManyToOne` 的 Bidirectional Association

- Project: `Maven Project`
- Spring Boot version: `2.7.4`
- Java version: `11`
- Dependencies:
    - `Spring Web`
    - `Spring Data JPA`
    - `Lombok`
    - `H2 Database`
- Configuration
    - application.yml
        
        ```yaml
        spring: 
            datasource: 
                driver-class-name: org.h2.Driver # 使用 H2 資料庫，並將 Data 設定在 memory
                url: jdbc:h2:mem:testdb # 將 Data 設定為 In-memory，並指定 schema 為 testdb
                username: sa # 設定 username
                password: sa # 設定 password
            h2:
                console:
                    enabled: true     # 啟用 H2 的 console
                    path: /h2-console # 可通過 http://localhost:8080/h2-console 開啟 console
            jpa: 
                show-sql: true # 印出 JPA 產生的 SQL
                hibernate: 
                    ddl-auto: update # 讓 Hibernate 根據 javax.persistence 相關的 Annotations 幫我們自動建立、更新 TABLE
                properties:
                    hibernate:
                        '[generate_statistics]': true # 啟用 Hibernate 的 statistics 功能
        ```
        

### Entities

- ProductType.java
    
    ```java
    @Getter
    @Setter
    @Table
    @Entity
    public class ProductType {
    
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "product_type_id")
        private Long productTypeId;
    
        @Column(name = "product_type_name")
        private String productTypeName;
    
        @OneToMany(
            mappedBy = "productType", 
            fetch = FetchType.EAGER, 
            cascade = CascadeType.PERSIST
        )
        private List<Product> products;
    }
    ```
    
- Product.java
    
    ```java
    @Getter
    @Setter
    @Table
    @Entity
    public class Product {
    
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "product_id")
        private Long productId;
    
        @Column(name = "product_name")
        private String productName;
    
        @ManyToOne
        @JoinColumn(name = "product_type_id")
        private ProductType productType;
        
    }
    ```
    

### Repository

- ProductTypeRepository.java
    
    ```java
    @Repository
    public interface ProductTypeRepository extends JpaRepository<ProductType, Long> {}
    ```
    
- ProductRepository.java
    
    ```java
    @Repository
    public interface ProductRepository extends JpaRepository<Product, Long> {}
    ```
    

### Controller

- TestController.java
    
    ```java
    @Slf4j
    @RestController
    public class TestController {
    
        @Autowired
        private ProductRepository productRepository;
    
        @Autowired
        private ProductTypeRepository productTypeRepository;
        
        /** 新增 5 筆商品類別，並在每筆商品類別中各新增一筆商品 */
        @GetMapping("/insert")
        public String insertProductTypesAndProudcts() {
            for (int i = 0; i < 5; i++) {
    
                // create Product
                List<Product> products = new LinkedList<>();
                Product product = new Product();
                product.setProductName("product" + i);
                products.add(product);
    
                // create ProductType
                ProductType productType = new ProductType();
                productType.setProductTypeName("productType" + i);
                productType.setProducts(products);
                productType = productTypeRepository.save(productType);
    
                // update product
                product.setProductType(productType);
                productRepository.save(product);
            }
            return "ok";
        }
    
        /** 模擬 N + 1 Problem */
        @GetMapping(value="/test")
        public String test() {
            productTypeRepository.findAll().forEach(
                productType -> {
                    log.info(productType.getProductTypeName());
                }
            );
            return "ok";
        }
        
    }
    ```
    > Note: 這裡的 Controller 並不會遵循 RESTful 的規範，因為這並不是本文的重點
    

### Test

1. 啟動 application
    
    ```bash
    ./mvnw spring-boot:run
    ```
    
2. 訪問 URL: [`http://localhost:8080/insert`](http://localhost:8080/insert) 
    - 新增 5 筆商品類別，並在每筆商品類別中各新增一筆商品
    - 可以開啟 [H2 console](http://localhost:8080/h2-console) 確認是否有成功新增 5 筆 `PRODUCT_TYPE` 及 `PRODUCT`
        - Driver Class: `org.h2.Driver`
        - JDBC URL: `jdbc:h2:mem:testdb`
        - User Name: `sa`
        - Password: `sa`
3. 訪問 URL: [`http://localhost:8080/test`](http://localhost:8080/test)
    - 模擬 $N+1$ Problem 。其中 $N$ 代表「一方(ProductType)的筆數」
    - 因為目前總共有 `5` 筆商品類別，故共會執行 `5 + 1` 次 query statements
    - 印出之 Log 如下:
    
    ```sql
    Hibernate: select producttyp0_.product_type_id as product_1_1_, producttyp0_.product_type_name as product_2_1_ from product_type producttyp0_
    Hibernate: select products0_.product_type_id as product_3_0_0_, products0_.product_id as product_1_0_0_, products0_.product_id as product_1_0_1_, products0_.product_name as product_2_0_1_, products0_.product_type_id as product_3_0_1_ from product products0_ where products0_.product_type_id=?
    Hibernate: select products0_.product_type_id as product_3_0_0_, products0_.product_id as product_1_0_0_, products0_.product_id as product_1_0_1_, products0_.product_name as product_2_0_1_, products0_.product_type_id as product_3_0_1_ from product products0_ where products0_.product_type_id=?
    Hibernate: select products0_.product_type_id as product_3_0_0_, products0_.product_id as product_1_0_0_, products0_.product_id as product_1_0_1_, products0_.product_name as product_2_0_1_, products0_.product_type_id as product_3_0_1_ from product products0_ where products0_.product_type_id=?
    Hibernate: select products0_.product_type_id as product_3_0_0_, products0_.product_id as product_1_0_0_, products0_.product_id as product_1_0_1_, products0_.product_name as product_2_0_1_, products0_.product_type_id as product_3_0_1_ from product products0_ where products0_.product_type_id=?
    Hibernate: select products0_.product_type_id as product_3_0_0_, products0_.product_id as product_1_0_0_, products0_.product_id as product_1_0_1_, products0_.product_name as product_2_0_1_, products0_.product_type_id as product_3_0_1_ from product products0_ where products0_.product_type_id=?
    2022-10-10 03:29:30.580  INFO 34891 --- [nio-8080-exec-2] c.y.s.Controller.TestController          : productType0
    2022-10-10 03:29:30.580  INFO 34891 --- [nio-8080-exec-2] c.y.s.Controller.TestController          : productType1
    2022-10-10 03:29:30.580  INFO 34891 --- [nio-8080-exec-2] c.y.s.Controller.TestController          : productType2
    2022-10-10 03:29:30.580  INFO 34891 --- [nio-8080-exec-2] c.y.s.Controller.TestController          : productType3
    2022-10-10 03:29:30.580  INFO 34891 --- [nio-8080-exec-2] c.y.s.Controller.TestController          : productType4
    2022-10-10 03:29:30.581  INFO 34891 --- [nio-8080-exec-2] i.StatisticalLoggingSessionEventListener : Session Metrics {
        117250 nanoseconds spent acquiring 1 JDBC connections;
        0 nanoseconds spent releasing 0 JDBC connections;
        580416 nanoseconds spent preparing 6 JDBC statements;
        790792 nanoseconds spent executing 6 JDBC statements;
        0 nanoseconds spent executing 0 JDBC batches;
        0 nanoseconds spent performing 0 L2C puts;
        0 nanoseconds spent performing 0 L2C hits;
        0 nanoseconds spent performing 0 L2C misses;
        0 nanoseconds spent executing 0 flushes (flushing a total of 0 entities and 0 collections);
        12708 nanoseconds spent executing 1 partial-flushes (flushing a total of 0 entities and 0 collections)
    }
    ```