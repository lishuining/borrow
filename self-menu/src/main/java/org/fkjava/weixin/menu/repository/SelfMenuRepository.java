package org.fkjava.weixin.menu.repository;

import org.fkjava.weixin.menu.domain.SelfMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// DAO，用于操作数据库表里面的数据，Menu对象不单独操作，而是通过SelfMenu来完成操作。
@Repository
public interface SelfMenuRepository extends JpaRepository<SelfMenu, String> {

}
