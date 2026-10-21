package com.vimspace.api.config;

import com.vimspace.api.entity.BarcodeEntity;
import com.vimspace.api.entity.ProductEntity;
import com.vimspace.api.entity.SupplierEntity;
import com.vimspace.api.entity.UserEntity;
import com.vimspace.api.repository.BarcodeRepository;
import com.vimspace.api.repository.ProductRepository;
import com.vimspace.api.repository.SupplierRepository;
import com.vimspace.api.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepo,
                                  ProductRepository productRepo,
                                  BarcodeRepository barcodeRepo,
                                  SupplierRepository supplierRepo) {
        return args -> {
            // Seed initial users if empty
            if (userRepo.count() == 0) {
                userRepo.save(new UserEntity("admin", "admin", "System Administrator"));
                userRepo.save(new UserEntity("manager", "manager", "Store Manager"));
                userRepo.save(new UserEntity("cashier", "cashier", "Cashier"));
            }

            // Seed sample products and barcodes if empty
            if (productRepo.count() == 0) {
                ProductEntity milk = productRepo.save(new ProductEntity(
                        "SKU-MILK-01", "Full Cream Milk 1L", "Dairy", "1L", "White",
                        new BigDecimal("2.50"), new BigDecimal("3.50"), new BigDecimal("100")));

                ProductEntity bread = productRepo.save(new ProductEntity(
                        "SKU-BREAD-01", "Whole Wheat Bread 800g", "Bakery", "800g", "Brown",
                        new BigDecimal("1.20"), new BigDecimal("2.00"), new BigDecimal("50")));

                ProductEntity water = productRepo.save(new ProductEntity(
                        "SKU-WATER-01", "Spring Water 500ml", "Beverages", "500ml", "Clear",
                        new BigDecimal("0.50"), new BigDecimal("1.00"), new BigDecimal("200")));

                ProductEntity apple = productRepo.save(new ProductEntity(
                        "SKU-APPLE-01", "Fresh Royal Gala Apples 1kg", "Produce", "1kg", "Red",
                        new BigDecimal("2.00"), new BigDecimal("3.99"), new BigDecimal("80")));

                barcodeRepo.save(new BarcodeEntity(milk, "1001"));
                barcodeRepo.save(new BarcodeEntity(milk, "012345678901"));
                barcodeRepo.save(new BarcodeEntity(bread, "1002"));
                barcodeRepo.save(new BarcodeEntity(water, "1003"));
                barcodeRepo.save(new BarcodeEntity(apple, "1004"));
            }

            // Seed sample suppliers if empty
            if (supplierRepo.count() == 0) {
                supplierRepo.save(new SupplierEntity(
                        "Acme Corp", "John Smith", "1-800-ACME", "john@acme.com", "123 Industrial Ave, Chicago, IL"));
                supplierRepo.save(new SupplierEntity(
                        "Great Goods Co.", "Sarah Johnson", "1-800-GREAT", "sarah@greatgoods.com", "456 Commerce Blvd, New York, NY"));
                supplierRepo.save(new SupplierEntity(
                        "Office Plus", "Mike Brown", "1-800-OFFICE", "mike@officeplus.com", "789 Office St, Boston, MA"));
            }
        };
    }
}
