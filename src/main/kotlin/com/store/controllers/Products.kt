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

    data class Product(val id: Int, val name: String, val type: String, val inventory: Int, val cost: Double)

    // Data class for product details
    data class ProductDetails(val name: Any?, val type: String, val inventory: Any?, val cost: Any?)

    // POST /products endpoint to create a new product
    @PostMapping
    fun createProduct(@RequestBody productDetails: ProductDetails): ResponseEntity<Any> {
        println("Received product details: $productDetails")

        // Validate the request body fields
        if (!isValidProductName(productDetails.name)) {
            println("Invalid product name: ${productDetails.name}")
            return ResponseEntity.badRequest().body(
                ErrorResponse(LocalDateTime.now().toString(), HttpStatus.BAD_REQUEST.value(), "Invalid product name", "/products")
            )
        }

        if (!isValidProductType(productDetails.type)) {
            println("Invalid product type: ${productDetails.type}")
            return ResponseEntity.badRequest().body(
                ErrorResponse(LocalDateTime.now().toString(), HttpStatus.BAD_REQUEST.value(), "Invalid product type", "/products")
            )
        }

        if (!isValidProductInventory(productDetails.inventory)) {
            println("Invalid product inventory: ${productDetails.inventory}")
            return ResponseEntity.badRequest().body(
                ErrorResponse(LocalDateTime.now().toString(), HttpStatus.BAD_REQUEST.value(), "Invalid product inventory", "/products")
            )
        }

        if (!isValidProductCost(productDetails.cost)) {
            println("Invalid product cost: ${productDetails.cost}")
            return ResponseEntity.badRequest().body(
                ErrorResponse(LocalDateTime.now().toString(), HttpStatus.BAD_REQUEST.value(), "Invalid product cost", "/products")
            )
        }

        // Generate a unique ID for the new product
        val productId = products.keys.maxOrNull()?.plus(1) ?: 1

        // Create the new product object
        val newProduct = Product(
            productId, 
            productDetails.name.toString(), 
            productDetails.type, 
            (productDetails.inventory as Number).toInt(),
            (productDetails.cost as Number).toDouble()
        )

        // Store the new product in the in-memory map
        products[productId] = newProduct

        // Return the ID of the newly created product
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductId(productId))
    }

    data class ProductId(val id: Int)

    data class ErrorResponse(val timestamp: String, val status: Int, val error: String, val path: String)

    private fun isValidProductType(type: String): Boolean {
        println("Validating product type: $type")
        return type in listOf("book", "food", "gadget", "other")
    }

    private fun isValidProductName(name: Any?): Boolean {
        println("Validating product name: $name")
        // Validate that the name is a non-empty string
        if (name !is String) {
            println("Product name is not a string")
            return false
        }
        if (name.isBlank()) {
            println("Product name is blank")
            return false
        }
        return true
    }

    private fun isValidProductInventory(inventory: Any?): Boolean {
        println("Validating product inventory: $inventory")
        // Validate that the inventory is a non-negative integer
        if (inventory !is Number) {
            println("Product inventory is not a number")
            return false
        }
        if (inventory.toInt() < 0) {
            println("Product inventory is negative")
            return false
        }
        return true
    }

    private fun isValidProductCost(cost: Any?): Boolean {
        println("Validating product cost: $cost")
        // Validate that the cost is a non-negative number
        if (cost !is Number) {
            println("Product cost is not a number")
            return false
        }
        if (cost.toDouble() < 0) {
            println("Product cost is negative")
            return false
        }
        return true
    }

    // GET /products endpoint to list all products
    @GetMapping
    fun listProducts(@RequestParam(required = false) type: Any?): ResponseEntity<Any> {
        println("Received query parameter: type=$type")

        // Validate the type query parameter
        if (type != null && !isValidQueryParamType(type)) {
            println("Invalid product type in query parameter: $type")
            return ResponseEntity.badRequest().body(
                ErrorResponse(LocalDateTime.now().toString(), HttpStatus.BAD_REQUEST.value(), "Invalid product type", "/products")
            )
        }

        val filteredProducts = if (type != null) {
            products.values.filter { it.type == type }
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

    // Validate the type query parameter
    private fun isValidQueryParamType(type: Any): Boolean {
        if (type !is String) {
            return false
        }
        return type in listOf("book", "food", "gadget", "other")
    }
}