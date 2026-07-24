package com.DhrubaStudio.E_commercePlatform.inventory;

import com.DhrubaStudio.E_commercePlatform.inventory.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
    public ResponseEntity<List<CategoryResponseDTO>> getAllCategories() {

        List<CategoryResponseDTO> categories = inventoryService.getAllCategories();

        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    @PostMapping("/categories")
    public ResponseEntity<CategoryResponseDTO> CreateCategory(@RequestBody CategoryRequestDTO request) {

        CategoryResponseDTO newCategory = inventoryService.createCategory(request);

        return new ResponseEntity<>(newCategory, HttpStatus.CREATED);
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts() {

        List<ProductResponseDTO> allProducts = inventoryService.getAllProducts();

        return new ResponseEntity<>(allProducts, HttpStatus.OK);
    }

    @PostMapping("/products")
    public ResponseEntity<?> CreateProduct(@RequestBody ProductRequestDTO request) {

        Product newProduct = inventoryService.createProduct(request);

        return new ResponseEntity<>(newProduct, HttpStatus.CREATED);
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id,@RequestBody ProductRequestDTO request) {

        ProductResponseDTO updatedProduct = inventoryService.updateProduct(id, request);

        return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
    }

    @GetMapping("/products/search")
    public ResponseEntity<PagedProductResponseDTO<ProductResponseDTO>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize,
            @RequestParam(value = "sortBy", defaultValue = "id", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "asc", required = false) String sortDir) {

    PagedProductResponseDTO<ProductResponseDTO> searchResults = inventoryService.searchProducts(
                keyword, maxPrice, categoryId, pageNo, pageSize, sortBy, sortDir);

        return new ResponseEntity<>(searchResults, HttpStatus.OK);
    }
}
