package com.DhrubaStudio.E_commercePlatform.inventory;
import com.DhrubaStudio.E_commercePlatform.inventory.dto.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class InventoryService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public InventoryService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public CategoryResponseDTO createCategory(CategoryRequestDTO request) {
        log.info("Attempting to create new category: {}", request.getName());

        Category existingCategory = categoryRepository.findByName(request.getName()).orElse(null);
        if (existingCategory != null) {
            log.warn("Category creation failed. Category '{}' already exists.", request.getName());
            throw new IllegalArgumentException("Category with name " + request.getName() + " already exists.");
        }

        Category category = new Category(request.getName(), request.getDescription());
        Category savedCategory = categoryRepository.save(category);

        log.info("Successfully created category '{}' with ID: {}", savedCategory.getName(), savedCategory.getId());
        return new CategoryResponseDTO(savedCategory.getId(), savedCategory.getName(), savedCategory.getDescription());
    }

    public List<CategoryResponseDTO> getAllCategories() {
        log.debug("Fetching all categories from the database.");
        List<Category> categories = categoryRepository.findAll();
        List<CategoryResponseDTO> responseDTOs = new ArrayList<>();

        for (Category category : categories) {
            responseDTOs.add(new CategoryResponseDTO(category.getId(), category.getName(), category.getDescription()));
        }

        return responseDTOs;
    }

    public Product createProduct(ProductRequestDTO request) {
        log.info("Attempting to create new product: {}", request.getName());

        Category requestProductCategory = categoryRepository.findById(request.getCategoryId()).orElse(null);
        if (requestProductCategory == null) {
            log.error("Product creation failed. Category ID {} not found.", request.getCategoryId());
            throw new IllegalArgumentException("Category with id " + request.getCategoryId() + " does not exist.");
        }

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setCategory(requestProductCategory);

        Product savedProduct = productRepository.save(product);
        log.info("Successfully created product '{}' with ID: {}", savedProduct.getName(), savedProduct.getId());
        return savedProduct;
    }

    public List<ProductResponseDTO> getAllProducts() {
        log.debug("Fetching all products from the database.");
        List<ProductResponseDTO> products = new ArrayList<>();
        List<Product> rawProducts = productRepository.findAll();

        for (Product product : rawProducts) {
            ProductResponseDTO responseDTO = createProductResponseDTO(product);
            products.add(responseDTO);
        }

        return products;
    }

    public ProductResponseDTO updateProduct(long productId, ProductRequestDTO request) {
        log.info("Attempting to update product with ID: {}", productId);

        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            log.warn("Product update failed. Product ID {} not found.", productId);
            throw new IllegalArgumentException("Product with id " + productId + " does not exist.");
        }

        Category requestProductCategory = categoryRepository.findById(request.getCategoryId()).orElse(null);

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setCategory(requestProductCategory);

        Product updatedProduct = productRepository.save(product);
        log.info("Successfully updated product ID: {}", updatedProduct.getId());
        return createProductResponseDTO(updatedProduct);
    }

    public PagedProductResponseDTO<ProductResponseDTO> searchProducts(String keyword, BigDecimal maxPrice,
                                                                      Long categoryId, int pageNo, int pageSize,
                                                                      String sortBy, String sortDir) {
        log.info("Searching products with keyword: '{}', maxPrice: {}, categoryId: {}", keyword, maxPrice, categoryId);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Page<Product> productPage = productRepository.searchAndFilterProducts(keyword, maxPrice, categoryId, pageable);

        List<ProductResponseDTO> responseDTOs = new ArrayList<>();
        for (Product product : productPage.getContent()) {
            responseDTOs.add(createProductResponseDTO(product));
        }

        log.debug("Search returned {} results out of {} total elements (Page {} of {}).",
                responseDTOs.size(), productPage.getTotalElements(), pageNo, productPage.getTotalPages());

        return new PagedProductResponseDTO<>(
                responseDTOs,
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isLast()
        );
    }

    private ProductResponseDTO createProductResponseDTO(Product product) {
        return new ProductResponseDTO(product.getId(), product.getName(),
                product.getDescription(), product.getPrice(), product.getStockQuantity(),
                product.getCategory().getId(), product.getCategory().getName());
    }

}