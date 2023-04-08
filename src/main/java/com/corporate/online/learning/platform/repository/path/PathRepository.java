package com.corporate.online.learning.platform.repository.path;

import com.corporate.online.learning.platform.model.path.Path;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PathRepository extends JpaRepository<Path, Long> {
}
