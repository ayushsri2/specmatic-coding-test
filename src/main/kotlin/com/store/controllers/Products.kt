package com.store.controllers

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/products")
class ProductsController {

    // In-memory list to store products
    private val products = mutableListOf<Product>()

    // Data classes for Product and ProductDetails
    data class Product(val id: Int, val name: String, val type: String, val inventory: Int)
    data class ProductDetails(val name: String?, val type: String?, val inventory: Int?)
    data class ProductId(val id: Int)
    data class ErrorResponseBody(val status: Int, val error: String) {
        constructor(statusCode: HttpStatus, errorMessage: String) : this(statusCode.value(), errorMessage)
    }

    // GET /products endpoint to retrieve products based on type (optional)
    @GetMapping
    fun getProducts(@RequestParam(required = false) type: String?): ResponseEntity<Any> {
        val filteredProducts = type?.let { filterByType(it) } ?: products

        return if (filteredProducts.isEmpty()) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponseBody(HttpStatus.NOT_FOUND, "No products found"))
        } else {
            ResponseEntity.ok(filteredProducts)
        }
    }

    // POST /products endpoint to create a new product with validation
    @PostMapping
    fun createProduct(@RequestBody productDetails: ProductDetails): ResponseEntity<Any> {
        // Validate product details
        val validationError = validateProductDetails(productDetails)
        if (validationError != null) {
            return ResponseEntity.badRequest().body(ErrorResponseBody(HttpStatus.BAD_REQUEST, validationError))
        }

        // Generate a unique ID for the new product
        val productId = products.size + 1
        // Create and add the new product to the in-memory list
        val newProduct = Product(productId, productDetails.name!!, productDetails.type!!, productDetails.inventory!!)
        products.add(newProduct)

        return ResponseEntity.status(HttpStatus.CREATED).body(ProductId(productId))
    }

    // Helper method to validate product details
    private fun validateProductDetails(productDetails: ProductDetails): String? {
        if (productDetails.name.isNullOrEmpty()) {
            return "Product name is required"
        }
        if (productDetails.type.isNullOrEmpty()) {
            return "Product type is required"
        }
        if (!isValidProductType(productDetails.type)) {
            return "Invalid product type"
        }
        if (productDetails.inventory == null) {
            return "Inventory is required"
        }
        if (productDetails.inventory < 0) {
            return "Inventory cannot be negative"
        }
        return null
    }

    // Helper method to filter products by type
    private fun filterByType(type: String): List<Product> {
        return products.filter { it.type == type }
    }

    // Helper method to check if the product type is valid
    private fun isValidProductType(type: String): Boolean {
        return type in listOf("book", "food", "gadget", "other")
    }
}
