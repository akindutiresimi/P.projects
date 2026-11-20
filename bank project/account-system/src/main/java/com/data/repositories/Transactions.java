package com.data.repositories;

import com.data.models.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Transactions extends MongoRepository<Transaction, String> {
    List<Transaction> findByAccountNumber(String accountNumber);
    List<Transaction> findByAccountNumberOrderByDateDesc(String accountNumber);
}
