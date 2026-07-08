package com.retailmanager.rmpaydashboard.services.services.CategoryService;

import org.springframework.http.ResponseEntity;

import com.retailmanager.rmpaydashboard.services.DTO.CategoryDTO;

public interface ICategoryService   {
    public ResponseEntity<?> save(CategoryDTO prmCategory);
    public ResponseEntity<?> update(Long categoryId, CategoryDTO prmCategory);
    public boolean delete(Long categoryId);
    public ResponseEntity<?> findById(Long categoryId);
    public ResponseEntity<?> findByName(String name, Long businessId);
    public ResponseEntity<?> updateEnable(Long categoryId, boolean enable);
    public ResponseEntity<?> findByBusinessId(Long businessId);
}
