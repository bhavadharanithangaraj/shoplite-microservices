package com.shoplite.inventory.repository;

import com.shoplite.inventory.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // Checks AND reduces stock in a single SQL statement, so two orders arriving at the
    // same moment can never both take the last item. Returns how many rows changed:
    // 1 = success, 0 = not enough stock (or no such product).
    // clearAutomatically: forget cached Product objects afterwards, so the next read sees the new stock
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Product p SET p.stock = p.stock - :qty WHERE p.id = :id AND p.stock >= :qty")
    int reduceStock(@Param("id") Long id, @Param("qty") int qty);
}
