package com.retailmanager.rmpaydashboard.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.retailmanager.rmpaydashboard.services.DTO.CategoryDTO;
import com.retailmanager.rmpaydashboard.services.services.CategoryService.ICategoryService;

@RestController
@RequestMapping("/api")
@Validated
public class CategoryController {
    
    @Autowired
    private ICategoryService categoryService;

    /**
     * Save a category.
     *
     * @param  prmCategory   the category to be saved
     * @return               the response entity
     */
    @PostMapping("/categories")
    public ResponseEntity<?> save(@Valid @RequestBody CategoryDTO prmCategory) {
        return categoryService.save(prmCategory);
    }

    /**
     * Update a category by ID.
     *
     * @param  categoryId    the ID of the category to update
     * @param  prmCategory   the updated category information
     * @return               the updated category
     */
    @PutMapping("/categories/{categoryId}")
    public ResponseEntity<?> update(@Valid @PathVariable @Positive(message = "categoryId.positive") Long categoryId,
            @Valid @RequestBody CategoryDTO prmCategory) {
        return categoryService.update(categoryId, prmCategory);
    }

    /**
     * Deletes a category by its ID.
     *
     * @param  categoryId     the ID of the category to delete
     * @return               true if the category was successfully deleted, false otherwise
     */
    @DeleteMapping("/categories/{categoryId}")
    public boolean delete(@Valid @PathVariable @Positive(message = "categoryId.positive") Long categoryId) {
        return categoryService.delete(categoryId);
    }

    /**
     * Find category by ID.
     *
     * @param  categoryId  the ID of the category to find
     * @return            the ResponseEntity containing the category found
     */
    @GetMapping("/categories/{categoryId}")
    public ResponseEntity<?> findById(@Valid @PathVariable @Positive(message = "categoryId.positive") Long categoryId) {
        return categoryService.findById(categoryId);
    }
    @GetMapping("/categories/business/{businessId}")
    public ResponseEntity<?> findByBusinessId(@Valid @PathVariable @Positive(message = "businessId.positive") Long businessId) {
            return categoryService.findByBusinessId(businessId);
        }
    /**
     * Find category by name.
     *
     * @param  name   name of the category to find
     * @return        response entity with the found category
     */
    @GetMapping("/categories")
    public ResponseEntity<?> findByName(@Valid @RequestParam(name = "name") @NotBlank(message = "name.notBlank") String name, 
            @Valid @RequestParam(name = "businessId") @Positive(message = "businessId.positive") Long businessId) {
        return categoryService.findByName(name, businessId);
    }

    /**
     * Update the enable status for a specific category.
     *
     * @param  categoryId  the ID of the category to be updated
     * @param  enable      the new enable status for the category
     * @return             the ResponseEntity representing the result of the update
     */
    @PutMapping("/categories/{categoryId}/enable/{enable}")
    public ResponseEntity<?> updateEnable(@Valid @PathVariable @Positive(message = "categoryId.positive") Long categoryId,
            @Valid @PathVariable boolean enable) {
        return categoryService.updateEnable(categoryId, enable);
    }
}