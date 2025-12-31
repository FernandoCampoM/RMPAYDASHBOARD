package com.retailmanager.rmpaydashboard.services.services.ProductService;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadNoExisteException;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadYaExisteException;
import com.retailmanager.rmpaydashboard.models.Business;
import com.retailmanager.rmpaydashboard.models.Category;
import com.retailmanager.rmpaydashboard.models.InventoryReceipt;
import com.retailmanager.rmpaydashboard.models.Product;
import com.retailmanager.rmpaydashboard.models.UserBusiness_Category;
import com.retailmanager.rmpaydashboard.models.UserBusiness_Product;
import com.retailmanager.rmpaydashboard.models.UsersBusiness;
import com.retailmanager.rmpaydashboard.repositories.BusinessRepository;
import com.retailmanager.rmpaydashboard.repositories.CategoryRepository;
import com.retailmanager.rmpaydashboard.repositories.InventoryReceiptRepository;
import com.retailmanager.rmpaydashboard.repositories.ProductRepository;
import com.retailmanager.rmpaydashboard.repositories.UserBusiness_CategoryRepository;
import com.retailmanager.rmpaydashboard.repositories.UserBusiness_ProductRepository;
import com.retailmanager.rmpaydashboard.services.DTO.InventoryItemDTO;
import com.retailmanager.rmpaydashboard.services.DTO.InventoryReceiptDTO;
import com.retailmanager.rmpaydashboard.services.DTO.ProductDTO;



/* Implementa la interface IProductService. Ver documentación en la declaración de la interface */
@Service
public class ProductService implements IProductService {
    @Autowired
    @Qualifier("mapperbase")
    private ModelMapper mapperBase;
    @Autowired
    private ProductRepository serviceDBProducts;
    @Autowired
    private CategoryRepository serviceDBCategory;
    @Autowired
    private BusinessRepository serviceDBBusiness;
    @Autowired
    private UserBusiness_ProductRepository ubpServices;
    @Autowired
    private UserBusiness_CategoryRepository ubcServices;

