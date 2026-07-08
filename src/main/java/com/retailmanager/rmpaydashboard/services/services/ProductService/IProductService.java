package com.retailmanager.rmpaydashboard.services.services.ProductService;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import com.retailmanager.rmpaydashboard.services.DTO.InventoryReceiptDTO;
import com.retailmanager.rmpaydashboard.services.DTO.ProductDTO;


public interface IProductService {
    /**
     * Persiste u nuevo producto
     * @param prmProduct Nuevo producto
     * @return Objeto del producto persistido
     */
    public ResponseEntity<?> save(ProductDTO prmProduct);
    /**
     * Persiste una lista de Productos Pertenecientes a una cadena 
     * @param listProducts Lista de nuevos productos
     * @return Lista de productos persistidos
     */
    public ResponseEntity<?> save(List<ProductDTO> listProducts);
    /**
     * Actualiza la información de un producto existente
     * @param prmProduct Objeto del producto
     * @return Objeto del producto actualizado
     */
    public ResponseEntity<?> update(ProductDTO prmProduct);
    /**
     * Elimina el producto especificado.
     * @param productCode Codigo del producto
     * @return True si es exitoso. False de lo contrario.
     */
    public ResponseEntity<?> delete(Long productId);
    /**
     * Consulta un producto especificado
     * @param productCode Codigo del producto
     * @return Objeto del producto consultado
     */
    public ResponseEntity<?> findById(Long productId);
    public ResponseEntity<?> updateEnable(Long productId, boolean enable);
    public ResponseEntity<?> findByCategory(Long categoryId,Pageable pageable);
    public ResponseEntity<?> findByCategory(List<Long> categoryIds,Pageable pageable);
    public ResponseEntity<?> findAllAndFilter(Long businessId,String filter,Pageable pageable);
    public ResponseEntity<?> findAllByBusinessId(Long businessId,Pageable pageable);
    public ResponseEntity<?> findAllByBusinessId(Long businessId);
    public ResponseEntity<?> findAllByBusinessIdCSV(Long businessId);
    public ResponseEntity<?> getQuantity(Long productId);

    public ResponseEntity<?> receiveInventory(InventoryReceiptDTO prmInventoryReceipt);
    public ResponseEntity<?> getInventoryHistory(Long businessId);

    public Long generateUniqueReceiptId();
}
