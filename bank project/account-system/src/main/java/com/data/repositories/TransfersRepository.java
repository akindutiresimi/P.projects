package com.data.repositories;

import com.data.models.Transfers;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransfersRepository extends MongoRepository<Transfers, String> {
    List<Transfers> findByAccountNumber(String accountNumber);
}
