package com.innople.loyalty.repository;

import com.innople.loyalty.domain.member.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {
}
