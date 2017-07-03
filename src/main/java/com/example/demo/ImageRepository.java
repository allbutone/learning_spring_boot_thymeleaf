package com.example.demo;

import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by ren_xt
 */
public interface ImageRepository extends PagingAndSortingRepository<Image, Long> {

    Image findByName(String name);

}
