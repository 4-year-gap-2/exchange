package com.exchange.matching.domain.repository;

import com.exchange.matching.domain.entiry.CompletedOrder;

import java.util.List;

public interface CompletedOrderReader {

    List<CompletedOrder> findAll();
}
