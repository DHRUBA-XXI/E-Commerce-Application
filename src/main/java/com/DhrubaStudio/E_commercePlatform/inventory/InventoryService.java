package com.DhrubaStudio.E_commercePlatform.inventory;

import com.DhrubaStudio.E_commercePlatform.inventory.dto.ProductRequestDTO;
import com.DhrubaStudio.E_commercePlatform.inventory.dto.ProductResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class InventoryService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public InventoryService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public Category createCategory(Category category) {

        Category existingCategory = categoryRepository.findByName(category.getName()).orElse(null);
        if (existingCategory != null) {
            throw new IllegalArgumentException("Category with name " + category.getName() + " already exists.");
        }
        return categoryRepository.save(category);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Product createProduct(ProductRequestDTO request) {

        Category requestProductCategory = categoryRepository.findById(request.getCategoryId()).orElse(null);
        if (requestProductCategory == null) {
            throw new IllegalArgumentException("Category with id " + request.getCategoryId() + " does not exist.");
        }

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setCategory(requestProductCategory);

        return productRepository.save(product);
    }

    public List<ProductResponseDTO> getAllProducts() {

        List<ProductResponseDTO>  products = new ArrayList<>();
        List<Product> allProducts = productRepository.findAll();

        for (Product product : allProducts) {
            ProductResponseDTO responseDTO = createProductResponseDTO(product);
            products.add(responseDTO);
        }

        return products;
    }

    public ProductResponseDTO updateProduct(long productId,ProductRequestDTO request) {

        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            throw new IllegalArgumentException("Product with id " + productId + " does not exist.");
        }

        Category requestProductCategory = categoryRepository.findById(request.getCategoryId()).orElse(null);

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setCategory(requestProductCategory);

        Product updatedProduct = productRepository.save(product);
        return createProductResponseDTO(updatedProduct);
    }


    private ProductResponseDTO createProductResponseDTO(Product product) {

        ProductResponseDTO productResponseDTO = new ProductResponseDTO(product.getId(), product.getName(),
                product.getDescription(),product.getPrice(),product.getStockQuantity(),
                product.getCategory().getId(), product.getCategory().getName());

        return productResponseDTO;
    }

}
