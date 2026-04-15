package com.DhrubaStudio.E_commercePlatform.inventory;

import com.DhrubaStudio.E_commercePlatform.inventory.dto.ProductRequestDTO;
import com.DhrubaStudio.E_commercePlatform.inventory.dto.ProductResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    @Autowired
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getAllCategories() {

        List<Category> categories = inventoryService.getAllCategories();
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    @PostMapping("/categories")
    public ResponseEntity<?> CreateCategory(@RequestBody Category category) {
        try{
            Category newCategory = inventoryService.createCategory(category);
            return new ResponseEntity<>(newCategory, HttpStatus.CREATED);
        } catch(IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts() {

        List<ProductResponseDTO> allProducts = inventoryService.getAllProducts();
        return new ResponseEntity<>(allProducts, HttpStatus.OK);
    }

    @PostMapping("/products")
    public ResponseEntity<?> CreateProduct(@RequestBody ProductRequestDTO request) {
        try{
            Product newProduct = inventoryService.createProduct(request);
            return new ResponseEntity<>(newProduct, HttpStatus.CREATED);
        } catch(IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id,@RequestBody ProductRequestDTO request) {
        try{
            ProductResponseDTO updatedProduct = inventoryService.updateProduct(id, request);
            return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
        } catch(IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

}
