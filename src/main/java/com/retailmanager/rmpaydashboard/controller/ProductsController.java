package com.retailmanager.rmpaydashboard.controller;

import java.util.HashMap;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.retailmanager.rmpaydashboard.services.DTO.InventoryReceiptDTO;
import com.retailmanager.rmpaydashboard.services.DTO.ProductDTO;
import com.retailmanager.rmpaydashboard.services.services.ProductService.IProductService;

@RestController
@RequestMapping("/api")
@Validated
public class ProductsController {
     /*Atributo para el servicio de persistencia */
    @Autowired
    private IProductService productService;
    /**
     * Permite crear un nuevo producto
     * @param prmProduct Nuevo Producto
     * @return objeto del Producto creado
     */
    @PostMapping("/products")
    public ResponseEntity<?> save(@Valid @RequestBody ProductDTO prmProduct){
        return productService.save(prmProduct);
    }
    /**
     * Permite CREAR una lista de Productos Pertenecientes a una cadena
     * @param prmProduct Lista de nuevos productos
     * @return Lista de productos creados
     */
    @PostMapping("/products/list")
    public ResponseEntity<?> saveList( @Valid @RequestBody List<ProductDTO> listProductsDTO){
        return productService.save(listProductsDTO);
    }
    
    
    /**
     * Actualiza la información de un producto ya existente
     * @param prmProduct Objeto de Producto
     * @param productCode Identificador del producto
     * @return Objeto del producto actualizado
     */
    @PutMapping("/products/{productId}")
    public ResponseEntity<?> update(@Valid @RequestBody ProductDTO prmProduct,@PathVariable Long productId){
        prmProduct.setProductId(productId);
        return productService.update(prmProduct);
    }
    /**
     * Retorna todos los productos usando paginación con una cantidad de items por pagina por defecto de 200.
     * Además se pueden dos filtros opcionales sobre los productos. El identificador de la cadena o un filtro sobre los atributos del producto
     * @param pageable Objeto para paginación
     * @param costumerId identificador de una cadena. Opcional
     * @param filter Filtro sobre los atriutos del producto. Opcional
     * @return 
     */
    // @GetMapping("/products")
    // public ResponseEntity<?> findAll(@PageableDefault(size = 200,page = 0) Pageable pageable,@RequestParam(required=false) String costumerId,@RequestParam(required=false) String filter){
    //     return productService.findAllAndFilterCustomerId(costumerId, filter, pageable);
    // }
    /**
     * Consulta un producto por su codigo
     * @param productCode Codigo del producto
     * @return Objeto del producto
     */
    @GetMapping("/products/{productId}")
    public ResponseEntity<?> findById(@PathVariable Long productId){
        return productService.findById(productId);
    }
    /**
     * Consulta la cantidad de un producto por su ID.
     *
     * @param  productId   el ID del producto a consultar
     * @return             la cantidad del producto
     */
    @GetMapping("/products/{productId}/quantity")
    public ResponseEntity<?> getQuantity(@PathVariable Long productId){
        return productService.getQuantity(productId);
    }
    /**
     * Elimina un producto
     * @param productCode Codigo del producto
     * @return True si es exitoso. False de lo contrario
     */
    @DeleteMapping("/products/{productId}")
    public ResponseEntity<?> delete(@PathVariable Long productId){
        return productService.delete(productId);
    }
    
    /**
     * Finds products by category ID.
     *
     * @param  pageable     the pageable object for pagination
     * @param  categoryId   the ID of the category to search for
     * @return             the response entity containing the products found
     */
    @GetMapping("/products/category/{categoryId}")
    public ResponseEntity<?> findByCategory(@PageableDefault(size = 200,page = 0) Pageable pageable,@PathVariable Long categoryId){
        return productService.findByCategory(categoryId, pageable);
    }
    @PostMapping("/products/category")
    public ResponseEntity<?> findByCategory(@PageableDefault(size = 200,page = 0) Pageable pageable,@Valid @RequestBody List<Long> categoryIds){
        return productService.findByCategory(categoryIds, pageable);
    }
    @GetMapping("/products/byBusiness/{businessId}")
    public ResponseEntity<?> findByBusiness(@PageableDefault(size = 200,page = 0) Pageable pageable,@PathVariable Long businessId){
        return productService.findAllByBusinessId(businessId, pageable);
    }
    @GetMapping("/products/allByBusiness/{businessId}")
    public ResponseEntity<?> findaLLByBusiness(@PathVariable Long businessId){
        return productService.findAllByBusinessId(businessId);
    }
    @PostMapping("/products/inventory")
    public ResponseEntity<?> receiveInventory(@Valid @RequestBody InventoryReceiptDTO prmInventoryReceipt){
        return productService.receiveInventory(prmInventoryReceipt);
    }
    @GetMapping("/products/inventory/generatedId")
    public ResponseEntity<?> generatedId(){
        long generatedId = this.productService.generateUniqueReceiptId();
        HashMap<String, Long> rta = new HashMap<>();
        rta.put("generatedId", generatedId);
        return new ResponseEntity<>(rta,HttpStatus.OK);
    }@GetMapping("/products/inventory/{businessId}")
    public ResponseEntity<?> getInventoryHistory(@PathVariable Long businessId){
        return productService.getInventoryHistory(businessId);
    }
    
    
    
}

