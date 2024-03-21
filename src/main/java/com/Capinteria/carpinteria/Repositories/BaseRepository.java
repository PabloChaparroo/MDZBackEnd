package com.Capinteria.carpinteria.Repositories;

import com.Capinteria.carpinteria.Entity.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

@NoRepositoryBean
public interface BaseRepository <E extends BaseEntity, ID extends Serializable> extends JpaRepository<E,ID> {
}
