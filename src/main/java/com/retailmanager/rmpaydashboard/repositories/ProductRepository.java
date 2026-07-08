package com.retailmanager.rmpaydashboard.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.retailmanager.rmpaydashboard.models.Category;
import com.retailmanager.rmpaydashboard.models.Product;



public interface ProductRepository extends CrudRepository<Product,Long>,PagingAndSortingRepository<Product,Long> {
   
    public Page<Product> findByCategoryIs(Category category,Pageable pageable);
    @Query("SELECT p FROM Product p WHERE p.category.categoryId IN :categoryIds")
    public Page<Product> findByCategoryIn(List<Long> categoryIds,Pageable pageable);
    /**
     * Recupera la información de un producto por codigo de producto o codigo de barras
     * @param productCode Codigo del producto
     * @param barCode Codigo de barras
     * @return Opcional del producto
     */
    public Optional<Product> findOneByCodeOrBarcode(String productCode, String barCode);
    
    /**
     * Obtiene los productos por nombre
     * @param name Nombre del cual se quieren recupear los productos
     * @param pageable Objeto de paginación
     * @return
     */
    public Page<Product> findByName(String name, Pageable pageable);
    
    /**
     * Updates the enable status of a product.
     *
     * @param  productId  the ID of the product to update
     * @param  enable     the new enable status
     * @return            void
     */
    @Modifying
    @Query("UPDATE Product u SET u.enable = :enable WHERE u.productId = :productId")
    void updateEnable(Long productId, boolean enable);

    /**
     * Finds products by filter.
     *
     * @param  businessId  the business id to filter products
     * @param  filter      the filter string to search products
     * @param  pageable    the pageable object for pagination and sorting
     * @return             a page of products matching the filter
     */
    @Query(value = "select p from Product p where p.category.business.businessId=:businessId and (p.code like :filter or p.barcode like :filter or p.name like :filter or p.description like :filter or p.category.name like :filter )")
    public Page<Product> findProductsByFilter(Long businessId,String filter,Pageable pageable);
    /**
     * Find products by business ID and return a page of results.
     *
     * @param  businessId  the ID of the business to search for
     * @param  pageable    the pagination information
     * @return             a page of products matching the given business ID
     */
    @Query(value = "select p from Product p where p.category.business.businessId=:businessId order by p.name")
    public Page<Product> findProductsByBusinessId(Long businessId,Pageable pageable);
    
    @Query(value = "select p from Product p where p.category.business.businessId=:businessId order by p.name")
    public List<Product> findProductsByBusinessId(Long businessId);

    @Query(value = "select p from Product p where p.category.business.businessId=:businessId and p.quantity < p.minimumLevel")  
    public List<Product> getLowInventory(Long businessId);

    @Modifying
    @Query("UPDATE Product u SET u.quantity = u.quantity - :quantity WHERE u.productId = :productId")
    public int reduceInventory(Long productId, int quantity);
    
}
