package com.hvc.brandlocus.repositories;

import com.hvc.brandlocus.entities.BaseUser;
import com.hvc.brandlocus.entities.ChatSession;
import com.hvc.brandlocus.entities.Forms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface FormRepository extends JpaRepository<Forms, Long>,
        JpaSpecificationExecutor<Forms> {

//    List<Forms> findByUserAndIsActiveTrue(BaseUser user);

    List<Forms> findByIsActiveTrue();

}