    @Autowired 
    private InventoryReceiptRepository serviceDBInventoryReceipt;

    
    /**
     * Save a product and handle exceptions.
     *
     * @param  prmProduct	the product to be saved
     * @return         	the response entity
     */
    @Transactional
    @Override
    public ResponseEntity<?> save(ProductDTO prmProduct) {
        Optional<Category> optionalCategory = null;
        ResponseEntity<?> rta = null;
         Long categoryId=prmProduct.getIdCategory();
        if (categoryId != null) {
            optionalCategory = this.serviceDBCategory.findById(categoryId);
             if (!optionalCategory.isPresent()) {
                 EntidadNoExisteException objException = new EntidadNoExisteException(
                         "La categoria con idCategory " + prmProduct.getIdCategory() + " no existe en la base de datos");
                 throw objException;
             }
             if (prmProduct.getCode() != null) {
                Optional<Product> optionalProduct = this.serviceDBProducts
                        .findOneByCodeOrBarcode(prmProduct.getCode(), prmProduct.getCode());
                if (optionalProduct.isPresent()) {
                    if(optionalCategory.get().getBusiness().getBusinessId()==optionalProduct.get().getCategory().getBusiness().getBusinessId()) {
                        EntidadYaExisteException objException = new EntidadYaExisteException("El producto con Code  "
                        + prmProduct.getCode() + " ya existe en la base de datos");
                        throw objException;
                    }
                    
                }
            }
            if (prmProduct.getBarcode() != null) {
                Optional<Product> optionalProduct = this.serviceDBProducts
                        .findOneByCodeOrBarcode(prmProduct.getBarcode(), prmProduct.getBarcode());
                if (optionalProduct.isPresent()) {
                    if(optionalCategory.get().getBusiness().getBusinessId()==optionalProduct.get().getCategory().getBusiness().getBusinessId()) {
                        EntidadYaExisteException objException = new EntidadYaExisteException(
                            "El producto con barCode  " + prmProduct.getBarcode() + " ya existe en la base de datos");
                        throw objException;
                    }
                    
                }
            }
            Product objProduct = this.mapperBase.map(prmProduct, Product.class);
            objProduct.setCategory(optionalCategory.get());
            objProduct.setCreatedAt(LocalDateTime.now());
            objProduct.setUpdatedAt(LocalDateTime.now());
            if(objProduct!=null){
                objProduct = this.serviceDBProducts.save(objProduct);
                for(UsersBusiness usersBusiness:objProduct.getCategory().getBusiness().getUsersBusiness()){
                    UserBusiness_Product ubp = new UserBusiness_Product();
                    ubp.setObjProduct(objProduct);
                    ubp.setDownload(false);
                    ubp.setObjUser(usersBusiness);
                    ubpServices.save(ubp);
                }
            }
            
            if (objProduct != null) {
                ProductDTO objProductRta = this.mapperBase.map(objProduct, ProductDTO.class);
                objProductRta.setIdCategory(categoryId);
                rta = new ResponseEntity<ProductDTO>(objProductRta, HttpStatus.CREATED);
            }
        }
        
        return rta;
    }
    /**
     * Update a product based on the given ProductDTO.
     *
     * @param  prmProduct  the product data to be updated
     * @return             the response entity with the updated product data
     */
    @Transactional
    @Override
    public ResponseEntity<?> update(ProductDTO prmProduct) {
        Long productId = prmProduct.getProductId();
        if(productId==null){
            throw new EntidadNoExisteException("El identificador del producto no puede ser nulo");
        }
        Optional<Product> optionalProduct=this.serviceDBProducts.findById(productId);
        if (!optionalProduct.isPresent()) {
            EntidadNoExisteException objException = new EntidadNoExisteException(
                    "El producto con idProduct " + prmProduct.getProductId()
                            + " no existe en la base de datos");
            throw objException;
        }
        Optional<Product> optionalProduct2=null;
        if(prmProduct.getBarcode()!=null){
            optionalProduct2= this.serviceDBProducts.findOneByCodeOrBarcode(prmProduct.getCode(), prmProduct.getBarcode());
        }else{
            optionalProduct2= this.serviceDBProducts.findOneByCodeOrBarcode(prmProduct.getCode(), prmProduct.getCode());
        }
         if(optionalProduct2.isPresent()){
            
             if(optionalProduct2.get().getProductId()!=productId && optionalProduct2.get().getCategory().getBusiness().getBusinessId()==optionalProduct.get().getCategory().getBusiness().getBusinessId()){
                 EntidadYaExisteException objException = new EntidadYaExisteException(
                         "El producto con Code o barCode " + prmProduct.getCode()
                                 + " ya existe en la base de datos");
                 throw objException;
             }
         }
        
        Product objProduct = optionalProduct.get();
        objProduct.setDescription(prmProduct.getDescription());
        objProduct.setBarcode(prmProduct.getBarcode());
        objProduct.setCost(prmProduct.getCost());
        objProduct.setPrice(prmProduct.getPrice());
        objProduct.setCode(prmProduct.getCode());
        objProduct.setEstatal(prmProduct.isEstatal());
        objProduct.setMunicipal(prmProduct.isMunicipal());
        objProduct.setEnable(prmProduct.getEnable());
        objProduct.setQuantity(prmProduct.getQuantity());
        objProduct.setMinimumLevel(prmProduct.getMinimumLevel());
        objProduct.setMaximumLevel(prmProduct.getMaximumLevel());
        objProduct.setName(prmProduct.getName());
        objProduct.setUpdatedAt(LocalDateTime.now());
        Long categoryId=prmProduct.getIdCategory();
        if(categoryId!=null){
            Optional<Category> optionalCategory = this.serviceDBCategory.findById(categoryId);
            if(!optionalCategory.isPresent()){
                EntidadNoExisteException objException = new EntidadNoExisteException(
                        "La categoria con idCategory " + prmProduct.getIdCategory() + " no existe en la base de datos");
                
                throw objException;
            }else{
                objProduct.setCategory(optionalCategory.get());
            }
        }
        objProduct = this.serviceDBProducts.save(objProduct);
        ResponseEntity<?> rta = null;
        if (objProduct != null) {
            for(UsersBusiness usersBusiness:objProduct.getCategory().getBusiness().getUsersBusiness()){
                ubpServices.updateDownload(productId,usersBusiness.getUserBusinessId(),false);
            }
            ProductDTO objProductRta = this.mapperBase.map(objProduct, ProductDTO.class);
            objProductRta.setIdCategory(categoryId);
            rta = new ResponseEntity<ProductDTO>(objProductRta, HttpStatus.OK);
        } 
        return rta;

    }
    /**
     * Delete a product by its ID.
     *
     * @param  productId	ID of the product to be deleted
     * @return         	Response entity with a boolean indicating success or failure
     */
    @Transactional
    @Override
    public ResponseEntity<?> delete(Long productId) {
        if(productId==null){
            return new ResponseEntity<Boolean>(false, HttpStatus.OK);
        }
        Optional<Product> optionalProduct = this.serviceDBProducts.findById(productId);
        if (!optionalProduct.isPresent()) {
            EntidadNoExisteException objException = new EntidadNoExisteException(
                    "El producto con productId " + productId + " no existe en la base de datos");
            throw objException;
        }
        Product objProduct = optionalProduct.get();
        if(objProduct!=null){
            this.ubpServices.deleteByProductId(productId);
            this.serviceDBProducts.delete(objProduct);
        }
        return new ResponseEntity<Boolean>(true, HttpStatus.OK);
    }

