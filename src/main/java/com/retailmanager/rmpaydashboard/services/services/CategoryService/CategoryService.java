package com.retailmanager.rmpaydashboard.services.services.CategoryService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadNoExisteException;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadYaExisteException;
import com.retailmanager.rmpaydashboard.models.Business;
import com.retailmanager.rmpaydashboard.models.Category;
import com.retailmanager.rmpaydashboard.models.UserBusiness_Category;
import com.retailmanager.rmpaydashboard.models.UsersBusiness;
import com.retailmanager.rmpaydashboard.repositories.BusinessRepository;
import com.retailmanager.rmpaydashboard.repositories.CategoryRepository;
import com.retailmanager.rmpaydashboard.repositories.UserBusiness_CategoryRepository;
import com.retailmanager.rmpaydashboard.services.DTO.CategoryDTO;
@Service
public class CategoryService implements ICategoryService {

    @Autowired
    @Qualifier("mapperbase")
    private ModelMapper mapper;
    @Autowired
    private CategoryRepository serviceDBCategory;
    @Autowired
    private BusinessRepository serviceDBBusiness;
    @Autowired
    private UserBusiness_CategoryRepository ubcServices;
	/**
	 * Save CategoryDTO entity in the database
	 *
	 * @param  prmCategory	CategoryDTO object to be saved
	 * @return         	ResponseEntity containing the saved CategoryDTO or an error message
	 */
	@Override
    @Transactional
	public ResponseEntity<?> save(CategoryDTO prmCategory) {
		Long categoryId = prmCategory.getCategoryId();
        if(categoryId!=null){
            final boolean exist = this.serviceDBCategory.existsById(categoryId);
            if(exist){
                EntidadYaExisteException objExeption = new EntidadYaExisteException("La category con categoryId "+prmCategory.getCategoryId()+" ya existe en la Base de datos");
                throw objExeption;
            }else{
                prmCategory.setCategoryId(null);
            }
        }
        Optional<Category> exist = this.serviceDBCategory.findByNameAndBusinessId(prmCategory.getName(), prmCategory.getBusinesId());
        if(exist.isPresent()){
            EntidadYaExisteException objExeption = new EntidadYaExisteException("El negocio con Id "+prmCategory.getBusinesId()+" ya tiene la category con name "+prmCategory.getName()+" ya existe en la Base de datos");
            throw objExeption;
        }
        ResponseEntity<?> rta;
         Category objCategory= this.mapper.map(prmCategory, Category.class);
         objCategory.setCreatedAt(Instant.now());
            objCategory.setUpdatedAt(Instant.now());
         if(objCategory!=null){
            Long businessId=prmCategory.getBusinesId();
            if(businessId!=null){
                Optional<Business> existBusiness = this.serviceDBBusiness.findById(businessId);
                if(!existBusiness.isPresent()){
                    EntidadNoExisteException objExeption = new EntidadNoExisteException("El business con businessId "+businessId+" no existe en la Base de datos");
                    throw objExeption;
                }else{
                    
                    objCategory.setBusiness(existBusiness.get());
                }
            }
            objCategory=this.serviceDBCategory.save(objCategory);
            if(objCategory!=null){
                for(UsersBusiness usersBusiness:objCategory.getBusiness().getUsersBusiness()){
                    UserBusiness_Category ubp = new UserBusiness_Category();
                    ubp.setObjCategory(objCategory);
                    ubp.setDownload(false);
                    ubp.setObjUser(usersBusiness);
                    this.ubcServices.save(ubp);
                }
            }

         }
        CategoryDTO categoryDTO=this.mapper.map(objCategory, CategoryDTO.class);
        if(categoryDTO!=null){
            
            rta=new ResponseEntity<CategoryDTO>(categoryDTO, HttpStatus.CREATED);
        }else{
            rta= new ResponseEntity<String>("Error al crear la category",HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return rta;
	}

	/**
	 * Update a category in the database.
	 *
	 * @param  categoryId     the ID of the category to update
	 * @param  prmCategory    the updated category information
	 * @return                the response entity with the updated category or an error message
	 */
	@Override
    @Transactional
	public ResponseEntity<?> update(Long categoryId, CategoryDTO prmCategory) {
		Category objCategory=null;
        ResponseEntity<?> rta=null;
        if(categoryId!=null){
            Optional<Category> exist = this.serviceDBCategory.findById(categoryId);
            if(!exist.isPresent()){
                EntidadNoExisteException objExeption = new EntidadNoExisteException("La category con categoryId "+categoryId+" No existe en la Base de datos");
                throw objExeption;
            }
            objCategory=exist.get();
            if(objCategory.getName().compareTo(prmCategory.getName())!=0){
                Optional<Category> existBySerial = this.serviceDBCategory.findByNameAndBusinessId(prmCategory.getName(), objCategory.getBusiness().getBusinessId());
                if(existBySerial.isPresent()){
                    EntidadYaExisteException objExeption = new EntidadYaExisteException("El negocio con Id "+objCategory.getBusiness().getBusinessId()+" ya tiene la category con name "+prmCategory.getName()+" ya existe en la Base de datos");
                    throw objExeption;
                }
            }
            objCategory.setUpdatedAt(Instant.now());
             objCategory.setEnable(prmCategory.getEnable());
             objCategory.setName(prmCategory.getName());
             objCategory.setColor(prmCategory.getColor());
             objCategory.setPosition(prmCategory.getPosition());
             objCategory.setIcon(prmCategory.getIcon());
             
             if(objCategory!=null){
                objCategory=this.serviceDBCategory.save(objCategory);
                for(UsersBusiness usersBusiness:objCategory.getBusiness().getUsersBusiness()){
                    ubcServices.updateDownload(objCategory.getCategoryId(),usersBusiness.getUserBusinessId(),false);
                }
             }

            CategoryDTO categoryDTO=this.mapper.map(objCategory, CategoryDTO.class);
            if(categoryDTO!=null){
                rta=new ResponseEntity<CategoryDTO>(categoryDTO, HttpStatus.OK);
            }else{
                rta= new ResponseEntity<String>("Error al actualizar la category",HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return rta;
	}

	/**
	 * Deletes a category by its ID.
	 *
	 * @param  categoryId  the ID of the category to be deleted
	 * @return            true if the category is deleted, false otherwise
	 */
	@Override
    @Transactional
	public boolean delete(Long categoryId) {
		boolean bandera=false;
        if(categoryId!=null){
            Optional<Category> optional= this.serviceDBCategory.findById(categoryId);
            if(optional.isPresent()){
                Category objCategory=optional.get();
                if(objCategory!=null){
                    this.ubcServices.deleteByCategoryId(objCategory.getCategoryId());
                    this.serviceDBCategory.delete(objCategory);
                    bandera=true;
                }
                
            }
        }
        return bandera;
	}

	/**
	 * A description of the entire Java function.
	 *
	 * @param  categoryId	description of parameter
	 * @return         	description of return value
	 */
	@Override
    @Transactional(readOnly = true)
	public ResponseEntity<?> findById(Long categoryId) {
		if(categoryId!=null){
            Optional<Category> optional= this.serviceDBCategory.findById(categoryId);
            if(optional.isPresent()){
                CategoryDTO objCategoryDTO=this.mapper.map(optional.get(),CategoryDTO.class);
                objCategoryDTO.setBusinesId(optional.get().getBusiness().getBusinessId());
                return new ResponseEntity<CategoryDTO>(objCategoryDTO,HttpStatus.OK);
            }
        }
        EntidadNoExisteException objExeption = new EntidadNoExisteException("La category con categoryId "+categoryId+" no existe en la Base de datos");
                throw objExeption;
	}

	/**
	 * Update the enable status of a category.
	 *
	 * @param  categoryId   the ID of the category to be updated
	 * @param  enable       the new enable status
	 * @return             a ResponseEntity with a boolean indicating success
	 */
	@Override
    @Transactional
	public ResponseEntity<?> updateEnable(Long categoryId, boolean enable) {
		if(categoryId!=null){
            Optional<Category> optional= this.serviceDBCategory.findById(categoryId);
            if(optional.isPresent()){
                optional.get().setEnable(enable);
                optional.get().setUpdatedAt(Instant.now());
                this.serviceDBCategory.save(optional.get());
                if(enable){
                    for(UsersBusiness usersBusiness:optional.get().getBusiness().getUsersBusiness()){
                        ubcServices.updateDownload(optional.get().getCategoryId(),usersBusiness.getUserBusinessId(),false);
                    }
                }
                
                return new ResponseEntity<Boolean>(true,HttpStatus.OK);
            }
        }
        EntidadNoExisteException objExeption = new EntidadNoExisteException("La category con categoryId "+categoryId+" no existe en la Base de datos");
                throw objExeption;
	}

    /**
     * Finds a category by name in the database and returns a ResponseEntity with the category DTO.
     *
     * @param  name  the name of the category to find
     * @return       a ResponseEntity with the category DTO if found, or throws an exception if not found
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> findByName(String name, Long businessId) {
        if(name!=null && businessId!=null){
            Optional<Category> optional= this.serviceDBCategory.findByNameAndBusinessId(name, businessId);
            if(optional.isPresent()){
                CategoryDTO objCategoryDTO=this.mapper.map(optional.get(),CategoryDTO.class);
                return new ResponseEntity<CategoryDTO>(objCategoryDTO,HttpStatus.OK);
            }
        }
        EntidadNoExisteException objExeption = new EntidadNoExisteException("La category con name "+name+" para el negocio con businessId "+businessId+" no existe en la Base de datos");
                throw objExeption;
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> findByBusinessId(Long businessId) {
        if(businessId!=null){
            Optional<Business> optional= this.serviceDBBusiness.findById(businessId);
            if(optional.isPresent()){
                Business objBusiness=optional.get();
                if(objBusiness!=null){
                    List<Category> listCategory = this.serviceDBCategory.findByBusiness(objBusiness);
                    List<CategoryDTO> listCategoryDTO = this.mapper.map(listCategory, new TypeToken<List<CategoryDTO>>(){}.getType());
                    return new ResponseEntity<>(listCategoryDTO,HttpStatus.OK);
                }
            }
        }
        EntidadNoExisteException objExeption = new EntidadNoExisteException("El business con businessId "+businessId+" no existe en la Base de datos");
        throw objExeption;
    }
    
}
