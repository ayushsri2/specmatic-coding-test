package com.store.controllers

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/products")
class ProductsController {

    // In-memory map to store products (consider using a database in production)
    private val products: MutableMap<Int, Product> = mutableMapOf()

    data class Product(val id: Int, val name: String, val type: String, val inventory: Int)

    // Data class for product details
    data class ProductDetails(val name: Any?, val type: String, val inventory: Any?)

    // POST /products endpoint to create a new product
    @PostMapping
    fun createProduct(@RequestBody productDetails: ProductDetails): ResponseEntity<Any> {
        // Validate the request body fields
        if (!isValidProductName(productDetails.name)) {
            return ResponseEntity.badRequest().body(
                ErrorResponse(LocalDateTime.now().toString(), HttpStatus.BAD_REQUEST.value(), "Invalid product name", "/products")
            )
        }

        if (!isValidProductType(productDetails.type)) {
            return ResponseEntity.badRequest().body(
                ErrorResponse(LocalDateTime.now().toString(), HttpStatus.BAD_REQUEST.value(), "Invalid product type", "/products")
            )
        }

        if (!isValidProductInventory(productDetails.inventory)) {
            return ResponseEntity.badRequest().body(
                ErrorResponse(LocalDateTime.now().toString(), HttpStatus.BAD_REQUEST.value(), "Invalid product inventory", "/products")
            )
        }

        // Generate a unique ID for the new product
        val productId = products.keys.maxOrNull()?.plus(1) ?: 1

        // Create the new product object
        val newProduct = Product(
            productId,
            productDetails.name.toString(),
            productDetails.type,
            (productDetails.inventory as Number).toInt()
        )

        // Store the new product in the in-memory map
        products[productId] = newProduct

        // Return the ID of the newly created product
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductId(productId))
    }

    data class ProductId(val id: Int)

    data class ErrorResponse(val timestamp: String, val status: Int, val error: String, val path: String)

    private fun isValidProductType(type: String): Boolean {
        return type in listOf("book", "food", "gadget", "other")
    }

    private fun isValidProductName(name: Any?): Boolean {
        // Validate that the name is a non-empty string
        return name is String && name.isNotBlank()
    }

    private fun isValidProductInventory(inventory: Any?): Boolean {
        // Validate that the inventory is a non-negative integer
        return inventory is Number && inventory.toInt() >= 0
    }

    // GET /products endpoint to list all products
    @GetMapping
    fun listProducts(@RequestParam(required = false) type: String?): ResponseEntity<Any> {
        if (type != null && !isValidProductType(type)) {
            return ResponseEntity.badRequest().body(
                ErrorResponse(
                    LocalDateTime.now().toString(),
                    HttpStatus.BAD_REQUEST.value(),
                    "Invalid product type: $type",
                    "/products"
                )
            )
        }

        // Filter products based on type if provided
        val filteredProducts = if (type != null) {
            products.values.filter { it.type == type }.toList()
        } else {
            products.values.toList()
        }

        return ResponseEntity.ok(filteredProducts)
    }

    // GET /products/{id} endpoint to get a product by ID
    @GetMapping("/{id}")
    fun getProduct(@PathVariable id: Int): ResponseEntity<Any> {
        val product = products[id]
        return if (product != null) {
            ResponseEntity.ok(product)
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse(LocalDateTime.now().toString(), HttpStatus.NOT_FOUND.value(), "Product not found", "/products/$id")
            )
        }
    }
}