    /**
     * Method to find a product by its ID.
     *
     * @param  productId  the ID of the product to find
     * @return            a ResponseEntity containing the product information
     */
    @Transactional(readOnly = true)
    @Override
    public ResponseEntity<?> findById(Long productId) {
        ResponseEntity<?> rta = null;
        Optional<Product> optionalProduct=null;
        if(productId!=null){
            optionalProduct = this.serviceDBProducts.findById(productId);
            if (!optionalProduct.isPresent()) {
                EntidadNoExisteException objException = new EntidadNoExisteException(
                        "El producto con productId " + productId + " no existe en la base de datos");
                throw objException;
            }
        }
        if(optionalProduct!=null){
            ProductDTO objProductDTO = this.mapperBase.map(optionalProduct.get(), ProductDTO.class);
            objProductDTO.setIdCategory(optionalProduct.get().getCategory().getCategoryId());
            rta = new ResponseEntity<ProductDTO>(objProductDTO, HttpStatus.OK);
        }
        return rta;
    }

    /**
     * Finds products by category.
     *
     * @param  categoryId  the ID of the category
     * @param  filter      the filter for the search
     * @param  pageable    the pagination information
     * @return             the response entity with the result
     */
    @Transactional()
    @Override
    public ResponseEntity<?> findByCategory(Long categoryId,  Pageable pageable) {
        ResponseEntity<?> rta = null;
        if(categoryId==null){
            return new ResponseEntity<String>("El id de la categoria no puede ser nulo",HttpStatus.BAD_REQUEST);
        }
        Optional<Category> optionalCategory = this.serviceDBCategory.findById(categoryId);
        Page<Product> result = new PageImpl<>(new ArrayList<>());
        if (optionalCategory.isPresent()) {
            
               result = this.serviceDBProducts.findByCategoryIs(optionalCategory.get(), pageable);
           
       }
       Page<ProductDTO> resultDTO = result.map(product -> ProductDTO.tOProduct(product));
        rta = new ResponseEntity<>(resultDTO, HttpStatus.OK);
       return rta;
    }

     /**
      * Find all and filter products.
      *
      * @param  businessId   the business id
      * @param  filter       the filter
      * @param  pageable     the pageable
      * @return              the response entity
      */
     @Transactional()
     @Override
     public ResponseEntity<?> findAllAndFilter(Long businessId,String filter, Pageable pageable) {
         ResponseEntity<?> rta = null;
         if(businessId!=null){
            Optional<Business> exist = this.serviceDBBusiness.findById(businessId);
            if(!exist.isPresent()){
                EntidadNoExisteException objExeption = new EntidadNoExisteException("El business con businessId "+businessId+" ya existe en la Base de datos");
                throw objExeption;
            }
        }
        if(filter != null) {
             Page<Product> result = new PageImpl<>(new ArrayList<>());
             filter = "%" + filter + "%";
             result = this.serviceDBProducts.findProductsByFilter(businessId,filter, pageable);
             Page<ProductDTO> resultDTO = result.map(product -> ProductDTO.tOProduct(product));
             rta = new ResponseEntity<>(resultDTO, HttpStatus.OK);
        }
        return rta;
     }
    /**
     * Save a list of product DTOs and return a list of created product DTOs.
     *
     * @param  listProductsDTO    the list of product DTOs to be saved
     * @return                   a list of created product DTOs
     */
    @Transactional()
    @Override
    public ResponseEntity<?> save( List<ProductDTO> listProductsDTO) {
        if(listProductsDTO==null && listProductsDTO.isEmpty()){
            return new ResponseEntity<String>("La lista de Productos no puede ser vacia ni null",HttpStatus.BAD_REQUEST);
        }
        Business business = this.serviceDBBusiness.findById(listProductsDTO.get(0).getIdBusiness()).orElse(null);
        if(business==null){
            EntidadNoExisteException objException = new EntidadNoExisteException(
                    "El business con businessId "+listProductsDTO.get(0).getIdBusiness()+" no existe en la Base de datos");
            
            throw objException;
        }
        Long maxPosition=this.serviceDBCategory.maxPositionInCategory(business.getBusinessId());
        if(maxPosition==null){
            maxPosition=0L;
        }
        List<ProductDTO> listProductsRTA=new ArrayList<>();
        for (ProductDTO productDTO : listProductsDTO) {
            
            Product objProduct=null;
            if (productDTO.getCode() != null) {
                Optional<Product> optionalProduct = this.serviceDBProducts
                        .findOneByCodeOrBarcode(productDTO.getCode(), productDTO.getCode());
                if (optionalProduct.isPresent()) {
                    objProduct=optionalProduct.get();
                }
            }
            if (objProduct==null && productDTO.getBarcode() != null) {
                Optional<Product> optionalProduct = this.serviceDBProducts
                        .findOneByCodeOrBarcode(productDTO.getBarcode(), productDTO.getBarcode());
                if (optionalProduct.isPresent()) {
                    objProduct=optionalProduct.get();
                }
            }
            if(objProduct==null){
                objProduct = this.mapperBase.map(productDTO, Product.class);
            }
            String  categoryName=productDTO.getNameCategory();
            Long categoryId=0L;
            if(categoryName!=null){
                Optional<Category> optionalCategory = this.serviceDBCategory.findFirstByNameAndBusiness(categoryName, business);
                if(optionalCategory.isPresent()){
                    categoryId=optionalCategory.get().getCategoryId();
                    objProduct.setCategory(optionalCategory.get());
                }else{
                    Category objCategory = new Category();
                    objCategory.setCategoryId(null);
                    objCategory.setPosition(String.valueOf(maxPosition!=null?maxPosition+1:1L));
                    objCategory.setName(categoryName);
                    objCategory.setBusiness(business);
                    if(productDTO.getPosition()==null){
                        maxPosition++;
                    }else{
                        objCategory.setPosition(String.valueOf(productDTO.getPosition()));
                        if(productDTO.getPosition()>maxPosition){
                            maxPosition=productDTO.getPosition();
                        }
                    }
                    objCategory = this.serviceDBCategory.save(objCategory);
                    categoryId=objCategory.getCategoryId();
                    objProduct.setCategory(objCategory);
                    for(UsersBusiness usersBusiness:business.getUsersBusiness()){
                        UserBusiness_Category ubp = new UserBusiness_Category();
                        ubp.setObjCategory(objCategory);
                        ubp.setDownload(false);
                        ubp.setObjUser(usersBusiness);
                        this.ubcServices.save(ubp);
                    }
                }
            }
            if(objProduct!=null){
                objProduct.setCreatedAt(LocalDateTime.now());
                objProduct.setUpdatedAt(LocalDateTime.now());
                objProduct = this.serviceDBProducts.save(objProduct);
                
            }
            if (objProduct != null) {
                ProductDTO objProductRta = this.mapperBase.map(objProduct, ProductDTO.class);
                objProductRta.setIdCategory(categoryId);
                listProductsRTA.add(objProductRta);
                for(UsersBusiness usersBusiness:objProduct.getCategory().getBusiness().getUsersBusiness()){
                    UserBusiness_Product ubp = new UserBusiness_Product();
                    ubp.setObjProduct(objProduct);
                    ubp.setDownload(false);
                    ubp.setObjUser(usersBusiness);
                    ubpServices.save(ubp);
                }
            }
        }
        return new ResponseEntity<>(listProductsRTA, HttpStatus.CREATED);
    }
    /**
     * Updates the enable status of a product.
     *
     * @param  productId    the ID of the product to be updated
     * @param  enable       the new enable status
     * @return              a ResponseEntity with a boolean indicating if the update was successful
     */
    @Override
    @Transactional
    public ResponseEntity<?> updateEnable(Long productId, boolean enable) {
        if(productId!=null){
            Optional<Product> optional= this.serviceDBProducts.findById(productId);
            if(optional.isPresent()){
                optional.get().setEnable(enable);
                optional.get().setUpdatedAt(LocalDateTime.now());
                this.serviceDBProducts.save(optional.get());
                if(enable){
                    for(UsersBusiness usersBusiness:optional.get().getCategory().getBusiness().getUsersBusiness()){
                        ubpServices.updateDownload(productId,usersBusiness.getUserBusinessId(),false);
                    }
                }
                return new ResponseEntity<Boolean>(true,HttpStatus.OK);
            }
        }
        EntidadNoExisteException objExeption = new EntidadNoExisteException("El Producto con productId "+productId+" no existe en la Base de datos");
                throw objExeption;
    }
    /**
     * Find all products by business ID.
     *
     * @param  businessId  the ID of the business
     * @param  pageable    the pageable object for pagination
     * @return             a ResponseEntity with the list of products and HTTP status
     */
    @Override
    @Transactional
    public ResponseEntity<?> findAllByBusinessId(Long businessId, Pageable pageable) {
        ResponseEntity<?> rta = null;
         if(businessId!=null){
            Optional<Business> exist = this.serviceDBBusiness.findById(businessId);
            if(!exist.isPresent()){
                EntidadNoExisteException objExeption = new EntidadNoExisteException("El business con businessId "+businessId+" ya existe en la Base de datos");
                throw objExeption;
            }
        }
             Page<Product> result = new PageImpl<>(new ArrayList<>());
             result = this.serviceDBProducts.findProductsByBusinessId(businessId, pageable);
             Page<ProductDTO> resultDTO = result.map(product -> ProductDTO.tOProduct(product));
             rta = new ResponseEntity<>(resultDTO, HttpStatus.OK);
        return rta;
    }
    @Override
    @Transactional
    public ResponseEntity<?> findAllByBusinessId(Long businessId) {
        ResponseEntity<?> rta = null;
         if(businessId!=null){
            Optional<Business> exist = this.serviceDBBusiness.findById(businessId);
            if(!exist.isPresent()){
                EntidadNoExisteException objExeption = new EntidadNoExisteException("El business con businessId "+businessId+" ya existe en la Base de datos");
                throw objExeption;
            }
        }
             List<Product> result = new ArrayList<>();
             result = this.serviceDBProducts.findProductsByBusinessId(businessId);
             List<ProductDTO> resultDTO = result.stream().map(product -> ProductDTO.tOProduct(product)).collect(Collectors.toList());
             rta = new ResponseEntity<>(resultDTO, HttpStatus.OK);
        return rta;
    }
    /**
     * Find all products by business ID and generate a CSV file with the product details.
     *
     * @param  businessId    the ID of the business
     * @return               a ResponseEntity with the generated CSV file
     */
    @Override
    public ResponseEntity<?> findAllByBusinessIdCSV(Long businessId) {
        List<Product> productos=new ArrayList<>();
        if(businessId!=null){
           
            boolean existe = this.serviceDBBusiness.existsById(businessId);
            if(!existe){
                EntidadNoExisteException objException = new EntidadNoExisteException(
                        "El business con businessId "+businessId+ " no existe en la base de datos");
                throw objException;
            }
            productos=this.serviceDBProducts.findProductsByBusinessId(businessId);
        }
        String encabezado = "Product Id," +
                    "Barcode," +
                    "Code," +
                    "Name," +
                    "Description," +
                    "Cost," +
                    "Price," +
                    "Category," +
                    "Estatal Tax," +
                    "Municipal Tax," +
                    "Quantity," +
                    "Minimum Level," +
                    "Maximum Level," +
                    "Enable\n";
        String archCSV = System.getProperty("user.dir") + "/products.csv";
        try {
            File statText = new File(archCSV);
            FileOutputStream is = new FileOutputStream(statText);
            OutputStreamWriter osw = new OutputStreamWriter(is);
            Writer w = new BufferedWriter(osw);
            w.write(encabezado);
            if(productos!=null){
                for (Product objProduct : productos) {
                    w.write(createLine(objProduct));
                }
            }
            w.close();
            File file = new File(archCSV);
            InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
            return ResponseEntity.ok()
                    .contentLength(file.length())
                    .contentType( MediaType.parseMediaTypes("text/csv").get(0))
                    .body(resource);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return null;
    }
    private String createLine(Product objProduct) {
        String line = objProduct.getProductId() + "," +
                nullToZero(objProduct.getBarcode()) + "," +
                nullToZero(objProduct.getCode()) + "," +
                nullToZero(objProduct.getName()) + "," +
                nullToZero(objProduct.getDescription()) + "," +
                nullToZero(objProduct.getCost()) + "," +
                nullToZero(objProduct.getPrice()) + "," +
                nullToZero(objProduct.getCategory().getName()) + "," +
                nullToZero(objProduct.isEstatal()) + "," +
                nullToZero(objProduct.isMunicipal()) + "," +
                nullToZero(objProduct.getQuantity()) + "," +
                nullToZero(objProduct.getMinimumLevel()) + "," +
                nullToZero(objProduct.getMaximumLevel()) + "," +
                nullToZero(objProduct.isEnable()) + "\n";
        return line;
    }
    
    private String nullToZero(Object prmObject){
        if(prmObject==null)
            return "0";
        else
            return prmObject.toString();
    }
    @Override
    public ResponseEntity<?> findByCategory(List<Long> categoryIds,Pageable pageable) {
        ResponseEntity<?> rta = null;
        if(categoryIds==null){
            return new ResponseEntity<String>("Debe especificar una lista de categorías",HttpStatus.BAD_REQUEST);
        }
        List<Long> validatedCategoryIds =new ArrayList<>();
        for(Long id : categoryIds){
            if(this.serviceDBCategory.existsById(id)){
                validatedCategoryIds.add(id);
            }
        }
        Page<Product> result = new PageImpl<>(new ArrayList<>());
        result = this.serviceDBProducts.findByCategoryIn(validatedCategoryIds, pageable);
           
       
       Page<ProductDTO> resultDTO = result.map(product -> ProductDTO.tOProduct(product));
        rta = new ResponseEntity<>(resultDTO, HttpStatus.OK);
       return rta;
    }
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getQuantity(Long productId) {
        Product objProduct = this.serviceDBProducts.findById(productId).orElse(null);
        if(objProduct!=null){
            HashMap<String, Object> result = new HashMap<>();
            result.put("quantity", objProduct.getQuantity());
            return new ResponseEntity<>(result,HttpStatus.OK);
        }else{
            throw new EntidadNoExisteException("El producto con productId "+productId+" no existe en la base de datos");
        }
    }
    /**
     * Recibe un inventario de productos y lo registra en la base de datos.
     * Actualiza la cantidad y el costo de los productos existentes y 
     * registra un nuevo recibo de inventario.
     * @param prmInventoryReceipt Inventario de productos a recibir
     * @return El inventario recibido
     */
    @Override
    @Transactional
    public ResponseEntity<?> receiveInventory(InventoryReceiptDTO prmInventoryReceipt) {
        Gson gson = new Gson();
        InventoryReceipt objInventoryReceipt = null;
        if(prmInventoryReceipt.getReceiptId()!=null){
            objInventoryReceipt = this.serviceDBInventoryReceipt.findById(prmInventoryReceipt.getReceiptId()).orElse(null);
            if(objInventoryReceipt!=null){
                throw new EntidadYaExisteException("El recibo de inventario con ID "+prmInventoryReceipt.getReceiptId()+" ya existe en la base de datos");
            }
        }
        objInventoryReceipt = new InventoryReceipt();
        objInventoryReceipt.setRegisterDate(LocalDateTime.now());
        objInventoryReceipt.setReceiptId(prmInventoryReceipt.getReceiptId());
        objInventoryReceipt.setComments(prmInventoryReceipt.getComments());
        objInventoryReceipt.setInventoryEntered(gson.toJson(prmInventoryReceipt.getInventoryItems()));
        objInventoryReceipt.setSupplier(prmInventoryReceipt.getSupplier());
        Business objBusiness = this.serviceDBBusiness.findById(prmInventoryReceipt.getBusinessId()).orElse(null);
        if(objBusiness==null){
            throw new EntidadNoExisteException("El negocio con ID "+prmInventoryReceipt.getBusinessId()+" no existe en la base de datos");
        }
        objInventoryReceipt.setObjBusiness(objBusiness);
        for(InventoryItemDTO prmInventoryItem : prmInventoryReceipt.getInventoryItems()){
            Product objProduct = this.serviceDBProducts.findById(prmInventoryItem.getProductId()).orElse(null);
            if(objProduct!=null){
                objProduct.setQuantity(objProduct.getQuantity()+prmInventoryItem.getQuantity());
                objProduct.setCost(prmInventoryItem.getUnitaryCost());
                objProduct.setPrice(prmInventoryItem.getPrice());
                this.serviceDBProducts.save(objProduct);    
            }
        }
        if(objInventoryReceipt.getReceiptId()==null){
            objInventoryReceipt.setReceiptId(generateUniqueReceiptId()); 
        }
        this.serviceDBInventoryReceipt.save(objInventoryReceipt);
        prmInventoryReceipt.setReceiptId(objInventoryReceipt.getReceiptId());
        prmInventoryReceipt.setInventoryEntered(objInventoryReceipt.getInventoryEntered()); 
        return new ResponseEntity<>(prmInventoryReceipt,HttpStatus.CREATED);
    }
    @Override
    public Long generateUniqueReceiptId() {
        // Implementa tu lógica para generar un ID único aquí, por ejemplo:
        // Puede ser una combinación de la fecha actual, un contador, o cualquier lógica que asegure unicidad.
        // Ejemplo simple:
        // Random random = new Random();
        // long currentTimeMillis = System.currentTimeMillis();
        // int randomInt = random.nextInt(1000); // Agrega una aleatoriedad para reducir colisiones
        // return currentTimeMillis + randomInt; // Esto generará un timestamp como ID
        Long lastId=this.serviceDBInventoryReceipt.getLastId();
        if(lastId==null){
            return 1L;
        }
        if(lastId==0){
            return 1L;
        }
        return lastId+1;
    }
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getInventoryHistory(Long businessId) {
        Business objBusiness = this.serviceDBBusiness.findById(businessId).orElse(null);
        if(objBusiness==null){
            throw new EntidadNoExisteException("El negocio con ID "+businessId+" no existe en la base de datos");
        }
        Iterable<InventoryReceipt> objInventoryReceipts = this.serviceDBInventoryReceipt.findByBusiness(businessId);
        List<InventoryReceiptDTO> objInventoryReceiptsDTO = new ArrayList<>();
        for(InventoryReceipt objInventoryReceipt : objInventoryReceipts){
            objInventoryReceiptsDTO.add(objInventoryReceipt.toInventoryReceiptDTO());
        }
        
        return new ResponseEntity<>(objInventoryReceiptsDTO,HttpStatus.OK);
    }
}
